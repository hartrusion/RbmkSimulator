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
import com.hartrusion.control.SerialRunner;
import com.hartrusion.modeling.solvers.SuperPosition;
import com.hartrusion.mvc.ActionCommand;
import com.hartrusion.mvc.ModelListener;
import com.hartrusion.mvc.ModelManipulation;
import com.hartrusion.rbmksim.gui.ExceptionPopup;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Viktor Alexander Hartung
 */
public class MainLoop implements Runnable, ModelManipulation {

    private ModelListener controller;

    private ReactorCore core = new ReactorCore();
    private ThermalLayout process = new ThermalLayout();

    private ParameterHandler outputValues = new ParameterHandler();
    private AlarmManager alarms = new AlarmManager();

    long maxTime;
    long initialIterations = 0;

    FloatSeriesVault plotData = new FloatSeriesVault();
    private final int plotCountDiv = 4; // each nth cycle the plots will be updated

    public void init() {
        try {
            plotData.initTime(751, 0.1F * (float) plotCountDiv);
            plotData.setCountDiv(plotCountDiv);
            
            core.registerAlarmManager(alarms);
            core.init();
            core.registerPlotDataVault(plotData);
            core.registerParameterOutput(outputValues);
            
            
            process.registerReactor(core);
            process.registerParameterOutput(outputValues);
            process.registerController(controller);
            process.registerAlarmManager(alarms);
            process.init();
            process.registerPlotDataVault(plotData);
                        
        } catch (Exception e) {
            // Throw exception as error message popup
            ExceptionPopup.show(e);
            System.exit(0);
        }
    }

    @Override
    public void run() {
        long startTime, stopTime;
        startTime = System.nanoTime();

        try {
            // Get all the values and GUI commands first.
            controller.fireActions();
            
            // Feedback from process to reactor core model (previous cycle)
            core.setCoreTemp(process.getCoreTemp());
            core.setVoiding(process.getVoiding());
            
            core.run();
             
            process.run();

            // Send all measurement data to the GUI by sending a reference.
            controller.propertyChange("OutputValues", outputValues);
            
            // Sent all timeseries data also
            controller.propertyChange("PlotData", plotData);

        } catch (Exception e) {
            ExceptionPopup.show(e);
            System.exit(0);
        }

        
        stopTime = System.nanoTime();
        if (stopTime - startTime > maxTime) {
            if (initialIterations > 2) {
                maxTime = stopTime - startTime;
                System.getLogger(MainLoop.class.getName()).log(
                        System.Logger.Level.INFO,
                        "New max cyclic time: " + maxTime / 1000
                        + " us");
            } else {
                initialIterations++;
            }
        }
    }

    @Override
    public void updateNotification(String propertyName) {

    }

    @Override // Called from controller upon fireActions here in run()
    public void handleAction(ActionCommand ac) {
         Logger.getLogger(MainLoop.class.getName())
                    .log(Level.INFO, "Received Action: " + ac.getPropertyName()
                    + ", Value: " + ac.getValue());
        
        core.handleAction(ac);
        process.handleAction(ac);
    }

    @Override // called on initialization
    public void registerController(ModelListener controller) {
        this.controller = controller;
        core.registerController(controller);
    }
}
