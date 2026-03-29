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
import com.hartrusion.plot.FigureJPane;
import com.hartrusion.plot.Legend;
import com.hartrusion.plot.Line;
import com.hartrusion.plot.MYAxes;
import com.hartrusion.plot.SubPlot;
import com.hartrusion.plot.YYAxes;
import com.hartrusion.values.ValueHandler;
import java.awt.Color;

/**
 * Collection of methods to configure preset diagrams. A FigureJPane object can
 * be passed to each of the static methods here and gets configured by adding
 * lines, axes limits, descriptions and so on.
 * <p>
 * This allows the Figure to be implemented either in separate windows or in
 * embedded JInternalFrame objects.
 *
 * @author Viktor Alexander Hartung
 */
public class DiagramPresets {

    public static void drums(FigureJPane figure, ValueHandler plotData) {
        figure.setYRulers(0);
        figure.setSubplotLayout(new int[]{2, 2});
        figure.setSubplotPosition(new float[]{0.19f, 0.12f, 0.75f, 0.8f});

        figure.getSubPlot().getAxes(1).ylabel("Temperature (°C)");
        figure.getSubPlot().getAxes(2).ylabel("Pressure (bar)");
        figure.getSubPlot().getAxes(3).ylabel("Level (cm)");
        figure.getSubPlot().getAxes(4).ylabel("Feed Flow (kg/s)");

        Line l;
        SubPlot subPlot = figure.getSubPlot();

        subPlot.getAxes(1).setHold(true);
        l = new Line();
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Loop1#DrumTemperature", 5));
        subPlot.getAxes(1).addLine(l);
        l = new Line();
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Loop2#DrumTemperature", 5));
        subPlot.getAxes(1).addLine(l);
        subPlot.getAxes(1).ylabel("Temperature (°C)");
        subPlot.getAxes(1).yLim(50, 300);
        subPlot.getAxes(1).autoX();

        subPlot.getAxes(2).setHold(true);
        l = new Line();
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Loop1#DrumPressure", 5));
        subPlot.getAxes(2).addLine(l);
        l = new Line();
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Loop2#DrumPressure", 5));
        subPlot.getAxes(2).addLine(l);
        subPlot.getAxes(2).ylabel("Pressure (bar)");
        subPlot.getAxes(2).yLim(0, 80);
        subPlot.getAxes(2).autoX();

        subPlot.getAxes(3).setHold(true);
        l = new Line();
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Loop1#DrumLevel", 5));
        subPlot.getAxes(3).addLine(l);
        l = new Line();
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Loop2#DrumLevel", 5));
        subPlot.getAxes(3).addLine(l);
        subPlot.getAxes(3).ylabel("Level (cm)");
        subPlot.getAxes(3).yLim(-20, 20);
        subPlot.getAxes(3).autoX();

        subPlot.getAxes(4).setHold(true);
        l = new Line();
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Feedwater1#Flow", 5));
        subPlot.getAxes(4).addLine(l);
        l = new Line();
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Feedwater2#Flow", 5));
        subPlot.getAxes(4).addLine(l);
        subPlot.getAxes(4).ylabel("Feed flow (kg/s)");
        subPlot.getAxes(4).yLim(0, 1600);
        subPlot.getAxes(4).autoX();
    }

    public static void globalControl(FigureJPane figure, ValueHandler plotData) {
        figure.setYRulers(2);

        YYAxes ax = (YYAxes) figure.getLastAxes();
        Legend le = new Legend();
        ax.setHold(true);
        Line l;

        l = new Line();
        l.setLabel("Target Flux");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Reactor#TargetNeutronFlux", 5));
        l.setLineColor(Color.GRAY);
        ax.addLine(1, l);
        le.addLine(l);

        l = new Line();
        l.setLabel("Active Setpoint");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Reactor#SetpointNeutronFlux", 5));
        l.setLineColor(Color.BLACK);
        ax.addLine(1, l);
        le.addLine(l);

        l = new Line();
        l.setLabel("Neutron Flux");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Reactor#NeutronFlux", 5));
        l.setLineColor(Color.BLUE);
        ax.addLine(1, l);
        le.addLine(l);

        l = new Line();
        l.setLabel("Avg. Auto Rod Pos. (Y2)");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("GlobalControl#AvgActiveAutoRodsPos", 5));
        ax.addLine(2, l);
        le.addLine(l);

