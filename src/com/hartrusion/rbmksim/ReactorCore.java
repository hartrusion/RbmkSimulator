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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import com.hartrusion.control.ParameterHandler;
import com.hartrusion.control.Setpoint;
import com.hartrusion.mvc.ActionCommand;

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
     * Each rod has the possibility to absorb between 0 and 1, short rods will
     * do a smaller value. This is the total sum of those factors.
     */
    private double maxAbsorption = 0.0; // should be inited to 35.4

    private double avgRodPosition = 7.3;

    private double rodAbsorption = 100.0;

    /**
     * Available manual movement rod speeds which can be selected with the
     * rodSpeedIndex variable.
     */
    private final double[] rodSpeeds = {0.1, 0.2, 0.3};
    private int rodSpeedIndex = 1;

    /**
     * Positive reactivity in 0..100 % which has same dimension as
     * rodAbsorption.
     */
    private double reactivity;

    private double voiding = 0;
    private double coreTemp = 200;

    private final NeutronFluxModel neutronFluxModel = new NeutronFluxModel();
    private final XenonModel xenonModel = new XenonModel();
    
    private boolean rpsActive = true;

    ReactorCore() {
        setpointPowerGradient = new Setpoint();
        setpointPowerGradient.initName("SetpointPowerGradient");

        setpointNeutronFlux = new Setpoint();
        setpointNeutronFlux.initName("SetpointNeutronFlux");
    }

    @Override
    public void run() {
        setpointNeutronFlux.run();
        setpointPowerGradient.run();

        for (ControlRod rod : controlRods) {
            // Write speed change to selected rods:
            if (rod.isSelected() && !rod.isAutomatic()) {
                rod.getSwi().setMaxRate(rodSpeeds[rodSpeedIndex]);
            }
        }
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
        avgRodPosition = totalPosition / (double) controlRods.size();
        // Generate a 0..100 % value from rod absorption
        rodAbsorption = absorption / maxAbsorption * 100;

        // This magic formula sets how the whole thing behaves
        reactivity = 65.0 // generally present reactivity.
                - xenonModel.getYXenon() / 200 * 60
                - Math.min(15, coreTemp / 30)
                + voiding / 20 * 5;

        // pass reactivity to and get the neutron flux from state space model.
        neutronFluxModel.setInputs(rodAbsorption, reactivity);
        neutronFluxModel.run();

        // Pass neutron flux to xenon model and generate xenon poisoning value.
        xenonModel.setInputs(neutronFluxModel.getYNeutronFlux());

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
            case "Reactor#RodSelectAllManual":
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
                if (rodSpeedIndex < rodSpeeds.length - 1) {
                    rodSpeedIndex++;
                }
                break;
            case "Reactor#DecreaseRodSpeed":
                if (rodSpeedIndex > 1) {
                    rodSpeedIndex--;
                }
                break;
            case "Reactor#RodStop":
                // This command stops all selected control rods and
                // also all non-automatic control rods.
                for (ControlRod cRod : controlRods) {
                    if (cRod.isSelected() || !cRod.isAutomatic()) {
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
        rodSpeedIndex = rodSpeeds.length - 1;
        for (ControlRod c : controlRods) {
            if (c.getRodType() == ChannelType.SHORT_CONTROLROD) {
                c.getSwi().setInputMin();
            } else {
                c.getSwi().setInputMax();
            }
        }
    }
}
