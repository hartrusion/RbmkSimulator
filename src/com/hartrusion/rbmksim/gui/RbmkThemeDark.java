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

import com.hartrusion.swing.ChornobylMetalThemeDark;
import java.awt.Color;

/**
 * Dark GUI theme of the simulator, which takes the dark colors of the Metal
 * look and feel from ChornobylMetalThemeDark of the utils package and adds the
 * colors of the panel buttons. The class has to derive from
 * ChornobylMetalThemeDark instead of RbmkTheme, because Java allows only one
 * base class and the dark Metal colors are only available from the dark
 * theme, so that the panel button colors are defined by the interface
 * RbmkPanelButtonColors, which both RbmkTheme and this class implement.
 * <p>
 * LLM code generated with Claude Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public class RbmkThemeDark extends ChornobylMetalThemeDark
        implements RbmkPanelButtonColors {
    
    // Panel button colors, which are the same as in the light theme for now
    // and can be adapted to the dark background here.
    private static final Color GREEN_BUTTON_BACKGROUND = new Color(0, 128, 0);
    private static final Color GREEN_BUTTON_HIGHLIGHT = new Color(0, 255, 0);
    private static final Color GREEN_BUTTON_FOREGROUND = new Color(255, 255, 255);

    private static final Color RED_BUTTON_BACKGROUND = new Color(128, 0, 0);
    private static final Color RED_BUTTON_HIGHLIGHT = new Color(255, 0, 0);
    private static final Color RED_BUTTON_FOREGROUND = new Color(255, 255, 255);

    private static final Color GRAY_BUTTON_BACKGROUND = new Color(92, 92, 92);
    private static final Color GRAY_BUTTON_HIGHLIGHT = new Color(208, 208, 208);
    private static final Color GRAY_BUTTON_FOREGROUND = new Color(255, 255, 255);

    private static final Color YELLOW_BUTTON_BACKGROUND = new Color(128, 128, 0);
    private static final Color YELLOW_BUTTON_HIGHLIGHT = new Color(208, 208, 0);
    private static final Color YELLOW_BUTTON_FOREGROUND = new Color(255, 255, 255);

    private static final Color WHITE_BUTTON_BACKGROUND = new Color(128, 128, 128);
    private static final Color WHITE_BUTTON_HIGHLIGHT = new Color(255, 255, 255);
    private static final Color WHITE_BUTTON_FOREGROUND = new Color(0, 0, 0);
    

    @Override
    public Color getGreenButtonBackground() {
        return GREEN_BUTTON_BACKGROUND;
    }

    @Override
    public Color getGreenButtonHighlight() {
        return GREEN_BUTTON_HIGHLIGHT;
    }

    @Override
    public Color getGreenButtonForeground() {
        return GREEN_BUTTON_FOREGROUND;
    }

    @Override
    public Color getRedButtonBackground() {
        return RED_BUTTON_BACKGROUND;
    }

    @Override
    public Color getRedButtonHighlight() {
        return RED_BUTTON_HIGHLIGHT;
    }

    @Override
    public Color getRedButtonForeground() {
        return RED_BUTTON_FOREGROUND;
    }

    @Override
    public Color getGrayButtonBackground() {
        return GRAY_BUTTON_BACKGROUND;
    }

    @Override
    public Color getGrayButtonHighlight() {
        return GRAY_BUTTON_HIGHLIGHT;
    }

    @Override
    public Color getGrayButtonForeground() {
        return GRAY_BUTTON_FOREGROUND;
    }

    @Override
    public Color getYellowButtonBackground() {
        return YELLOW_BUTTON_BACKGROUND;
    }

    @Override
    public Color getYellowButtonHighlight() {
        return YELLOW_BUTTON_HIGHLIGHT;
    }

    @Override
    public Color getYellowButtonForeground() {
        return YELLOW_BUTTON_FOREGROUND;
    }

    @Override
    public Color getWhiteButtonBackground() {
        return WHITE_BUTTON_BACKGROUND;
    }

    @Override
    public Color getWhiteButtonHighlight() {
        return WHITE_BUTTON_HIGHLIGHT;
    }

    @Override
    public Color getWhiteButtonForeground() {
        return WHITE_BUTTON_FOREGROUND;
    }
    
}
