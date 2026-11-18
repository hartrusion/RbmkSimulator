/*
 * Copyright (C) 2025 Viktor Alexander Hartung
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
import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.OceanTheme;

/**
 * Modifies the default theme "Ocean" from the Metal look and feel. Gradients
 * from buttons are removed and the bluish color is replaced by gray colors.
 * <p>
 * Besides the colors of the Metal look and feel, the theme also defines the
 * colors of the panel buttons, which are the colored buttons of the control
 * panels that show a state like an open or closed valve. These buttons exist
 * in the types green, red, gray, yellow and white, and each type has a dark
 * background for the normal state, a bright background for the highlighted
 * state and a text color. AbstractPanelWidget reads these colors with the
 * getters of the active theme whenever it sets the state of such a button, so
 * that the panels do not need to know which theme is installed.
 * ChornobylMetalThemeDark derives from this class and only overrides the
 * getters of the colors, so that both themes keep the same Metal shapes and
 * just differ in color.
 * <p>
 * The panel button colors are plain Color objects instead of ColorUIResource,
 * because Swing replaces any ColorUIResource of a component by the default of
 * the look and feel when the component UI is updated, which would reset the
 * state colors of the buttons.
 *
 * @author Viktor Alexander Hartung
 */
public class ChornobylMetalTheme extends OceanTheme {

    // Menu element outlines
    // Table and tree selection frames
    // slider right side and selection highlight outline
    private static final ColorUIResource PRIMARY1
            = new ColorUIResource(0x808080); // removed bluish color

    @Override
    protected ColorUIResource getPrimary1() {
        return PRIMARY1;
    }

    // Selection highlight color for menu entries and dropdown lists
    // The frame around the text after a button was pressed
    // Tick marks and gradient color on Slider
    private static final ColorUIResource PRIMARY2
            = new ColorUIResource(0xCCCCCC);

    @Override
    protected ColorUIResource getPrimary2() {
        return PRIMARY2;
    }

    // double frame around a button while it is pressed down
    // Tree and table selection
    // Checkbox lower gradient
    // Selection highlight (outer frame) from the sliders slider.
    private static final ColorUIResource PRIMARY3
            = new ColorUIResource(0xE6E6E6);

    @Override
    protected ColorUIResource getPrimary3() {
        return PRIMARY3;
    }

    // Button and ToggleButton outlines, outline stays same also when pressed.
    // slider right side
    // Outlines for tree, table, dropdown list
    // Menu selection frame
    // Checkbox and RadioButton frame
    // Replaced with a grey only, slightly darker color
    private static final ColorUIResource SECONDARY1
            = new ColorUIResource(0x626262);

    @Override
    protected ColorUIResource getSecondary1() {
        return SECONDARY1;
    }

    // Replace the light blue color when buttons are pressed with a greyish
    // non-blue color, also applies for some gradients for the lower side.
    // also the lower border of a checkbox and inner frame on dropdowns.
    // a replacement would be E6E6E6 but we go slightly darker to D0D0D0
    private static final ColorUIResource SECONDARY2
            = new ColorUIResource(0xD0D0D0);

    @Override
    protected ColorUIResource getSecondary2() {
        return SECONDARY2;
    }

    // Frame and Panel Background colors
    // Dropdown and Menu entry backgrounds
    // Replaced with a slightly darker Brackground from the Nimbus theme
    private static final ColorUIResource SECONDARY3
            = new ColorUIResource(0xD6D9DF);

    @Override
    protected ColorUIResource getSecondary3() {
        return SECONDARY3;
    }

    // Background of all standard buttons and toggle buttons in released
    // state, a dark gray without gradient.
    private static final ColorUIResource BUTTON_RELEASED
            = new ColorUIResource(48, 48, 48);

    /**
     * Returns the background of standard buttons and toggle buttons, which is
     * written into the UIDefaults by addCustomEntriesToTable.
     * <p>
     * LLM code generated with Claude Opus 5.5
     *
     * @return Background of a released standard button
     */
    protected ColorUIResource getButtonReleased() {
        return BUTTON_RELEASED;
    }

    // Background of standard buttons while pressed and of selected toggle
    // buttons, slightly lighter than the released state.
    private static final ColorUIResource BUTTON_PRESSED
            = new ColorUIResource(64, 64, 64);

    /**
     * LLM code generated with Claude Opus 5.5
     *
     * @return Background of a pressed standard button and a selected toggle
     * button
     */
    protected ColorUIResource getButtonPressed() {
        return BUTTON_PRESSED;
    }

    // Text color of the standard buttons and toggle buttons.
    private static final ColorUIResource BUTTON_FOREGROUND
            = new ColorUIResource(0xFFFFFF);

    /**
     * LLM code generated with Claude Opus 5.5
     *
     * @return Text color of standard buttons and toggle buttons
     */
    protected ColorUIResource getButtonForeground() {
        return BUTTON_FOREGROUND;
    }

    // JDesktopPane has a white background as default, use something a bit
    // darker than the panel backgrounds with even less blue parts
    private static final ColorUIResource DESKTOP_BACKGROUND
            = new ColorUIResource(0xA5ABB5);

    /**
     * LLM code generated with Claude Opus 5.5
     *
     * @return Background of the JDesktopPane behind the internal frames
     */
    protected ColorUIResource getDesktopBackground() {
        return DESKTOP_BACKGROUND;
    }

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

    @Override
    public String getName() {
        return "Chornobyl Metal";
    }

    @Override
    public void addCustomEntriesToTable(UIDefaults table) {
        // This gets called from getDefaults from the metal look and feel.
        // The ocean theme will add all of its special things here if we call
        // the method from the ocean theme.
        super.addCustomEntriesToTable(table);
        // Manipulate table (it is derived from HashTable) again for our needs.

        // No text shall be "bold" by default, see DefaultMetalTheme docs.
        table.put("swing.boldMetal", Boolean.FALSE);

        // Button: No gradient color, but a more darker gray as base color.
        table.remove("Button.gradient");
        table.put("Button.background", getButtonReleased());
        table.put("Button.select", getButtonPressed());
        table.put("Button.foreground", getButtonForeground());

        // Toggle Button, same color as normal button, select-state will be
        // a bit lighter
        table.remove("ToggleButton.gradient");
        table.put("ToggleButton.background", getButtonReleased());
        table.put("ToggleButton.select", getButtonPressed());
        table.put("ToggleButton.foreground", getButtonForeground());

        table.put("Desktop.background", getDesktopBackground());

        // Frames inside JDesktopPane are JInternalFrames
        // Remove the gradient from the selected frame
        table.remove("InternalFrame.activeTitleGradient");
    }
}
