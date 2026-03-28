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
package com.hartrusion.rbmksim.gui.diagrams;

import com.hartrusion.plot.Axes;
import com.hartrusion.plot.Cursor;
import com.hartrusion.plot.Line;
import com.hartrusion.rbmksim.ThermalLayout;
import com.hartrusion.values.ValueHandler;

/**
 * Separate extension for the use of Cursor elements in diagram.
 *
 * @author Viktor Alexander Hartung
 */
public class DiagramStartupPressureSetpoint extends InternalFrameDiagram {

    private static final float P_TH_MAX = 500;

    private float[] xDataPressure = new float[4];
    private float[] yDataPressure = new float[4];

    private Line pressureGradient;

    private Cursor drum1Pressure;
    private Cursor drum2Pressure;

    private ValueHandler plotData;
    
    /**
     * Generates line objects for pressure gradient displays
     */
    public void initPlotObjects(ValueHandler plotData) {
        this.plotData = plotData;
        
        // Generate a line from the static parameters from the thermal layout
        xDataPressure[0] = 0.0F;
        xDataPressure[1] = (float) ThermalLayout.PRESSURE_SETPOINT_POWER_START;
        xDataPressure[2] = (float) ThermalLayout.PRESSURE_SETPOINT_POWER_END;
        xDataPressure[3] = P_TH_MAX;
        yDataPressure[0] = (float) ThermalLayout.PRESSURE_SETPOINT_LOWER;
        yDataPressure[1] = (float) ThermalLayout.PRESSURE_SETPOINT_LOWER;
        yDataPressure[2] = (float) ThermalLayout.PRESSURE_SETPOINT_UPPER;
        yDataPressure[3] = (float) ThermalLayout.PRESSURE_SETPOINT_UPPER;

        pressureGradient = new Line();
        pressureGradient.setData(xDataPressure, yDataPressure);

        drum1Pressure = new Cursor();
        drum2Pressure = new Cursor();
        
        Axes ax = getFigure().getLastAxes(); // access field of super
        ax.addLine(pressureGradient);
        
        ax.yLim(0, 80);
        ax.xLim(0, P_TH_MAX);
        
        ax.xlabel("Reactor Thermal Power (MW)");
        ax.ylabel("Pressure Setpoint (bar)");
        
        ax.addCursor(drum1Pressure);
        ax.addCursor(drum2Pressure);
    }

    @Override
    public void updatePlots() {
        // get values and paint cursors
        float x = (float) plotData.getParameterDouble("Reactor#ThermalPowerDisplay");
        float y1 = (float) plotData.getParameterDouble("Loop1#DrumPressure");
        float y2 = (float) plotData.getParameterDouble("Loop2#DrumPressure");

        // always keep cursors limited to the right diagram side.
        drum1Pressure.setPoint(Math.min(P_TH_MAX, x), y1);
        drum2Pressure.setPoint(Math.min(P_TH_MAX, x), y2);

        super.updatePlots();
    }
}
