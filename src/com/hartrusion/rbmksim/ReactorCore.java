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

import com.hartrusion.alarm.AlarmAction;
import com.hartrusion.alarm.AlarmState;
import com.hartrusion.alarm.ValueAlarmMonitor;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import com.hartrusion.control.ParameterHandler;
import com.hartrusion.control.SerialRunner;
import com.hartrusion.control.Setpoint;
import com.hartrusion.mvc.ActionCommand;
import java.util.function.DoubleSupplier;

/**
 * Models everything that has to do something with the reactor.
 *
 * <ul>
 * <li>Has a neutron flux model</li>
 * <li>Has lists and arrays with all fuel elements and rods</li>
 * <li>Handles control rod selection and movement commands</li>
 * </ul>
 *
 * @author Viktor Alexander Hartung
 */
public class ReactorCore extends Subsystem implements Runnable {

    private final Setpoint setpointPowerGradient;
    private final Setpoint setpointNeutronFlux;

    private final int[][] rodIndex = new int[23][23];
    private final int[][] fuelIndex = new int[23][23];

    private final List<FuelElement> fuelElements = new ArrayList<>();
    private final List<ControlRod> controlRods = new ArrayList<>();

    /**
     * Each rod has the possibility to absorb between 0 and 1.0, short rods will
     * do a smaller value. This is the total sum of those factors for all
     * control rods. This value is 100 % rodAbsorption. It can be calculated 
     * that one rod with 1.0 makes 2.82 % rodAbsportion with this value.
     */
    private double maxAbsorption = 0.0; // should be inited to 35.40000

    private double avgRodPosition = 7.3;

    private double rodAbsorption = 100.0;

    /**
     * Positive reactivity in 0..100 % which has same dimension as
     * rodAbsorption.
     */
    private double reactivity;

    private double voiding = 0;
    private double coreTemp = 200;

    private final NeutronFluxModel neutronFluxModel = new NeutronFluxModel();
    private final XenonModel xenonModel = new XenonModel();

    private final SerialRunner alarmUpdater = new SerialRunner();

    private boolean rpsActive = false;

    ReactorCore() {
        setpointPowerGradient = new Setpoint();
        setpointPowerGradient.initName("Reactor#SetpointPowerGradient");

        setpointNeutronFlux = new Setpoint();
        setpointNeutronFlux.initName("Reactor#SetpointNeutronFlux");
    }

