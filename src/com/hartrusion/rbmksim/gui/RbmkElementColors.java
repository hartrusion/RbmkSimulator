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

import java.awt.Color;

/**
 * Defines the colors of the self drawn elements of the control panels, which
 * are the linear ChornobylGauge, the pointer instrument Ammeter, the two
 * control loop modules ControlLoop and ControlLoopValve and the core
 * instrument of PanelCoreControl. These elements imitate unique panel parts of
 * a real control room, so that their colors can not be taken from the Metal
 * look and feel and have to be provided by the GUI theme in addition to the
 * Metal colors.
 * <p>
 * The colors are defined by an interface for the same reason as the
 * RbmkPanelButtonColors, because the light RbmkTheme and the dark
 * RbmkThemeDark derive from different Metal themes of the utils package.
 * GuiThemeManager provides the element colors of the active GUI theme with
 * getElementColors(), and each element reads them once when it is created,
 * which is sufficient because all windows are created again when the GUI theme
 * is switched. The colors have to be plain Color objects instead of
 * ColorUIResource, because Swing replaces any ColorUIResource of a component
 * by the default of the look and feel when the component UI is updated.
 * <p>
 * The getters of the ChornobylGauge start with "Gauge" and those of the
 * Ammeter with "Ammeter", while the getters that start with "ControlLoop" are
 * used by both control loop modules unless they name the ControlLoopValve
 * explicitly, because both modules have the same kind of small display with a
 * frame, a scale and markers. The getters that start with "CoreInstrument"
 * belong to the panel jPanelCoreInstrument of PanelCoreControl, which shows
 * the neutron flux, the reactivity and the xenon on digital readings and
 * contains two Ammeters.
 * <p>
 * LLM code generated with Claude Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public interface RbmkElementColors {

    /**
     * @return Background of the whole gauge around the bar
     */
    Color getGaugeBackground();

    /**
     * @return Line border around the gauge
     */
    Color getGaugeBorder();

    /**
     * @return Bar on which the indicator spots move
     */
    Color getGaugeBar();

    /**
     * @return Tick marks, tick labels and the unit text of the gauge
     */
    Color getGaugeScale();

    /**
     * @return Indicator spots while the value is inside the scale
     */
    Color getGaugeIndicator();

    /**
     * @return Indicator spots while the value is outside of the scale
     */
    Color getGaugeIndicatorOutOfRange();

    /**
     * @return Line border around both control loop modules
     */
    Color getControlLoopBorder();

    /**
     * @return Background of the small displays of both control loop modules
     */
    Color getControlLoopDisplayBackground();

    /**
     * @return Frame and ticks of the displays of both control loop modules,
     * which is also the value marker of the ControlLoopValve
     */
    Color getControlLoopDisplayScale();

    /**
     * @return Marker of the current value of the ControlLoop
     */
    Color getControlLoopValueMarker();

    /**
     * @return Marker of the setpoint of the ControlLoop
     */
    Color getControlLoopSetpointMarker();

    /**
     * @return Marker of the output value of the ControlLoop
     */
    Color getControlLoopOutputMarker();

    /**
     * @return Indicator light of the ControlLoop in automatic mode
     */
    Color getControlLoopIndicatorActive();

    /**
     * @return Indicator light of the ControlLoop in manual mode
     */
    Color getControlLoopIndicatorInactive();

    /**
     * @return Background of the ControlLoop module
     */
    Color getControlLoopBackground();

    /**
     * @return Background of the buttons of the ControlLoop
     */
    Color getControlLoopButtonBackground();

    /**
     * @return Background of the setpoint button of the ControlLoop while the
     * up and down buttons change the setpoint
     */
    Color getControlLoopButtonSelected();

    /**
     * @return Text of the buttons of the ControlLoop
     */
    Color getControlLoopButtonForeground();

    /**
     * @return Background of the increase button of the ControlLoop
     */
    Color getControlLoopIncreaseButtonBackground();

    /**
     * @return Background of the ControlLoopValve module
     */
    Color getControlLoopValveBackground();

    /**
     * @return Background of the buttons and of the mode label of the
     * ControlLoopValve
     */
    Color getControlLoopValveButtonBackground();

    /**
     * @return Text of the buttons and of the mode label of the
     * ControlLoopValve
     */
    Color getControlLoopValveButtonForeground();

    /**
     * @return Background of the scale of the Ammeter
     */
    Color getAmmeterBackground();

    /**
     * @return Arc, pointer and labels of the Ammeter
     */
    Color getAmmeterForeground();

    /**
     * @return Line border around the Ammeter
     */
    Color getAmmeterBorder();

    /**
     * @return Background of the core instrument panel
     */
    Color getCoreInstrumentBackground();

    /**
     * @return Background of the digital readings of the core instrument
     */
    Color getCoreInstrumentReadingBackground();

    /**
     * @return Digits of the digital readings of the core instrument
     */
    Color getCoreInstrumentReadingForeground();
}
