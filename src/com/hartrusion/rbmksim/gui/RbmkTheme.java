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
import java.awt.Color;

/**
 * Extends the generic Theme which was designed for the simulator but placed 
 * in the external utils package by colors that are only used by the simulator.
 * 
 * @author Viktor Alexander Hartung
 */
public class RbmkTheme extends ChornobylMetalTheme {
    
    
    // Green panel buttons, which show for example a closed valve.
    private static final Color GREEN_BUTTON_BACKGROUND = new Color(0, 128, 0);
    private static final Color GREEN_BUTTON_HIGHLIGHT = new Color(0, 255, 0);
    private static final Color GREEN_BUTTON_FOREGROUND = new Color(255, 255, 255);

    // Red panel buttons, which show for example an open valve or a selected
    // automatic control rod.
    private static final Color RED_BUTTON_BACKGROUND = new Color(128, 0, 0);
    private static final Color RED_BUTTON_HIGHLIGHT = new Color(255, 0, 0);
    private static final Color RED_BUTTON_FOREGROUND = new Color(255, 255, 255);

    // Gray panel buttons, which replace the green close buttons where a
    // less colorful panel is wanted.
    private static final Color GRAY_BUTTON_BACKGROUND = new Color(92, 92, 92);
    private static final Color GRAY_BUTTON_HIGHLIGHT = new Color(208, 208, 208);
    private static final Color GRAY_BUTTON_FOREGROUND = new Color(255, 255, 255);

    // Yellow panel buttons, which show the selection of short control rods.
    private static final Color YELLOW_BUTTON_BACKGROUND = new Color(128, 128, 0);
    private static final Color YELLOW_BUTTON_HIGHLIGHT = new Color(208, 208, 0);
    private static final Color YELLOW_BUTTON_FOREGROUND = new Color(255, 255, 255);

    // White panel buttons, which show the selection of manual control rods.
    // The text is black because it would not be readable on white.
    private static final Color WHITE_BUTTON_BACKGROUND = new Color(128, 128, 128);
    private static final Color WHITE_BUTTON_HIGHLIGHT = new Color(255, 255, 255);
    private static final Color WHITE_BUTTON_FOREGROUND = new Color(0, 0, 0);

    /**
     * Returns the background of a green panel button in its normal state. The
     * getters of the panel button colors follow the same scheme for all
     * types, where the background belongs to the normal state, the highlight
     * replaces the background in the highlighted state and the foreground is
     * the text color of both states.
     * <p>
     * LLM code generated with Claude Opus 5.5
     *
     * @return Background of a not highlighted green panel button
     */
    public Color getGreenButtonBackground() {
        return GREEN_BUTTON_BACKGROUND;
    }

    public Color getGreenButtonHighlight() {
        return GREEN_BUTTON_HIGHLIGHT;
    }

    public Color getGreenButtonForeground() {
        return GREEN_BUTTON_FOREGROUND;
    }

    public Color getRedButtonBackground() {
        return RED_BUTTON_BACKGROUND;
    }

    public Color getRedButtonHighlight() {
        return RED_BUTTON_HIGHLIGHT;
    }

    public Color getRedButtonForeground() {
        return RED_BUTTON_FOREGROUND;
    }

    public Color getGrayButtonBackground() {
        return GRAY_BUTTON_BACKGROUND;
    }

    public Color getGrayButtonHighlight() {
        return GRAY_BUTTON_HIGHLIGHT;
    }

    public Color getGrayButtonForeground() {
        return GRAY_BUTTON_FOREGROUND;
    }

    public Color getYellowButtonBackground() {
        return YELLOW_BUTTON_BACKGROUND;
    }

    public Color getYellowButtonHighlight() {
        return YELLOW_BUTTON_HIGHLIGHT;
    }

    public Color getYellowButtonForeground() {
        return YELLOW_BUTTON_FOREGROUND;
    }

    public Color getWhiteButtonBackground() {
        return WHITE_BUTTON_BACKGROUND;
    }

    public Color getWhiteButtonHighlight() {
        return WHITE_BUTTON_HIGHLIGHT;
    }

    public Color getWhiteButtonForeground() {
        return WHITE_BUTTON_FOREGROUND;
    }
}
