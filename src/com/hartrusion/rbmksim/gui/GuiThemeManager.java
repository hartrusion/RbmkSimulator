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

import com.hartrusion.swing.ChornobylMetalTheme;
import com.hartrusion.swing.ChornobylMetalThemeDark;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Lists the GUI themes that the simulator can be displayed with and installs
 * them as the Swing look and feel. All GUI themes use the Metal look and feel,
 * so that the shapes of buttons and other components stay the same, and only
 * differ in the Metal theme that colors it. Each Metal theme derives from
 * ChornobylMetalTheme, which also defines the colors of the panel buttons, so
 * that AbstractPanelWidget can read these colors with getChornobylMetalTheme()
 * from the active GUI theme.
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
public enum GuiThemeManager {

    /**
     * Metal look and feel with the gray ChornobylMetalTheme, which is the
     * original appearance of the simulator.
     */
    CHORNOBYL_METAL("Chornobyl Metal", new ChornobylMetalTheme()),
    /**
     * Metal look and feel with the dark ChornobylMetalThemeDark.
     */
    CHORNOBYL_METAL_DARK("Chornobyl Metal Dark", new ChornobylMetalThemeDark());

    /**
     * The GUI theme that is installed by default on startup.
     */
    public static final GuiThemeManager DEFAULT_THEME = CHORNOBYL_METAL;

    /**
     * The GUI theme that was installed last with apply(), which is null as long
     * as no GUI theme was installed by this enumeration.
     */
    private static GuiThemeManager activeTheme;

    /**
     * Name of the GUI theme as it is presented to the user.
     */
    private final String displayName;

    /**
     * Metal theme that colors the Metal look and feel of this GUI theme. The
     * instance is created once with the enumeration, so that the colors which
     * are read from it on every button update are never created again.
     */
    private final ChornobylMetalTheme chornobylMetalTheme;

    GuiThemeManager(String displayName, ChornobylMetalTheme chornobylMetalTheme) {
        this.displayName = displayName;
        this.chornobylMetalTheme = chornobylMetalTheme;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChornobylMetalTheme getChornobylMetalTheme() {
        return chornobylMetalTheme;
    }

    /**
     * Returns the GUI theme that was installed last, or the default theme if
     * no GUI theme was installed by this enumeration yet.
     *
     * @return Active GUI theme
     */
    public static GuiThemeManager getActiveTheme() {
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
        // The Metal theme is a static setting of Metal and has to be set
        // again each time, because the Metal look and feel reads it only when
        // it is installed.
        MetalLookAndFeel.setCurrentTheme(chornobylMetalTheme);
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GuiThemeManager.class.getName()).log(Level.SEVERE,
                    "Unable to install GUI theme " + displayName, ex);
            return false;
        }
        activeTheme = this;
        return true;
    }
}
