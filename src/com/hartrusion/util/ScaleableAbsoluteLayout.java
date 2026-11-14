/*
 * The MIT License
 *
 * Copyright 2026 Viktor Alexander Hartung.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.hartrusion.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

/**
 * An AbsoluteLayout that zooms its content to fit the size of the container.
 * The absolute constraints describe the content at a scale factor of 1.0, and
 * the base size is the preferred size set on the container or, if none is
 * set, the size the constraints require. Whenever the container gets resized, the scale factor is
 * calculated from the ratio of the available size to the base size, keeping
 * the aspect ratio. The content is centered if the container is larger in one
 * direction than required by the scale factor.
 *
 * <p>
 * Besides the positions and sizes, the fonts of all direct child components
 * and the image icons of child JLabels are scaled as well. Fonts and icons are
 * captured as base values the first time a component is laid out, changing
 * them later from application code is not supported as the next change of the
 * scale factor will restore the captured base values. Nested containers are
 * resized but their content is not zoomed by this layout.
 *
 * <p>
 * Image icons are scaled with interpolation which makes them blurry when
 * enlarged. Components that draw their content relative to their size stay
 * sharp.
 *
 * <p>
 * Forms generated with the NetBeans form editor use the plain AbsoluteLayout,
 * because the editor only supports that class. {@link JPanelScaleable} is
 * used as superclass of such a form (or as panel from the palette) and
 * replaces the AbsoluteLayout by this layout at runtime, so that neither the
 * form nor the generated code needs any change. The layout was moved here from
 * the RbmkSimulator project and adapted to determine its base size itself;
 * the adaption is LLM code generated with Claude Opus 5.5.
 *
 * @author Viktor Alexander Hartung
 */
public class ScaleableAbsoluteLayout extends AbsoluteLayout {

    /**
     * Lowest allowed scale factor to prevent fonts and images from collapsing
     * to zero size on very small containers.
     */
    private static final double MIN_SCALE_FACTOR = 0.25;

    /**
     * Size of the content area (container size without insets) at a scale
     * factor of 1.0. The scale factor is the ratio of the available content
     * area to this size.
     */
    private final Dimension baseSize = new Dimension();

    /**
     * True if baseSize belongs to the current set of child components. It is
     * reset whenever a component is added or removed, so that the base size
     * is determined again at the next layout or size query.
     */
    private boolean baseSizeValid;

    /**
     * Current scale factor, 1.0 means the components are placed exactly as
     * described by their absolute constraints.
     */
    private double scaleFactor = 1.0;

    /**
     * Font of each child component at a scale factor of 1.0. A component that
     * is contained in this map has been prepared for scaling. Components
     * without a font are mapped to null.
     */
    private final Map<Component, Font> baseFonts = new HashMap<>();

    /**
     * Unscaled image icon of each child JLabel that had an image icon at the
     * time it was prepared for scaling.
     */
    private final Map<JLabel, ImageIcon> baseIcons = new HashMap<>();

    public double getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public void addLayoutComponent(Component component, Object constraints) {
        super.addLayoutComponent(component, constraints);
        baseSizeValid = false;
    }

    @Override
    public void removeLayoutComponent(Component component) {
        super.removeLayoutComponent(component);
        baseFonts.remove(component);
        baseIcons.remove(component);
        baseSizeValid = false;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        updateBaseSize(parent);
        return getScaledLayoutSize(parent, scaleFactor);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        updateBaseSize(parent);
        return getScaledLayoutSize(parent, MIN_SCALE_FACTOR);
    }

    @Override
    public void layoutContainer(Container parent) {
        updateBaseSize(parent);
        Insets insets = parent.getInsets();
        int availableWidth = parent.getWidth() - insets.left - insets.right;
        int availableHeight = parent.getHeight() - insets.top
                - insets.bottom;
        boolean scaleFactorChanged = false;

        // A container that is not yet realized has no size, the current
        // scale factor is kept in that case.
        if (availableWidth > 0 && availableHeight > 0
                && baseSize.width > 0 && baseSize.height > 0) {
            double fitFactor = Math.min(
                    (double) availableWidth / baseSize.width,
                    (double) availableHeight / baseSize.height);
            fitFactor = Math.max(fitFactor, MIN_SCALE_FACTOR);
            if (fitFactor != scaleFactor) {
                scaleFactor = fitFactor;
                scaleFactorChanged = true;
            }
        }

        // Offset that centers the scaled content in the available area.
        int offsetX = insets.left + Math.max(0, (availableWidth
                - (int) Math.round(baseSize.width * scaleFactor)) / 2);
        int offsetY = insets.top + Math.max(0, (availableHeight
                - (int) Math.round(baseSize.height * scaleFactor)) / 2);

        for (Component component : parent.getComponents()) {
            AbsoluteConstraints constraint
                    = (AbsoluteConstraints) constraints.get(component);
            if (constraint == null) {
                continue;
            }
            if (!baseFonts.containsKey(component)) {
                captureBaseValues(component);
                applyScaleFactor(component);
            } else if (scaleFactorChanged) {
                applyScaleFactor(component);
            }

            // Left and right edges are rounded separately so that components
            // which touch each other at scale 1.0 keep touching when scaled.
            int x = (int) Math.round(constraint.x * scaleFactor);
            int y = (int) Math.round(constraint.y * scaleFactor);
            int width;
            int height;
            Dimension preferredSize = component.getPreferredSize();
            if (constraint.width == -1) {
                width = preferredSize.width;
            } else {
                width = (int) Math.round((constraint.x + constraint.width)
                        * scaleFactor) - x;
            }
            if (constraint.height == -1) {
                height = preferredSize.height;
            } else {
                height = (int) Math.round((constraint.y + constraint.height)
                        * scaleFactor) - y;
            }
            component.setBounds(offsetX + x, offsetY + y, width, height);
        }
    }

