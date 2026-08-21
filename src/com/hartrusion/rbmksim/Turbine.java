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
package com.hartrusion.rbmksim;

import com.hartrusion.alarm.AlarmAction;
import com.hartrusion.alarm.AlarmState;
import com.hartrusion.alarm.AlarmUpdater;
import com.hartrusion.alarm.ValueAlarmMonitor;
import com.hartrusion.control.AutomationRunner;
import com.hartrusion.control.ControlCommand;
import com.hartrusion.control.Setpoint;
import com.hartrusion.modeling.PhysicalDomain;
import com.hartrusion.modeling.automated.DummyValve;
import com.hartrusion.modeling.general.ClosedOrigin;
import com.hartrusion.modeling.general.EffortSource;
import com.hartrusion.modeling.general.FlowSource;
import com.hartrusion.modeling.general.GeneralNode;
import com.hartrusion.modeling.general.LinearDissipator;
import com.hartrusion.modeling.general.MutualCapacitance;
import com.hartrusion.modeling.general.OpenOrigin;
import com.hartrusion.modeling.general.SelfCapacitance;
import com.hartrusion.modeling.solvers.DomainAnalogySolver;
import com.hartrusion.mvc.ActionCommand;
import com.hartrusion.mvc.ModelListener;
import com.hartrusion.values.ValueHandler;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the turbine and its systems. Holds a network model with the
 * thermal part that represents the masses that get heated up. The model is
 * connected and solved in the ThermalLayout class, the instantiation of the
 * elements is done here.
 *
 * @author Viktor Alexander Hartung
 */
