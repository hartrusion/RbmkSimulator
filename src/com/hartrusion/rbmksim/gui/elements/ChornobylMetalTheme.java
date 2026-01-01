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

    @Override
    public void addCustomEntriesToTable(UIDefaults table) {
        Color buttonReleased = new ColorUIResource(48, 48, 48);
        Color buttonPressed = new ColorUIResource(64, 64, 64);

        // This gets called from getDefaults from the metal look and feel.
        // The ocean theme will add all of its special things here if we call
        // the method from the ocean theme.
        super.addCustomEntriesToTable(table);
        // Manipulate table (it is derived from HashTable) again for our needs.

        // No text shall be "bold" by default, see DefaultMetalTheme docs.
        table.put("swing.boldMetal", Boolean.FALSE);

        // Button: No gradient color, but a more darker gray as base color.
        table.remove("Button.gradient");
        table.put("Button.background", buttonReleased);
        table.put("Button.select", buttonPressed);
        table.put("Button.foreground", Color.WHITE);

        // Toggle Button, same color as normal button, select-state will be
        // a bit lighter
        table.remove("ToggleButton.gradient");
        table.put("ToggleButton.background", buttonReleased);
        table.put("ToggleButton.select", buttonPressed);
        table.put("ToggleButton.foreground", Color.WHITE);
        
        // JDesktopPane has a white background as default, use something a bit 
        // darker than the panel backgrounds with even less blue parts
        table.put("Desktop.background", new ColorUIResource(0xA5ABB5));
        
        // Frames inside JDesktopPane are JInternalFrames
        // Remove the gradient from the selected frame
        table.remove("InternalFrame.activeTitleGradient");
    }
}
