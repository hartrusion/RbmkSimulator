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
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * A simple area that is fully drawn with solid color, set as the foreground
 * color.
 *
 * @author vikto
 */
public class SolidColor extends javax.swing.JComponent {

    public SolidColor() {
        setPreferredSize(new Dimension(128, 5));
        setForeground(new Color(14, 222, 194));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(getForeground());
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
