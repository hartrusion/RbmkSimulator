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
package com.hartrusion.rbmksim.gui.elements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.beans.BeanProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * A circular gauge used for the control rod positions. I
 *
 * @author Viktor Alexander Hartung
 */
public class SelsynGauge extends javax.swing.JComponent {

    private static final float PI_HALF = 1.5707963267949F;

    /**
     * Manually set the numbers where to make a major tick display to have the
     * ability to remove some if things look to messy.
     */
    private final int[] majorTickLabels = {0, 1, 2, 3, 4, 5, 6, 7};

    /**
     * Maximum displayed value
     */
    private final double maxValue = 7.3;
    private double majorTickSpacing = 1.0;
    private double minorTickSpacing = 0.2;
    private double[] majorTicks;
    private double[] minorTicks;

    /**
     * Position for maximum value in radians
     */
    private final float maxValuePhi = 5.14872129F; // 295°

    private float size, outerRingRadius, outerTickRadius, majorTicksInnerRadius,
            minorTicksInnerRadius, innerCircleRadius, pointerWidth,
            pointerLength, indicatorSize, halfSize;

    /**
     * currently displayed value
     */
    private double value;

    private boolean showIndicator = true;
    private boolean indicatorStatus;
    private boolean reverse;

    private float phiValue = 0;

    private int oldWidth, oldHeight;

    private final List<Shape> majorTickLines = new ArrayList<>();
    private final List<Shape> minorTickLines = new ArrayList<>();

    /**
     * Contains the one-time rendered part with all the dials and so on, will
     * only be drawn once or refreshed on size change.
     */
    private BufferedImage gaugeBack;

    /**
     * Creates new component SelsynGauge
     */
    public SelsynGauge() {
        initTicks();
        initComponents();
    }

