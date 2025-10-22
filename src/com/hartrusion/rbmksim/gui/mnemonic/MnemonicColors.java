/*
 * Copyright (C) 2025 Viktor Alexander Hartung
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
package com.hartrusion.rbmksim.gui.mnemonic;

import java.awt.Color;

/**
 *
 * @author Viktor Alexander Hartung
 */
public class MnemonicColors {
    public static final Color PASSIVE = new Color(95, 98, 88);
    
    public static final Color ACTIVE = new Color(14, 222, 194);
    
    public static final Color BACKGROUND = new Color(56, 59, 58);
    
    public static final Color DIM = new Color(93, 133, 94);
    
    public static final Color READING_BACKGROUND = new Color(77, 69, 27);
    
    public static final Color READING_TEXT = new Color(231, 255, 166);

    public static Color getPassive() {
        return PASSIVE;
    }

    public static Color getActive() {
        return ACTIVE;
    }

    public static Color getBackground() {
        return BACKGROUND;
    }
    
    public static Color getReadingText() {
        return READING_TEXT;
    }
    
        public static Color getReadingBackground() {
        return READING_BACKGROUND;
    }
    
    
    
}