        ax.yLim(1, 0, 100F);
        ax.yLim(2, 0, 7.3F);

        ax.autoX();

        ax.ylabel(1, "Neutron Flux (%)");
        ax.ylabel(2, "Avg. Auto Rods Position (m)");

        figure.addLegend(le);
        le.setLocationInsideAxes(ax);
    }

    public static void neutronFlux(FigureJPane figure, ValueHandler plotData) {
        figure.setYRulers(4);

        MYAxes ax = (MYAxes) figure.getLastAxes();
        ax.setHold(true);
        Line l;
        l = new Line();
        l.setDataSource(plotData.getTime60(2),
                plotData.getParameterDoubleSeries("Reactor#NeutronFlux", 2));
        ax.addLine(1, l);
        l = new Line();
        l.setDataSource(plotData.getTime60(2),
                plotData.getParameterDoubleSeries("Reactor#NeutronFluxLog", 2));
        ax.addLine(2, l);
        l = new Line();
        l.setDataSource(plotData.getTime60(2),
                plotData.getParameterDoubleSeries("Reactor#NeutronRate", 2));
        ax.addLine(3, l);
        l = new Line();
        l.setDataSource(plotData.getTime60(2),
                plotData.getParameterDoubleSeries("Reactor#Reactivity", 2));
        ax.addLine(4, l);
        l = new Line();

        ax.yLim(1, 0, 100);
        ax.yLim(2, -6, -1);
        ax.yLim(3, -5, 5);
        ax.yLim(4, -0.005F, 0.005F);

        ax.autoX();

        ax.ylabel(1, "Neutron Flux (%)");
        ax.ylabel(2, "Neutron Flux Log");
        ax.ylabel(3, "Neutron Rate (10%/s)");
        ax.ylabel(4, "Reactivity");
    }

    public static void loopLevelControl(FigureJPane figure, ValueHandler plotData, int loop) {
        figure.setYRulers(2);

        YYAxes ax = (YYAxes) figure.getLastAxes();
        Legend le = new Legend();
        ax.setHold(true);
        Line l;
        l = new Line();
        le.addLine(l);
        l.setLabel("Drum Level");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Loop" + loop + "#DrumLevel", 5));
        ax.addLine(1, l);
        l = new Line();
        le.addLine(l);
        l.setLabel("Setpoint");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Loop" + loop + "#DrumLevelSetpoint", 5));
        l.setLineColor(Color.BLACK);
        ax.addLine(1, l);
        l = new Line();
        le.addLine(l);
        l.setLabel("Startup Valve");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Feedwater" + loop + "#FlowRegulationValve1", 5));
        l.setLineColor(new Color(0, 192, 0));
        ax.addLine(2, l);
        l = new Line();
        le.addLine(l);
        l.setLabel("Main 1");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Feedwater" + loop + "#FlowRegulationValve2", 5));
        l.setLineColor(new Color(0, 128, 0));
        ax.addLine(2, l);
        l = new Line();
        le.addLine(l);
        l.setLabel("Main 2");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Feedwater" + loop + "#FlowRegulationValve3", 5));
        l.setLineColor(new Color(0, 64, 0));
        ax.addLine(2, l);

        ax.yLim(1, -20, 20);
        ax.yLim(2, 0, 100);

        ax.autoX();

        figure.addLegend(le);
        le.setLocationInsideAxes(ax);

        ax.ylabel(1, "Drum Level and Setpoint (cm)");
        ax.ylabel(2, "Flow Valve Positions (%)");
    }

    public static void debugTurbineWarmup(FigureJPane figure, ValueHandler plotData) {
        Axes ax = figure.getLastAxes();
        Legend le = new Legend();
        ax.setHold(true);
        Line l;
        l = new Line();
        l.setLabel("HP In");
        l.setDataSource(plotData.getTime60(2),
                plotData.getParameterDoubleSeries("Turbine#DebugHPInTemp", 2));
        ax.addLine(l);
        le.addLine(l);
        l = new Line();
        l.setLabel("HP Out");
        l.setDataSource(plotData.getTime60(2),
                plotData.getParameterDoubleSeries("Turbine#DebugHPOutTemp", 2));
        ax.addLine(l);
        le.addLine(l);
        l = new Line();
        l.setLabel("LP In");
        l.setDataSource(plotData.getTime60(2),
                plotData.getParameterDoubleSeries("Turbine#DebugLPInTemp", 2));
        ax.addLine(l);
        le.addLine(l);

        ax.yLim(0, 300);
        ax.autoX();

        ax.ylabel("Temperature (°C)");

        figure.addLegend(le);
        le.setLocationInsideAxes(ax);
    }

    public static void turbineHPTemperatures(FigureJPane figure, ValueHandler plotData) {
        Axes ax = figure.getLastAxes();
        Legend le = new Legend();
        ax.setHold(true);
        Line l;

        l = new Line();
        l.setLabel("Stator In");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Turbine#TemperatureHpStatorIn", 5));
        ax.addLine(l);
        le.addLine(l);

        l = new Line();
        l.setLabel("Rotor In");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Turbine#TemperatureHpRotorIn", 5));
        ax.addLine(l);
        le.addLine(l);

        l = new Line();
        l.setLabel("Stator Out");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Turbine#TemperatureHpStatorOut", 5));
        ax.addLine(l);
        le.addLine(l);

        l = new Line();
        l.setLabel("Rotor Out");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Turbine#TemperatureHpRotorOut", 5));
        ax.addLine(l);
        le.addLine(l);
        
        // Debugging data
        l = new Line();
        l.setLabel("Steam Temp In (Debug)");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Turbine#DebugHPInTemp", 5));
        ax.addLine(l);
        le.addLine(l);
        l = new Line();
        l.setLabel("Steam Temp Out (Debug)");
        l.setDataSource(plotData.getTime60(5),
                plotData.getParameterDoubleSeries("Turbine#DebugHPOutTemp", 5));
        ax.addLine(l);
        le.addLine(l);

        ax.yLim(0, 300);
        ax.autoX();

        ax.ylabel("Temperature (°C)");

        figure.addLegend(le);
        le.setLocationInsideAxes(ax);
    }

    public static void turbineWarmpup(FigureJPane figure, ValueHandler plotData) {
        Axes ax = figure.getLastAxes();
        Legend le = new Legend();
        ax.setHold(true);
        Line l;
        l = new Line();
        l.setLabel("HP In");
        l.setDataSource(plotData.getTime60(2),
                plotData.getParameterDoubleSeries("Turbine#DebugHPInTemp", 2));
        ax.addLine(l);
        le.addLine(l);
        l = new Line();
        l.setLabel("HP Out");
        l.setDataSource(plotData.getTime60(2),
                plotData.getParameterDoubleSeries("Turbine#DebugHPOutTemp", 2));
        ax.addLine(l);
        le.addLine(l);
        l = new Line();
        l.setLabel("LP In");
        l.setDataSource(plotData.getTime60(2),
                plotData.getParameterDoubleSeries("Turbine#DebugLPInTemp", 2));
        ax.addLine(l);
        le.addLine(l);

        ax.yLim(0, 300);
        ax.autoX();

        ax.ylabel("Temperature (°C)");

        figure.addLegend(le);
        le.setLocationInsideAxes(ax);
    }
}
