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
import com.hartrusion.control.PIControl;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import com.hartrusion.control.ParameterHandler;
import com.hartrusion.control.SerialRunner;
import com.hartrusion.control.Setpoint;
import com.hartrusion.mvc.ActionCommand;
import com.hartrusion.mvc.ModelListener;
import java.util.function.DoubleSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger LOGGER = Logger.getLogger(
            ReactorCore.class.getName());

    /**
     * Target neutron flux value to which the reactor should be driven to, this
     * value is likely to be set only once and that's it. The gradient is more
     * of a feedback to the user to have a speed for the input that is visible
     * on the GUI.
     */
    private final Setpoint setpointTargetNeutronFlux;

    /**
     * Gradient for the setpointNeutronFlux, the setpointNeutronFlux will run to
     * the setpointTargetNeutronFlux with the gradient that is set here.
     */
    private final Setpoint setpointPowerGradient;

    /**
     * Setpoint value for the control elements, this will ramp up to target and
     * the controllers have this set as a setpoint value.
     */
    private final Setpoint setpointNeutronFlux;

    /**
     * Control instance for moving the automatic control rods, this generates
     * the insertion value for the selected automatic control rods.
     */
    private final PIControl globalControl;

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

    /**
     * Current voiding in the core as a percentage between 0 and 100 %, this is
     * calculated by using the density in the evaporator elements.
     */
    private double voiding = 0;

    /**
     * Core temperature in degrees Celsius, used to generate the negative
     * temperature coefficient.
     */
    private double coreTemp = 80;

    private final NeutronFluxModel neutronFluxModel = new NeutronFluxModel();
    private final XenonModel xenonModel = new XenonModel();
    private final GraphiteEffectModel graphiteModel = new GraphiteEffectModel();

    private final SerialRunner alarmUpdater = new SerialRunner();

    private boolean rpsEnabled = true;
    private boolean rpsActive = false;

    private boolean globalControlEnabled = false;

    /**
     * Global control is in a state where it is allowed to be turned on
     */
    private boolean globalControlRodsAvailable = true;

    /**
     * Global control has active control over selected rods
     */
    private boolean globalControlActive = false;

    /**
     * setpointNeutronFlux is following towards the target value
     */
    private boolean globalControlTransient = false;

    private boolean globalControlTarget = false;
    private boolean oldGlobalControlTarget;
    
    /**
     * Active while button is pressed.
     */
    private boolean globalControlOverride = false;
    private boolean globalControlOverridePositive = false;

    private int selectedAutoRods = 0;

    private AlarmState autoRodsPositionAlarmState;
    private AlarmState oldAutoRodsPositionAlarmState;

    ReactorCore() {
        globalControl = new PIControl();

        setpointTargetNeutronFlux = new Setpoint();
        setpointTargetNeutronFlux.initName("Reactor#TargetNeutronFlux");

        setpointPowerGradient = new Setpoint();
        setpointPowerGradient.initName("Reactor#SetpointPowerGradient");

        setpointNeutronFlux = new Setpoint();
        setpointNeutronFlux.initName("Reactor#SetpointNeutronFlux");
    }

    @Override
    public void run() {
        setpointTargetNeutronFlux.run();

        if (!globalControlEnabled) {
            setpointNeutronFlux.forceOutputValue(0.0);
            // Deactivate everything to force the oeprator to flip switches 
            // after enabling
            globalControlTransient = false;
            globalControlActive = false;
            globalControlTarget = false;
            globalControlOverride = false;
        } else {
            if (globalControlTarget) {
                setpointNeutronFlux.setInput(
                        setpointTargetNeutronFlux.getOutput());
            } else if (!globalControlActive || oldGlobalControlTarget) {
                // As long as control is off, set setpoint to current flux.
                // Also in case of global control is switched no longer to 
                // target, force the setpoint once to run back to the current
                // value.
                setpointNeutronFlux.forceOutputValue(
                        neutronFluxModel.getYNeutronFlux());
            }

            // Transient switch: Stops the setpoint integrator.
            if (globalControlTransient) {
                setpointNeutronFlux.setMaxRate(
                        setpointPowerGradient.getOutput());
            } else {
                setpointNeutronFlux.setMaxRate(0.0);
            }
        }

        setpointNeutronFlux.run();
        setpointPowerGradient.run();

        // Determine the average position of the automatic control rods and
        // use this value to set follow up and alarm values
        double avgPositionAutomatic = 0.0;
        selectedAutoRods = 0;
        for (ControlRod rod : controlRods) {
            // sum up avg positons for all rods which are in selected auto mode
            if (rod.getRodType() == ChannelType.AUTOMATIC_CONTROLROD
                    && rod.isAutomatic()) {
                avgPositionAutomatic += rod.getSwi().getOutput();
                selectedAutoRods += 1;
            }
        }
        if (selectedAutoRods >= 1) {
            globalControlRodsAvailable = true;
            avgPositionAutomatic = avgPositionAutomatic / selectedAutoRods;
        } else {
            globalControlRodsAvailable = false;
            avgPositionAutomatic = 7.4; // full inserted
        }

        // Generate average position and an alarm state value to make the 
        // operator aware that the controller might not be able to work
        if (selectedAutoRods >= 1 && globalControlActive
                && neutronFluxModel.getYNeutronFlux() >= 0.01) {
            if (avgPositionAutomatic < 0.4) {
                autoRodsPositionAlarmState = AlarmState.LOW2;
            } else if (avgPositionAutomatic < 1.0) {
                autoRodsPositionAlarmState = AlarmState.LOW1;
            } else if (avgPositionAutomatic >= 7.2) {
                autoRodsPositionAlarmState = AlarmState.MAX1;
                // All active auto rods fully inserted: no more control
                // possible, it should have been either deactivated or manual 
                // rods shoudl have been used to compensate. now its too late.
                triggerAutoShutdown();
            } else if (avgPositionAutomatic > 6.8) {
                autoRodsPositionAlarmState = AlarmState.HIGH2;
            } else if (avgPositionAutomatic > 6.5) {
                autoRodsPositionAlarmState = AlarmState.HIGH1;
            } else {
                autoRodsPositionAlarmState = AlarmState.NONE;
            }
        } else {
            autoRodsPositionAlarmState = AlarmState.NONE;
        }
        if (autoRodsPositionAlarmState != oldAutoRodsPositionAlarmState) {
            oldAutoRodsPositionAlarmState = autoRodsPositionAlarmState;
            alarmManager.fireAlarm("ReactorAutoRodsPosition",
                    autoRodsPositionAlarmState, false);
        }

        globalControl.setFollowUp(avgPositionAutomatic);
        globalControl.setManualMode(
                !globalControlActive || globalControlOverride);
        globalControl.run();

        // Write all controller outputs to the rods if they're in auto mode
        // and finally call run for all rods to update their positions (this is
        // the actual movement).
        for (ControlRod rod : controlRods) {
            // control of selected rods by the global control
            if (globalControlActive
                    && rod.getRodType() == ChannelType.AUTOMATIC_CONTROLROD
                    && rod.isAutomatic()) {
                if (globalControlOverride) {
                    if (globalControlOverridePositive) {
                        rod.getSwi().setInput(0);
                    } else {
                        rod.getSwi().setInput(7.4);
                    }
                } else {
                    rod.getSwi().setInput(globalControl.getOutput());
                }
            }

            rod.run(); // update all rods
            // Send new position values to GUI
            String propName = "Reactor#RodPosition" + rod.getIdentifier();
            outputValues.setParameterValue(propName, rod.getSwi().getOutput());
        }

        // Calculate total absorption and average rod position
        double absorption = 0.0;
        double totalPosition = 0.0;
        double displacerBoost = 0.0;
        for (ControlRod rod : controlRods) {
            absorption += rod.getAbsorption();
            if (rod.getRodType() == ChannelType.SHORT_CONTROLROD) {
                totalPosition += (7.3 - rod.getSwi().getOutput());
            } else {
                totalPosition += rod.getSwi().getOutput();
            }
            // sum up all displacer boost values
            displacerBoost += rod.getDisplacerBoost();
        }
        // Total position is the total sum of control rod pull lengts. 
        avgRodPosition = totalPosition / (double) controlRods.size();

        // Generate a 0..100 % value from total rod absorption first
        rodAbsorption = absorption / maxAbsorption * 100;

        // To trigger the accident, a displacer boost value is obtained from 
        // the control rods. The accident is represented by having too many 
        // manual control rods going from top to bottom at the same time,
        // triggering a prompt neutron excursion by exactly this. As it was 
        // stated in dyatlovs how it was book, the positive void coefficient 
        // was there but the automatic regulators did take care of this and 
        // before AZ-5 was pressed, nothing happend and everything was calm.
        // We implement this by having the displacerBoost value from the rods
        // but the effect of this will ony be used if more than 18 of the 28 
        // manual rods are withdrawn. The effect will be visible with 18 rods 
        // and be fully fatal at 25 rods. The boost is 1.0 max per rod, so we 
        // hide a value of 18.0 and cap it. 7 rods then will then result in 16 %
        // absorption decrease so it's 16/7=2.3 as a factor. This is not adding
        // reactivity but removing the absorption as we have a DT1-part in the
        // neturon flux model that will help to kick the prompt neutron 
        // excursion that way. The factor 3.0 was modified afterwards to make 
        // it more sure the excursion happens.
        rodAbsorption -= Math.max(0.0, displacerBoost - 18.0) * 3.0;

        /* This magic formula sets how the whole thing behaves. The reactivity
        * is given in same unit and dimension as the rods absorption, the 
        * difference is integrated by the neutron flux model to get the neutron
        * flux. We use unit %N as percentage of neturon flux here.
        * - The value of Absorption with all auto rods on 50 % and 4 man rods 
        *   out is 81.73 %. Those will be starting conditions for fresh start.
        *   This allows to pull 4 manual rods first and use the 4 auto rods at
        *   half pull distance to smootly control the initial reaction.
        * - The value of Absorption with all auto rods on 2.4 (about 70 % out)
        *   and 25 manual rods out (accident conditions) is 21.7 %
        * - Short control rods absorp 6.78 % if they are fully withdrawn (note 
        *   that they have an opposite direction!
        * - One manual control rod gives about 2.26 %N of absorption.
        * - coreTemp is given in °C and rises up to 570 °C in normal operation.
        *   A negative temperature coefficient is wanted with 2 rods so we'll
        *   have 2 * 2.26%N / 570 °C = 7.93e-3 %N/°C. However, it will be 
        *   limited to a certain value to not prevent a meltdown too much.
        * - There is a 5.5 % reduction of reactivity by the temperature 
        *   coefficient in thot reactor state
        *   81.73 % - 5.5 % = 76.23 % reactivity remaining with hot core
        * - On scheduled power drops (down to 50 % and down to almost 0) there
        *   will be about 135 % Xenon peak value.
        * - The graphite effect will go to 25 % and stay there on 50 % power. It
        *   will ramp up to almost 100 % on shutdown or when trying to reach the
        *   700 MW after a long period on 50 %.
        * - Therefore, 135 %Xe and 100 %Gr should consume 75 % of reactivity to
        *   make it possible to stall/choke the reactor, lets use those numbers
        *   equally and have a 235 % value for 75 % reactivity. 75/235 = 0.319
        * - There is a total of 28 manual control rods.
        * - Steam voiding should be tackled by the automatic rods without bigger
        *   issues. 5 * 2.2 % = 11 % roughly by auto rods, lets assume to have
        *   15 % voiding meaning 11 %N so its *0.73
         */
        reactivity = 81.73 // generally present reactivity.
                - xenonModel.getYXenon() * 0.319
                - graphiteModel.getYGraphie() * 0.319
                - Math.min(700, coreTemp) * 7.93e-3
                + voiding * 0.73;

        // For testing the accident conditions and trigger, set reactivity to
        // 28 instead of 81.73 and remove 25 manual rods (but NOT at the same
        // time!). use auto rods for getting k=1 and press AZ5
        // pass reactivity to and get the neutron flux from state space model.
        neutronFluxModel.setInputs(rodAbsorption, reactivity);
        neutronFluxModel.run();

        // Pass neutron flux to xenon model and generate xenon poisoning value.
        xenonModel.setInputs(neutronFluxModel.getYNeutronFlux());
        xenonModel.run();
        graphiteModel.setInputs(neutronFluxModel.getYNeutronFlux());
        graphiteModel.run();

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
        //    outputValues.setParameterValue("Reactor#RodAbsorption",
        //            rodAbsorption);
        outputValues.setParameterValue("Reactor#Xenon",
                xenonModel.getYXenon());
        outputValues.setParameterValue("Reactor#ThermalPower",
                neutronFluxModel.getYThermalPower());
        outputValues.setParameterValue("Reactor#k",
                neutronFluxModel.getYK());
        outputValues.setParameterValue("Reactor#Graphite",
                graphiteModel.getYGraphie());

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

        
        oldGlobalControlTarget = globalControlTarget;
    }

    /**
     * Receive events from GUI. This receives rod selection commands and other
     * commands.
     */
    @Override
    public void handleAction(ActionCommand ac) {
        ControlRod rod;
        if (setpointTargetNeutronFlux.handleAction(ac)) {
            return;
        } else if (setpointNeutronFlux.handleAction(ac)) {
            return;
        } else if (setpointPowerGradient.handleAction(ac)) {
            return;
        }
        if (!ac.getPropertyName().startsWith("Reactor#")) {
            return;
        }
        if (ac.getPropertyName().equals("Reactor#AZ5")) {
            shutdown();
            LOGGER.log(Level.INFO, "Received AZ-5 Command");
            return;
        }
        if (ac.getPropertyName().equals("Reactor#RPS")) {
            rpsEnabled = (boolean) ac.getValue();
            return;
        }
        if (ac.getPropertyName().equals("Reactor#RPSReset")) {
            // allow reset only after rods are in and there's no neutron flux.
            if (avgRodPosition >= 7.1
                    && neutronFluxModel.getYNeutronFlux() <= 0.5) {
                rpsActive = false;
            }
        }
        if (ac.getPropertyName().equals("Reactor#GlobalControlEnabled")) {
            if (!rpsActive) {
                globalControlEnabled = (boolean) ac.getValue();
            }
            return;
        }
        if (ac.getPropertyName().equals("Reactor#GlobalControlAuto")) {
            // Button press to start control operation
            if (globalControlRodsAvailable && globalControlEnabled) {
                globalControlActive = true;
                LOGGER.log(Level.INFO, "Global Control activated.");
            }
            return;
        }
        if (ac.getPropertyName().equals("Reactor#GlobalControlTransient")) {
            // do not accept enabling without enabled system, so its sure it 
            // never starts with transient.
            if (globalControlEnabled) {
                globalControlTransient = (boolean) ac.getValue();
                LOGGER.log(Level.INFO, "Global Control Transient: "
                        + globalControlTransient);
            }
            return;
        }
        if (ac.getPropertyName().equals("Reactor#GlobalControlTarget")) {
            // do not accept enabling without enabled system, so its sure it 
            // never starts with transient.
            if (globalControlEnabled) {
                globalControlTarget = (boolean) ac.getValue();
                LOGGER.log(Level.INFO, "Global Control Target: "
                        + globalControlTarget);
            }
            return;
        }
        if (ac.getPropertyName().equals("Reactor#AutoRodControl")) {
            // do not accept enabling without enabled system, so its sure it 
            // never starts with transient.
            if (globalControlEnabled && globalControlActive) {
                globalControlOverride = 0 != (int) ac.getValue();
                globalControlOverridePositive = 1 == (int) ac.getValue();
            }
            return;
        }
        int identifier, x, y;
        boolean value;
        switch (ac.getPropertyName()) {
            case "Reactor#RodSelect": // Manual selection of single rod
                identifier = (int) ac.getValue();
                x = identifier / 100;
                y = identifier % 100;
                rod = (ControlRod) getElement(x, y);
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
            case "Reactor#RodAutoEnable": // Manual selection of single rod
                identifier = (int) ac.getValue();
                x = identifier / 100;
                y = identifier % 100;
                rod = (ControlRod) getElement(x, y);
                rod.setAutomatic(true);
                break;
            case "Reactor#RodAutoDisable": // Manual selection of single rod
                identifier = (int) ac.getValue();
                x = identifier / 100;
                y = identifier % 100;
                rod = (ControlRod) getElement(x, y);
                rod.setAutomatic(false);
                break;
            case "Reactor#RodSelectAllManual": // not used anymore, bad idea
                for (ControlRod cRod : controlRods) {
                    if (cRod.getRodType() == ChannelType.MANUAL_CONTROLROD
                            && !cRod.isSelected()) {
                        cRod.setSelected(true);
                        // Sent selection of this rod to GUI:
                        controller.propertyChange(
                                new PropertyChangeEvent(this,
                                        "Reactor#RodSelected",
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
                                new PropertyChangeEvent(this,
                                        "Reactor#RodSelected",
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
                                new PropertyChangeEvent(this,
                                        "Reactor#RodSelected",
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
                                new PropertyChangeEvent(this,
                                        "Reactor#RodDeselected",
                                        null, cRod.getIdentifier()));
                    }
                }
                break;
            case "Reactor#IncreaseRodSpeed":
                if (rpsActive) {
                    LOGGER.log(Level.INFO, "Command refused due to RPS active");
                    return;
                }
                for (ControlRod cRod : controlRods) {
                    if (cRod.isSelected()) {
                        cRod.rodSpeedIncrease();
                    }
                }
                break;
            case "Reactor#DecreaseRodSpeed":
                if (rpsActive) {
                    LOGGER.log(Level.INFO, "Command refused due to RPS active");
                    return;
                }
                for (ControlRod cRod : controlRods) {
                    if (cRod.isSelected()) {
                        cRod.rodSpeedDecrease();
                    }
                }
                break;
            case "Reactor#RodStop":
                if (rpsActive) {
                    LOGGER.log(Level.INFO, "Command refused due to RPS active");
                    return;
                }
                // This command stops all selected control rods 
                for (ControlRod cRod : controlRods) {
                    if (cRod.isSelected()) {
                        cRod.getSwi().setStop();
                    }
                }
                break;
            case "Reactor#RodManualUp":
                if (rpsActive) {
                    LOGGER.log(Level.INFO, "Command refused due to RPS active");
                    return;
                }
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
                if (rpsActive) {
                    LOGGER.log(Level.INFO, "Command refused due to RPS active");
                    return;
                }
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

    public void init() {
        // Auto Control is limited between 4 and 110 % flux
        setpointNeutronFlux.setLowerLimit(0.0);
        setpointNeutronFlux.setUpperLimit(120.0);
        setpointTargetNeutronFlux.setLowerLimit(4.0);
        setpointTargetNeutronFlux.setUpperLimit(110.0);
        setpointTargetNeutronFlux.setMaxRate(2.0);

        // Define controler input for automatic rods. e = -(setpoing - flux)
        // is negative, inserting rods means positive output values, removing is
        // negtive.
        // Use a limitation for positive or negative neutron flux rate on the
        // controller input value to prevent scram or shutoff.
        globalControl.addInputProvider(()
                -> - // Limit negative neutron rate
                Math.max(-11 * (neutronFluxModel
                        .getYNeutronRateFiltered() + 2.3),
                        // Limit positive neutron rate
                        Math.min(-11 * (neutronFluxModel
                                .getYNeutronRateFiltered() - 2.4),
                                (setpointNeutronFlux.getOutput()
                                - neutronFluxModel.getYNeutronFlux()))));

        globalControl.setMaxOutput(7.4);
        globalControl.setParameterK(2.0);
        globalControl.setParameterTN(10);

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

        setpointPowerGradient.setUpperLimit(1.4);
        setpointPowerGradient.setMaxRate(0.4);
        setpointPowerGradient.forceOutputValue(0.3); // initial value

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
        am.defineAlarm(3.8, AlarmState.MAX1);
        am.defineAlarm(3.0, AlarmState.HIGH2);
        am.defineAlarm(2.6, AlarmState.HIGH1);
        am.defineAlarm(-2.6, AlarmState.LOW1);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                triggerAutoShutdown();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);
    }

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

    /**
     * Can be invoked by this or externally, this is the RPS (reactor protection
     * system) shutdown command that is supposed to turn off the reactor. It can
     * be overridden however.
     */
    public void triggerAutoShutdown() {
        if (!rpsEnabled) {
            return;
        }
        shutdown();
        rpsActive = true;
    }

    /**
     * Shutdown makes all rods move into the core immediately with maximum speed
     * (this is the AZ5 command).
     */
    private void shutdown() {
        if (globalControlEnabled) {
            LOGGER.log(Level.INFO, "Deactivated Global Control (Shutdown)");
        }
        globalControlEnabled = false;
        for (ControlRod c : controlRods) {
            c.setAutomatic(false);
            c.rodSpeedMax();
            if (c.getRodType() == ChannelType.SHORT_CONTROLROD) {
                c.getSwi().setInputMin(); // those need to be pulled out
            } else {
                c.getSwi().setInputMax();
            }
        }
    }

    @Override
    public void registerController(ModelListener controller) {
        super.registerController(controller);
        globalControl.addPropertyChangeListener(controller);
    }

    @Override
    public void registerParameterOutput(ParameterHandler output) {
        super.registerParameterOutput(output);
        setpointTargetNeutronFlux.registerParameterHandler(output);
        setpointNeutronFlux.registerParameterHandler(output);
        setpointPowerGradient.registerParameterHandler(output);
    }
}
