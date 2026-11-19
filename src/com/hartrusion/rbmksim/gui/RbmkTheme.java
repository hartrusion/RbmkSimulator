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

import com.hartrusion.swing.ChornobylMetalTheme;
import java.awt.Color;

/**
 * Extends the generic Theme which was designed for the simulator but placed 
 * in the external utils package by colors that are only used by the simulator.
 * The colors of the Metal look and feel are inherited from the light
 * ChornobylMetalTheme, while this class adds the colors of the panel buttons
 * and of the self drawn elements, which are defined by the interfaces
 * RbmkPanelButtonColors and RbmkElementColors, because the dark RbmkThemeDark
 * derives from ChornobylMetalThemeDark and can therefore not derive from this
 * class. The element colors keep the original appearance of the gauges and
 * control loop modules, which had these colors fixed in their forms before.
 * <p>
 * LLM code generated with Claude Opus 5.5 (element colors)
 * 
 * @author Viktor Alexander Hartung
 */
public class RbmkTheme extends ChornobylMetalTheme
        implements RbmkPanelButtonColors, RbmkElementColors {

    // Green panel buttons, which show for example a closed valve.
    private static final Color GREEN_BUTTON_BACKGROUND = new Color(0, 128, 0);
    private static final Color GREEN_BUTTON_HIGHLIGHT = new Color(0, 255, 0);
    private static final Color GREEN_BUTTON_FOREGROUND = new Color(255, 255, 255);

    // Red panel buttons, which show for example an open valve or a selected
    // automatic control rod.
    private static final Color RED_BUTTON_BACKGROUND = new Color(128, 0, 0);
    private static final Color RED_BUTTON_HIGHLIGHT = new Color(255, 0, 0);
    private static final Color RED_BUTTON_FOREGROUND = new Color(255, 255, 255);

    // Gray panel buttons, which replace the green close buttons where a
    // less colorful panel is wanted.
    private static final Color GRAY_BUTTON_BACKGROUND = new Color(92, 92, 92);
    private static final Color GRAY_BUTTON_HIGHLIGHT = new Color(208, 208, 208);
    private static final Color GRAY_BUTTON_FOREGROUND = new Color(255, 255, 255);

    // Yellow panel buttons, which show the selection of short control rods.
    private static final Color YELLOW_BUTTON_BACKGROUND = new Color(128, 128, 0);
    private static final Color YELLOW_BUTTON_HIGHLIGHT = new Color(208, 208, 0);
    private static final Color YELLOW_BUTTON_FOREGROUND = new Color(255, 255, 255);

    // White panel buttons, which show the selection of manual control rods.
    // The text is black because it would not be readable on white.
    private static final Color WHITE_BUTTON_BACKGROUND = new Color(128, 128, 128);
    private static final Color WHITE_BUTTON_HIGHLIGHT = new Color(255, 255, 255);
    private static final Color WHITE_BUTTON_FOREGROUND = new Color(0, 0, 0);

    // ChornobylGauge colors. The light colors are the original black bar with
    // orange spots on a pale panel, while the dark theme uses a dark panel on
    // which the spots and all labels are drawn in one modern blue.
    private static final Color GAUGE_BACKGROUND = new Color(245, 245, 225);
    private static final Color GAUGE_BORDER = new Color(0, 0, 0);
    private static final Color GAUGE_BAR = new Color(0, 0, 0);
    private static final Color GAUGE_SCALE = new Color(0, 0, 0);
    private static final Color GAUGE_INDICATOR = new Color(255, 200, 0);
    private static final Color GAUGE_INDICATOR_OUT_OF_RANGE = new Color(255, 0, 0);

    // Displays of both control loop modules, which are white with black lines
    // in the light theme and nearly black with gray blue lines and softened
    // marker colors in the dark theme.
    private static final Color CONTROL_LOOP_BORDER = new Color(0, 0, 0);
    private static final Color CONTROL_LOOP_DISPLAY_BACKGROUND = new Color(255, 255, 255);
    private static final Color CONTROL_LOOP_DISPLAY_SCALE = new Color(0, 0, 0);
    private static final Color CONTROL_LOOP_VALUE_MARKER = new Color(0, 128, 0);
    private static final Color CONTROL_LOOP_SETPOINT_MARKER = new Color(255, 0, 0);
    private static final Color CONTROL_LOOP_OUTPUT_MARKER = new Color(0, 0, 255);
    private static final Color CONTROL_LOOP_INDICATOR_ACTIVE = new Color(255, 255, 255);
    private static final Color CONTROL_LOOP_INDICATOR_INACTIVE = new Color(128, 128, 128);

    // Modules of the control loops, which imitate yellowed plastic parts in the
    // light theme and dark metallic blue parts in the dark theme.
    private static final Color CONTROL_LOOP_BACKGROUND = new Color(205, 197, 170);
    private static final Color CONTROL_LOOP_BUTTON_BACKGROUND = new Color(229, 229, 229);
    private static final Color CONTROL_LOOP_BUTTON_SELECTED = new Color(192, 192, 192);
    private static final Color CONTROL_LOOP_BUTTON_FOREGROUND = new Color(0, 0, 0);
    private static final Color CONTROL_LOOP_INCREASE_BUTTON_BACKGROUND = new Color(128, 0, 0);
    private static final Color CONTROL_LOOP_VALVE_BACKGROUND = new Color(204, 204, 204);
    private static final Color CONTROL_LOOP_VALVE_BUTTON_BACKGROUND = new Color(255, 255, 204);
    private static final Color CONTROL_LOOP_VALVE_BUTTON_FOREGROUND = new Color(0, 0, 0);

    @Override
    public Color getGreenButtonBackground() {
        return GREEN_BUTTON_BACKGROUND;
    }

    @Override
    public Color getGreenButtonHighlight() {
        return GREEN_BUTTON_HIGHLIGHT;
    }

    @Override
    public Color getGreenButtonForeground() {
        return GREEN_BUTTON_FOREGROUND;
    }

    @Override
    public Color getRedButtonBackground() {
        return RED_BUTTON_BACKGROUND;
    }

    @Override
    public Color getRedButtonHighlight() {
        return RED_BUTTON_HIGHLIGHT;
    }

    @Override
    public Color getRedButtonForeground() {
        return RED_BUTTON_FOREGROUND;
    }

    @Override
    public Color getGrayButtonBackground() {
        return GRAY_BUTTON_BACKGROUND;
    }

    @Override
    public Color getGrayButtonHighlight() {
        return GRAY_BUTTON_HIGHLIGHT;
    }

    @Override
    public Color getGrayButtonForeground() {
        return GRAY_BUTTON_FOREGROUND;
    }

    @Override
    public Color getYellowButtonBackground() {
        return YELLOW_BUTTON_BACKGROUND;
    }

    @Override
    public Color getYellowButtonHighlight() {
        return YELLOW_BUTTON_HIGHLIGHT;
    }

    @Override
    public Color getYellowButtonForeground() {
        return YELLOW_BUTTON_FOREGROUND;
    }

    @Override
    public Color getWhiteButtonBackground() {
        return WHITE_BUTTON_BACKGROUND;
    }

    @Override
    public Color getWhiteButtonHighlight() {
        return WHITE_BUTTON_HIGHLIGHT;
    }

    @Override
    public Color getWhiteButtonForeground() {
        return WHITE_BUTTON_FOREGROUND;
    }

    @Override
    public Color getGaugeBackground() {
        return GAUGE_BACKGROUND;
    }

    @Override
    public Color getGaugeBorder() {
        return GAUGE_BORDER;
    }

    @Override
    public Color getGaugeBar() {
        return GAUGE_BAR;
    }

    @Override
    public Color getGaugeScale() {
        return GAUGE_SCALE;
    }

    @Override
    public Color getGaugeIndicator() {
        return GAUGE_INDICATOR;
    }

    @Override
    public Color getGaugeIndicatorOutOfRange() {
        return GAUGE_INDICATOR_OUT_OF_RANGE;
    }

    @Override
    public Color getControlLoopBorder() {
        return CONTROL_LOOP_BORDER;
    }

    @Override
    public Color getControlLoopDisplayBackground() {
        return CONTROL_LOOP_DISPLAY_BACKGROUND;
    }

    @Override
    public Color getControlLoopDisplayScale() {
        return CONTROL_LOOP_DISPLAY_SCALE;
    }

    @Override
    public Color getControlLoopValueMarker() {
        return CONTROL_LOOP_VALUE_MARKER;
    }

    @Override
    public Color getControlLoopSetpointMarker() {
        return CONTROL_LOOP_SETPOINT_MARKER;
    }

    @Override
    public Color getControlLoopOutputMarker() {
        return CONTROL_LOOP_OUTPUT_MARKER;
    }

    @Override
    public Color getControlLoopIndicatorActive() {
        return CONTROL_LOOP_INDICATOR_ACTIVE;
    }

    @Override
    public Color getControlLoopIndicatorInactive() {
        return CONTROL_LOOP_INDICATOR_INACTIVE;
    }

    @Override
    public Color getControlLoopBackground() {
        return CONTROL_LOOP_BACKGROUND;
    }

    @Override
    public Color getControlLoopButtonBackground() {
        return CONTROL_LOOP_BUTTON_BACKGROUND;
    }

    @Override
    public Color getControlLoopButtonSelected() {
        return CONTROL_LOOP_BUTTON_SELECTED;
    }

    @Override
    public Color getControlLoopButtonForeground() {
        return CONTROL_LOOP_BUTTON_FOREGROUND;
    }

    @Override
    public Color getControlLoopIncreaseButtonBackground() {
        return CONTROL_LOOP_INCREASE_BUTTON_BACKGROUND;
    }

    @Override
    public Color getControlLoopValveBackground() {
        return CONTROL_LOOP_VALVE_BACKGROUND;
    }

    @Override
    public Color getControlLoopValveButtonBackground() {
        return CONTROL_LOOP_VALVE_BUTTON_BACKGROUND;
    }

    @Override
    public Color getControlLoopValveButtonForeground() {
        return CONTROL_LOOP_VALVE_BUTTON_FOREGROUND;
    }
}
