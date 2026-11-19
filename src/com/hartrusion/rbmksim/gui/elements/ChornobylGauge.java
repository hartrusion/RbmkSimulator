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

import com.hartrusion.rbmksim.gui.GuiThemeManager;
import com.hartrusion.rbmksim.gui.RbmkElementColors;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/**
 * A linear gauge as it can be found everywhere in the control panel, which
 * shows a value as two small orange spots on a black bar with tick marks and
 * labels above it and the unit in the upper left corner. All colors are taken
 * from the RbmkElementColors of the active GUI theme, so that the dark theme
 * can show the gauge with a dark background and blue spots and labels.
 * <p>
 * The gauge is designed for a base size of 125 x 27 pixels, but it scales to
 * any size that it gets from its layout. All coordinates are calculated as
 * integer pixel values of the device (which includes a global HiDPI scaling),
 * and the bar, the ticks and the spots are drawn as filled rectangles without
 * antialiasing, so that every line covers whole pixels and the spacing stays
 * exact instead of being interpolated.
 * <p>
 * The values of the gauges are calculated by the model and change slightly in
 * every cycle, so the gauge does not compare the values but the pixel position
 * of the spots that results from them. The static part with the bar, the ticks
 * and the texts is rendered once into the image gaugeBack, and the complete
 * gauge with the spots into the image gaugeComplete, which is only rendered
 * again if the spot position or its color changed. A repaint is only
 * requested in that case, otherwise the gauge does not cause any drawing.
 * <p>
 * LLM code generated with Claude Opus 5.5 (scaling and buffered images)
 *
 * @author Viktor Alexander Hartung
 */
@JavaBean(defaultProperty = "UI", description = "Chornobyl styled pixel linear gauge.")
public final class ChornobylGauge extends javax.swing.JComponent {

    /**
     * Width of the design at scale 1.0, which is used to calculate the scale
     * from the current size.
     */
    private static final int BASE_WIDTH = 125;

    /**
     * Height of the design at scale 1.0.
     */
    private static final int BASE_HEIGHT = 27;

    // The following values describe the geometry in pixels at scale 1.0.
    private static final int BASE_Y_POSITION_BAR = 16;
    private static final int BASE_X_POSITION_BAR = 27;
    private static final int BASE_SCALE_WIDTH = 80;
    private static final int BASE_BAR_HEIGHT = 8;
    private static final int BASE_SPOT_WIDTH = 4;
    private static final int BASE_TICK_HEIGHT = 4;

    /**
     * Width and height of the box in which a tick label is centered above its
     * tick mark at scale 1.0.
     */
    private static final int BASE_TICK_LABEL_WIDTH = 30;
    private static final int BASE_TICK_LABEL_HEIGHT = 13;

    // Box of the unit text at scale 1.0, the text is placed left aligned and
    // vertically centered in it.
    private static final int BASE_UNIT_X = 3;
    private static final int BASE_UNIT_Y = 2;
    private static final int BASE_UNIT_HEIGHT = 16;

    /**
     * Difference of the font size of tick labels and unit text to the default
     * label font.
     */
    private static final int FONT_SIZE_REDUCTION = 3;

    // Geometry in device pixels for the current size, calculated by
    // updateGeometry from the base values.
    private int barX, barY, barWidth, barHeight, spotWidth, lineWidth,
            tickHeight, scaleWidth;

    private int xPosIndicatorMin;
    private int xPosIndicatorMax;

    /**
     * Pixel position of the gap between the two spots, which marks the
     * displayed value. A new value only causes a repaint if it changes this
     * position.
     */
    private int xPosIndicator;

    /**
     * Color of the spots, which is one of the two indicator colors of the
     * theme, depending on whether the value is inside the scale.
     */
    private Color spotColor;

    /**
     * Scale of the current geometry, which is the ratio of the image size in
     * device pixels to the base size.
     */
    private double scale = 1.0;

    private float maximum = 110;
    private float minimum = -10;
    private float value = 20;
    private final NumberFormat numberFormat = DecimalFormat.getInstance();

    private float[] tickValues = {0F, 60F, 100F};

    /**
     * Pixel position of each tick mark in the current geometry.
     */
    private int[] tickCoordinates;

    /**
     * Formatted text of each tick value, displayed above the tick marks.
     */
    private String[] tickTexts;

    private String unitText = "cm";

    /**
     * Contains the static part of the gauge with background, bar, ticks and
     * texts in device pixels.
     */
    private BufferedImage gaugeBack;

    /**
     * Contains the complete gauge, which is the background image with the
     * spots of the current value on top of it.
     */
    private BufferedImage gaugeComplete;