    /**
     * Determines the base size, which is the content area at a scale factor
     * of 1.0, if it is not known for the current set of components. A
     * preferred size that was set on the container (for example in the
     * NetBeans GUI editor) defines it; otherwise it is the size that the
     * AbsoluteLayout itself would need for the constraints. The base size is
     * determined again after components were added or removed, which happens
     * normally only once in initComponents() before the first layout. The
     * constraints with width or height -1 then use the preferred sizes of
     * the components at their current, possibly already scaled, fonts.
     *
     * @param parent The container using this layout.
     */
    private void updateBaseSize(Container parent) {
        if (baseSizeValid) {
            return;
        }
        Dimension size;
        if (parent.isPreferredSizeSet()) {
            size = parent.getPreferredSize();
        } else {
            size = super.preferredLayoutSize(parent);
        }
        Insets insets = parent.getInsets();
        baseSize.width = size.width - insets.left - insets.right;
        baseSize.height = size.height - insets.top - insets.bottom;
        baseSizeValid = true;
    }

    /**
     * Calculates the size of the container that is required to show the
     * content with the given scale factor.
     *
     * @param parent The container using this layout.
     * @param factor Scale factor
     * @return Size including the insets of the container.
     */
    private Dimension getScaledLayoutSize(Container parent, double factor) {
        Insets insets = parent.getInsets();
        return new Dimension(
                (int) Math.round(baseSize.width * factor)
                + insets.left + insets.right,
                (int) Math.round(baseSize.height * factor)
                + insets.top + insets.bottom);
    }

    /**
     * Stores the current font and image icon of the component as values that
     * belong to a scale factor of 1.0.
     *
     * @param component Child component of the container.
     */
    private void captureBaseValues(Component component) {
        baseFonts.put(component, component.getFont());
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            Icon icon = label.getIcon();
            if (icon instanceof ImageIcon) {
                baseIcons.put(label, (ImageIcon) icon);
            }
        }
    }

    /**
     * Sets font and image icon of the component to the scaled versions of
     * their captured base values, using the current scale factor.
     *
     * @param component Child component that has its base values captured.
     */
    private void applyScaleFactor(Component component) {
        Font baseFont = baseFonts.get(component);
        if (baseFont != null) {
            component.setFont(baseFont.deriveFont(
                    (float) (baseFont.getSize2D() * scaleFactor)));
        }
        ImageIcon baseIcon = baseIcons.get(component);
        if (baseIcon != null) {
            int width = Math.max(1, (int) Math.round(
                    baseIcon.getIconWidth() * scaleFactor));
            int height = Math.max(1, (int) Math.round(
                    baseIcon.getIconHeight() * scaleFactor));
            ((JLabel) component).setIcon(new ImageIcon(createScaledImage(
                    baseIcon.getImage(), width, height)));
        }
    }

    /**
     * Creates a scaled copy of the image. Bicubic interpolation only takes
     * the neighboring source pixels into account, if the source is more than
     * twice as large as the target, pixels would be skipped and thin lines
     * could vanish. Therefore the image is halved in size repeatedly until it
     * is less than twice the target size before the final scaling is done.
     *
     * @param source Fully loaded image.
     * @param width Target width in pixels.
     * @param height Target height in pixels.
     * @return Image with the target size.
     */
    private static Image createScaledImage(Image source, int width,
            int height) {
        Image current = source;
        int currentWidth = source.getWidth(null);
        int currentHeight = source.getHeight(null);
        while (currentWidth / 2 >= width && currentHeight / 2 >= height) {
            currentWidth = currentWidth / 2;
            currentHeight = currentHeight / 2;
            current = drawScaledImage(current, currentWidth, currentHeight);
        }
        return drawScaledImage(current, width, height);
    }

    private static BufferedImage drawScaledImage(Image source, int width,
            int height) {
        BufferedImage target = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = target.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(source, 0, 0, width, height, null);
        g2d.dispose();
        return target;
    }
}