public class Turbine extends Subsystem implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(
            Turbine.class.getName());

    private double hpRotorTemperature;
    private double hpStatorTemperature;
    private double lpRotorTemperature;
    private double lpStatorTemperature;

    /**
     * Length of the High Pressure turbine used for length expansion calculation
     */
    private static final double HP_LENGTH = 6.0;

    /**
     * Length of the Low Pressure turbine used for length expansion calculation
     */
    private static final double LP_LENGTH = 17.0;

    /**
     * Length expansion factor that is used to calculate the length difference
     * from the turbines metal temperature, given in mm/K/m
     */
    private static final double EXP_C = 1.2e-2;

    private double hpDiffExpansion;
    private double lpDiffExpansion;
    private double statorAbsExpansion;

    /**
     * Reference to the thermal layout process class
     */
    private ThermalLayout process;

    // <editor-fold defaultstate="collapsed" desc="Model elements declaration and array instantiation">
    private final OpenOrigin[] thermalOrigin = new OpenOrigin[4];
    private final GeneralNode[] thermalNodeZero = new GeneralNode[4];
    private final EffortSource[] thermalSteamTemperature = new EffortSource[4];
    private final GeneralNode[] thermalNodeSteamTemperature = new GeneralNode[4];
    private final LinearDissipator[] thermalResistanceSteamToStator = new LinearDissipator[4];
    private final LinearDissipator[] thermalResistanceSteamToRotor = new LinearDissipator[4];
    private final GeneralNode[] thermalNodeStator = new GeneralNode[4];
    private final SelfCapacitance[] thermalMassStator = new SelfCapacitance[4];
    private final GeneralNode[] thermalNodeRotor = new GeneralNode[4];
    private final SelfCapacitance[] thermalMassRotor = new SelfCapacitance[4];
    private final GeneralNode[] thermalNodeEnvTemperature = new GeneralNode[4];
    private final EffortSource[] thermalEnvTemperature = new EffortSource[4];
    private final LinearDissipator[] thermalResistanceStatorToEnv = new LinearDissipator[4];

    private final ClosedOrigin turbineOrigin;
    private final GeneralNode turbineReference;
    private final GeneralNode turbineVelocity;
    private final FlowSource turbineMomentum;
    private final FlowSource turbineTurningGear;
    private final MutualCapacitance turbineInertia;
    private final LinearDissipator turbineFriction;

    private final FlowSource[] oilPump = new FlowSource[2];
    private final FlowSource oilPumpTurbine;
    private final LinearDissipator oilFlow;
    private final GeneralNode oilGnd;
    private final ClosedOrigin oilOrigin;
    private final GeneralNode oilPressure;
    private final MutualCapacitance oilPressureDelay;

    // </editor-fold>
    private double targetTurbineSpeed = 0.0;
    private final Setpoint setpointTurbineSpeed;
    private SpeedSelect setpointSpeedGradient = SpeedSelect.MED;
    private SpeedSelect oldSetpointSpeedGradient = null;

    /**
     * Holds the state of the Turbine protection system, uses the ControlCommand
     * which is used usually by control loops as a variable.
     */
    private ControlCommand tps = ControlCommand.AUTOMATIC;
    private ControlCommand oldTps = null;

    /**
     * Active in terms of not allowing the turbine valves to open.
     */
    private boolean tpsActive = false;
    private boolean oldTpsActive = false; // previous value

    /**
     * Value from thermal model, this is the value provided by the steam part
     * given in Megawatts. The number is a high estimate.
     */
    private double shaftPower;

    private final DomainAnalogySolver rotorSolver = new DomainAnalogySolver();

    private final AlarmUpdater alarmUpdater = new AlarmUpdater();

    private boolean generatorSynched = false;
    private boolean oldGeneratorSynched = true;

    /**
     * Angle between generator and grid, given in value between 0..2*PI
     */
    private double syncAngle = 0.0;

    /**
     * Power from the steam part that is required to spin the turbine exactly at
     * 3000 1/min.
     */
    private final double HOLD_POWER = 35.0;

    private double generatorPower = 0.0;

    private boolean speedSetpointFollowup;

    /**
     * State of the turning gear mechanism. 0: offline, 1: ready, 2: active.
     */
    private int turningGearState = 2;
    private int oldTurningGearState = -1;

    /**
     * Represents the state of the hydraulic control pumps, just a switch. The
     * valve state is used to determine the pump state on the GUI.
     */
    private DummyValve hydraulic;

    /**
     * Represents the state of two auxiliary oil pumps for lube oil.
     */
    private DummyValve[] lubeOilPump = new DummyValve[2];

    private final DomainAnalogySolver oilSolver = new DomainAnalogySolver();

    private final AutomationRunner runner = new AutomationRunner();
    
    /**
     * Flow in kg/s for each active auxiliary lube oil pump
     */
    private static final double OIL_FLOW_AUX = 13.5;

    Turbine() {
        // <editor-fold defaultstate="collapsed" desc="Model elements instantiation">
        for (int idx = 0; idx < 4; idx++) {
            thermalOrigin[idx] = new OpenOrigin(PhysicalDomain.THERMAL);
            thermalOrigin[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalOriginSteam");
            thermalNodeZero[idx] = new GeneralNode(PhysicalDomain.THERMAL);
            thermalNodeZero[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalNodeSteamGnd");
            thermalSteamTemperature[idx] = new EffortSource(PhysicalDomain.THERMAL);
            thermalSteamTemperature[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalSteamTemperature");
            thermalNodeSteamTemperature[idx] = new GeneralNode(PhysicalDomain.THERMAL);
            thermalNodeSteamTemperature[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalNodeSteamTemperature");
            thermalResistanceSteamToStator[idx] = new LinearDissipator(PhysicalDomain.THERMAL);
            thermalResistanceSteamToStator[idx].setName("Turbine"
                    + (idx + 1) + "#ResistanceSteamToStator");
            thermalResistanceSteamToRotor[idx] = new LinearDissipator(PhysicalDomain.THERMAL);
            thermalResistanceSteamToRotor[idx].setName("Turbine"
                    + (idx + 1) + "#ResistanceSteamToRotor");
            thermalNodeStator[idx] = new GeneralNode(PhysicalDomain.THERMAL);
            thermalNodeStator[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalNodeStator");
            thermalMassStator[idx] = new SelfCapacitance(PhysicalDomain.THERMAL);
            thermalMassStator[idx].setName("Turbine"
                    + (idx + 1) + "#MassStator");
            thermalNodeRotor[idx] = new GeneralNode(PhysicalDomain.THERMAL);
            thermalNodeRotor[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalNodeRotor");
            thermalMassRotor[idx] = new SelfCapacitance(PhysicalDomain.THERMAL);
            thermalMassRotor[idx].setName("Turbine"
                    + (idx + 1) + "#MassRotor");
            thermalNodeEnvTemperature[idx] = new GeneralNode(PhysicalDomain.THERMAL);
            thermalNodeEnvTemperature[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalNodeEnvTemperature");
            thermalEnvTemperature[idx] = new EffortSource(PhysicalDomain.THERMAL);
            thermalEnvTemperature[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalEnvTemperature");
            thermalResistanceStatorToEnv[idx] = new LinearDissipator(PhysicalDomain.THERMAL);
            thermalResistanceStatorToEnv[idx].setName("Turbine"
                    + (idx + 1) + "#ResistanceStatorToEnv");
        }

        // Turbine and Generator Rotor mechanical model
        turbineOrigin = new ClosedOrigin(PhysicalDomain.MECHANICAL);
        turbineOrigin.setName("Rotor#Origin");
        turbineReference = new GeneralNode(PhysicalDomain.MECHANICAL);
        turbineReference.setName("Rotor#Reference");
        turbineVelocity = new GeneralNode(PhysicalDomain.MECHANICAL);
        turbineVelocity.setName("Rotor#Reference");
        turbineMomentum = new FlowSource(PhysicalDomain.MECHANICAL);
        turbineMomentum.setName("Rotor#Momentum");
        turbineTurningGear = new FlowSource(PhysicalDomain.MECHANICAL);
        turbineTurningGear.setName("Rotor#TurningGear");
        turbineInertia = new MutualCapacitance(PhysicalDomain.MECHANICAL);
        turbineInertia.setName("Rotor#Inertia");
        turbineFriction = new LinearDissipator(PhysicalDomain.MECHANICAL);
        turbineFriction.setName("Rotor#Inertia");

        for (int idx = 0; idx < 2; idx++) {
            oilPump[idx] = new FlowSource(PhysicalDomain.HYDRAULIC);
            oilPump[idx].setName("Oil" + (idx + 1) + "#Pump");
        }
        oilPumpTurbine = new FlowSource(PhysicalDomain.HYDRAULIC);
        oilPumpTurbine.setName("Oil#PumpTurbine");
        oilFlow = new LinearDissipator(PhysicalDomain.HYDRAULIC);
        oilFlow.setName("Oil#Flow");
        oilGnd = new GeneralNode(PhysicalDomain.HYDRAULIC);
        oilGnd.setName("Oil#Gnd");
        oilOrigin = new ClosedOrigin(PhysicalDomain.HYDRAULIC);
        oilOrigin.setName("Oil#Origin");
        oilPressure = new GeneralNode(PhysicalDomain.HYDRAULIC);
        oilPressure.setName("Oil#Pressure");
        oilPressureDelay = new MutualCapacitance(PhysicalDomain.HYDRAULIC);
        oilPressureDelay.setName("Oil#PressureDelay");

        // </editor-fold>
        setpointTurbineSpeed = new Setpoint();
        rotorSolver.setString("TurbineRotorSolver");
        oilSolver.setString("TurbineOilSolver");
    }

    @Override
    public void run() {
        // This method is called after run() of ThermalLayout where the
        // solver is getting invoked, so all elements should be completely 
        // updated. 
        // Generate average temperatures:
        hpRotorTemperature = (thermalNodeRotor[0].getEffort()
                + thermalNodeRotor[1].getEffort()) / 2.0;
        hpStatorTemperature = (thermalNodeStator[0].getEffort()
                + thermalNodeStator[1].getEffort()) / 2.0;
        lpRotorTemperature = (thermalNodeRotor[2].getEffort()
                + thermalNodeRotor[3].getEffort()) / 2.0;
        lpStatorTemperature = (thermalNodeStator[2].getEffort()
                + thermalNodeStator[3].getEffort()) / 2.0;

        // Rotor and stator lengths are calculated by using the average
        // temperatures of both and the lengths as follows:
        // HP: 6 Meters
        // LP: 17 Meters
        // This is taken from the actual turbine but adapted to the simplified
        // turbine that is modeled here. Those lengts are for 20 °C so the 
        // current length is calculated with the 1.2e-5 1/K coefficient. 
        double hpRotorLength
                = HP_LENGTH * (1 + (hpRotorTemperature - 293.15) * EXP_C);
        double hpStatorLength
                = HP_LENGTH * (1 + (hpStatorTemperature - 293.15) * EXP_C);
        double lpRotorLength
                = LP_LENGTH * (1 + (lpRotorTemperature - 293.15) * EXP_C);
        double lpStatorLength
                = LP_LENGTH * (1 + (lpStatorTemperature - 293.15) * EXP_C);

        // Calculate expansions: Stator lengths are manipulated as the stator
        // is slightly colder due to contact to environment for cooldown 
        // modeling. There's sure some formula (simply solve the network 
        // circuit) but for now those numbers were taken from steady state using
        // debugging. 
        hpDiffExpansion = hpRotorLength - hpStatorLength * 1.03980254387591;
        lpDiffExpansion = lpRotorLength - lpStatorLength * 1.03397295439119;
        statorAbsExpansion = hpStatorLength + lpStatorLength
                - LP_LENGTH - HP_LENGTH;

        // Check if speed setpoint gradient was changed, if so, apply it
        // and send it back as a property to controller (to have a light
        // on the panel).
        if (oldSetpointSpeedGradient != setpointSpeedGradient) {
            switch (setpointSpeedGradient) {
                case LOW ->
                    setpointTurbineSpeed.setMaxRate(10);
                case MED ->
                    setpointTurbineSpeed.setMaxRate(30);
                case HIGH ->
                    setpointTurbineSpeed.setMaxRate(60);
            }
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Turbine#SpeedSetpointGradient",
                    oldSetpointSpeedGradient, setpointSpeedGradient));
            oldSetpointSpeedGradient = setpointSpeedGradient;
        }

        if (turningGearState != oldTurningGearState) {
            controller.propertyChange(new PropertyChangeEvent(this,
                    "Turbine#TurningGearState",
                    oldTurningGearState, turningGearState));

            if (turningGearState == 2) {
                // Turning gear is modeled as a constant applied momentum.
                turbineTurningGear.setFlow(0.24); // 20.568 1/min with U=R*I
            } else {
                turbineTurningGear.setFlow(0.0);
            }
        }
        oldTurningGearState = turningGearState;

        runLubeOil();

        // Calculate current turbine speed as long as the generator breaker
        // is open. We do not model the force of the generator towards the
        // turbine for simplification reasons. Force to exactly 3000.0 as long
        // as the generator is synced.
        if (!generatorSynched) {
            turbineMomentum.setFlow(shaftPower);
            rotorSolver.prepareCalculation();
            rotorSolver.doCalculation();

            generatorPower = 0.0;
        } else {
            turbineInertia.setInitialEffort(-3000); // sync model to 3000

            // as values are kind of made up and nothing is really calculated,
            // we need to add a factor here to make the output exactly 1000 on
            // 3200 mw thermal
            if (oilPressure.getEffort() < 1.6e5) {
                // Model bearing lube oil missing here
                generatorPower = (shaftPower - HOLD_POWER * 10.0) * 0.57313;
            } else {
                generatorPower = (shaftPower - HOLD_POWER) * 0.57313;
            }

        }

        // Get startup valves auto/manual mode
        speedSetpointFollowup = !process.isTurbineStartupValveAutomatic();

        // Force the setpoint to the current value as long as no controller 
        // can control it.
        if (speedSetpointFollowup && !generatorSynched) {
            targetTurbineSpeed = turbineVelocity.getEffort();
        }

        if (generatorSynched) {
            setpointTurbineSpeed.setInput(3000);
            setpointTurbineSpeed.forceOutputValue(3000);
        }
        // Do not force the turbine setpoint to current value to allow it to be
        // set individually.

        // In case of backwards energy flow and turbine ventilation, open the
        // generator breaker. This must be done in a better way with some alarm
        if (generatorSynched) {
            if (generatorPower <= -10) {
                generatorSynched = false;
            }
        }

        runner.invokeAll();

        if (!generatorSynched) {
            double tVel = turbineVelocity.getEffort();
            if (tVel >= 2900) {
                // sum up using discrete steps of 0.1 s
                syncAngle += tVel * 0.10471975512 * 0.1;
                // limit between 0 and +2pi
                syncAngle = syncAngle % (2 * Math.PI);
            } else {
                syncAngle = 0.0;
            }

            // Turning gear requires Lube oil and it can't be engaged when the
            // turbine is already running
            if (tVel >= 25 || oilPressure.getEffort() < 1.5e5) {
                turningGearState = 0;
            } else if (turningGearState == 0 && tVel < 50) {
                // auto-ready on low rpm
                turningGearState = 1;
            }
        } else {
            syncAngle = 0.0;
            turningGearState = 0;
        }

        alarmUpdater.invokeAll();

        // Send the TPS state to controller
        if (tps != oldTps) {
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Turbine#TPSState", oldTps, tps));
            oldTps = tps;
        }

        // Send the generator breaker state on change
        if (generatorSynched != oldGeneratorSynched) {
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Generator#BreakerClosed",
                    oldGeneratorSynched, generatorSynched));
            oldGeneratorSynched = generatorSynched;
        }

        // Send the RPS alarm message on change
        if (tpsActive != oldTpsActive) {
            oldTpsActive = tpsActive;
            if (tpsActive) {
                alarmManager.fireAlarm("TurbineProtection",
                        AlarmState.ACTIVE, false);
            } else {
                alarmManager.fireAlarm("TurbineProtection",
                        AlarmState.NONE, false);
            }
            // this is used for the light bulb on the panel
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Turbine#ProtectionLock", oldTpsActive, tpsActive));
        }

        // Send target value back, used for the control panel lights
        outputValues.setParameterValue("Turbine#SpeedSetpointTarget",
                targetTurbineSpeed);

        if (generatorSynched) {
            outputValues.setParameterValue("Turbine#Speed", 3000.0);
        } else {
            outputValues.setParameterValue("Turbine#Speed",
                    turbineVelocity.getEffort());
        }

        outputValues.setParameterValue("Generator#SyncAngle", syncAngle);
        outputValues.setParameterValue("Generator#Power", generatorPower);

        // Sent temperatures
        outputValues.setParameterValue("Turbine#TemperatureHpStatorIn",
                thermalNodeStator[0].getEffort() - 273.15);
        outputValues.setParameterValue("Turbine#TemperatureHpRotorIn",
                thermalNodeRotor[0].getEffort() - 273.15);
        outputValues.setParameterValue("Turbine#TemperatureHpStatorOut",
                thermalNodeStator[1].getEffort() - 273.15);
        outputValues.setParameterValue("Turbine#TemperatureHpRotorOut",
                thermalNodeRotor[1].getEffort() - 273.15);
        outputValues.setParameterValue("Turbine#TemperatureLpStatorIn",
                thermalNodeStator[2].getEffort() - 273.15);
        outputValues.setParameterValue("Turbine#TemperatureLpRotorIn",
                thermalNodeRotor[2].getEffort() - 273.15);
        outputValues.setParameterValue("Turbine#TemperatureLpStatorOut",
                thermalNodeStator[3].getEffort() - 273.15);
        outputValues.setParameterValue("Turbine#TemperatureLpRotorOut",
                thermalNodeRotor[3].getEffort() - 273.15);

        outputValues.setParameterValue("Turbine#TemperatureHpRotorAvg",
                hpRotorTemperature - 273.15);
        outputValues.setParameterValue("Turbine#TemperatureHpStatorAvg",
                hpStatorTemperature - 273.15);
        outputValues.setParameterValue("Turbine#TemperatureHpRotorAvg",
                lpRotorTemperature - 273.15);
        outputValues.setParameterValue("Turbine#TemperatureHpStatorAvg",
                lpStatorTemperature - 273.15);

        outputValues.setParameterValue("Turbine#HPDiffExpansion",
                hpDiffExpansion);
        outputValues.setParameterValue("Turbine#LPDiffExpansion",
                lpDiffExpansion);
        outputValues.setParameterValue("Turbine#AbsExpansion",
                statorAbsExpansion);
    }

    @Override
    public void handleAction(ActionCommand ac) {
        switch (ac.getPropertyName()) {
            case "Turbine#SpeedSetpointTargetValue" -> {
                targetTurbineSpeed = (double) ac.getValue();
                setpointTurbineSpeed.setInput(targetTurbineSpeed);
            }
            case "Turbine#SpeedSetpointGradient" ->
                setpointSpeedGradient = (SpeedSelect) ac.getValue();

            case "Turbine#SpeedSetpointHold" -> {
                targetTurbineSpeed = setpointTurbineSpeed.getOutput();
                setpointTurbineSpeed.setStop();
            }

            case "Turbine#TurningGear" -> {
                if ((boolean) ac.getValue()) {
                    // only switch to active when ready, otherwise ignore.
                    if (turningGearState == 1) {
                        turningGearState = 2;
                    } else {
                        LOGGER.log(Level.INFO, "Turbine Turning Gear not "
                                + "ready, command refused.");
                    }
                } else {
                    turningGearState = 0; // disable
                }
            }

            case "Turbine#Trip" -> {
                process.turbineTrip();
                LOGGER.log(Level.INFO, "Received Turbine Trip Command");
            }

            case "Turbine#TPSReset" -> {
                // allow reset only after rods are in and there's no neutron flux.
                if (checkTpsDisengage()) {
                    tpsActive = false;
                } else {
                    LOGGER.log(Level.INFO, "Command refused, TPS still active.");
                }
            }

            case "Turbine#TPS" -> {
                // translate boolean on/off into ControlCommand state
                if ((boolean) ac.getValue()) {
                    tps = ControlCommand.AUTOMATIC;
                } else {
                    tps = ControlCommand.MANUAL_OPERATION;
                }
                if (tps.equals(ControlCommand.MANUAL_OPERATION)) {
                    tpsActive = false; // skip the need for reset when turning off
                } else {
                    // When switching on, check conditions if reactor can be
                    // started up, otherwise block immediately.
                    if (!checkTpsDisengage()) {
                        tpsActive = true;
                        process.turbineTrip(); // close fast valves
                        turbineTrip(); // this class
                    }
                }
            }

            case "Generator#Breaker" -> {
                boolean close = (boolean) ac.getValue();
                if (close != generatorSynched) {
                    if (close) {
                        // Check if the generator breaker can be closed. The
                        // Speed must be in a defined range and the angle 
                        // must be near 0
                        if (turbineVelocity.getEffort() >= 2997
                                && turbineVelocity.getEffort() <= 3005
                                // Define which angles are valid in positive
                                // and negative range.
                                // Positive:
                                && (syncAngle <= 0.1 && syncAngle >= 0.0
                                // Negative:
                                || (syncAngle - 2 * Math.PI) >= -0.1
                                && (syncAngle - 2 * Math.PI) <= 0.0)) {
                            generatorSynched = true;
                        } else {
                            LOGGER.log(Level.INFO, "Sync failed.");
                        }
                    } else {
                        generatorSynched = false;
                    }
                }
            }
        }
        setpointTurbineSpeed.handleAction(ac);
        hydraulic.handleAction(ac);
        lubeOilPump[0].handleAction(ac);
        lubeOilPump[1].handleAction(ac);
    }

    public void init() {
        setpointTurbineSpeed.initName("Turbine#SpeedSetpoint");

        setpointTurbineSpeed.setLowerLimit(750);
        setpointTurbineSpeed.setUpperLimit(3100);
        setpointTurbineSpeed.setMaxRate(30);
        runner.submit(setpointTurbineSpeed);

        // Build the mechanical model of the turbine rotor. We use a full linear
        // analogy on how to describe and build the rotor model.
        turbineOrigin.connectTo(turbineReference);
        turbineMomentum.connectBetween(turbineReference, turbineVelocity);
        turbineTurningGear.connectBetween(turbineReference, turbineVelocity);
        turbineInertia.connectBetween(turbineReference, turbineVelocity);
        turbineFriction.connectBetween(turbineReference, turbineVelocity);

        // Decide that we need a continuous shaft power of X to hold the 
        // turbine on 3000, this energy will be consumed by the resistor and 
        // defines the working point for the spin up model.
        // this is only an initial value for first step, it will be overwritten
        // by the lube oil system as soon as its there
        double turnResistance = 3000.0 / HOLD_POWER; // 3000/35 = 85.7
        turbineFriction.setResistanceParameter(turnResistance);

        // There could be a fancy calculation on how to get the time constand 
        // but this number was found by trying some and getting a nice spin up
        // dynamic behavior.
        turbineInertia.setTimeConstant(0.4);

        // Set up a solver for this network
        rotorSolver.addNetwork(turbineVelocity);

        // Initial conditions
        turbineMomentum.setFlow(0.0);
        // This value is calculated by the momentum flow from the intitial
        // active turbine turning gear.
        turbineInertia.setInitialEffort(-20.568);

        // Set up and generate the auxiliary turbine systems, they are mostly
        // very simplified fakes. 
        hydraulic = new DummyValve();
        hydraulic.initName("Turbine#Hydraulic");
        runner.submit(hydraulic);
        for (int idx = 0; idx < lubeOilPump.length; idx++) {
            lubeOilPump[idx] = new DummyValve();
            lubeOilPump[idx].initName("Turbine" + (idx + 1) + "#LubeOilPump");
            // Valves could close down to -5 but we need a clean 0.0 as lower
            // limit as we use the valve percentage to generate flow values.
            lubeOilPump[idx].getIntegrator().setLowerLimit(0.0);
            runner.submit(lubeOilPump[idx]);
        }

        // Build a network that describes the oil pressure. Each of the pump 
        // is represented as flow source and the turbine itself is also a flow
        // source as the turbine has it's own oil pump. At least some turbines
        // do this, I have no clue if its the same here.
        //
        //      .-------------------------o-------
        //      |        |        |       |      |
        //      |        |        |       X      |
        //  p1 (-)   p2 (-)   trb(-)      X     ===
        //      |        |        |       X      |
        //      |        |        |       |      |
        //      o---------------------------------
        //      |
        //     _|_
        oilOrigin.connectTo(oilGnd);
        oilPump[0].connectBetween(oilGnd, oilPressure);
        oilPump[1].connectBetween(oilGnd, oilPressure);
        oilPumpTurbine.connectBetween(oilGnd, oilPressure);
        oilFlow.connectBetween(oilGnd, oilPressure);
        oilPressureDelay.connectBetween(oilGnd, oilPressure);

        oilSolver.addNetwork(oilGnd);

        // Assumptions:
        // Full speed turbine: 2.7 bar (2.7e5 Pa) with 40 kg/s flow
        // so it is R = 2.7e5 / 40 = 6750 to get that 2.7 bar
        // one pump: 1.8 bar: I = 1.8e5 / 6750 = 27 kg/s as pump value
        oilFlow.setResistanceParameter(6750);
        oilPressureDelay.setTimeConstant(0.0001); // try and error

        // initial conditions: Both pumps running, turbine is already spinning
        lubeOilPump[0].initOpening(100);
        lubeOilPump[1].initOpening(100);
        oilPump[0].setFlow(OIL_FLOW_AUX);
        oilPump[1].setFlow(OIL_FLOW_AUX);
        oilPressureDelay.setInitialEffort(-182250);

        ValueAlarmMonitor am;

        // Turbine rotor speed alarm
        am = new ValueAlarmMonitor();
        am.setName("TurbineSpeed");
        am.addInputProvider(() -> turbineVelocity.getEffort());
        am.defineAlarm(3100.0, AlarmState.MAX1);
        am.defineAlarm(3030.0, AlarmState.HIGH2);
        am.defineAlarm(3015.0, AlarmState.HIGH1);
        am.defineAlarm(50.0, AlarmState.LOW1);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                triggerTurbineTrip();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        // Expansion for HP and LP with trip 
        am = new ValueAlarmMonitor();
        am.setName("DiffExpansionHPTurbine");
        am.addInputProvider(() -> Math.abs(hpDiffExpansion));
        am.defineAlarm(1.5, AlarmState.MAX1);
        am.defineAlarm(1.3, AlarmState.HIGH2);
        am.defineAlarm(1.1, AlarmState.HIGH1);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                triggerTurbineTrip();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("DiffExpansionLPTurbine");
        am.addInputProvider(() -> Math.abs(lpDiffExpansion));
        am.defineAlarm(3.4, AlarmState.MAX1);
        am.defineAlarm(3.0, AlarmState.HIGH2);
        am.defineAlarm(2.6, AlarmState.HIGH1);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                triggerTurbineTrip();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("TurbineLubeOilPressure");
        am.addInputProvider(() -> oilPressure.getEffort() * 1e-5);
        am.defineAlarm(2.8, AlarmState.HIGH1);
        am.defineAlarm(1.6, AlarmState.MIN1);
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                triggerTurbineTrip();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);
    }

    public void runLubeOil() {
        // Set pump flow values from turbine speed (turbine has its own pump
        // driven by main shaft) and the auxiliary pumps, those pumps states 
        // are represented by dummy valve objects which generate a 0..100 %
        // value.
        for (int idx = 0; idx < 2; idx++) {
            oilPump[idx].setFlow(OIL_FLOW_AUX * lubeOilPump[idx].getOpening() / 100.0);
        }

        // The velocity is not available on first run and will be skipped. 
        // loading a save will hard-write the value.
        if (turbineVelocity.effortUpdated()) {
            oilPumpTurbine.setFlow(
                    40.0 * turbineVelocity.getEffort() / 3000.0);
        } else {
            oilPumpTurbine.setFlow(0.0);
        }

        // run one cycle
        oilSolver.prepareCalculation();
        oilSolver.doCalculation();

        // Manipulate the turbines turn resistance in case of low oil pressure
        // to show fatal bearin failure.
        double turnResistance = 3000.0 / HOLD_POWER; // 3000/35 = 85.7
        if (oilPressure.getEffort() < 1.6e5) {
            turnResistance = turnResistance / 8.0; // its not R, its friction
        }
        turbineFriction.setResistanceParameter(turnResistance);

        outputValues.setParameterValue("Turbine#LubeOilPressure",
                oilPressure.getEffort() * 1e-5);
    }

    /**
     * Called from model setup in ThermalLayout, this prepares the part that
     * will be managed here.
     */
    public void initConnections() {
        // Build the thermal model that represents the thermal behavior that is 
        // used to determine the turbine expansion.
        //
        //     Rotor      Stator   <- thermal Masses
        //     =====      =====
        //       |          |          Each node represents a temperature with
        //       o          o------    its effort value.
        //       |          |     |    
        // Rotor X   Stator X     X    Stator to Ambient Thermal Resistance
        // therm X   therm  X     X    (this cools down slowly during idle)
        // Res-  |   Res    |     |    
        //       -----o------     o  <- thermalNodeEnvTemperature
        //            |           |    Ambient temperature
        //  Steam    (|)         (|) 
        //  Volume    |           |
        //  Temperat. o------------
        //            |
        //           _|_  OpenOrigin: Absolute Zero (0 K)
        //
        // There are 4 of those models for both inlet and outlet of both high 
        // and low pressure turbine. The low pressure part is kind of nonsense.
        for (int idx = 0; idx < 4; idx++) {
            thermalOrigin[idx].connectToVia(thermalSteamTemperature[idx],
                    thermalNodeZero[idx]);
            thermalSteamTemperature[idx].connectTo(
                    thermalNodeSteamTemperature[idx]);
            thermalResistanceSteamToStator[idx].connectBetween(
                    thermalNodeSteamTemperature[idx], thermalNodeStator[idx]);
            thermalMassStator[idx].connectTo(thermalNodeStator[idx]);
            thermalResistanceSteamToRotor[idx].connectBetween(
                    thermalNodeSteamTemperature[idx], thermalNodeRotor[idx]);
            thermalMassRotor[idx].connectTo(thermalNodeRotor[idx]);
            thermalEnvTemperature[idx].connectBetween(thermalNodeZero[idx],
                    thermalNodeEnvTemperature[idx]);
            thermalResistanceStatorToEnv[idx].connectBetween(
                    thermalNodeEnvTemperature[idx], thermalNodeStator[idx]);
        }
    }

    public void initElementProperties() {
        // The ThermalLayout has an element which represents steam volume 
        // inside the turbine, it is unfortunately fixed. With a specific heat
        // capacity of 4200 J/kg/K on 100 kg that is 420.000 J/K. Note that 
        // everything here is in SI units. We use at least 10 kg/s to spin up
        // the turbine, more like 15 to 20 maybe. This gives a rough idea on
        // how long it takes to heat up the 100 kg/s by ideally mixing in the 
        // 10-20 kg/s steam.
        for (int idx = 0; idx < 4; idx++) {
            thermalOrigin[idx].setEffort(0.0);

            // Usually the turbine is super heavy and the stator weighs tons, 
            // but as this would take many hours we make it very light to 
            // enable fast heatup.
            thermalMassRotor[idx].setTimeConstant(4200 * 200); // this is C
            thermalMassStator[idx].setTimeConstant(4200 * 600);
            // Those values have to be more tuned:
            thermalResistanceSteamToStator[idx].setConductanceParameter(5e4);
            thermalResistanceSteamToRotor[idx].setConductanceParameter(5e4);

            thermalEnvTemperature[idx].setEffort(273.15 + 24.0);
            thermalResistanceStatorToEnv[idx].setConductanceParameter(3e3);
        }

        // Initialize Temperatures
        for (int idx = 0; idx < 4; idx++) {
            thermalMassRotor[idx].setInitialEffort(273.15 + 22);
            thermalMassStator[idx].setInitialEffort(273.15 + 22);
        }
    }

    /**
     * Sets the transferred power from the turbine steam layout to this class.
     * Note that the number is totally made up but in the units of megawatts. We
     * use it as a momentum to spin up the turbine here.
     *
     * @param power
     */
    public void setShaftPower(double power) {
        shaftPower = power;
        // Some notes on shaftPower: This is a highly cheated value that is 
        // used for both startup and full load. It is tweaked in a way the 
        // turbine startup will be possible with given parameters.
        // On 140 MWth / 14.3 bar: Start spinning the turbine. With both
        // startup valves on 75 %: shaftPower raises to about 10.2, takes about
        // 5 minutes. HP flow is 7,5 kg/s. This behavior is realistic as the 
        // steam looses all its energy on the cold turbine blades immediately.
        // On 326 MWth / 50 bar, with startup valves on 75 % will have a flow of
        // 25.5 kg/s, shaftPower is around 34, this is the value which is the 
        // desired use to sync the turbine to the grid.
        // We do not intend to use superheating until sync.
    }

    @Override
    public void updateNotification(String propertyName) {

    }

    /**
     * Can be invoked by this or externally, this is the TPS (turbine protection
     * system) shutdown command that is supposed to close all inlet valves. It
     * can be overridden however.
     */
    public void triggerTurbineTrip() {
        if (tps.equals(ControlCommand.MANUAL_OPERATION)) {
            return;
        }
        turbineTrip();
        tpsActive = true;
    }

    /**
     * Handles the trip functions of this class, is called from whatever trips
     * the turbine, mostly the triggerTurbineTrip function.
     */
    public void turbineTrip() {

    }

    public boolean isTpsActive() {
        return tpsActive;
    }

    public boolean valveHydraulicAvailable() {
        return hydraulic.getOpening() > 99;
    }

    /**
     * Checks the plant state that it is in a state where it allows the TPS to
     * be cleared. Note that the Alarm system is a trigger-event only
     *
     * @return true if everything is fine.
     */
    private boolean checkTpsDisengage() {
        if (alarmManager.isAlarmActive("HotwellLevel", AlarmState.MAX1)) {
            return false;
        }
        if (alarmManager.isAlarmActive("CondenserVacuum", AlarmState.MIN1)) {
            return false;
        }
        if (alarmManager.isAlarmActive("Drum1Pressure", AlarmState.MIN1)) {
            return false;
        }
        if (alarmManager.isAlarmActive("Drum2Pressure", AlarmState.MIN1)) {
            return false;
        }
        if (alarmManager.isAlarmActive("TurbineLubeOilPressure", AlarmState.MIN1)) {
            return false;
        }
        // Checks that will only be performed when not switchting the RPS back 
        // on during already running operator. Those are designed to make sure
        // some other state must be reached first befroe able to reset the RPS
        if (!oldTps.equals(ControlCommand.MANUAL_OPERATION)) {
            if (turbineVelocity.getEffort() > 500) {
                return false; // turbine needs to spin down first
            }
        }
        
        if (turningGearState == 2) {
            return false; // turning gear must be disengaged
        }
        return true;
    }

    @Override
    public void registerController(ModelListener controller) {
        // Will be called after init() - note that it is the other way round in
        // the ThermalLayout!
        super.registerController(controller);
        runner.setSignalListener(controller);
    }

    @Override
    public void registerParameterOutput(ValueHandler output) {
        super.registerParameterOutput(output);
        setpointTurbineSpeed.registerParameterHandler(output);
    }

    /**
     * Returns a reference to one of the thermal effort sources representing the
     * temperature of the steam volume inside the turbine. There are 4 volumes
     * represented with phased thermal exchangers which also have some volume in
     * the ThermalLayout. This is to access the counterpart of the thermal
     * turbine model.
     *
     * @param idx 0 HP in, 1 HP out, 2 LP in, 3 LP out
     * @return EffortSource in Thermal Domain
     */
    public EffortSource getThermalEffortSource(int idx) {
        return thermalSteamTemperature[idx];
    }

    public void registerThermalLayout(ThermalLayout process) {
        this.process = process;
    }

    /**
     * For startup, this returns the current difference between desired and
     * current turbine speed. This is used by the startup steam valves to
     * control the turbine spin up.
     *
     * @return
     */
    public double getTurbineSpeedDeviation() {
        if (generatorSynched) {
            return setpointTurbineSpeed.getOutput() - 3000;
        } else if (!turbineVelocity.effortUpdated()) {
            return 0.0;
        } else {
            return setpointTurbineSpeed.getOutput()
                    - turbineVelocity.getEffort();
        }
    }
    
    /**
     * Called from reactor core on explosion, after the explosion the thermal
     * layout will no longer be updated so this is used to make certain set
     * operations.
     */
    public void reactorExplosion() {
        shaftPower = 0.0;
    }

    @Override
    public void saveTo(SaveGame save) {
        // Generate a new object that contains all values for current state.
        TurbineState ts = new TurbineState();

        ts.setGeneratorSynched(generatorSynched);
        ts.setSetpointSpeedGradient(setpointSpeedGradient);
        ts.setSyncAngle(syncAngle);
        ts.setTargetTurbineSpeed(targetTurbineSpeed);
        ts.setTps(tps);
        ts.setTpsActive(tpsActive);
        ts.setTurningGear(turningGearState);

        // Lube oil system needs some more data as it is always dependend on
        // the previous cycle and would have old results otherwise.
        ts.setLubeOilPressue(oilPressure.getEffort());
        ts.setTurnResistance(turbineFriction.getResistance());
        ts.setShaftOilPumpFlow(oilPumpTurbine.getFlow());

        save.addTurbineState(ts);

        save.addSolverState(rotorSolver.toString(),
                rotorSolver.getCurrentNetworkCondition());
        save.addRunnerState("turbineSetpoints",
                runner.getCurrentAutomationCondition());
        save.addSolverState(oilSolver.toString(),
                oilSolver.getCurrentNetworkCondition());

    }

    @Override
    public void load(SaveGame save) {
        TurbineState ts = save.getTurbineState();

        generatorSynched = ts.isGeneratorSynched();
        setpointSpeedGradient = ts.getSetpointSpeedGradient();
        syncAngle = ts.getSyncAngle();
        targetTurbineSpeed = ts.getTargetTurbineSpeed();
        tps = ts.getTps();
        tpsActive = ts.isTpsActive();
        turningGearState = ts.getTurningGear();

        oilPressure.setEffort(ts.getLubeOilPressue());
        turbineFriction.setResistanceParameter(ts.getTurnResistance());
        oilPumpTurbine.setFlow(ts.getShaftOilPumpFlow());

        // Reset old values to trigger all related events
        oldSetpointSpeedGradient = null;
        oldTps = null;
        oldTpsActive = !tpsActive;
        oldGeneratorSynched = !generatorSynched;

        rotorSolver.setNetworkInitialCondition(
                save.getSolverState(rotorSolver.toString()));
        oilSolver.setNetworkInitialCondition(
                save.getSolverState(oilSolver.toString()));
        runner.setRunnablesAutomationCondition(
                save.getRunnerState("turbineSetpoints"));

        if (generatorSynched) {
            // rotor solver will not be called as soon as sync is completed, so
            // the turbineVelocity node still holds the old value.
            // manually force the model to that state, at least that part.
            turbineVelocity.setEffort(3000);
        }

        alarmUpdater.clearAlarmUpdaters();
    }
}