    @Override
    public void run() {
        setpointNeutronFlux.run();
        setpointPowerGradient.run();
        
        for (ControlRod rod : controlRods) {
            rod.run(); // update all rods
            // Send new position values to GUI
            String propName = "Reactor#RodPosition" + rod.getIdentifier();
            outputValues.setParameterValue(propName, rod.getSwi().getOutput());
        }

        // Calculate total absorption and average rod position
        double absorption = 0.0;
        double totalPosition = 0.0;
        for (ControlRod rod : controlRods) {
            absorption += rod.getAbsorption();
            if (rod.getRodType() == ChannelType.SHORT_CONTROLROD) {
                totalPosition += (7.3 - rod.getSwi().getOutput());
            } else {
                totalPosition += rod.getSwi().getOutput();
            }
        }
        // Total position is the total sum of control rod pull lengts. 
        avgRodPosition = totalPosition / (double) controlRods.size();
        // Generate a 0..100 % value from total rod absorption
        rodAbsorption = absorption / maxAbsorption * 100;

        /* This magic formula sets how the whole thing behaves. The reactivity
        * is given in same unit and dimension as the rods absorption, the 
        * difference is integrated by the neutron flux model to get the neutron
        * flux. We use unit %N as percentage of neturon flux here.
        * - With no negative reactivity effects, it is defined that we need 5 
        *   manual rods out to have positive reactitvity. 
        *   100 %N - 5 * 2.26 %N = 88.7 %N
        *   This allows to pull 4 manual rods first and use the 4 auto rods at
        *   half pull distance to smootly control the initial reaction.
        * - One manual control rod gives about 2.26 %N of absorption.
        * - If the accident sequence is simulated correctly, the xenon model
        *   output value will jump up to about 175 %Xe on dropping to 40 %N 
        *   and, if the power is then reduced to 5 %N, it will rise up to a 
        *   value between 200 and 230 %Xe. This means that a xenon value of  
        *   230 %Xe needs all rods out. This allows to stall the core with 
        *   xenon poisoning.
        *   1 / 230 %Xe * 80 %N = 0.348 %N/%Xe
        * - coreTemp is given in °C and rises up to 570 °C in normal operation.
        *   A negative temperature coefficient is wanted with 2 rods so we'll
        *   have 2 * 2.26%N / 570 °C = 7.93e-3 %N/°C. However, it will be 
        *   limited to a certain value to not prevent a meltdown too much.
        * - There is a total of 28 manual control rods. If 24 of them are 
        *   pulled out and everything else is still in, ORM is 41.2 which is an
        *   absorption of 58.8 reactivity by those rods.
        *   14 Rods out: 39.54 %N max, 35.59 with rod effect.
        
         */
        reactivity = 88.7 // generally present reactivity.
                - xenonModel.getYXenon() * 0.348
                - Math.min(700, coreTemp) * 7.93e-3
                + voiding / 20 * 5;

        // pass reactivity to and get the neutron flux from state space model.
        neutronFluxModel.setInputs(rodAbsorption, reactivity);
        neutronFluxModel.run();

        // Pass neutron flux to xenon model and generate xenon poisoning value.
        xenonModel.setInputs(neutronFluxModel.getYNeutronFlux());
        xenonModel.run();

        alarmUpdater.invokeAll();

        // Send the neutron values to the gui
        outputValues.setParameterValue("Reactor#NeutronFlux",
                neutronFluxModel.getYNeutronFlux());
        outputValues.setParameterValue("Reactor#NeutronFluxLog",
                neutronFluxModel.getYNeutronFluxLog());
        outputValues.setParameterValue("Reactor#NeutronRate",
                neutronFluxModel.getYNeutronRate());
        outputValues.setParameterValue("Reactor#AvgRodPos",
                avgRodPosition);
        outputValues.setParameterValue("Reactor#RodAbsorption",
                rodAbsorption);
        outputValues.setParameterValue("Reactor#Xenon",
                xenonModel.getYXenon());
        outputValues.setParameterValue("Reactor#ThermalPower",
                neutronFluxModel.getYThermalPower());

        // Save values to plot manager
        if (plotUpdateCount == 0) {
            plotData.insertValue("Reactor#NeutronFlux",
                    (float) neutronFluxModel.getYNeutronFlux());
            plotData.insertValue("Reactor#NeutronFluxLog",
                    (float) neutronFluxModel.getYNeutronFluxLog());
            plotData.insertValue("Reactor#NeutronRate",
                    (float) neutronFluxModel.getYNeutronRate());
            plotData.insertValue("Reactor#Xenon",
                    (float) xenonModel.getYXenon());
            plotUpdateCount = plotData.getCountDiv() - 1;
        } else {
            plotUpdateCount--;
        }

    }

