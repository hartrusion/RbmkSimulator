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
import com.hartrusion.values.ValueHandler;
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
    private Turbine turbine = new Turbine();

    private ValueHandler outputValues = new ValueHandler();
    public AlarmManager alarms = new AlarmManager(); // temporary public
    
    /**
     * An instance for the core indicator panel.
     */
    private final CoreIndicator indicator1 = new CoreIndicator();

    private boolean pause;

    long maxTime;
    long initialIterations = 0;

    public void init() {
        core.registerAlarmManager(alarms);
        core.init();
        core.registerParameterOutput(outputValues);
        core.registerController(controller);

        process.registerReactor(core);
        process.registerTurbine(turbine);
        
        process.registerParameterOutput(outputValues);
        process.registerController(controller);
        process.registerAlarmManager(alarms);
        process.init();

        indicator1.setCore(core);
    }

    @Override
    public void run() {
        long startTime, stopTime;
        startTime = System.nanoTime();

        try {
            // Get all the values and GUI commands first.
            controller.fireActions();

            if (!pause) {
                // Feedback from process to reactor core model (previous cycle)
                core.setCoreTemp(process.getCoreTemp());
                core.setVoiding(process.getVoiding());
                core.run();
                process.run();
                indicator1.run();

                // Send all measurement data to the GUI by sending a reference.
                controller.propertyChange("OutputValues", outputValues);
                // Send the core indicator to update the view
                controller.propertyChange("CoreIndicator#1", indicator1);
            }

        } catch (Exception e) {
            pause = true;
            ExceptionPopup.show(e);
            // System.exit(0);
        }

        stopTime = System.nanoTime();
        if (stopTime - startTime > maxTime) {
            if (initialIterations > 2) {
                maxTime = stopTime - startTime;
                Logger.getLogger(MainLoop.class.getName())
                        .log(Level.INFO, "New max cyclic time: "
                                + maxTime / 1000 + " us");
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

        if (ac.getPropertyName().equals("PauseSimulation")) {
            pause = !pause;
        }
        
        if (ac.getPropertyName().equals("AcknowledgeAlarms")) {
            alarms.acknowledge();
        }

        if (pause) {
            return; // irgnore commands during pause
        }

        core.handleAction(ac);
        process.handleAction(ac);
    }

    @Override // called on initialization
    public void registerController(ModelListener controller) {
        this.controller = controller;
        core.registerController(controller);
    }
}
