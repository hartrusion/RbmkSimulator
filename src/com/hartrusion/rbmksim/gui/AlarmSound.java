/*
 * Copyright (C) 2026 Viktor Alexander Hartung
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hartrusion.rbmksim.gui;

import com.hartrusion.alarm.AlarmObject;
import com.hartrusion.alarm.AlarmState;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Plays an alarm sound as long as there are unacknowledged alarms. Can be
 * globally muted from the GUI via {@link #setSoundEnabled(boolean)}.
 * <p>
 * Two operating modes exist, selected by the hardcoded
 * {@link #USE_SYSTEM_SOUND} switch:
 * <ul>
 * <li>{@code false} &ndash; the "proper" mode: bundled {@code .wav} resources
 * are looped continuously depending on the highest active severity. This needs
 * the sound files to actually exist in the resources folder.</li>
 * <li>{@code true} (current default, temporary) &ndash; no bundled files are
 * needed. The operating system's own notification sound is played once per
 * second for as long as an unacknowledged alarm is present. Where the platform
 * reliably offers a distinct "error" sound, MIN/MAX trip level alarms use that
 * instead of the plain notification sound.</li>
 * </ul>
 * Windows and macOS have one obvious way of doing this; Linux does not, so
 * several sound file locations (including the Linux Mint ones) and several
 * players are tried until one actually works, see {@link #initLinuxSounds()}.
 * On Cinnamon the sound configured in the desktop's own sound settings is
 * preferred. If every attempt fails the AWT beep is used, so an alarm is never
 * completely silent.
 *
 * This is fully LLM generated with Github Copilot using Claude Opus 4.8 and
 * using Claude Code with Opus 5
 *
 * @author Viktor Alexander Hartung
 */
public final class AlarmSound {

    private AlarmSound() {
    } // allowed

    /**
     * Hardcoded switch. When {@code true} the bundled {@code .wav} files are
     * ignored and the OS notification sound is used instead (temporary solution
     * until proper sound files are available). Flip to {@code false} in code
     * once the resource {@code .wav} files exist.
     */
    private static final boolean USE_SYSTEM_SOUND = true;

    /**
     * Interval between system-sound beeps while an alarm is pending.
     */
    private static long interval = 1000;

    /**
     * Global enable/disable, toggled from the GUI.
     */
    private static volatile boolean soundEnabled = true;

    // ----------------------------------------------------------------- clips
    // (only used when USE_SYSTEM_SOUND == false)
    /**
     * Currently looping clip and the level it belongs to.
     */
    private static Clip currentClip;
    private static AlarmState currentLevel;

    // Severity groups -> sound resource. Adjust grouping/files as you like.
    private static Clip clipMax;   // MAX1/MAX2, MIN1/MIN2 (trip level)
    private static Clip clipHigh;  // HIGH2/LOW2
    private static Clip clipWarn;  // HIGH1/LOW1

    private static boolean clipsInitialized;

    // --------------------------------------------------------- system sounds
    // (only used when USE_SYSTEM_SOUND == true)
    /**
     * Highest-priority unacknowledged state, or {@code null} if none.
     */
    private static volatile AlarmState pendingLevel;

    /**
     * Repeating timer that emits the system sound once per second.
     */
    private static Timer beepTimer;

    /**
     * Runnable that plays the plain notification sound. Never {@code null}.
     */
    private static Runnable notificationSound;

    /**
     * Runnable that plays the error sound, or {@code null} if this platform
     * cannot reliably provide a distinct error sound. When {@code null} the
     * notification sound is used for every severity.
     */
    private static Runnable errorSound;

    private static boolean systemSoundInitialized;

    // ------------------------------------------------------------ public API
    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        if (!enabled) {
            stop();
        }
        // if re-enabled, the next update() call will restart the sound
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Call this on the EDT whenever new alarm data arrives. Decides which (if
     * any) sound must play, based on the highest-priority unacknowledged alarm.
     */
    public static synchronized void update(List<AlarmObject> alarms) {
        if (!soundEnabled || alarms == null) {
            stop();
            return;
        }

        AlarmState highest = null;
        for (AlarmObject ao : alarms) {
            if (ao == null || ao.isAcknowledged()) {
                continue;
            }
            AlarmState s = ao.getState();
            if (s == null || s == AlarmState.NONE) {
                continue;
            }
            if (highest == null || priority(s) < priority(highest)) {
                highest = s;
            }
        }

        if (USE_SYSTEM_SOUND) {
            updateSystemSound(highest);
        } else {
            updateClip(highest);
        }
    }

