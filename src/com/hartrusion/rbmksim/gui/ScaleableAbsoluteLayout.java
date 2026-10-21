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
package com.hartrusion.rbmksim.gui;

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
 * The absolute constraints describe the content at a scale factor of 1.0, the
 * size the container had at the time the layout was installed is used as the
 * base size. Whenever the container gets resized, the scale factor is
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
 * enlarged. Components that draw their content relative to their size, like
 * the valves, pumps and backgrounds of the mnemonic displays, stay sharp.
 *
 * <p>
 * Forms generated with the NetBeans form editor use the plain AbsoluteLayout,
 * use {@link #install(Container)} after initComponents() to replace it.
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

    /**
     * Replaces the AbsoluteLayout of the given container with a new instance
     * of this layout. The current bounds of all child components, resulting
     * from their absolute constraints, become the new constraints and the
     * current preferred size of the container becomes the base size.
     *
     * @param container A container that uses an AbsoluteLayout.
     * @return The installed layout.
     */
    public static ScaleableAbsoluteLayout install(Container container) {
        if (container.getLayout() instanceof ScaleableAbsoluteLayout) {
            return (ScaleableAbsoluteLayout) container.getLayout();
        }
        if (!(container.getLayout() instanceof AbsoluteLayout)) {
            throw new IllegalArgumentException(
                    "The container does not use an AbsoluteLayout.");
        }
        // The AbsoluteLayout places all components independently of the
        // container size, this resolves the unspecified (-1) constraint sizes
        // to the preferred sizes of the components.
        container.doLayout();

        ScaleableAbsoluteLayout layout = new ScaleableAbsoluteLayout();
        for (Component component : container.getComponents()) {
            layout.addLayoutComponent(component, new AbsoluteConstraints(
                    component.getX(), component.getY(),
                    component.getWidth(), component.getHeight()));
        }
        Dimension preferredSize = container.getPreferredSize();
        Insets insets = container.getInsets();
        layout.baseSize.width = preferredSize.width - insets.left
                - insets.right;
        layout.baseSize.height = preferredSize.height - insets.top
                - insets.bottom;
        container.setLayout(layout);
        return layout;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public void removeLayoutComponent(Component component) {
        super.removeLayoutComponent(component);
        baseFonts.remove(component);
        baseIcons.remove(component);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return getScaledLayoutSize(parent, scaleFactor);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return getScaledLayoutSize(parent, MIN_SCALE_FACTOR);
    }

    @Override
    public void layoutContainer(Container parent) {
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
