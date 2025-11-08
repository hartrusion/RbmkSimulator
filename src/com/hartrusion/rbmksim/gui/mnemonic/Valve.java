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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.beans.BeanProperty;

/**
 * A valve element for mnemonic displays. Even though it is designed to be used
 * with the exact same size, it was made scaleable. It can be placed vertically
 * or horizontally, changing this will automatically resize the element.
 *
 * @author vikto
 */
public class Valve extends javax.swing.JComponent {

    /**
     * Size of the end part in parts of the length
     */
    private static final float ENDS = 0.1F;

    /**
     * Size of the gap in parts of the length
     */
    private static final float GAP = 0.05F;

    private static final float INDICATOR_WIDTH = 0.2F;

    private static final float INDICATOR_HEIGT = 0.45F;

    private boolean vertical = false;

    private boolean active;

    private boolean hasControlIndicator;

    private boolean controlIndicatorActive;

    public Valve() {
        // Initialize mnemnomic display colors.
        setBackground(MnemonicColors.getPassive()); // passive
        setForeground(MnemonicColors.getActive()); // active

        setPreferredSize(new Dimension(40, 18));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        float length, diameter, middleStart, middleEnd, indicatorStart = 0.0F,
                indicatorEnd = 0.0F;
        Shape shape;
        Path2D.Float reduct;

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

        // Draw two end lines
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

        // Draw the valve itself (flow reduction depiction)
        reduct = new Path2D.Float();
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

        // Draw the indicator
        if (hasControlIndicator) {
            indicatorStart = length * (1.0F - INDICATOR_WIDTH) * 0.5F;
            indicatorEnd = indicatorStart + length * INDICATOR_WIDTH;
            if (controlIndicatorActive) {
                g.setColor(getForeground());
            } else {
                g.setColor(getBackground());
            }
            reduct = new Path2D.Float();
            if (vertical) {
                reduct.moveTo(diameter, indicatorStart);
                reduct.lineTo((1.0F - INDICATOR_HEIGT) * diameter, length * 0.5F);
                reduct.lineTo(diameter, indicatorEnd);
            } else {
                reduct.moveTo(indicatorStart, 0);
                reduct.lineTo(length * 0.5F, INDICATOR_HEIGT * diameter);
                reduct.lineTo(indicatorEnd, 0);
            }
            reduct.closePath();
            g2d.fill(reduct);
        }

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

    public boolean isControlIndicator() {
        return hasControlIndicator;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Display of a control indicator")
    public void setControlIndicator(boolean hasControlIndicator) {
        this.hasControlIndicator = hasControlIndicator;
        repaint();
    }

    public boolean isControlIndicatorActive() {
        return controlIndicatorActive;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "The control indicator is displayed with highlight color.")
    public void setControlIndicatorActive(boolean controlIndicatorActive) {
        this.controlIndicatorActive = controlIndicatorActive;
        repaint();
    }
}