    /**
     * Set when a property of the static part changed, so that gaugeBack has to
     * be rendered again before the next drawing.
     */
    private boolean gaugeBackDeprecated = true;

    /**
     * Set when the position or the color of the spots changed, so that
     * gaugeComplete has to be rendered again before the next drawing.
     */
    private boolean gaugeCompleteDeprecated = true;

    /**
     * Enabled state with which gaugeComplete was rendered, because a disabled
     * gauge shows no spots.
     */
    private boolean renderedEnabled;

    /**
     * Reference to the tooltip object if it is present.
     */
    private javax.swing.JToolTip activeToolTip;

    /**
     * Colors of the active GUI theme, which are read once when the gauge is
     * created, because all windows are created again when the GUI theme is
     * switched. The field is initialized before the constructor runs, so that
     * initComponents() can already take its colors from it.
     * <p>
     * LLM code generated with Claude Opus 5.5
     */
    private final RbmkElementColors elementColors
            = GuiThemeManager.getActiveTheme().getElementColors();

    /**
     * Creates new form Gauge
     */
    public ChornobylGauge() {
        // Get the default tooltip display duration in miliseconds.
        final int defaultDismissDelay
                = ToolTipManager.sharedInstance().getDismissDelay();

        // Mouse listener to manipulate tooltip duration
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // set to a duration of 60 secodnds
                ToolTipManager.sharedInstance().setDismissDelay(
                        60000);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // on leaving the mouse, we will restore the default setting 
                // to have the standard behavior on other elements.
                ToolTipManager.sharedInstance().setDismissDelay(
                        defaultDismissDelay);
            }
        });

        initComponents();
        updateTicks();
        updateGeometry(1.0);
        updateTooltipText();
    }

    /**
     * Calculates the integer geometry in device pixels for the given scale
     * from the base values and the positions of the ticks and the spots that
     * belong to it. A line of one pixel at scale 1.0 gets a width of the
     * rounded scale, and the bar is sized from the rounded parts so that the
     * spots always keep a margin of one line width to the end of the bar.
     *
     * @param newScale ratio of the image size to the base size
     */
    private void updateGeometry(double newScale) {
        scale = newScale;
        lineWidth = Math.max(1, scaled(1));
        barX = scaled(BASE_X_POSITION_BAR);
        barY = scaled(BASE_Y_POSITION_BAR);
        barHeight = scaled(BASE_BAR_HEIGHT);
        spotWidth = scaled(BASE_SPOT_WIDTH);
        tickHeight = scaled(BASE_TICK_HEIGHT);
        scaleWidth = scaled(BASE_SCALE_WIDTH);
        barWidth = scaleWidth + 2 * spotWidth + 3 * lineWidth;
        xPosIndicatorMin = barX + lineWidth + spotWidth;
        xPosIndicatorMax = xPosIndicatorMin + scaleWidth;

        tickCoordinates = new int[tickValues.length];
        for (int idx = 0; idx < tickCoordinates.length; idx++) {
            tickCoordinates[idx] = getCoordinateValue(tickValues[idx]);
        }
        updateIndicatorPosition();
    }

    /**
     * Scales a pixel value of the base geometry to the current geometry.
     *
     * @param baseValue pixels at scale 1.0
     * @return rounded pixels at the current scale
     */
    private int scaled(int baseValue) {
        return (int) Math.round(baseValue * scale);
    }

    /**
     * Calculates the pixel position and the color of the spots for the
     * current value and marks gaugeComplete as deprecated if one of them
     * changed.
     *
     * @return true if the displayed spots changed
     */
    private boolean updateIndicatorPosition() {
        int newPosition;
        Color newColor;
        if (value > maximum) {
            newPosition = xPosIndicatorMax;
            newColor = elementColors.getGaugeIndicatorOutOfRange();
        } else if (value < minimum) {
            newPosition = xPosIndicatorMin;
            newColor = elementColors.getGaugeIndicatorOutOfRange();
        } else {
            newPosition = getCoordinateValue(value);
            newColor = elementColors.getGaugeIndicator();
        }
        if (newPosition == xPosIndicator && newColor == spotColor) {
            return false;
        }
        xPosIndicator = newPosition;
        spotColor = newColor;
        gaugeCompleteDeprecated = true;
        return true;
    }

    /**
     * Formats the tick values with a number of digits that fits to the range
     * of the scale.
     */
    private void updateTicks() {
        // Generate a proper number of digits
        int digits = -2 + (int) (Math.log10(maximum - minimum));
        if (digits < 0) {
            numberFormat.setMaximumFractionDigits(-digits);
        } else {
            numberFormat.setMaximumFractionDigits(0);
        }
        tickTexts = new String[tickValues.length];
        for (int idx = 0; idx < tickTexts.length; idx++) {
            tickTexts[idx] = numberFormat.format(tickValues[idx]);
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

    /**
     * Marks the static part as deprecated after a change of the scale or its
     * texts, so that it is rendered again with the next paint.
     */
    private void deprecateGaugeBack() {
        gaugeBackDeprecated = true;
        repaint();
    }

    public float getChornobylMaximum() {
        return maximum;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Sets the scale end value")
    public void setChornobylMaximum(float chornobylMaximum) {
        double old = this.maximum;
        this.maximum = chornobylMaximum;
        updateTicks();
        updateGeometry(scale);
        firePropertyChange("chornobylMaximum", old, chornobylMaximum);
        deprecateGaugeBack();
    }

    public float getChornobylMinimum() {
        return minimum;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Sets the scale start value")
    public void setChornobylMinimum(float chornobylMinimum) {
        float old = this.minimum;
        this.minimum = chornobylMinimum;
        updateTicks();
        updateGeometry(scale);
        firePropertyChange("chornobylMinimum", old, chornobylMinimum);
        deprecateGaugeBack();
    }

    public float getChornobylValue() {
        return value;
    }

    /**
     * Sets the displayed value, which is called in every cycle of the
     * simulation. A repaint is only requested if the pixel position or the
     * color of the spots changed, because small changes of a calculated value
     * mostly result in the same position.
     *
     * @param value value in the unit of the gauge
     */
    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Sets the current display value")
    public void setChornobylValue(float value) {
        float old = this.value;
        this.value = value;
        if (value != old) {
            firePropertyChange("chornobylValue", old, value);
            updateTooltipText();
            if (updateIndicatorPosition()) {
                repaint();
            }
        }
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
        updateGeometry(scale);
        firePropertyChange("chornobylTicks", old, chornobylTicks);
        deprecateGaugeBack();
    }

    public String getChornobylUnitText() {
        return unitText;
    }

    @BeanProperty(preferred = true, visualUpdate = true, description
            = "Unit String displayed on the left")
    public void setChornobylUnitText(String value) {
        String old = unitText;
        unitText = value;
        firePropertyChange("chornobylUnitText", old, value);
        updateTooltipText();
        deprecateGaugeBack();
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        deprecateGaugeBack();
    }

    /**
     * Renders the texts again after a change of the look and feel, because
     * their font is derived from the default label font.
     */
    @Override
    public void updateUI() {
        super.updateUI();
        deprecateGaugeBack();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(elementColors.getGaugeBackground());
        setBorder(javax.swing.BorderFactory.createLineBorder(elementColors.getGaugeBorder()));
        setMaximumSize(new java.awt.Dimension(125, 27));
        setMinimumSize(new java.awt.Dimension(125, 27));
        setName(""); // NOI18N
        setOpaque(true);
        setPreferredSize(new java.awt.Dimension(125, 27));
        setLayout(null);
    }// </editor-fold>//GEN-END:initComponents

    private void updateTooltipText() {
        String tooltip = String.format("%.1f %s", this.value, unitText);
        setToolTipText(tooltip);

        // If the tooltip is active and shown, force a repaint of its text
        // to allow a live view of the data.
        if (activeToolTip != null && activeToolTip.isShowing()) {
            activeToolTip.setTipText(tooltip);
            activeToolTip.repaint();
        }
    }

    /**
     * Draws the cached complete gauge, which is rendered in device pixels so
     * that it is drawn without interpolation also with a global HiDPI scaling.
     * The images are only rendered again if the size, a static property or
     * the spots changed.
     *
     * @param g graphics of the component
     */
    @Override
    public void paintComponent(Graphics g) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform deviceTransform = g2.getDeviceConfiguration()
                .getDefaultTransform();
        int imageWidth = Math.max(1, (int) Math.ceil(
                getWidth() * deviceTransform.getScaleX()));
        int imageHeight = Math.max(1, (int) Math.ceil(
                getHeight() * deviceTransform.getScaleY()));

        if (gaugeBackDeprecated || gaugeBack == null
                || gaugeBack.getWidth() != imageWidth
                || gaugeBack.getHeight() != imageHeight) {
            renderBackground(imageWidth, imageHeight);
        }
        if (gaugeCompleteDeprecated || renderedEnabled != isEnabled()) {
            renderIndicator();
        }
        g2.drawImage(gaugeComplete, 0, 0, getWidth(), getHeight(), null);
    }

    /**
     * Renders the static part of the gauge with the background, the bar, the
     * tick marks and all texts into a new gaugeBack image and calculates the
     * geometry for the given image size. The texts use the default label font
     * reduced by FONT_SIZE_REDUCTION and scaled to an integer size, and they
     * are drawn with the text rendering hints of the desktop, as the labels of
     * the panels are.
     *
     * @param imageWidth width in device pixels
     * @param imageHeight height in device pixels
     */
    private void renderBackground(int imageWidth, int imageHeight) {
        updateGeometry(Math.min((double) imageWidth / BASE_WIDTH,
                (double) imageHeight / BASE_HEIGHT));
        gaugeBack = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_INT_RGB);
        gaugeComplete = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_INT_RGB);
        gaugeBackDeprecated = false;
        gaugeCompleteDeprecated = true;

        Graphics2D g2b = gaugeBack.createGraphics();
        g2b.setColor(getBackground());
        g2b.fillRect(0, 0, imageWidth, imageHeight);

        // Bar in the back and the tick marks above it
        g2b.setColor(elementColors.getGaugeBar());
        g2b.fillRect(barX, barY, barWidth, barHeight);
        g2b.setColor(elementColors.getGaugeScale());
        for (int idx = 0; idx < tickCoordinates.length; idx++) {
            g2b.fillRect(tickCoordinates[idx], barY - tickHeight,
                    lineWidth, tickHeight);
        }

        // Texts
        Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit()
                .getDesktopProperty("awt.font.desktophints");
        if (desktopHints != null) {
            g2b.addRenderingHints(desktopHints);
        }
        Font labelFont = UIManager.getFont("Label.font");
        int fontSize = (int) Math.round(
                (labelFont.getSize() - FONT_SIZE_REDUCTION) * scale);
        g2b.setFont(labelFont.deriveFont(labelFont.getStyle() & ~Font.BOLD,
                (float) Math.max(1, fontSize)));
        FontMetrics fm = g2b.getFontMetrics();
        int tickLabelWidth = scaled(BASE_TICK_LABEL_WIDTH);
        int tickLabelHeight = scaled(BASE_TICK_LABEL_HEIGHT);
        for (int idx = 0; idx < tickTexts.length; idx++) {
            // The text is centered in a box above the tick mark
            int boxX = tickCoordinates[idx] - tickLabelWidth / 2;
            // Both halves are rounded separately like a JLabel does it.
            g2b.drawString(tickTexts[idx],
                    boxX + tickLabelWidth / 2
                    - fm.stringWidth(tickTexts[idx]) / 2,
                    getCenteredBaseline(fm, 0, tickLabelHeight));
        }
        if (unitText != null) {
            g2b.drawString(unitText, scaled(BASE_UNIT_X),
                    getCenteredBaseline(fm, scaled(BASE_UNIT_Y),
                            scaled(BASE_UNIT_HEIGHT)));
        }
        g2b.dispose();
    }

    /**
     * Calculates the baseline of a text that is vertically centered in a box,
     * in the same way as a JLabel places its text, which rounds both halves
     * separately.
     *
     * @param fm metrics of the used font
     * @param boxY upper edge of the box
     * @param boxHeight height of the box
     * @return y coordinate of the baseline
     */
    private static int getCenteredBaseline(FontMetrics fm, int boxY,
            int boxHeight) {
        return boxY + boxHeight / 2 - fm.getHeight() / 2 + fm.getAscent();
    }

    /**
     * Renders the complete gauge into gaugeComplete by copying the background
     * and drawing the two spots of the current value on top of it, while a
     * disabled gauge shows no spots.
     */
    private void renderIndicator() {
        Graphics2D g2p = gaugeComplete.createGraphics();
        g2p.drawImage(gaugeBack, 0, 0, null);
        renderedEnabled = isEnabled();
        if (renderedEnabled) {
            g2p.setColor(spotColor);
            g2p.fillRect(xPosIndicator - spotWidth, barY + lineWidth,
                    spotWidth, barHeight - 2 * lineWidth);
            g2p.fillRect(xPosIndicator + lineWidth, barY + lineWidth,
                    spotWidth, barHeight - 2 * lineWidth);
        }
        g2p.dispose();
        gaugeCompleteDeprecated = false;
    }

    @Override
    public javax.swing.JToolTip createToolTip() {
        // get the reference on the tooltip as it is created.
        activeToolTip = super.createToolTip();
        return activeToolTip;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
