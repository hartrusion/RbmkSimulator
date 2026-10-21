/*
 * Copyright (C) 2026 Viktor Alexander Hartung
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * Static background drawing of a mnemonic display with the lines and symbols
 * that connect the valves and pumps. The drawing is defined by subclasses in
 * pixel coordinates of the base size and gets scaled to the actual size of
 * the component, so it stays sharp when the mnemonic display is zoomed.
 *
 * <p>
 * The subclasses are generated from the SVG files in docs/mnemonics by the
 * MnemonicSvgConverter located there. Each subclass has a constructor
 * without arguments, so it can be placed with the GUI builder like any other
 * component and shows the drawing in the form editor.
 *
 * <p>
 * The drawing is rendered into an image that is only renewed when the size
 * of the component or the background color changes.
 *
 * @author Viktor Alexander Hartung
 */
public abstract class MnemonicBackground extends javax.swing.JComponent {

    /**
     * Width of the drawing in pixels at a scale factor of 1.0, which is the
     * coordinate range of the drawBackground method.
     */
    private final int baseWidth;

    /**
     * Height of the drawing in pixels at a scale factor of 1.0.
     */
    private final int baseHeight;

    /**
     * Rendered drawing in device pixels, null if not yet rendered.
     */
    private BufferedImage cachedImage;

    /**
     * Background color that was used to render the cached image.
     */
    private Color cachedBackground;

    /**
     * @param baseWidth Width of the drawing in pixels at scale 1.0.
     * @param baseHeight Height of the drawing in pixels at scale 1.0.
     */
    protected MnemonicBackground(int baseWidth, int baseHeight) {
        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
        setBackground(MnemonicColors.getBackground());
        setOpaque(true);
        setPreferredSize(new java.awt.Dimension(baseWidth, baseHeight));
    }

    /**
     * Draws the background graphics using pixel coordinates of the base
     * size. Color and stroke are set by the implementation, the area is
     * already filled with the background color.
     *
     * @param g2d Graphics that is scaled from the base size to the image size.
     */
    protected abstract void drawBackground(Graphics2D g2d);

    @Override
    protected void paintComponent(Graphics g) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g;

        // The image is rendered in device pixels to stay sharp if a global
        // UI scaling (HiDPI) is active.
        AffineTransform deviceTransform = g2d.getDeviceConfiguration()
                .getDefaultTransform();
        int imageWidth = Math.max(1, (int) Math.ceil(
                getWidth() * deviceTransform.getScaleX()));
        int imageHeight = Math.max(1, (int) Math.ceil(
                getHeight() * deviceTransform.getScaleY()));

        if (cachedImage == null || cachedImage.getWidth() != imageWidth
                || cachedImage.getHeight() != imageHeight
                || !getBackground().equals(cachedBackground)) {
            cachedImage = renderImage(imageWidth, imageHeight);
            cachedBackground = getBackground();
        }
        g2d.drawImage(cachedImage, 0, 0, getWidth(), getHeight(), null);
    }

    /**
     * Renders the drawing scaled to the given image size.
     *
     * @param imageWidth Width in device pixels.
     * @param imageHeight Height in device pixels.
     * @return Rendered image.
     */
    private BufferedImage renderImage(int imageWidth, int imageHeight) {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, imageWidth, imageHeight);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        // Pure stroke control places the lines exactly as defined, like the
        // SVG renderer of Inkscape does. The default normalization would
        // shift lines by fractions of a pixel.
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        g2d.scale((double) imageWidth / baseWidth,
                (double) imageHeight / baseHeight);
        drawBackground(g2d);
        g2d.dispose();
        return image;
    }

    /**
     * Draws connected straight lines with the current color and stroke.
     *
     * @param g2d Graphics to draw on.
     * @param coordinates Pairs of x and y coordinates of the points.
     */
    protected static void drawPolyline(Graphics2D g2d,
            double... coordinates) {
        g2d.draw(createPolyline(coordinates, false));
    }

    /**
     * Draws the outline of a closed polygon with the current color and
     * stroke.
     *
     * @param g2d Graphics to draw on.
     * @param coordinates Pairs of x and y coordinates of the corners.
     */
    protected static void drawPolygon(Graphics2D g2d, double... coordinates) {
        g2d.draw(createPolyline(coordinates, true));
    }

    /**
     * Fills a polygon with the current color.
     *
     * @param g2d Graphics to draw on.
     * @param coordinates Pairs of x and y coordinates of the corners.
     */
    protected static void fillPolygon(Graphics2D g2d, double... coordinates) {
        g2d.fill(createPolyline(coordinates, true));
    }

    private static Path2D.Double createPolyline(double[] coordinates,
            boolean closed) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(coordinates[0], coordinates[1]);
        for (int i = 2; i < coordinates.length - 1; i += 2) {
            path.lineTo(coordinates[i], coordinates[i + 1]);
        }
        if (closed) {
            path.closePath();
        }
        return path;
    }
}
