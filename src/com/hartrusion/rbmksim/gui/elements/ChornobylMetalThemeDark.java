/*
 * Copyright (C) 2026 Viktor Alexander Hartung
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.hartrusion.rbmksim.gui.elements;

import java.awt.Color;
import javax.swing.plaf.ColorUIResource;

/**
 * Dark variant of ChornobylMetalTheme, which keeps the Metal look and feel
 * with all its shapes of buttons, borders and sliders and only replaces the
 * colors. The class derives from ChornobylMetalTheme, so that the removal of
 * the gradients and all other UIDefaults entries of the light theme also
 * apply here, while each color getter is overridden with a dark color. The
 * colors of the panel buttons are overridden as well, because AbstractPanelWidget
 * reads them from the active theme, so that the colored state buttons of the
 * control panels can be adapted to the dark background in this class.
 * <p>
 * The Metal look and feel takes the text color from getBlack() and the
 * background of text fields, lists and tables from getWhite(), which is the
 * reason why these two getters are overridden with a light and a dark color
 * in this dark theme.
 * <p>
 * LLM code generated with Claude Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public class ChornobylMetalThemeDark extends ChornobylMetalTheme {

    // Menu element outlines, table and tree selection frames
    private static final ColorUIResource PRIMARY1
            = new ColorUIResource(0x9A9A9A);

    // Selection highlight of menu entries and dropdown lists
    private static final ColorUIResource PRIMARY2
            = new ColorUIResource(0x5A5A5A);

    // Frame around a pressed button, tree and table selection
    private static final ColorUIResource PRIMARY3
            = new ColorUIResource(0x4A4A4A);

    // Button, checkbox and list outlines
    private static final ColorUIResource SECONDARY1
            = new ColorUIResource(0x8A8A8A);

    // Pressed buttons and inner frames of dropdowns
    private static final ColorUIResource SECONDARY2
            = new ColorUIResource(0x505050);

    // Frame, panel, dropdown and menu backgrounds
    private static final ColorUIResource SECONDARY3
            = new ColorUIResource(0x3C3F41);

    // Background of text fields, lists and tables
    private static final ColorUIResource WHITE
            = new ColorUIResource(0x2B2B2B);

    // Text color of labels, menus and text fields
    private static final ColorUIResource BLACK
            = new ColorUIResource(0xDDDDDD);

    private static final ColorUIResource BUTTON_RELEASED
            = new ColorUIResource(0x1E1E1E);

    private static final ColorUIResource BUTTON_PRESSED
            = new ColorUIResource(0x2E2E2E);

    private static final ColorUIResource BUTTON_FOREGROUND
            = new ColorUIResource(0xDDDDDD);

    private static final ColorUIResource DESKTOP_BACKGROUND
            = new ColorUIResource(0x2E3033);

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
    protected ColorUIResource getPrimary1() {
        return PRIMARY1;
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return PRIMARY2;
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return PRIMARY3;
    }

    @Override
    protected ColorUIResource getSecondary1() {
        return SECONDARY1;
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return SECONDARY2;
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return SECONDARY3;
    }

    @Override
    protected ColorUIResource getWhite() {
        return WHITE;
    }

    @Override
    protected ColorUIResource getBlack() {
        return BLACK;
    }

    @Override
    protected ColorUIResource getButtonReleased() {
        return BUTTON_RELEASED;
    }

    @Override
    protected ColorUIResource getButtonPressed() {
        return BUTTON_PRESSED;
    }

    @Override
    protected ColorUIResource getButtonForeground() {
        return BUTTON_FOREGROUND;
    }

    @Override
    protected ColorUIResource getDesktopBackground() {
        return DESKTOP_BACKGROUND;
    }

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

    @Override
    public String getName() {
        return "Chornobyl Metal Dark";
    }
}
