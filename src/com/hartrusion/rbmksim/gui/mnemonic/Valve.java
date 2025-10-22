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

import com.hartrusion.rbmksim.gui.mnemonic.MnemonicColors;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.beans.BeanProperty;

/**
 *
 * @author vikto
 */
public class Valve extends javax.swing.JComponent {

    private static final float ENDS = 0.1F;
    private static final float GAP = 0.05F;

    private boolean vertical = false;

    private boolean active;

    public Valve() {
        // Initialize mnemnomic display colors.
        setBackground(MnemonicColors.getPassive()); // passive
        setForeground(MnemonicColors.getActive()); // active
        // background color would be 56, 59, 58

        setPreferredSize(new Dimension(40, 18));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        float length, diameter, middleStart, middleEnd;
        Shape shape;

        Graphics2D g2d = (Graphics2D) g;
        // Force the use of antialiasing to not look like 1997
        Object prevHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (vertical) {
            length = (float) getHeight();
            diameter = (float) getWidth();
        } else {
            length = (float) getWidth();
            diameter = (float) getHeight();
        }

        // Coordinate part for the middle valve thingy:
        middleStart = length * (ENDS + GAP);
        middleEnd = length * (1 - ENDS - GAP);

        if (active) {
            g.setColor(getForeground());
        } else {
            g.setColor(getBackground());
        }

        if (vertical) {
            shape = new Rectangle2D.Float(0, 0, diameter, length * ENDS);
            g2d.fill(shape);
            shape = new Rectangle2D.Float(0, length * (1.0F - ENDS), diameter, length * ENDS);
            g2d.fill(shape);
        } else {
            shape = new Rectangle2D.Float(0, 0, length * ENDS, diameter);
            g2d.fill(shape);
            shape = new Rectangle2D.Float(length * (1.0F - ENDS), 0, length * ENDS, diameter);
            g2d.fill(shape);
        }

        Path2D.Float reduct = new Path2D.Float();
        if (vertical) {
            reduct.moveTo(0, middleStart);
            reduct.lineTo(diameter, middleEnd);
            reduct.lineTo(0, middleEnd);
            reduct.lineTo(diameter, middleStart);
        } else {
            reduct.moveTo(middleStart, 0);
            reduct.lineTo(middleEnd, diameter);
            reduct.lineTo(middleEnd, 0);
            reduct.lineTo(middleStart, diameter);
        }
        reduct.closePath();
        g2d.fill(reduct);

        // Reset the antialiasing to its previous value
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevHint);
    }

    public boolean isVertical() {
        return vertical;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Sets the position to vertical, false is horizontal. Setting this will reset the size.")
    public void setVertical(boolean vertical) {
        this.vertical = vertical;
        if (vertical) {
            setPreferredSize(new Dimension(18, 40));
        } else {
            setPreferredSize(new Dimension(40, 18));
        }
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Switches to the foreground color")
    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }
}