    /**
     * Receive events from GUI. This receives rod selection commands and other
     * commands.
     *
     * @param evt
     */
    @Override
    public void handleAction(ActionCommand ac) {
        if (setpointNeutronFlux.handleAction(ac)) {
            return;
        } else if (setpointPowerGradient.handleAction(ac)) {
            return;
        }
        if (!ac.getPropertyName().startsWith("Reactor#")) {
            return;
        }
        if (ac.getPropertyName().equals("Reactor#AZ5")) {
            shutdown();
            return;
        }
        if (ac.getPropertyName().equals("Reactor#RPS")) {
            rpsActive = (boolean) ac.getValue();
        }
        int identifier, x, y;
        boolean value;
        switch (ac.getPropertyName()) {
            case "Reactor#RodSelect": // Manual selection of single rod
                identifier = (int) ac.getValue();
                x = identifier / 100;
                y = identifier % 100;
                ControlRod rod = (ControlRod) getElement(x, y);
                rod.setSelected(!rod.isSelected());
                // Send current selection of this rod back to view.
                String propertyName;
                if (rod.isSelected()) {
                    propertyName = "Reactor#RodSelected";
                } else {
                    propertyName = "Reactor#RodDeselected";
                }
                controller.propertyChange(
                        new PropertyChangeEvent(this, propertyName,
                                null, identifier));
                break;
            case "Reactor#RodSelectAllManual": // not used anymore, bad idea
                for (ControlRod cRod : controlRods) {
                    if (cRod.getRodType() == ChannelType.MANUAL_CONTROLROD
                            && !cRod.isSelected()) {
                        cRod.setSelected(true);
                        // Sent selection of this rod to GUI:
                        controller.propertyChange(
                                new PropertyChangeEvent(this, "Reactor#RodSelected",
                                        null, cRod.getIdentifier()));
                    }
                }
                break;
            case "Reactor#RodSelectAllAutomatic":
                for (ControlRod cRod : controlRods) {
                    if (cRod.getRodType() == ChannelType.AUTOMATIC_CONTROLROD
                            && !cRod.isSelected()) {
                        cRod.setSelected(true);
                        // Sent selection of this rod to GUI:
                        controller.propertyChange(
                                new PropertyChangeEvent(this, "Reactor#RodSelected",
                                        null, cRod.getIdentifier()));
                    }
                }
                break;
            case "Reactor#RodSelectAllShort":
                for (ControlRod cRod : controlRods) {
                    if (cRod.getRodType() == ChannelType.SHORT_CONTROLROD
                            && !cRod.isSelected()) {
                        cRod.setSelected(true);
                        // Sent selection of this rod to GUI:
                        controller.propertyChange(
                                new PropertyChangeEvent(this, "Reactor#RodSelected",
                                        null, cRod.getIdentifier()));
                    }
                }
                break;
            case "Reactor#RodSelectNone":
                for (ControlRod cRod : controlRods) {
                    if (cRod.isSelected()) {
                        cRod.setSelected(false);
                        // Sent selection of this rod to GUI:
                        controller.propertyChange(
                                new PropertyChangeEvent(this, "Reactor#RodDeselected",
                                        null, cRod.getIdentifier()));
                    }
                }
                break;
            case "Reactor#IncreaseRodSpeed":
                for (ControlRod cRod : controlRods) {
                    if (cRod.isSelected()) {
                        cRod.rodSpeedIncrease();
                    }
                }
                break;
            case "Reactor#DecreaseRodSpeed":
                for (ControlRod cRod : controlRods) {
                    if (cRod.isSelected()) {
                        cRod.rodSpeedDecrease();
                    }
                }
                break;
            case "Reactor#RodStop":
                // This command stops all selected control rods 
                for (ControlRod cRod : controlRods) {
                    if (cRod.isSelected()) {
                        cRod.getSwi().setStop();
                    }
                }
                break;
            case "Reactor#RodManualUp":
                // this event passes the new switch state.
                value = (boolean) ac.getValue();
                // All selected rods (and only those) will either be 
                // stopped on button out or moved up, out of core on button in.
                for (ControlRod cRod : controlRods) {
                    if (cRod.isSelected()) {
                        if (value) {
                            cRod.getSwi().setInputMin();
                        } else {
                            cRod.getSwi().setStop();
                        }
                    }
                }
                break;
            case "Reactor#RodManualDown":
                // this event passes the new switch state.
                value = (boolean) ac.getValue();
                // All selected rods (and only those) will either be 
                // stopped on button out or moved down into core on button in.
                for (ControlRod cRod : controlRods) {
                    if (cRod.isSelected()) {
                        if (value) {
                            cRod.getSwi().setInputMax();
                        } else {
                            cRod.getSwi().setStop();
                        }
                    }
                }
                break;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="init() Method">
    public void init() {

        int idx, jdx;

        // Initialize some fast access arrays so we dont need to search in
        // the arraylist if both indizies are given.
        for (idx = 0; idx < 23; idx++) {
            for (jdx = 0; jdx < 23; jdx++) {
                rodIndex[idx][jdx] = -1;
                fuelIndex[idx][jdx] = -1;
            }
        }

        // Iterate over known core structure and create rods and fuel
        for (idx = 20; idx <= 42; idx++) {
            for (jdx = 20; jdx <= 42; jdx++) {
                switch (ChannelData.getChannelType(idx, jdx)) {
                    case FUEL -> {
                        fuelIndex[idx - 20][jdx - 20] = fuelElements.size();
                        fuelElements.add(new FuelElement(idx, jdx));
                    }
                    case AUTOMATIC_CONTROLROD -> {
                        rodIndex[idx - 20][jdx - 20] = controlRods.size();
                        controlRods.add(new ControlRod(
                                idx, jdx, ChannelType.AUTOMATIC_CONTROLROD));
                    }
                    case SHORT_CONTROLROD -> {
                        rodIndex[idx - 20][jdx - 20] = controlRods.size();
                        controlRods.add(new ControlRod(
                                idx, jdx, ChannelType.SHORT_CONTROLROD));
                    }
                    case MANUAL_CONTROLROD -> {
                        rodIndex[idx - 20][jdx - 20] = controlRods.size();
                        controlRods.add(new ControlRod(
                                idx, jdx, ChannelType.MANUAL_CONTROLROD));
                    }
                }
            }
        }

        // Sum up to get the max possible absorption with all rods inserted.
        for (ControlRod rod : controlRods) {
            maxAbsorption += rod.getMaxAbsorption();
        }

        setpointPowerGradient.setUpperLimit(1.0);
        setpointPowerGradient.setMaxRate(0.2);

        setpointNeutronFlux.forceOutputValue(40.0);
        setpointNeutronFlux.setMaxRate(8);

        // Define alarms and consequences
        ValueAlarmMonitor am;
        am = new ValueAlarmMonitor();
        am.setName("NeutronRate");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return neutronFluxModel.getYNeutronRate();
            }
        });
        am.defineAlarm(2.0, AlarmState.MAX1);
        am.defineAlarm(1.6, AlarmState.HIGH2);
        am.defineAlarm(1.2, AlarmState.HIGH1);
        am.defineAlarm(-1.2, AlarmState.LOW1);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                triggerAutoShutdown();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);
    } // </editor-fold>

    public ReactorElement getElement(int x, int y) {
        if (x < 20 || x > 42 || y < 20 || y > 42) {
            return null; // invalid range
        }
        x = x - 20; // index in array starts with 0 for element nr. 20.
        y = y - 20;
        if (rodIndex[x][y] >= 0) {
            return controlRods.get(rodIndex[x][y]);
        } else if (fuelIndex[x][y] >= 0) {
            return fuelElements.get(fuelIndex[x][y]);
        }
        return null;
    }

    @Override
    public void updateNotification(String propertyName) {

    }

    public void setVoiding(double voiding) {
        this.voiding = voiding;
    }

    public void setCoreTemp(double coreTemp) {
        this.coreTemp = coreTemp;
    }

    public double getThermalPower(int loop) {
        return switch (loop) {
            case 0 ->
                neutronFluxModel.getYThermalPower1();
            case 1 ->
                neutronFluxModel.getYThermalPower2();
            default ->
                0.0;
        };
    }

    @Override
    public void registerParameterOutput(ParameterHandler output) {
        super.registerParameterOutput(output);
        setpointNeutronFlux.registerParameterHandler(outputValues);
        setpointPowerGradient.registerParameterHandler(outputValues);
    }

    /**
     * Can be invoked by this or other
     */
    public void triggerAutoShutdown() {
        if (!rpsActive) {
            return;
        }
        shutdown();
    }

    /**
     * AZ5 makes all rods move into the core immediately with maximum speed.
     */
    private void shutdown() {
        for (ControlRod c : controlRods) {
            if (c.getRodType() == ChannelType.SHORT_CONTROLROD) {
                c.getSwi().setInputMin();
            } else {
                c.getSwi().setInputMax();
            }
        }
    }
}
