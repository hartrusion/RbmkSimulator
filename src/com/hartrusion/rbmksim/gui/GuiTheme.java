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

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.hartrusion.rbmksim.gui.elements.ChornobylMetalTheme;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Lists the GUI themes that the simulator can be displayed with and installs
 * them as the Swing look and feel. A GUI theme is the combination of a look and
 * feel class and, for the Metal look and feel, the Metal theme that colors it,
 * so that the ChornobylMetalTheme and the FlatLaf themes (including the
 * FlatLaf variants in the style of IntelliJ and macOS) can be handled the same
 * way.
 * <p>
 * Swing only applies a look and feel to components that are created after it
 * was installed. The simulator therefore does not update existing windows,
 * but ControlPanelManager closes all windows when the GUI theme is switched
 * and opens a new ControlPanel, whose components are then created with the
 * new look and feel. The start classes RbmkSimulator and RbmkSimulatorClient
 * use this enumeration to install the initial GUI theme before the first
 * window is created, and ControlPanel reads the active GUI theme to mark it in
 * its theme menu.
 * <p>
 * LLM code generated with Claude Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public enum GuiTheme {

    /**
     * Metal look and feel with the gray ChornobylMetalTheme, which is the
     * original appearance of the simulator.
     */
    CHORNOBYL_METAL("Chornobyl Metal"),
    /**
     * Dark theme of the FlatLaf library.
     */
    FLATLAF_DARK("FlatLaf Dark"),
    /**
     * Light theme of the FlatLaf library.
     */
    FLATLAF_LIGHT("FlatLaf Light"),
    /**
     * Light theme of the FlatLaf library that resembles the IntelliJ Light
     * theme of the IntelliJ platform.
     */
    INTELLIJ("IntelliJ"),
    /**
     * Dark theme of the FlatLaf library that resembles the Darcula theme of
     * the IntelliJ platform.
     */
    DARCULA("Darcula"),
    /**
     * Light theme of the FlatLaf library in the style of macOS.
     */
    MAC_LIGHT("macOS Light"),
    /**
     * Dark theme of the FlatLaf library in the style of macOS.
     */
    MAC_DARK("macOS Dark");

    /**
     * The GUI theme that is installed by default on startup.
     */
    public static final GuiTheme DEFAULT_THEME = CHORNOBYL_METAL;

    /**
     * The GUI theme that was installed last with apply(), which is null as long
     * as no GUI theme was installed by this enumeration.
     */
    private static GuiTheme activeTheme;

    /**
     * Name of the GUI theme as it is presented to the user.
     */
    private final String displayName;

    GuiTheme(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the GUI theme that was installed last, or the default theme if
     * no GUI theme was installed by this enumeration yet.
     *
     * @return Active GUI theme
     */
    public static GuiTheme getActiveTheme() {
        if (activeTheme == null) {
            return DEFAULT_THEME;
        }
        return activeTheme;
    }

    /**
     * Installs this GUI theme as the Swing look and feel. Only components
     * created afterwards get the new appearance, which is the reason why the
     * windows are recreated on a switch. The method has to be called on the
     * event dispatch thread as soon as Swing components exist. A failure is
     * logged and leaves the previous look and feel installed.
     *
     * @return true if the GUI theme was installed successfully
     */
    public boolean apply() {
        LookAndFeel lookAndFeel;
        switch (this) {
            case FLATLAF_DARK:
                lookAndFeel = new FlatDarkLaf();
                break;
            case FLATLAF_LIGHT:
                lookAndFeel = new FlatLightLaf();
                break;
            case INTELLIJ:
                lookAndFeel = new FlatIntelliJLaf();
                break;
            case DARCULA:
                lookAndFeel = new FlatDarculaLaf();
                break;
            case MAC_LIGHT:
                lookAndFeel = new FlatMacLightLaf();
                break;
            case MAC_DARK:
                lookAndFeel = new FlatMacDarkLaf();
                break;
            case CHORNOBYL_METAL:
            default:
                // The Metal theme is a static setting of Metal and has to be
                // set again each time, because the Metal look and feel reads
                // it only when it is installed.
                MetalLookAndFeel.setCurrentTheme(new ChornobylMetalTheme());
                lookAndFeel = new MetalLookAndFeel();
                break;
        }
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GuiTheme.class.getName()).log(Level.SEVERE,
                    "Unable to install GUI theme " + displayName, ex);
            return false;
        }
        activeTheme = this;
        return true;
    }
}
