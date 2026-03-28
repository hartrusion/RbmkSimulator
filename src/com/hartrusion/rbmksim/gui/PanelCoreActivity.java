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

import com.hartrusion.rbmksim.ChannelData;
import com.hartrusion.rbmksim.ChannelType;
import com.hartrusion.rbmksim.CoreIndicator;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Displays the reactor core as a grid of colored tiles. Each channel in the
 * reactor is represented by a square JLabel whose color depends on the channel
 * type and highlight state. The grid is built programmatically from the reactor
 * layout defined in {@link ChannelData}.
 * <p>
 * Coordinate system: idx (Y) runs from bottom (20) to top (42), jdx (X) runs
 * from left (20) to right (42). On screen, idx=42 is at the top (gridy=0) and
 * idx=20 is at the bottom (gridy=22).
 *
 * @author Viktor Alexander Hartung
 */
public class PanelCoreActivity extends JPanel {

    private static final Color FUEL_OFF = new Color(128, 128, 128);
    private static final Color FUEL_ON = new Color(255, 255, 255);
    private static final Color MANROD_OFF = new Color(128, 128, 0);
    private static final Color MANROD_ON = new Color(255, 255, 0);
    private static final Color AUTOROD_OFF = new Color(0, 0, 128);
    private static final Color AUTOROD_ON = new Color(0, 0, 255);

    /**
     * Stores references to the JLabel for each grid position. Index [row][col]
     * where row = MAX_NUMBER - idx and col = jdx - MIN_NUMBER. Entries for VOID
     * positions are null.
     */
    private final JLabel[][] labels
            = new JLabel[ChannelData.LENGTH][ChannelData.LENGTH];

    /**
     * Stores the current highlight state. Index [kdx][ldx] where kdx = idx -
     * MIN_NUMBER and ldx = jdx - MIN_NUMBER.
     */
    private final boolean[][] highlight
            = new boolean[ChannelData.LENGTH][ChannelData.LENGTH];

    /**
     * Creates the display panel and populates it with JLabel tiles based on the
     * reactor layout from {@link ChannelData}.
     */
    public PanelCoreActivity() {
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

                if (type == ChannelType.VOID) {
                    // Invisible dummy to keep uniform grid spacing:
                    JLabel dummy = new JLabel();
                    dummy.setVisible(false);
                    add(dummy, gbc);
                } else {
                    JLabel label = new JLabel();
                    label.setOpaque(true);
                    label.setHorizontalAlignment(JLabel.CENTER);
                    label.setBackground(getColorForType(type, false));
                    label.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
                    label.setText(idx + "-" + jdx);
                    label.setFont(label.getFont().deriveFont(
                            label.getFont().getSize() - 5F));
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
     * Returns a color depending on the type of reactor element and whether it
     * is currently highlighted.
     *
     * @param type the channel type
     * @param active true if highlighted
     * @return the corresponding Color
     */
    private static Color getColorForType(ChannelType type, boolean active) {
        switch (type) {
            case FUEL:
                return active ? FUEL_ON : FUEL_OFF;
            case MANUAL_CONTROLROD:
                return active ? MANROD_ON : MANROD_OFF;
            case AUTOMATIC_CONTROLROD:
            case SHORT_CONTROLROD:
                return active ? AUTOROD_ON : AUTOROD_OFF;
            default:
                return null;
        }
    }

    /**
     * Updates the panel from the given {@link CoreIndicator}. Only repaints
     * labels whose highlight state has actually changed.
     *
     * @param source the CoreIndicator providing highlight data
     */
    public void updateDisplay(CoreIndicator source) {
        for (int idx = ChannelData.MIN_NUMBER;
                idx <= ChannelData.MAX_NUMBER; idx++) {
            for (int jdx = ChannelData.MIN_NUMBER;
                    jdx <= ChannelData.MAX_NUMBER; jdx++) {

                ChannelType type = ChannelData.getChannelType(idx, jdx);
                if (type == ChannelType.VOID) {
                    continue;
                }

                int kdx = idx - ChannelData.MIN_NUMBER;
                int ldx = jdx - ChannelData.MIN_NUMBER;
                boolean toHighlight = source.isHighlited(idx, jdx);

                if (toHighlight != highlight[kdx][ldx]) {
                    JLabel label = getLabel(idx, jdx);
                    if (label != null) {
                        label.setBackground(
                                getColorForType(type, toHighlight));
                    }
                    highlight[kdx][ldx] = toHighlight;
                }
            }
        }
    }
}
