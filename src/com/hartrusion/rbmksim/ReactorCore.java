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
import com.hartrusion.control.AutomationRunner;
import com.hartrusion.control.ControlCommand;
import com.hartrusion.control.PIControl;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import com.hartrusion.values.ValueHandler;
import com.hartrusion.control.SerialRunner;
import com.hartrusion.control.Setpoint;
import com.hartrusion.control.ValveState;
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

    private boolean coreOnlySimulation = false;

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

    /**
     * Holds the state of the Reactor protection system, uses the ControlCommand
     * which is used usually by control loops as a variable.
     */
    private ControlCommand rps = ControlCommand.AUTOMATIC;
    private ControlCommand oldRps = null;

    /**
     * Active in terms of not allowing the reactor to go critical.
     */
    private boolean rpsActive = false;
    private boolean oldRpsActive = false; // previous value

    private boolean globalControlEnabled = false;
    private boolean oldGlobalControlEnabled = true; // previous value

    /**
     * Global control is in a state where it is allowed to be turned on
     */
    private boolean globalControlRodsAvailable = true;

    /**
     * Global control has active control over selected rods
     */
    private boolean globalControlActive = false;
    private boolean oldGlobelControlActive = true; // previous value

    /**
     * setpointNeutronFlux is following towards the target value
     */
    private boolean globalControlTransient = false;
    private boolean oldGlobalControlTransient = true; // previous value

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

    /**
     * Use ValveState enum to describe the auto rods position, that way the
     * lights from the switch widgets can be controlled.
     */
    private ValveState autoRodsPositionState;
    private ValveState oldAutoRodsPositionState;

    private final AutomationRunner runner = new AutomationRunner();

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
        double avgPositionActiveAutomatic = 0.0;
        selectedAutoRods = 0;
        for (ControlRod rod : controlRods) {
            // sum up avg positons for all rods which are in selected auto mode
            if (rod.getRodType() == ChannelType.AUTOMATIC_CONTROLROD
                    && rod.isAutomatic()) {
                avgPositionActiveAutomatic += rod.getSwi().getOutput();
                selectedAutoRods += 1;
            }
        }
        if (selectedAutoRods >= 1) {
            globalControlRodsAvailable = true;
            avgPositionActiveAutomatic
                    = avgPositionActiveAutomatic / selectedAutoRods;
        } else {
            globalControlRodsAvailable = false;
            avgPositionActiveAutomatic = Double.NaN; // no value available
        }

        // Generate average position and an alarm state value to make the 
        // operator aware that the controller might not be able to work
        if (selectedAutoRods >= 1 && globalControlActive
                && neutronFluxModel.getYNeutronFlux() >= 0.01) {
            if (avgPositionActiveAutomatic < 0.4) {
                autoRodsPositionAlarmState = AlarmState.LOW2;
            } else if (avgPositionActiveAutomatic < 1.0) {
                autoRodsPositionAlarmState = AlarmState.LOW1;
            } else if (avgPositionActiveAutomatic >= 7.2) {
                autoRodsPositionAlarmState = AlarmState.MAX1;
                // All active auto rods fully inserted: no more control
                // possible, it should have been either deactivated or manual 
                // rods shoudl have been used to compensate. now its too late.
                triggerAutoShutdown();
            } else if (avgPositionActiveAutomatic > 6.8) {
                autoRodsPositionAlarmState = AlarmState.HIGH2;
            } else if (avgPositionActiveAutomatic > 6.5) {
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

        // Position lights on GUI:
        if (selectedAutoRods >= 1 && globalControlEnabled) {
            if (avgPositionActiveAutomatic < 1.4) {
                autoRodsPositionState = ValveState.OPEN;
            } else if (avgPositionActiveAutomatic > 6.0) {
                autoRodsPositionState = ValveState.CLOSED;
            } else {
                autoRodsPositionState = ValveState.INTERMEDIATE;
            }
        } else {
            autoRodsPositionState = ValveState.INTERMEDIATE;
        }
        // send a fake valve position enumeration for the indicator lights
        if (oldAutoRodsPositionState != autoRodsPositionState) {
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Reactor#AutoRodControl_Pos",
                    oldAutoRodsPositionState, autoRodsPositionState));
            oldAutoRodsPositionState = autoRodsPositionState;
        }

        if (Double.isFinite(avgPositionActiveAutomatic)) {
            globalControl.setFollowUp(avgPositionActiveAutomatic);
        }
        globalControl.setManualMode(
                !globalControlActive || globalControlOverride);
        globalControl.run();

        // Do a run of the affection calculation, each rod will distribute its
        // value to the surrounding fuel elemts when rod.run is called.
        for (FuelElement f : fuelElements) {
            f.prepareAffectionCalculation();
        }

        // Write all controller outputs to the rods if they're in auto mode
        // and finally call run for all rods to update their positions (this is
        // the actual movement).
        for (ControlRod rod : controlRods) {
            // control of selected rods by the global control
            if (globalControlActive
                    && rod.getRodType() == ChannelType.AUTOMATIC_CONTROLROD
                    && rod.isAutomatic()) {
                if (globalControlOverride) {
                    // The press of the override button will move the rods
                    // with current speed while this button is held down, the 
                    // controller will be in followup mode and set to the 
                    // position output.
                    if (globalControlOverridePositive) {
                        rod.getSwi().setInput(0);
                    } else {
                        rod.getSwi().setInput(7.4);
                    }
                } else {
                    rod.getSwi().setInput(globalControl.getOutput());
                }
            } else if (!globalControlEnabled
                    && rod.getRodType() == ChannelType.AUTOMATIC_CONTROLROD
                    && rod.isAutomatic()) {
                // disable automatic mode selection for all rods when
                // global control is disabled. This means the operator has to
                // re-select them each time.
                rod.setAutomatic(false);
            }
            // To stop the rods from working in case of propmt excursion, we 
            // just stop running it so the values stay the same and no more 
            // updates are sent. That way thes simply stop working.
            if (neutronFluxModel.isReactorIntact()) {
                rod.run(); // update all rods
            }
        }

        for (FuelElement f : fuelElements) {
            f.finalizeAffection();
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
        // but ony if the reactor is intact.
        if (neutronFluxModel.isReactorIntact()) {
            xenonModel.setInputs(neutronFluxModel.getYNeutronFlux());
            xenonModel.run();
            graphiteModel.setInputs(neutronFluxModel.getYNeutronFlux());
            graphiteModel.run();
        }

        alarmUpdater.invokeAll();

        // Send the RPS state to controller
        if (rps != oldRps) {
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Reactor#RPSState", oldRps, rps));
            oldRps = rps;
        }

        // Send the RPS alarm message on change
        if (rpsActive != oldRpsActive) {
            oldRpsActive = rpsActive;
            if (rpsActive) {
                alarmManager.fireAlarm("ReactorProtection",
                        AlarmState.ACTIVE, false);
            } else {
                alarmManager.fireAlarm("ReactorProtection",
                        AlarmState.NONE, false);
            }
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Reactor#ProtectionLock", oldRpsActive, rpsActive));
        }

        // Make property change events that describe the current state of the
        // global control
        if (globalControlEnabled != oldGlobalControlEnabled) {
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Reactor#GlobalControlEnabled",
                    oldGlobalControlEnabled, globalControlEnabled));
            oldGlobalControlEnabled = globalControlEnabled;
        }
        if (globalControlActive != oldGlobelControlActive) {
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Reactor#GlobalControlActive",
                    oldGlobelControlActive, globalControlActive));
            oldGlobelControlActive = globalControlActive;
        }
        if (globalControlTransient != oldGlobalControlTransient) {
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Reactor#GlobalControlTransient",
                    oldGlobalControlTransient, globalControlTransient));
            oldGlobalControlTransient = globalControlTransient;
        }
        if (globalControlTarget != oldGlobalControlTarget) {
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Reactor#GlobalControlTarget",
                    oldGlobalControlTarget, globalControlTarget));
            oldGlobalControlTarget = globalControlTarget;
        }

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
        outputValues.setParameterValue("Reactor#Reactivity",
                neutronFluxModel.getYReactivity());
        outputValues.setParameterValue("Reactor#Graphite",
                graphiteModel.getYGraphie());

        outputValues.setParameterValue("GlobalControl#AvgActiveAutoRodsPos",
                avgPositionActiveAutomatic);

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
        if (ac.getPropertyName().equals("SetCoreOnly")) {
            coreOnlySimulation = true;
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
            // translate boolean on/off into ControlCommand state
            if ((boolean) ac.getValue()) {
                rps = ControlCommand.AUTOMATIC;
            } else {
                rps = ControlCommand.MANUAL_OPERATION;
            }
            if (rps.equals(ControlCommand.MANUAL_OPERATION)) {
                rpsActive = false; // skip the need for reset when turning off
            } else {
                // When switching on, check conditions if reactor can be
                // started up, otherwise block immediately.
                if (!checkRpsDisengage()) {
                    rpsActive = true;
                    shutdown();
                }
            }
            return;
        }
        if (ac.getPropertyName().equals("Reactor#RPSReset")) {
            // allow reset only after rods are in and there's no neutron flux.
            if (checkRpsDisengage()) {
                rpsActive = false;
            } else {
                LOGGER.log(Level.INFO, "Command refused, RPS still active.");
            }
        }
        if (ac.getPropertyName().equals("Reactor#GlobalControlEnabled")) {
            if (!rpsActive) {
                globalControlEnabled = (boolean) ac.getValue();
            } else if ((boolean) ac.getValue()) {
                // rps is present and trying to switch on global control:
                LOGGER.log(Level.INFO, "Command refused due to RPS active");
            }
            return;
        }
        if (ac.getPropertyName().equals("Reactor#GlobalControlAuto")) {
            // Button press to start control operation
            if (globalControlRodsAvailable && globalControlEnabled) {
                globalControlActive = true;
                LOGGER.log(Level.INFO, "Global Control activated.");
            } else {
                LOGGER.log(Level.INFO, "Command refused (not enabled or no"
                        + " rods in auto mode).");
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
            } else {
                LOGGER.log(Level.INFO, "Command refused (Global Control not "
                        + "enabled.");
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
            } else {
                LOGGER.log(Level.INFO, "Command refused (Global Control not "
                        + "enabled.");
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
                break;
            case "Reactor#RodAutoEnable": // Global control Auto selection
                if (!globalControlEnabled) {
                    break;
                }
                identifier = (int) ac.getValue();
                x = identifier / 100;
                y = identifier % 100;
                rod = (ControlRod) getElement(x, y);
                rod.setAutomatic(true);
                break;
            case "Reactor#RodAutoDisable": // Global control Auto selection
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
                    }
                }
                break;
            case "Reactor#RodSelectAllAutomatic":
                for (ControlRod cRod : controlRods) {
                    if (cRod.getRodType() == ChannelType.AUTOMATIC_CONTROLROD
                            && !cRod.isSelected()) {
                        cRod.setSelected(true);
                    }
                }
                break;
            case "Reactor#RodSelectAllShort":
                for (ControlRod cRod : controlRods) {
                    if (cRod.getRodType() == ChannelType.SHORT_CONTROLROD
                            && !cRod.isSelected()) {
                        cRod.setSelected(true);
                    }
                }
                break;
            case "Reactor#RodSelectNone":
                for (ControlRod cRod : controlRods) {
                    if (cRod.isSelected()) {
                        cRod.setSelected(false);
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
        neutronFluxModel.setInitialConditions(100, 80.144, 0);

        // Auto Control is limited between 4 and 110 % flux
        setpointNeutronFlux.setLowerLimit(0.0);
        setpointNeutronFlux.setUpperLimit(120.0);
        setpointTargetNeutronFlux.setLowerLimit(4.0);
        setpointTargetNeutronFlux.setUpperLimit(110.0);
        setpointTargetNeutronFlux.setMaxRate(2.0);

        runner.submit(setpointNeutronFlux);
        runner.submit(setpointTargetNeutronFlux);

        // Define controler input for automatic rods. e = -(setpoing - flux)
        // is negative, inserting rods means positive output values, removing is
        // negtive.
        // Use a limitation for positive or negative neutron flux rate on the
        // controller input value to prevent scram or shutoff.
        globalControl.addInputProvider(()
                -> - // Limit negative neutron rate
                Math.max(-11 * (neutronFluxModel
                        .getYNeutronRateFiltered() + 3.8),
                        // Limit positive neutron rate
                        Math.min(-11 * (neutronFluxModel
                                .getYNeutronRateFiltered() - 4.5),
                                (setpointNeutronFlux.getOutput()
                                - neutronFluxModel.getYNeutronFlux()))));

        globalControl.setMaxOutput(7.4);
        globalControl.setParameterK(2.5);
        globalControl.setParameterTN(20);
        globalControl.setName("Reactor#GlobalControl");

        int idx, jdx;

        // Initialize some fast access arrays so we dont need to search in
        // the arraylist if both indizies are given.
        for (idx = 0; idx < ChannelData.LENGTH; idx++) {
            for (jdx = 0; jdx < ChannelData.LENGTH; jdx++) {
                rodIndex[idx][jdx] = -1;
                fuelIndex[idx][jdx] = -1;
            }
        }

        // Iterate over known core structure and create rods and fuel
        for (idx = ChannelData.MIN_NUMBER; idx <= ChannelData.MAX_NUMBER; idx++) {
            for (jdx = ChannelData.MIN_NUMBER; jdx <= ChannelData.MAX_NUMBER; jdx++) {
                switch (ChannelData.getChannelType(idx, jdx)) {
                    case FUEL -> {
                        fuelIndex[idx - ChannelData.MIN_NUMBER][jdx - ChannelData.MIN_NUMBER] = fuelElements.size();
                        fuelElements.add(new FuelElement(idx, jdx));
                    }
                    case AUTOMATIC_CONTROLROD -> {
                        rodIndex[idx - ChannelData.MIN_NUMBER][jdx - ChannelData.MIN_NUMBER] = controlRods.size();
                        controlRods.add(new ControlRod(
                                idx, jdx, ChannelType.AUTOMATIC_CONTROLROD));
                        controlRods.get(controlRods.size() - 1).setAffectionRadius(4.5);
                    }
                    case SHORT_CONTROLROD -> {
                        rodIndex[idx - ChannelData.MIN_NUMBER][jdx - ChannelData.MIN_NUMBER] = controlRods.size();
                        controlRods.add(new ControlRod(
                                idx, jdx, ChannelType.SHORT_CONTROLROD));
                        controlRods.get(controlRods.size() - 1).setAffectionRadius(8.2);
                    }
                    case MANUAL_CONTROLROD -> {
                        rodIndex[idx - ChannelData.MIN_NUMBER][jdx - ChannelData.MIN_NUMBER] = controlRods.size();
                        controlRods.add(new ControlRod(
                                idx, jdx, ChannelType.MANUAL_CONTROLROD));
                        controlRods.get(controlRods.size() - 1).setAffectionRadius(3.8);
                    }
                }
            }
        }

        // Sum up to get the max possible absorption with all rods inserted.
        for (ControlRod rod : controlRods) {
            maxAbsorption += rod.getMaxAbsorption();
        }

        for (ControlRod rod : controlRods) {
            rod.initAffection(fuelElements);
        }

        setpointPowerGradient.setUpperLimit(0.35);
        setpointPowerGradient.setMaxRate(0.1);
        setpointPowerGradient.forceOutputValue(0.2); // initial value

        runner.submit(setpointPowerGradient);

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
        am.defineAlarm(5.5, AlarmState.MAX1);
        am.defineAlarm(4.0, AlarmState.HIGH2);
        am.defineAlarm(2.5, AlarmState.HIGH1);
        am.defineAlarm(-2.5, AlarmState.LOW1);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                triggerAutoShutdown();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("Reactivity");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return neutronFluxModel.getYReactivity();
            }
        });
        am.defineAlarm(0.0034, AlarmState.MAX1);
        am.defineAlarm(0.0026, AlarmState.HIGH2);
        am.defineAlarm(0.0018, AlarmState.HIGH1);
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
        if (rps.equals(ControlCommand.MANUAL_OPERATION)) {
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
                // Short control rods need to be pushed upwards into the core.
                // As they do not have any effect in positions lower than 3.0
                // meters, they will not be moved but stopped in their 
                // current position. 
                if (c.getSwi().getOutput() <= 2.8) {
                    c.getSwi().setStop();
                } else {
                    c.getSwi().setInputMin(); // pull upwards
                }
            } else {
                c.getSwi().setInputMax(); // fully insert
            }
        }
    }

    /**
     * Checks the plant state that it is in a state where it allows the RPS to
     * be cleared. Note that the Alarm system is a trigger-event only, if an
     * alarm does shut down the reactor, it does not prevent it to be
     * reactivated immediately. Those checks have to be made separately here.
     *
     * @return true if everything is fine.
     */
    private boolean checkRpsDisengage() {
        if (!coreOnlySimulation) {
            if (alarmManager.isAlarmActive("Loop1Flow", AlarmState.MIN1)) {
                return false;
            }
            if (alarmManager.isAlarmActive("Loop2Flow", AlarmState.MIN1)) {
                return false;
            }
            if (alarmManager.isAlarmActive("Feed1Pressure", AlarmState.MIN1)) {
                return false;
            }
            if (alarmManager.isAlarmActive("Feed2Pressure", AlarmState.MIN1)) {
                return false;
            }
        }
        // Checks that will only be performed when not switchting the RPS back 
        // on during already running operator. Those are designed to make sure
        // some other state must be reached first befroe able to reset the RPS
        if (!oldRps.equals(ControlCommand.MANUAL_OPERATION)) {
            if (avgRodPosition < 7.1) {
                return false; // rods not fully inserted
            }
            if (neutronFluxModel.getYNeutronFlux() > 0.2) {
                return false; // chain reaction still active
            }
        }
        return true;
    }

    @Override
    public void registerController(ModelListener controller) {
        // Will be called after init() - note that it is the other way round in
        // the ThermalLayout!
        super.registerController(controller);
        globalControl.addPropertyChangeListener(controller);
        for (ControlRod r : controlRods) {
            r.registerSignalListener(controller);
        }
    }

    @Override
    public void registerParameterOutput(ValueHandler output) {
        super.registerParameterOutput(output);
        setpointTargetNeutronFlux.registerParameterHandler(output);
        setpointNeutronFlux.registerParameterHandler(output);
        setpointPowerGradient.registerParameterHandler(output);
        for (ControlRod r : controlRods) {
            r.registerValueHandler(output);
        }
    }

    public NeutronFluxModel getNeutronModel() {
        return neutronFluxModel;
    }

    @Override
    public void saveTo(SaveGame save) {
        // Generate a save object containint the reactor state
        ReactorState s = new ReactorState();
        for (int idx = 0; idx < 9; idx++) {
            s.setxNeutronFluxModel(
                    neutronFluxModel.getStateSpaceVariable(idx), idx);
        }
        for (int idx = 0; idx < 3; idx++) {
            s.setxXenonModel(
                    xenonModel.getStateSpaceVariable(idx), idx);
        }
        for (int idx = 0; idx < 3; idx++) {
            s.setxGraphiteModel(
                    graphiteModel.getStateSpaceVariable(idx), idx);
        }
        s.setRps(rps);
        s.setRpsActive(rpsActive);
        s.setGlobalControlEnabled(globalControlEnabled);
        s.setGlobalControlActive(globalControlActive);
        s.setGlobalControlTransient(globalControlTransient);
        s.setGlobalControlTarget(globalControlTarget);
        s.setCoreTemp(coreTemp);
        s.setVoiding(voiding);

        // This control object is not part of an assembly class so we need to 
        // save the integral part manually.
        s.setGlobalControlInputValue(globalControl.getInput());
        s.setGlobalControlOutputValue(globalControl.getOutput());

        for (ControlRod r : controlRods) {
            // generate RodState object and pass it to each control rod.
            RodState rs = new RodState();
            r.writeToRodStateObject(rs);
            s.getRodStates().add(rs);
        }

        save.addReactorState(s);

        // Add setpoint object properties
        save.addRunnerState("reactorRunner",
                runner.getCurrentAutomationCondition());
    }

    @Override
    public void load(SaveGame save) {
        ReactorState rs = save.getReactorState();

        coreOnlySimulation = save.isCoreOnlySimulation();
        for (int idx = 0; idx < 9; idx++) {
            neutronFluxModel.setStateSpaceVariable(idx, rs.getxNeutronFluxModel(idx));
        }
        for (int idx = 0; idx < 3; idx++) {
            xenonModel.setStateSpaceVariable(idx, rs.getxXenonModel(idx));
        }
        for (int idx = 0; idx < 3; idx++) {
            graphiteModel.setStateSpaceVariable(idx, rs.getxGraphiteModel(idx));
        }
        rps = rs.getRps();
        rpsActive = rs.isRpsActive();
        globalControlEnabled = rs.isGlobalControlEnabled();
        globalControlActive = rs.isGlobalControlActive();
        globalControlTransient = rs.isGlobalControlTransient();
        globalControlTarget = rs.isGlobalControlTarget();
        // those values get assigned from previous cycle from thermal layout.
        // as there is no previous cycle, we need to restore them.
        coreTemp = rs.getCoreTemp();
        voiding = rs.getVoiding();

        globalControl.acSetCondition(
                rs.getGlobalControlInputValue(),
                rs.getGlobalControlOutputValue());

        for (int idx = 0; idx < controlRods.size(); idx++) {
            // get RodState object and pass it to each control rod.
            controlRods.get(idx).applyRodState(rs.getRodStates().get(idx));
        }

        // sot old-variables to trigger all those property change events.
        oldGlobalControlTransient = !globalControlTransient;
        oldGlobalControlTarget = !globalControlTarget;
        oldGlobelControlActive = !globalControlActive;
        oldGlobalControlEnabled = !globalControlEnabled;
        oldRpsActive = !rpsActive;
        oldRps = null;
        oldAutoRodsPositionAlarmState = null;
        oldAutoRodsPositionState = null;

        // write back setpoint object states
        runner.setRunnablesAutomationCondition(
                save.getRunnerState("reactorRunner"));
    }
}
