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

import com.hartrusion.mvc.UpdateReceiver;
import com.hartrusion.rbmksim.ChannelData;
import com.hartrusion.rbmksim.ChannelType;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Debug-only display in the same style as {@link PanelCoreActivity}, but instead
 * of visualizing a {@link com.hartrusion.rbmksim.CoreIndicator}, it parses
 * incoming parameters such as {@code "Fuel3237#Temperature"} and writes the
 * formatted double value onto the label that corresponds to the reactor
 * coordinate (in this example the tile formerly labeled {@code "32-37"}).
 * <p>
 * The panel is configured with a single parameter suffix (e.g.
 * {@code "Temperature"} or {@code "Affection"}) and a {@link String#format}
 * pattern that determines how the double value is rendered. Only parameters
 * whose name starts with {@code "Fuel"} and whose suffix matches the configured
 * suffix are displayed; everything else is ignored.
 * <p>
 * Coordinate system: idx (Y) runs from bottom (20) to top (42), jdx (X) runs
 * from left (20) to right (42). On screen, idx=42 is at the top (gridy=0) and
 * idx=20 is at the bottom (gridy=22).
 *
 * @author Viktor Alexander Hartung
 */
@JavaBean(description = "Debug grid showing a single fuel parameter per core tile.")
public class PanelCoreDebugValues extends JPanel implements UpdateReceiver {

    private static final Color FUEL_BG = new Color(255, 255, 255);

    /**
     * Prefix every fuel parameter name starts with.
     */
    private static final String FUEL_PREFIX = "Fuel";

    /**
     * Stores references to the JLabel for each grid position. Index [row][col]
     * where row = MAX_NUMBER - idx and col = jdx - MIN_NUMBER. Entries for VOID
     * positions are null.
     */
    private final JLabel[][] labels
            = new JLabel[ChannelData.LENGTH][ChannelData.LENGTH];

    /**
     * The parameter suffix this panel listens for, e.g. {@code "Temperature"}.
     */
    private String suffix = "Temperature";

    /**
     * The full suffix token a property name must end with, e.g.
     * {@code "#Temperature"}. Kept in sync with {@link #suffix}.
     */
    private String suffixToken = "#" + suffix;

    /**
     * The {@link String#format} pattern used to render the double value.
     */
    private String valueFormat = "%.0f";

    /**
     * Creates the debug display panel with the default suffix
     * ({@code "Temperature"}) and default value format ({@code "%.0f"}). This
     * no-argument constructor allows the panel to be used in the NetBeans GUI
     * builder; the suffix and format can be configured afterwards through the
     * corresponding properties.
     */
    public PanelCoreDebugValues() {
        initComponents();
    }

    /**
     * Creates the debug display panel configured for a single parameter suffix.
     *
     * @param suffix the parameter suffix to display, e.g. {@code "Temperature"}
     * or {@code "Affection"} (without the leading {@code '#'})
     * @param valueFormat a {@link String#format} pattern applied to the double
     * value, e.g. {@code "%.0f"} or {@code "%.2f"}
     */
    public PanelCoreDebugValues(String suffix, String valueFormat) {
        this.suffix = suffix;
        this.suffixToken = "#" + suffix;
        this.valueFormat = valueFormat;
        initComponents();
    }

    /**
     * Iterates over all possible channel coordinates and creates JLabel
     * elements for non-VOID channels. VOID positions receive an invisible dummy
     * label so that the GridBagLayout maintains uniform cell sizing across the
     * entire 23x23 grid.
     */
    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new java.awt.Insets(1, 1, 0, 0);

        for (int idx = ChannelData.MAX_NUMBER;
                idx >= ChannelData.MIN_NUMBER; idx--) {
            for (int jdx = ChannelData.MIN_NUMBER;
                    jdx <= ChannelData.MAX_NUMBER; jdx++) {

                // idx runs bottom-to-top, gridy runs top-to-bottom:
                int row = ChannelData.MAX_NUMBER - idx;
                int col = jdx - ChannelData.MIN_NUMBER;

                gbc.gridx = col;
                gbc.gridy = row;

                ChannelType type = ChannelData.getChannelType(idx, jdx);

                if (type == ChannelType.VOID
                        || type == ChannelType.AUTOMATIC_CONTROLROD
                        || type == ChannelType.MANUAL_CONTROLROD
                        || type == ChannelType.SHORT_CONTROLROD) {
                    // Invisible dummy to keep uniform grid spacing:
                    JLabel dummy = new JLabel();
                    dummy.setVisible(false);
                    add(dummy, gbc);
                } else {
                    JLabel label = new JLabel();
                    label.setOpaque(true);
                    label.setHorizontalAlignment(JLabel.CENTER);
                    label.setBackground(FUEL_BG);
                    label.setForeground(new Color(0, 0, 0));
                    label.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
                    label.setText("");
                    label.setFont(label.getFont().deriveFont(
                            label.getFont().getSize() - 3F));
                    // Fixed size of 22
                    label.setMaximumSize(new java.awt.Dimension(22, 22));
                    label.setMinimumSize(new java.awt.Dimension(22, 22));
                    label.setPreferredSize(new java.awt.Dimension(22, 22));
                    labels[row][col] = label;
                    add(label, gbc);
                }
            }
        }
    }

    /**
     * Returns the JLabel at the given reactor coordinate, or null if the
     * position is VOID or outside the valid range.
     *
     * @param idx Y coordinate, from bottom to top (20 to 42)
     * @param jdx X coordinate, from left to right (20 to 42)
     * @return the JLabel reference, or null
     */
    public JLabel getLabel(int idx, int jdx) {
        if (idx < ChannelData.MIN_NUMBER || idx > ChannelData.MAX_NUMBER
                || jdx < ChannelData.MIN_NUMBER
                || jdx > ChannelData.MAX_NUMBER) {
            return null;
        }
        int row = ChannelData.MAX_NUMBER - idx;
        int col = jdx - ChannelData.MIN_NUMBER;
        return labels[row][col];
    }

    /**
     * Parses a fuel parameter name of the form {@code "Fuel<coord>#<suffix>"}
     * and writes the formatted value to the matching label. Property names that
     * do not start with {@code "Fuel"} or that do not carry the configured
     * suffix are ignored.
     *
     * @param propertyName the full parameter name, e.g. {@code "Fuel3237#Temperature"}
     * @param newValue the value to display
     */
    private void handleFuelValue(String propertyName, double newValue) {
        if (propertyName == null || !propertyName.startsWith(FUEL_PREFIX)) {
            return;
        }
        if (!propertyName.endsWith(suffixToken)) {
            return;
        }

        // The coordinate number is everything between "Fuel" and "#suffix".
        String coord = propertyName.substring(
                FUEL_PREFIX.length(),
                propertyName.length() - suffixToken.length());

        int encoded;
        try {
            encoded = Integer.parseInt(coord);
        } catch (NumberFormatException ex) {
            return;
        }

        // Coordinate is encoded as (100 * idx + jdx), see FuelElement.
        int idx = encoded / 100;
        int jdx = encoded % 100;

        JLabel label = getLabel(idx, jdx);
        if (label != null) {
            label.setText(String.format(valueFormat, newValue));
        }
    }

    /**
     * Returns the parameter suffix this panel is configured to display.
     *
     * @return the parameter suffix, e.g. {@code "Temperature"}
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the parameter suffix this panel listens for. Only parameters ending
     * in {@code "#" + suffix} are displayed.
     *
     * @param suffix the parameter suffix, e.g. {@code "Temperature"} or
     * {@code "Affection"} (without the leading {@code '#'})
     */
    @BeanProperty(preferred = true, description
            = "The fuel parameter suffix to display, e.g. Temperature or Affection.")
    public void setSuffix(String suffix) {
        String old = this.suffix;
        this.suffix = suffix;
        this.suffixToken = "#" + suffix;
        firePropertyChange("suffix", old, suffix);
    }

    /**
     * Returns the {@link String#format} pattern used to render the value.
     *
     * @return the format pattern, e.g. {@code "%.0f"}
     */
    public String getValueFormat() {
        return valueFormat;
    }

    /**
     * Sets the {@link String#format} pattern used to render the double value.
     *
     * @param valueFormat a format pattern, e.g. {@code "%.0f"} or {@code "%.2f"}
     */
    @BeanProperty(preferred = true, description
            = "The String.format pattern applied to the displayed double value.")
    public void setValueFormat(String valueFormat) {
        String old = this.valueFormat;
        this.valueFormat = valueFormat;
        firePropertyChange("valueFormat", old, valueFormat);
    }

    @Override
    public void updateComponent(PropertyChangeEvent evt) {
        if (evt != null && evt.getNewValue() instanceof Number) {
            handleFuelValue(evt.getPropertyName(),
                    ((Number) evt.getNewValue()).doubleValue());
        }
    }

    @Override
    public void updateComponent(String propertyName, Object newValue) {
        if (newValue instanceof Number) {
            handleFuelValue(propertyName, ((Number) newValue).doubleValue());
        }
    }

    @Override
    public void updateComponent(String propertyName, double newValue) {
        handleFuelValue(propertyName, newValue);
    }

    @Override
    public void updateComponent(String propertyName, boolean newValue) {
        // Booleans are not displayed by this debug value panel.
    }
}
