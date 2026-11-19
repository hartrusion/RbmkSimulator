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

import java.awt.Color;

/**
 * Defines the colors of the panel buttons, which are the colored buttons of
 * the control panels that show a state like an open or closed valve. These
 * colors are only used by the simulator and are therefore not part of the
 * generic ChornobylMetalTheme of the utils package.
 * <p>
 * The colors are defined by an interface instead of a common base class,
 * because the light RbmkTheme derives from ChornobylMetalTheme while the dark
 * RbmkThemeDark has to derive from ChornobylMetalThemeDark to get the dark
 * Metal colors, so that both theme classes can only share an interface.
 * GuiThemeManager provides the panel button colors of the active GUI theme
 * with this interface, and AbstractPanelWidget reads them whenever it sets
 * the state of a panel button.
 * <p>
 * The panel buttons exist in the types green, red, gray, yellow and white.
 * The getters follow the same scheme for all types, where the background
 * belongs to the normal state, the highlight replaces the background in the
 * highlighted state and the foreground is the text color of both states. The
 * colors have to be plain Color objects instead of ColorUIResource, because
 * Swing replaces any ColorUIResource of a component by the default of the look
 * and feel when the component UI is updated, which would reset the state
 * colors of the buttons.
 * <p>
 * LLM code generated with Claude Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public interface RbmkPanelButtonColors {

    Color getGreenButtonBackground();

    Color getGreenButtonHighlight();

    Color getGreenButtonForeground();

    Color getRedButtonBackground();

    Color getRedButtonHighlight();

    Color getRedButtonForeground();

    Color getGrayButtonBackground();

    Color getGrayButtonHighlight();

    Color getGrayButtonForeground();

    Color getYellowButtonBackground();

    Color getYellowButtonHighlight();

    Color getYellowButtonForeground();

    Color getWhiteButtonBackground();

    Color getWhiteButtonHighlight();

    Color getWhiteButtonForeground();
}
