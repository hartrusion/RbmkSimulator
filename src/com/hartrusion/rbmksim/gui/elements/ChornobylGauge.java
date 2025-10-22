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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JLabel;

/**
 * A fixed size gauge as it can be found everywhere in the control panel.
 *
 * @author Viktor Alexander Hartung
 */
@JavaBean(defaultProperty = "UI", description = "Chornobyl styled pixel linear gauge.")
public final class ChornobylGauge extends javax.swing.JPanel {

    private static final int Y_POSITION_BAR = 16;
    private static final int X_POSITION_BAR = 27;
    private static final int SCALE_WIDTH = 80;
    private static final int BAR_HEIGHT = 8;
    private static final int SPOT_WIDTH = 4;
    private static final int TICK_HEIGHT = 4;

    private final int barWidth;

    private final int xPosIndicatorMin;
    private final int xPosIndicatorMax;
    private int xPosIndicator;

    private Color spotColor;

    private int xPositionLeftSpot, xPositionRightSpot;

    private float maximum = 110;
    private float minimum = -10;
    private float value = 20;
    private final NumberFormat numberFormat = DecimalFormat.getInstance();

    private float[] tickValues = {0F, 60F, 100F};
    private int[] tickCoordinates;
    private javax.swing.JLabel[] tickLabels = new javax.swing.JLabel[3];

    /**
     * Creates new form Gauge
     */
    public ChornobylGauge() {
        // one-time calculations
        barWidth = SCALE_WIDTH + 2 * SPOT_WIDTH + 3;
        xPosIndicatorMin = X_POSITION_BAR + 1 + SPOT_WIDTH;
        xPosIndicatorMax = xPosIndicatorMin + SCALE_WIDTH;

        updateIndicatorPosition();
        initComponents();
        updateTicks();
    }

    private void updateIndicatorPosition() {
        if (value > maximum) {
            xPosIndicator = xPosIndicatorMax;
            spotColor = Color.RED;
        } else if (value < minimum) {
            xPosIndicator = xPosIndicatorMin;
            spotColor = Color.RED;
        } else {
            xPosIndicator = getCoordinateValue(value);
            spotColor = Color.ORANGE;
        }
        xPositionLeftSpot = xPosIndicator - SPOT_WIDTH;
        xPositionRightSpot = xPosIndicator + 1;
    }

    private void updateTicks() {
        // Generate a proper number of digits
        int digits = -2 + (int) (Math.log10(maximum - minimum));
        if (digits < 0) {
            numberFormat.setMaximumFractionDigits(-digits);
        } else {
            numberFormat.setMaximumFractionDigits(0);
        }
        if (tickLabels != null) {
            for (JLabel tickLabel : tickLabels) {
                if (tickLabel != null) {
                    remove(tickLabel);
                }
            }
        }
        tickLabels = new javax.swing.JLabel[tickValues.length];
        // generate labels using numberFormat
        for (int idx = 0; idx < tickLabels.length; idx++) {
            tickLabels[idx] = new javax.swing.JLabel();
            tickLabels[idx].setText(numberFormat.format(tickValues[idx]));
            tickLabels[idx].setFont(tickLabels[idx].getFont().deriveFont(tickLabels[idx].getFont().getStyle() & ~java.awt.Font.BOLD, tickLabels[idx].getFont().getSize() - 3));
            tickLabels[idx].setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            add(tickLabels[idx]);
        }
        // Update Tick coordinates
        tickCoordinates = new int[tickValues.length];
        for (int idx = 0; idx < tickCoordinates.length; idx++) {
            tickCoordinates[idx] = getCoordinateValue(tickValues[idx]);
            tickLabels[idx].setBounds(tickCoordinates[idx] - 15,
                    Y_POSITION_BAR - TICK_HEIGHT - 12, 30, 13);
        }
    }

