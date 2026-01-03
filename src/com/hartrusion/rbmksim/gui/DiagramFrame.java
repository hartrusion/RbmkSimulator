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

import com.hartrusion.values.ValueHandler;

/**
 * Implemented by frames which are used to display diagrams using the JMPlot
 * package. Using this interface allows to organize open line plot frames as
 * lists and managing them.
 *
 * @author Viktor Alexander Hartung
 */
public interface DiagramFrame {

    /**
     * Initializes the plot.
     *
     * @param plotData Used valueHandler that contains the data to display
     * @param number A number to identify the plot number in case of there being
     * more than one (Like loop number 1 or 2 or similar).
     */
    public void initPlots(ValueHandler plotData, int number);

    /**
     * Needs to be called as soon as new data is available to trigger a
     * repainting of the UI.
     */
    public void updatePlots();

    /**
     * Returns a string to identify the plot. This can be used to determine
     * which windows are already opened and prevent opening a plot frame a
     * second time.
     *
     * @return String
     */
    public String getPlotName();
}
