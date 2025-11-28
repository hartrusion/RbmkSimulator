/*
 * Copyright (C) 2025 Viktor Alexander Hartung
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

import com.hartrusion.alarm.AlarmManager;
import com.hartrusion.control.FloatSeriesVault;
import com.hartrusion.control.ParameterHandler;
import com.hartrusion.mvc.ModelListener;
import com.hartrusion.mvc.ModelManipulation;

/**
 * Base class for common methods and fields which are used by a system that has 
 * to be simulated.
 * 
 * @author Viktor Alexander Hartung
 */
public abstract class Subsystem implements ModelManipulation {
    
    protected ModelListener controller;
    
    /**
     * Stores all output values as string value pair. Values from model will be
     * written into this handler.
     */
    protected ParameterHandler outputValues;

    protected FloatSeriesVault plotData;
    protected int plotUpdateCount;
    
    protected AlarmManager alarmManager;
    
    @Override
    public void registerController(ModelListener controller) {
        this.controller = controller;
    }

    public void registerParameterOutput(ParameterHandler output) {
        this.outputValues = output;
    }

    public void registerPlotDataVault(FloatSeriesVault plotData) {
        this.plotData = plotData;
    }
    
    public void registerAlarmManager(AlarmManager alarmManager) {
        this.alarmManager = alarmManager;
    }
}