    /**
     * To calculate the pixel value from a given float value on the draw area.
     * This is used multiple times so its a function for its own.
     *
     * @param value float value
     * @return int coordinate in component.
     */
    private int getCoordinateValue(float value) {
        return xPosIndicatorMin
                + (int) (((float) (xPosIndicatorMax - xPosIndicatorMin))
                * (value - minimum) / (maximum - minimum));
    }

    public float getChornobylMaximum() {
        return maximum;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Sets the scale end value")
    public void setChornobylMaximum(float chornobylMaximum) {
        double old = this.maximum;
        this.maximum = chornobylMaximum;
        updateIndicatorPosition();
        firePropertyChange("chornobylMaximum", old, chornobylMaximum);
        repaint();
    }

    public float getChornobylMinimum() {
        return minimum;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Sets the scale start value")
    public void setChornobylMinimum(float chornobylMinimum) {
        float old = this.minimum;
        this.minimum = chornobylMinimum;
        updateIndicatorPosition();
        firePropertyChange("chornobylMinimum", old, chornobylMinimum);
        repaint();
    }

    public float getChornobylValue() {
        return value;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Sets the current display value")
    public void setChornobylValue(float value) {
        float old = this.value;
        this.value = value;
        updateIndicatorPosition();
        firePropertyChange("chornobylValue", old, value);
        repaint();
    }

    public float[] getChornobylTicks() {
        return tickValues;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Sets the  values which will be marked with dashes and ticks")
    public void setChornobylTicks(float[] chornobylTicks) {
        float[] old = new float[this.tickValues.length];
        System.arraycopy(this.tickValues, 0, old, 0, this.tickValues.length);
        this.tickValues = chornobylTicks;
        updateTicks();
        firePropertyChange("chornobylTicks", old, chornobylTicks);
        repaint();
    }

    public String getChornobylUnitText() {
        return jLabelUnit.getText();
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Unit String displayed on the left")
    public void setChornobylUnitText(String value) {
        String old = jLabelUnit.getText();
        jLabelUnit.setText(value);
        firePropertyChange("chornobylUnitText", old, value);
        repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelUnit = new javax.swing.JLabel();

        setBackground(new java.awt.Color(245, 245, 225));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(125, 27));
        setMinimumSize(new java.awt.Dimension(125, 27));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(125, 27));
        setLayout(null);

        jLabelUnit.setFont(jLabelUnit.getFont().deriveFont(jLabelUnit.getFont().getStyle() & ~java.awt.Font.BOLD, jLabelUnit.getFont().getSize()-3));
        jLabelUnit.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelUnit.setText("cm");
        add(jLabelUnit);
        jLabelUnit.setBounds(3, 2, 38, 16);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Black rectanle in the back
        g2.setColor(Color.BLACK);
        g2.fillRect(X_POSITION_BAR, Y_POSITION_BAR, barWidth, BAR_HEIGHT);

        // tick marks
        if (tickCoordinates != null) {
            for (int idx = 0; idx < tickCoordinates.length; idx++) {
                g2.drawLine(tickCoordinates[idx], Y_POSITION_BAR - TICK_HEIGHT,
                        tickCoordinates[idx], Y_POSITION_BAR);
            }
        }

        // tick labels
//        FontMetrics fm = g.getFontMetrics();
//        for (int idx = 0; idx < tickLabels.length; idx++) {
//            g.drawString(tickLabels[idx],
//                        tickCoordinates[idx]
//                        - fm.stringWidth(tickLabels[idx]) / 2,
//                        Y_POSITION_BAR - TICK_HEIGHT - 2);
//        }
        // Two orange squares for value
        g2.setColor(spotColor);
        g2.fillRect(xPositionLeftSpot, Y_POSITION_BAR + 1, SPOT_WIDTH, BAR_HEIGHT - 2);
        g2.fillRect(xPositionRightSpot, Y_POSITION_BAR + 1, SPOT_WIDTH, BAR_HEIGHT - 2);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelUnit;
    // End of variables declaration//GEN-END:variables
}