    public double getChornobylValue() {
        return value;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Sets the current display value")
    public void setChornobylValue(double value) {
        if (value < 0.0) {
            value = 0.0;
        } else if (value > maxValue) {
            value = maxValue;
        }
        double old = this.value;
        this.value = value;
        phiValue = getPhi(value); // where should it point to?
        firePropertyChange("chornobylValue", old, value);
        repaint();
    }

    public boolean getChornobylIndicator() {
        return indicatorStatus;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Sets the little indicator light status")
    public void setChornobylIndicator(boolean indicator) {
        boolean old = this.indicatorStatus;
        this.indicatorStatus = indicator;
        firePropertyChange("chornobylIndicator", old, indicator);
        repaint();
    }

    public boolean getChornobylShowIndicator() {
        return showIndicator;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Sets wether the little indicator is visible or not")
    public void setChornobylShowIndicator(boolean show) {
        boolean old = this.showIndicator;
        this.showIndicator = show;
        firePropertyChange("chornobylShowIndicator", old, show);
        repaint();
    }

    public boolean getChornobylReverse() {
        return reverse;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Used for reverse direction and gauge, rods from below.")
    public void setChornobylReverse(boolean reverse) {
        boolean old = this.reverse;
        this.reverse = reverse;
        firePropertyChange("chornobylReverse", old, reverse);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(41, 41);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(null);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Ellipse2D.Float circle;

        // Force the use of antialiasing to not look like 1997
        Object prevHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // The gauges background is heavy to calculate and it is kind of static 
        // so we only calculate it once if necessary. This improves the overal
        // performance of the view significantly on low end hardware. It will 
        // result in a slow delay on first rendering but that's fine.
        if (oldWidth != getWidth() || oldHeight != getHeight()
                || gaugeBack == null) {
            oldWidth = getWidth();
            oldHeight = getHeight();

            // generate or overwrite the background image on request.
            // Note that we use g2b instead of g2!
            gaugeBack = new BufferedImage(oldWidth, oldHeight,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2b = gaugeBack.createGraphics();
            g2b.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            size = Math.min(oldWidth, oldHeight);
            outerRingRadius = 0.45F * (float) size;
            outerTickRadius = 0.4F * (float) size;
            majorTicksInnerRadius = outerTickRadius * 0.8F;
            minorTicksInnerRadius = outerTickRadius * 0.9F;
            innerCircleRadius = 0.08F * (float) size;
            pointerWidth = innerCircleRadius * 0.5F;
            pointerLength = outerTickRadius * 1.05F;
            indicatorSize = size * 0.14F;
            halfSize = size * 0.5F;

            // This calls sin and cosine a lot, so we will have this calculated
            // only once and just draw the shapes stored in those lists
            majorTickLines.clear();
            for (int idx = 0; idx < majorTicks.length; idx++) {
                majorTickLines.add(getLine(majorTicksInnerRadius,
                        outerTickRadius,
                        getPhi((double) majorTicks[idx])));
            }
            minorTickLines.clear();
            for (int idx = 0; idx < minorTicks.length; idx++) {
                minorTickLines.add(getLine(minorTicksInnerRadius,
                        outerTickRadius,
                        getPhi((double) minorTicks[idx])));
            }

            // Draw background
            g2b.setColor(Color.WHITE);
            circle = new Ellipse2D.Float(
                    halfSize - outerRingRadius,
                    halfSize - outerRingRadius,
                    2.0F * outerRingRadius, 2.0F * outerRingRadius);
            g2b.fill(circle);
            // Draw the outer ring using foreground color.
            g2b.setColor(getForeground());
            g2b.draw(circle);
            // draw major tick lines
            g2b.setColor(Color.BLACK);
            for (Shape s : majorTickLines) {
                g2b.draw(s);
            }
            // Draw minor tick lines
            for (Shape s : minorTickLines) {
                g2b.draw(s);
            }
            // draw the inner cirle:
            circle = new Ellipse2D.Float(
                    halfSize - innerCircleRadius,
                    halfSize - innerCircleRadius,
                    2.0F * innerCircleRadius, 2.0F * innerCircleRadius);
            g2b.fill(circle);
        }
        
        // Draw image from buffer, either it is still present or it was 
        // just generated.
        g2.drawImage(gaugeBack, 0, 0, null);

        // generate the pointer as a triangle
        g2.setColor(Color.BLACK);
        Path2D.Float triangle = new Path2D.Float();
        // path through 3 points:
        triangle.moveTo(getXCoordinate(pointerWidth, phiValue - PI_HALF),
                getYCoordinate(pointerWidth, phiValue - PI_HALF));
        triangle.lineTo(getXCoordinate(pointerLength, phiValue),
                getYCoordinate(pointerLength, phiValue));
        triangle.lineTo(getXCoordinate(pointerWidth, phiValue + PI_HALF),
                getYCoordinate(pointerWidth, phiValue + PI_HALF));
        triangle.closePath();
        g2.fill(triangle);
        // Draw the litte inidcator light on the bottom
        if (showIndicator) {
            if (indicatorStatus) {
                g2.setColor(Color.GREEN);
            } else {
                g2.setColor(Color.GRAY);
            }
            circle = new Ellipse2D.Float(
                    halfSize - indicatorSize * 0.5F - 1.0F,
                    size - indicatorSize - 1.0F, // 1 px up, clips...
                    indicatorSize, indicatorSize);
            g2.fill(circle);
            // Draw the outer ring in black
            g2.setColor(Color.BLACK);
            g2.draw(circle);
        }
        // Reset the antialiasing to its previous value
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevHint);
    }

    /**
     * Generates a line (shape) between two radius values and a given phi value,
     * used to draw the tick lines.
     *
     * @param rStart distance from center where to start (subpixel double)
     * @param rEnd distance from center where to end (subpixel double)
     * @param phi where should the line be placed, in radians.
     * @return Reference to Shape of Line2D type.
     */
    private Shape getLine(float rStart, float rEnd, float phi) {
        return new Line2D.Float(getXCoordinate(rStart, phi),
                getYCoordinate(rStart, phi),
                getXCoordinate(rEnd, phi),
                getYCoordinate(rEnd, phi));
    }

    /**
     * Calculate the X coordinate for given R and phi for a rotation around the
     * center.
     *
     * @param R radius (subpixel double)
     * @param phi in radians (the thing with pi)
     * @return X coordinate (subpixel double)
     */
    private float getXCoordinate(float r, float phi) {
        return size * 0.5F + (float) Math.sin((double) phi) * r;
    }

    /**
     * Calculate the Y coordinate for given R and phi for a rotation around the
     * center.
     *
     * @param R radius (subpixel double)
     * @param phi in radians (the thing with pi)
     * @return X coordinate (subpixel double)
     */
    private float getYCoordinate(float r, float phi) {
        if (reverse) {
            return size * 0.5F + (float) Math.cos((double) phi) * r;
        }
        return size * 0.5F - (float) Math.cos((double) phi) * r;
    }

    /**
     * Calculate phi (radians) for given physical value
     *
     * @param value
     * @return phi - radians!
     */
    private float getPhi(double value) {
        return (float) (value / maxValue * maxValuePhi);
    }

    private void initTicks() {
        // Init major tick values:
        int nrOfTicks = (int) (maxValue / majorTickSpacing) + 1;
        majorTicks = new double[nrOfTicks];
        majorTicks[0] = 0.0;
        for (int idx = 1; idx < majorTicks.length; idx++) {
            majorTicks[idx] = majorTicks[idx - 1] + majorTickSpacing;
        }

        // init minor tick values, initialize a temporary array first
        double[] minorTicks = new double[(int) (maxValue / minorTickSpacing) + 1];
        double tickValue = 0.0;
        boolean hasMajorTickValue;
        nrOfTicks = 0;
        while (tickValue < maxValue) { // how many ticks do we need?
            // check if we have a major tick here to avoid double lines:
            hasMajorTickValue = false;
            for (int idx = 0; idx < majorTicks.length; idx++) {
                if (majorTicks[idx] == 0.0 && tickValue == 0.0) {
                    hasMajorTickValue = true;
                    break;
                }
                if (Math.abs(majorTicks[idx] / tickValue - 1.0) < 1e-8) {
                    hasMajorTickValue = true;
                    break;
                }
            }
            if (!hasMajorTickValue) {
                minorTicks[nrOfTicks] = tickValue;
                nrOfTicks++;
            }
            tickValue = tickValue + minorTickSpacing;
        }
        this.minorTicks = new double[nrOfTicks];
        System.arraycopy(minorTicks, 0, this.minorTicks, 0, nrOfTicks);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
