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
 * Two operating modes exist, selected by the hardcoded {@link #USE_SYSTEM_SOUND}
 * switch:
 * <ul>
 *   <li>{@code false} &ndash; the "proper" mode: bundled {@code .wav} resources
 *       are looped continuously depending on the highest active severity. This
 *       needs the sound files to actually exist in the resources folder.</li>
 *   <li>{@code true} (current default, temporary) &ndash; no bundled files are
 *       needed. The operating system's own notification sound is played once
 *       per second for as long as an unacknowledged alarm is present. Where the
 *       platform reliably offers a distinct "error" sound, MIN/MAX trip level
 *       alarms use that instead of the plain notification sound.</li>
 * </ul>
 *
 * This is fully LLM generated with Github Copilot using Claude Opus 4.8 and 
 * using Claude Code with 
 *
 * @author Viktor Alexander Hartung
 */
public final class AlarmSound {

    private AlarmSound() {} // allowed

    /**
     * Hardcoded switch. When {@code true} the bundled {@code .wav} files are
     * ignored and the OS notification sound is used instead (temporary solution
     * until proper sound files are available). Flip to {@code false} in code
     * once the resource {@code .wav} files exist.
     */
    private static final boolean USE_SYSTEM_SOUND = true;

    /** Interval between system-sound beeps while an alarm is pending. */
    private static final long BEEP_INTERVAL_MS = 1000;

    /** Global enable/disable, toggled from the GUI. */
    private static volatile boolean soundEnabled = true;

    // ----------------------------------------------------------------- clips
    // (only used when USE_SYSTEM_SOUND == false)

    /** Currently looping clip and the level it belongs to. */
    private static Clip currentClip;
    private static AlarmState currentLevel;

    // Severity groups -> sound resource. Adjust grouping/files as you like.
    private static Clip clipMax;   // MAX1/MAX2, MIN1/MIN2 (trip level)
    private static Clip clipHigh;  // HIGH2/LOW2
    private static Clip clipWarn;  // HIGH1/LOW1

    private static boolean clipsInitialized;

    // --------------------------------------------------------- system sounds
    // (only used when USE_SYSTEM_SOUND == true)

    /** Highest-priority unacknowledged state, or {@code null} if none. */
    private static volatile AlarmState pendingLevel;

    /** Repeating timer that emits the system sound once per second. */
    private static Timer beepTimer;

    /** Runnable that plays the plain notification sound. Never {@code null}. */
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
     * Call this on the EDT whenever new alarm data arrives. Decides which
     * (if any) sound must play, based on the highest-priority unacknowledged
     * alarm.
     */
    public static synchronized void update(List<AlarmObject> alarms) {
        if (!soundEnabled || alarms == null) {
            stop();
            return;
        }

        AlarmState highest = null;
        for (AlarmObject ao : alarms) {
            if (ao == null || ao.isAcknowledged()) continue;
            AlarmState s = ao.getState();
            if (s == null || s == AlarmState.NONE) continue;
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
        // Start the repeating beep if it is not already running. A fixed-delay
        // schedule with zero initial delay plays immediately and then once per
        // second, which is exactly the behaviour we want.
        if (beepTimer == null) {
            initSystemSound();
            beepTimer = new Timer("AlarmSystemSound", true);
            beepTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    AlarmState level = pendingLevel;
                    if (!soundEnabled || level == null) {
                        return;
                    }
                    playSystemSound(isTripLevel(level));
                }
            }, 0, BEEP_INTERVAL_MS);
        }
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
        if (systemSoundInitialized) return;
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
        if (note instanceof Runnable) notificationSound = (Runnable) note;
        if (err instanceof Runnable) errorSound = (Runnable) err;
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
     * Linux has no built-in API. We try, in order of preference:
     * <ol>
     *   <li>{@code canberra-gtk-play} with freedesktop event names
     *       ({@code dialog-information} / {@code dialog-error}) &ndash; present
     *       on most GNOME/XFCE setups and honours the desktop's sound theme;</li>
     *   <li>{@code paplay} with the freedesktop {@code .oga} sample files.</li>
     * </ol>
     * If neither is available the generic beep fallback (set by the caller) is
     * used for every severity.
     */
    private static void initLinuxSounds() {
        if (commandExists("canberra-gtk-play")) {
            notificationSound =
                    exec("canberra-gtk-play", "-i", "dialog-information");
            errorSound = exec("canberra-gtk-play", "-i", "dialog-error");
            return;
        }
        if (commandExists("paplay")) {
            String dir = "/usr/share/sounds/freedesktop/stereo/";
            File note = firstExisting(dir + "dialog-information.oga",
                    dir + "message.oga", dir + "bell.oga", dir + "complete.oga");
            File err = firstExisting(dir + "dialog-error.oga",
                    dir + "dialog-warning.oga");
            if (note != null) notificationSound = exec("paplay", note.getPath());
            if (err != null) errorSound = exec("paplay", err.getPath());
        }
    }

    /** Builds a fire-and-forget player that runs an external command. */
    private static Runnable exec(String... command) {
        return () -> {
            try {
                new ProcessBuilder(command)
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .redirectError(ProcessBuilder.Redirect.DISCARD)
                        .start()
                        .waitFor();
            } catch (java.io.IOException ex) {
                System.err.println("Could not run sound command "
                        + String.join(" ", command) + ": " + ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        };
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

    private static File firstExisting(String... paths) {
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) return f;
        }
        return null;
    }

    // --------------------------------------------------------- clip handling

    private static synchronized void initClips() {
        if (clipsInitialized) return;
        clipsInitialized = true;
        clipMax  = load("/com/hartrusion/rbmksim/gui/alarm_max.wav");
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
            case MAX2, MIN2, MAX1, MIN1 -> clipMax;
            case HIGH2, LOW2 -> clipHigh;
            case HIGH1, LOW1 -> clipWarn;
            default -> null;
        };
    }

    // --------------------------------------------------------------- helpers

    // Same ordering as the alarm table: MAX2, MIN2, MAX1, MIN1, HIGH2, LOW2, HIGH1, LOW1
    private static int priority(AlarmState s) {
        return switch (s) {
            case MAX2 -> 0; case MIN2 -> 1; case MAX1 -> 2; case MIN1 -> 3;
            case HIGH2 -> 4; case LOW2 -> 5; case HIGH1 -> 6; case LOW1 -> 7;
            default -> Integer.MAX_VALUE;
        };
    }

    /** MIN/MAX trip levels get the error sound; everything else notification. */
    private static boolean isTripLevel(AlarmState s) {
        return switch (s) {
            case MAX2, MAX1, MIN1, MIN2 -> true;
            default -> false;
        };
    }
}
