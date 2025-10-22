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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.beans.BeanProperty;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 * Pump Icon for mnmonic displays.
 *
 * <ul>
 * <li>Pump wired: Outline glows bright.</li>
 * <li>Hot reserve: Inside slightly glows</li>
 * <li>Pump active: Everything glows</li>
 * </ul>
 *
 * @author Viktor Alexander Hartung
 */
public class Pump extends JComponent implements SwingConstants {

    private final float[] u = new float[3];
    private final float[] v = new float[3];

    private int orientation = 4; // TOP 1, LEFT 2. BOTTOM 3, RIGHT 4

    private float width, length, centerXOffset, centerYOffset;

    private float subtrCircleDiameter;
    private float subtrCircleX;
    private float subtrCircleY;

    private float innerRingDiameter, innerRingX, innerRingY;

    private Color innerColor, outerColor;

    private int status; // 0: off, 1: wired, 2: hot reserve, 3: active 

    public static final int OFF = 0;
    public static final int WIRED = 1;
    public static final int HOT_RESERVE = 2;
    public static final int ACTIVE = 3;

    public Pump() {
        // Initialize mnemnomic display colors.
        setBackground(new Color(95, 98, 88)); // passive
        setForeground(new Color(14, 222, 194)); // active
        // background color would be 56, 59, 58
        setPreferredSize(new Dimension(41, 36));

        setStatus(OFF); // This also inits the color variables

        calculateCoordinates();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                calculateCoordinates();
                repaint();
            }
        });
    }

    private void calculateCoordinates() {
        // length/width ratio is 1:1.15. if orientation is top or botton, length
        // is height, otherwise length is width.
        if (orientation == TOP || orientation == BOTTOM) {
            width = Math.min(0.8695652175F * (float) getHeight(), (float) getWidth());
            length = Math.min((float) getHeight(), 1.15F * (float) getWidth());
        } else {
            width = Math.min(0.8695652175F * (float) getWidth(), (float) getHeight());
            length = Math.min((float) getWidth(), 1.15F * (float) getHeight());
        }
        // length distances for outlet shape
        u[0] = 0.23F * length;
        u[1] = 0.41F * length;
        u[2] = 0.55F * length;
        // width distances for outlet shape
        v[0] = 0.12F * width;
        v[1] = 0.25F * width;
        v[2] = 0.34F * width;

        // circles will be off-centered for some orientations:
        switch (orientation) {
            case 1: // TOP
                centerXOffset = 0.0F;
                centerYOffset = length * 0.15F;
                break;
            case 2: // LEFT
                centerXOffset = length * 0.15F;
                centerYOffset = 0.0F;
                break;
            case 3, 4: // BOTTOM, RIGHT:
                centerXOffset = 0.0F;
                centerYOffset = 0.0F;
                break;
        }

        float outerRingWidth = width * 0.1F;
        subtrCircleX = centerXOffset + outerRingWidth;
        subtrCircleY = centerYOffset + outerRingWidth;
        subtrCircleDiameter = width - 2 * outerRingWidth;

        innerRingDiameter = width * 0.7F;
        innerRingX = centerXOffset + width * 0.15F;
        innerRingY = centerYOffset + width * 0.15F;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // Force the use of antialiasing to not look like 1997
        Object prevHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Paint the outlet depending on the orientation. Note that right view
        // is also different from the other 3 and not just turned.
        Path2D.Float outlet = new Path2D.Float();
        switch (orientation) {
            case 1: // top
                outlet.moveTo(0, 0);
                outlet.lineTo(0, u[2]);
                outlet.lineTo(v[0], u[1]);
                outlet.lineTo(v[2], u[0]);
                outlet.lineTo(v[1], 0);
                break;
            case 2: // left
                outlet.moveTo(0, width);
                outlet.lineTo(u[2], width);
                outlet.lineTo(u[1], width - v[0]);
                outlet.lineTo(u[0], width - v[2]);
                outlet.lineTo(0, width - v[1]);
                break;
            case 3: // BOTTOM
                outlet.moveTo(width, length);
                outlet.lineTo(width - v[1], length);
                outlet.lineTo(width - v[2], length - u[0]);
                outlet.lineTo(width - v[0], length - u[1]);
                outlet.lineTo(width, length - u[2]);
                break;
            case 4: // RIGHT
                outlet.moveTo(length, width);
                outlet.lineTo(length - u[2], width);
                outlet.lineTo(length - u[1], width - v[0]);
                outlet.lineTo(length - u[0], width - v[2]);
                outlet.lineTo(length, width - v[1]);
                break;
        }
        outlet.closePath();
        g2d.setColor(outerColor);
        g2d.fill(outlet);
        // Outer Ring:
        Ellipse2D.Float outer = new Ellipse2D.Float(
                centerXOffset, centerYOffset,
                width, width);
        Ellipse2D.Float inner = new Ellipse2D.Float(
                subtrCircleX, subtrCircleY,
                subtrCircleDiameter, subtrCircleDiameter);
        Area ring = new Area(outer);
        ring.subtract(new Area(inner));
        g2d.fill(ring);

        // Inner circle:
        Ellipse2D.Float center = new Ellipse2D.Float(
                innerRingX, innerRingY,
                innerRingDiameter, innerRingDiameter);
        g2d.setColor(innerColor);
        g2d.fill(center);
        // Reset the antialiasing to its previous value
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevHint);
    }

    public int getOrientation() {
        return orientation;
    }

    /**
     * Set the pump output direction.
     *
     * @param orientation {@code TOP}, {@code LEFT}, {@code BOTTOM} or
     * {@code RIGHT}
     * @throws IllegalArgumentException if orientation is an illegal value
     */
    @BeanProperty(preferred = true, visualUpdate = true, enumerationValues = {
        "Pump.TOP",
        "Pump.LEFT",
        "Pump.BOTTOM",
        "Pump.RIGHT"}, description
            = "Sets the pump output direction")
    public void setOrientation(int orientation) {
        if (orientation < 1 || orientation > 4) {
            throw new IllegalArgumentException("Orientation must be between "
                    + "1 and 4.");
        }
        int oldValue = this.orientation;
        this.orientation = orientation;
        calculateCoordinates();
        firePropertyChange("orientation", oldValue, orientation);
        repaint();
    }

    public int getStatus() {
        return status;
    }

    /**
     * Set the pump output direction.
     *
     * @param status {@code OFF}, {@code WIRED}, {@code HOT_RESERVE} or
     * {@code ACTIVE}
     * @throws IllegalArgumentException if orientation is an illegal value
     */
    @BeanProperty(preferred = true, visualUpdate = true, enumerationValues = {
        "Pump.OFF",
        "Pump.WIRED",
        "Pump.HOT_RESERVE",
        "Pump.ACTIVE"}, description
            = "Sets the pump display status.")
    public final void setStatus(int status) {
        if (status < OFF || status > ACTIVE) {
            throw new IllegalArgumentException("Status value not valid.");
        }
        int oldStatus = this.status;
        this.status = status;
        switch (status) {
            case OFF:
                outerColor = MnemonicColors.PASSIVE;
                innerColor = MnemonicColors.PASSIVE;
                break;
            case WIRED:
                outerColor = MnemonicColors.ACTIVE;
                innerColor = MnemonicColors.PASSIVE;
                break;
            case HOT_RESERVE:
                outerColor = MnemonicColors.ACTIVE;
                innerColor = MnemonicColors.DIM;
                break;
            case ACTIVE:
                outerColor = MnemonicColors.ACTIVE;
                innerColor = MnemonicColors.ACTIVE;
                break;
        }

        firePropertyChange("status", oldStatus, status);
        repaint();
    }
}