    private static synchronized void stop() {
        stopClip();
        stopBeepTimer();
    }

    // ------------------------------------------------- system sound handling
    private static void updateSystemSound(AlarmState highest) {
        pendingLevel = highest;
        if (highest == null) {
            stopBeepTimer();
            return;
        }
        // Start the repeating beep if it is not already running.
        if (beepTimer == null) {
            beepTimer = new Timer("AlarmSystemSound", true);
            scheduleBeep(beepTimer, 0);
        }
    }

    /**
     * Schedules one beep. The task re-schedules itself afterwards instead of
     * using a fixed-rate timer, for two reasons: the sound detection runs
     * lazily on the timer thread (it may play and wait for test sounds, which
     * must never happen on the EDT) and may adjust {@link #interval}, and
     * external players block until the sound has finished, so measuring the
     * pause from the end of the previous sound avoids overlapping playback.
     */
    private static void scheduleBeep(final Timer timer, long delay) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                initSystemSound(); // no-op after the first call
                AlarmState level = pendingLevel;
                if (soundEnabled && level != null) {
                    playSystemSound(isTripLevel(level));
                }
                synchronized (AlarmSound.class) {
                    // Carry on only while this is still the active timer. The
                    // alarm can be cleared and raised again while the sound is
                    // playing, which replaces the timer; without this check the
                    // old task would keep beeping on the new one as well.
                    if (beepTimer == timer) {
                        scheduleBeep(timer, interval);
                    }
                }
            }
        }, delay);
    }

    private static void stopBeepTimer() {
        if (beepTimer != null) {
            beepTimer.cancel();
            beepTimer = null;
        }
        pendingLevel = null;
    }

    private static void playSystemSound(boolean error) {
        try {
            Runnable r = (error && errorSound != null) ? errorSound
                    : notificationSound;
            r.run();
        } catch (RuntimeException ex) {
            // Never let a missing player tool break the simulation.
            System.err.println("Could not play system alarm sound: " + ex);
        }
    }

    /**
     * Detects, once, how to play a notification and (if reliably available) a
     * distinct error sound on this operating system.
     */
    private static synchronized void initSystemSound() {
        if (systemSoundInitialized) {
            return;
        }
        systemSoundInitialized = true;

        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            initWindowsSounds();
        } else if (os.contains("mac") || os.contains("darwin")) {
            initMacSounds();
        } else {
            initLinuxSounds();
        }

        // Last-resort fallback: if no notification player could be built, fall
        // back to the AWT beep so there is always some audible signal.
        if (notificationSound == null) {
            notificationSound = () -> Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Windows exposes ready-to-run {@link Runnable}s for the configured system
     * sounds through AWT desktop properties, so both a notification and an
     * error sound are reliably available without any external process.
     */
    private static void initWindowsSounds() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Object note = tk.getDesktopProperty("win.sound.asterisk");
        Object err = tk.getDesktopProperty("win.sound.hand");
        if (note instanceof Runnable) {
            notificationSound = (Runnable) note;
        }
        if (err instanceof Runnable) {
            errorSound = (Runnable) err;
        }
        // Windows 11 sounds are a bit longer and would sound horrible on 1.0
        // second intervall
        interval = 1600;
    }

    /**
     * macOS always ships the standard system sounds under
     * {@code /System/Library/Sounds}; {@code afplay} plays them. Both a
     * notification and an error sound are therefore reliably available.
     */
    private static void initMacSounds() {
        File ping = new File("/System/Library/Sounds/Ping.aiff");
        File basso = new File("/System/Library/Sounds/Basso.aiff");
        if (ping.exists()) {
            notificationSound = exec("afplay", ping.getPath());
        }
        if (basso.exists()) {
            errorSound = exec("afplay", basso.getPath());
        }
    }

    /**
     * Directories that are searched for a playable sound file, in this order.
     * The Linux Mint locations come first so that a Mint desktop gets its own
     * sounds rather than the freedesktop ones; every other distribution simply
     * does not have those directories and falls through.
     */
    private static final String[] SOUND_DIRS = {
        "/usr/share/mint-artwork/sounds", // Linux Mint (Cinnamon/MATE/XFCE)
        "/usr/share/sounds/LinuxMint/stereo", // Linux Mint sound theme
        "/usr/share/sounds/freedesktop/stereo", // XDG standard, nearly universal
        "/usr/share/sounds/ubuntu/stereo",
        "/usr/share/sounds/gnome/default/alerts"
    };

    private static final String[] SOUND_EXTENSIONS = {".oga", ".ogg", ".wav"};

    /**
     * Base names of a "something happened" sound, best first. Freedesktop uses
     * {@code dialog-information}, Linux Mint names its file
     * {@code notification} and GNOME ships {@code drip} among its alert sounds.
     */
    private static final String[] NOTIFICATION_NAMES = {
        "dialog-information", "notification", "message", "bell", "complete",
        "drip"
    };

    /**
     * Base names of a distinctly more alarming sound, best first.
     */
    private static final String[] ERROR_NAMES = {
        "dialog-error", "dialog-warning", "error", "window-attention",
        "suspend-error", "sonar"
    };

    /**
     * Linux has no built-in API, so an external player has to be driven. We
     * look for an actual sound file first and only fall back to letting
     * libcanberra resolve an event name against the desktop's sound theme:
     * {@code canberra-gtk-play} is missing on plenty of installations (KDE
     * being a common one) and, worse, silently exits successfully when the
     * active theme does not contain the requested event, which leaves the alarm
     * inaudible. Playing a file we have verified to exist is the more
     * dependable route, at the cost of ignoring a customised sound theme.
     * <p>
     * On Cinnamon (Linux Mint) the sound the user picked in "System Settings
     * &rarr; Sound &rarr; Sounds" takes precedence over the file search, see
     * {@link #cinnamonSound(String)}.
     * <p>
     * Candidate players are tried in turn until one exits successfully, so a
     * missing sound server or an unsupported codec moves on to the next tool
     * instead of leaving the operator without an alarm. Whichever player wins
     * is reused for the error sound.
     */
    private static void initLinuxSounds() {
        File note = null;
        if (isCinnamon()) {
            note = cinnamonSound("notification-file");
        }
        if (note == null) {
            note = findSound(NOTIFICATION_NAMES);
        }
        File err = findSound(ERROR_NAMES);

        if (note != null) {
            for (String[] player : filePlayers(note)) {
                String[] command = append(player, note.getPath());
                if (run(command) == 0) {
                    notificationSound = exec(command);
                    if (err != null) {
                        errorSound = exec(append(player, err.getPath()));
                    }
                    return;
                }
            }
        }

        // Nothing playable found: let the sound theme sort it out.
        if (commandExists("canberra-gtk-play")) {
            notificationSound
                    = exec("canberra-gtk-play", "-i", "dialog-information");
            errorSound = exec("canberra-gtk-play", "-i", "dialog-error");
        }
    }

    /**
     * Linux Mint reports itself as {@code X-Cinnamon} rather than
     * {@code Cinnamon}, hence the substring match.
     */
    private static boolean isCinnamon() {
        for (String var : new String[]{"XDG_CURRENT_DESKTOP",
            "XDG_SESSION_DESKTOP", "DESKTOP_SESSION"}) {
            String value = System.getenv(var);
            if (value != null
                    && value.toLowerCase(Locale.ROOT).contains("cinnamon")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads one of the sound files configured in Cinnamon. The sound settings
     * are stored as absolute paths in the {@code org.cinnamon.sounds} GSettings
     * schema (keys {@code notification-file}, {@code login-file},
     * {@code plug-file}, ...); on Linux Mint they point into
     * {@code /usr/share/mint-artwork/sounds}. The matching {@code *-enabled}
     * key is deliberately ignored: it only says whether Cinnamon itself uses
     * the sound, while we want the file either way.
     *
     * @return the configured file, or {@code null} if unset, unreadable or
     * gsettings is not installed
     */
    private static File cinnamonSound(String key) {
        if (!commandExists("gsettings")) {
            return null;
        }
        String out = capture("gsettings", "get", "org.cinnamon.sounds", key);
        if (out == null) {
            return null;
        }
        // gsettings prints strings quoted, e.g. '/usr/share/.../notification.oga'
        String path = out.trim();
        if (path.length() >= 2 && path.startsWith("'") && path.endsWith("'")) {
            path = path.substring(1, path.length() - 1);
        }
        if (path.isEmpty()) {
            return null;
        }
        File f = new File(path);
        return f.isFile() ? f : null;
    }

    /**
     * Searches {@link #SOUND_DIRS} for the first of the given base names that
     * exists with any known extension.
     */
    private static File findSound(String... names) {
        for (String dir : SOUND_DIRS) {
            for (String name : names) {
                for (String ext : SOUND_EXTENSIONS) {
                    File f = new File(dir, name + ext);
                    if (f.isFile()) {
                        return f;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Command prefixes able to play the given file, limited to the tools that
     * are actually installed. {@code paplay} and {@code pw-play} come first
     * because they talk to the sound server directly and are present on every
     * PulseAudio respectively PipeWire desktop, which includes Linux Mint.
     * {@code aplay} is only offered for {@code .wav}, being the one format ALSA
     * can play without a decoder.
     */
    private static List<String[]> filePlayers(File file) {
        List<String[]> players = new ArrayList<>();
        addPlayer(players, "paplay");
        addPlayer(players, "pw-play");
        addPlayer(players, "canberra-gtk-play", "-f");
        addPlayer(players, "ogg123", "-q");
        addPlayer(players, "mpv", "--really-quiet", "--no-video");
        addPlayer(players, "ffplay", "-nodisp", "-autoexit", "-loglevel",
                "quiet");
        addPlayer(players, "gst-play-1.0", "--quiet");
        addPlayer(players, "cvlc", "--play-and-exit", "--quiet");
        if (file.getName().toLowerCase(Locale.ROOT).endsWith(".wav")) {
            addPlayer(players, "aplay", "-q");
        }
        return players;
    }

    private static void addPlayer(List<String[]> players, String... command) {
        if (commandExists(command[0])) {
            players.add(command);
        }
    }

    private static String[] append(String[] command, String argument) {
        String[] full = Arrays.copyOf(command, command.length + 1);
        full[command.length] = argument;
        return full;
    }

    /**
     * Builds a fire-and-forget player that runs an external command.
     */
    private static Runnable exec(String... command) {
        return () -> run(command);
    }

    /**
     * Runs a command and waits for it, discarding its output.
     *
     * @return the exit code, or a negative value if it could not be run
     */
    private static int run(String... command) {
        try {
            return new ProcessBuilder(command)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start()
                    .waitFor();
        } catch (IOException ex) {
            System.err.println("Could not run sound command "
                    + String.join(" ", command) + ": " + ex);
            return -1;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    /**
     * Runs a command and returns its standard output, or {@code null} if it
     * failed.
     */
    private static String capture(String... command) {
        try {
            Process p = new ProcessBuilder(command)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            String out = new String(p.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
            return p.waitFor() == 0 ? out : null;
        } catch (IOException ex) {
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static boolean commandExists(String cmd) {
        try {
            return new ProcessBuilder("which", cmd)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start()
                    .waitFor() == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    // --------------------------------------------------------- clip handling
    private static synchronized void initClips() {
        if (clipsInitialized) {
            return;
        }
        clipsInitialized = true;
        clipMax = load("/com/hartrusion/rbmksim/gui/alarm_max.wav");
        clipHigh = load("/com/hartrusion/rbmksim/gui/alarm_high.wav");
        clipWarn = load("/com/hartrusion/rbmksim/gui/alarm_warn.wav");
    }

    private static Clip load(String resource) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(
                    AlarmSound.class.getResource(resource)));
            return clip;
        } catch (Exception ex) {
            System.err.println("Could not load alarm sound " + resource
                    + ": " + ex);
            return null;
        }
    }

    private static void updateClip(AlarmState highest) {
        initClips();
        if (highest == null) {
            stopClip();
            return;
        }
        if (highest == currentLevel) {
            return; // already playing the right sound
        }
        stopClip();
        currentLevel = highest;
        currentClip = clipFor(highest);
        if (currentClip != null) {
            currentClip.setFramePosition(0);
            currentClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private static void stopClip() {
        if (currentClip != null) {
            currentClip.stop();
        }
        currentClip = null;
        currentLevel = null;
    }

    private static Clip clipFor(AlarmState s) {
        return switch (s) {
            case MAX2, MIN2, MAX1, MIN1 ->
                clipMax;
            case HIGH2, LOW2 ->
                clipHigh;
            case HIGH1, LOW1 ->
                clipWarn;
            default ->
                null;
        };
    }

    // --------------------------------------------------------------- helpers
    // Same ordering as the alarm table: MAX2, MIN2, MAX1, MIN1, HIGH2, LOW2, HIGH1, LOW1
    private static int priority(AlarmState s) {
        return switch (s) {
            case MAX2 ->
                0;
            case MIN2 ->
                1;
            case MAX1 ->
                2;
            case MIN1 ->
                3;
            case HIGH2 ->
                4;
            case LOW2 ->
                5;
            case HIGH1 ->
                6;
            case LOW1 ->
                7;
            default ->
                Integer.MAX_VALUE;
        };
    }

    /**
     * MIN/MAX trip levels get the error sound; everything else notification.
     */
    private static boolean isTripLevel(AlarmState s) {
        return switch (s) {
            case MAX2, MAX1, MIN1, MIN2 ->
                true;
            default ->
                false;
        };
    }
}
