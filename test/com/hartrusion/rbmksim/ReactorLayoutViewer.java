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
package com.hartrusion.rbmksim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Standalone viewer that draws the reactor layout returned by
 * {@link ChannelData#getChannelType(int, int)} as a grid of rectangles, so the
 * hand drawn layout (reactorLayout.png) can be compared against what the code
 * actually returns.
 * <p>
 * The colors are chosen to match the hand drawing:
 * <ul>
 * <li>gray: {@link ChannelType#FUEL}</li>
 * <li>green: {@link ChannelType#MANUAL_CONTROLROD}</li>
 * <li>red: {@link ChannelType#AUTOMATIC_CONTROLROD}</li>
 * <li>yellow: {@link ChannelType#SHORT_CONTROLROD}</li>
 * <li>nothing drawn: {@link ChannelType#VOID}</li>
 * </ul>
 * Coordinate system: idx (Y) runs from bottom (MIN_NUMBER) to top (MAX_NUMBER),
 * jdx (X) runs from left (MIN_NUMBER) to right (MAX_NUMBER). On screen,
 * idx = MAX_NUMBER is the topmost row, as in
 * {@link com.hartrusion.rbmksim.gui.PanelCoreActivity}.
 * <p>
 * This test class was completely written by Claude Code with Opus 5
 *
 * @author Viktor Alexander Hartung
 */
public final class ReactorLayoutViewer extends JPanel {

    /**
     * Edge length of one channel rectangle in pixels.
     */
    private static final int CELL_SIZE = 22;

    /**
     * Empty border around the grid in pixels.
     */
    private static final int MARGIN = 10;

    private static final Color FUEL_COLOR = new Color(128, 128, 128);
    private static final Color MANROD_COLOR = new Color(0, 128, 0);
    private static final Color AUTOROD_COLOR = new Color(255, 0, 0);
    private static final Color SHORTROD_COLOR = new Color(255, 255, 0);
    private static final Color GRID_COLOR = new Color(0, 0, 0);

    /**
     * If true, every channel gets its coordinate painted into the rectangle.
     * The hand drawing has no labels, so this is off by default.
     */
    private boolean showCoordinates = false;

    /**
     * Creates the drawing panel sized to hold the whole 23x23 grid.
     */
    public ReactorLayoutViewer() {
        int size = 2 * MARGIN + ChannelData.LENGTH * CELL_SIZE;
        setPreferredSize(new Dimension(size, size));
        setBackground(new Color(255, 255, 255));
    }

    /**
     * Switches the coordinate text inside the rectangles on or off.
     *
     * @param showCoordinates true to paint the "idx-jdx" text
     */
    public void setShowCoordinates(boolean showCoordinates) {
        this.showCoordinates = showCoordinates;
        repaint();
    }

    /**
     * Returns the color the given channel type is drawn with.
     *
     * @param type the channel type
     *
     * @return the corresponding color, or null for {@link ChannelType#VOID}
     */
    private static Color getColorForType(ChannelType type) {
        switch (type) {
            case FUEL:
                return FUEL_COLOR;
            case MANUAL_CONTROLROD:
                return MANROD_COLOR;
            case AUTOMATIC_CONTROLROD:
                return AUTOROD_COLOR;
            case SHORT_CONTROLROD:
                return SHORTROD_COLOR;
            default:
                return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (showCoordinates) {
            g2.setFont(g2.getFont().deriveFont(8F));
        }

        for (int idx = ChannelData.MAX_NUMBER;
                idx >= ChannelData.MIN_NUMBER; idx--) {
            for (int jdx = ChannelData.MIN_NUMBER;
                    jdx <= ChannelData.MAX_NUMBER; jdx++) {

                ChannelType type = ChannelData.getChannelType(idx, jdx);
                Color color = getColorForType(type);
                if (color == null) {
                    // VOID, nothing is drawn here:
                    continue;
                }

                // idx runs bottom-to-top, the screen rows run top-to-bottom:
                int row = ChannelData.MAX_NUMBER - idx;
                int col = jdx - ChannelData.MIN_NUMBER;
                int posX = MARGIN + col * CELL_SIZE;
                int posY = MARGIN + row * CELL_SIZE;

                g2.setColor(color);
                g2.fillRect(posX, posY, CELL_SIZE, CELL_SIZE);
                g2.setColor(GRID_COLOR);
                g2.drawRect(posX, posY, CELL_SIZE, CELL_SIZE);

                if (showCoordinates) {
                    String text = idx + "-" + jdx;
                    int textWidth = g2.getFontMetrics().stringWidth(text);
                    g2.drawString(text,
                            posX + (CELL_SIZE - textWidth) / 2,
                            posY + CELL_SIZE / 2
                            + g2.getFontMetrics().getAscent() / 2);
                }
            }
        }
        g2.dispose();
    }

    /**
     * Opens a frame showing the layout and prints a short summary of the
     * channel counts to the console.
     *
     * @param args pass "coords" to get the coordinates painted into the cells
     */
    public static void main(String[] args) {
        boolean showCoordinates = args.length > 0
                && "coords".equalsIgnoreCase(args[0]);
        printSummary();

        SwingUtilities.invokeLater(() -> {
            ReactorLayoutViewer panel = new ReactorLayoutViewer();
            panel.setShowCoordinates(showCoordinates);

            JFrame frame = new JFrame("RBMK reactor layout - getChannelType()");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * Counts how often each channel type occurs in the layout and prints the
     * result, which makes it easy to compare against the hand drawing.
     */
    private static void printSummary() {
        int[] counts = new int[ChannelType.values().length];
        for (int idx = ChannelData.MIN_NUMBER;
                idx <= ChannelData.MAX_NUMBER; idx++) {
            for (int jdx = ChannelData.MIN_NUMBER;
                    jdx <= ChannelData.MAX_NUMBER; jdx++) {
                counts[ChannelData.getChannelType(idx, jdx).ordinal()]++;
            }
        }
        for (ChannelType type : ChannelType.values()) {
            System.out.println(type + ": " + counts[type.ordinal()]);
        }
    }
}
