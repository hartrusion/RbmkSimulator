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
import com.hartrusion.alarm.ValueAlarmMonitor;
import com.hartrusion.control.ControlCommand;
import com.hartrusion.control.SerialRunner;
import com.hartrusion.control.Setpoint;
import com.hartrusion.modeling.PhysicalDomain;
import com.hartrusion.modeling.general.ClosedOrigin;
import com.hartrusion.modeling.general.EffortSource;
import com.hartrusion.modeling.general.FlowSource;
import com.hartrusion.modeling.general.GeneralNode;
import com.hartrusion.modeling.general.LinearDissipator;
import com.hartrusion.modeling.general.MutualCapacitance;
import com.hartrusion.modeling.general.OpenOrigin;
import com.hartrusion.modeling.solvers.DomainAnalogySolver;
import com.hartrusion.mvc.ActionCommand;
import com.hartrusion.mvc.ModelListener;
import static com.hartrusion.rbmksim.SpeedSelect.LOW;
import com.hartrusion.values.ValueHandler;
import java.beans.PropertyChangeEvent;
import java.util.function.DoubleSupplier;
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
    
    /**
     * To convert 1/min to rad/second the number can be multiplied with 
     * 2pi/60 = pi/30.
     */
    private static final double RPM_TO_RAD = 0.10471975512;

    /**
     * Reference to the thermal layout process class
     */
    private ThermalLayout process;

    // <editor-fold defaultstate="collapsed" desc="Model elements declaration and array instantiation">
    private final OpenOrigin[] thermalOriginSteam = new OpenOrigin[4];
    private final GeneralNode[] thermalNodeSteamGnd = new GeneralNode[4];
    private final EffortSource[] thermalSteamTemperature = new EffortSource[4];
    private final GeneralNode[] thermalNodeSteamTemperature = new GeneralNode[4];

    private final ClosedOrigin turbineOrigin;
    private final GeneralNode turbineReference;
    private final GeneralNode turbineVelocity;
    private final FlowSource turbineMomentum;
    private final FlowSource turbineTurningGear;
    private final MutualCapacitance turbineInertia;
    private final LinearDissipator turbineFriction;

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

    private final DomainAnalogySolver turbineRotor = new DomainAnalogySolver();

    private final SerialRunner alarmUpdater = new SerialRunner();

    private final boolean generatorSynched = false;
    
    private double syncAngle = 0.0;

    Turbine() {
        // <editor-fold defaultstate="collapsed" desc="Model elements instantiation">
        for (int idx = 0; idx < 4; idx++) {
            thermalOriginSteam[idx] = new OpenOrigin(PhysicalDomain.THERMAL);
            thermalOriginSteam[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalOriginSteam");
            thermalNodeSteamGnd[idx] = new GeneralNode(PhysicalDomain.THERMAL);
            thermalNodeSteamGnd[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalNodeSteamGnd");
            thermalSteamTemperature[idx] = new EffortSource(PhysicalDomain.THERMAL);
            thermalSteamTemperature[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalSteamTemperature");
            thermalNodeSteamTemperature[idx] = new GeneralNode(PhysicalDomain.THERMAL);
            thermalNodeSteamTemperature[idx].setName("Turbine"
                    + (idx + 1) + "#ThermalNodeSteamTemperature");
        }

        // Turbine and Generator Rotor mechanical model
        turbineOrigin = new ClosedOrigin(PhysicalDomain.MECHANICAL);
        turbineReference = new GeneralNode(PhysicalDomain.MECHANICAL);
        turbineVelocity = new GeneralNode(PhysicalDomain.MECHANICAL);
        turbineMomentum = new FlowSource(PhysicalDomain.MECHANICAL);
        turbineTurningGear = new FlowSource(PhysicalDomain.MECHANICAL);
        turbineInertia = new MutualCapacitance(PhysicalDomain.MECHANICAL);
        turbineFriction = new LinearDissipator(PhysicalDomain.MECHANICAL);

        // </editor-fold>
        setpointTurbineSpeed = new Setpoint();
    }

    @Override
    public void run() {
        // Check if speed setpoint gradient was changed, if so, apply it
        // and send it back as a property to controller (to have a light
        // on the panel).
        if (oldSetpointSpeedGradient != setpointSpeedGradient) {
            switch (setpointSpeedGradient) {
                case LOW ->
                    setpointTurbineSpeed.setMaxRate(30);
                case MED ->
                    setpointTurbineSpeed.setMaxRate(60);
                case HIGH ->
                    setpointTurbineSpeed.setMaxRate(90);
            }
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Turbine#SpeedSetpointGradient",
                    oldSetpointSpeedGradient, setpointSpeedGradient));
            oldSetpointSpeedGradient = setpointSpeedGradient;
        }

        // Calculate current turbine speed as long as the generator breaker
        // is open. We do not model the force of the generator towards the
        // turbine for simplification reasons. Force to exactly 3000.0 as long
        // as the generator is synced.
        if (!generatorSynched) {
            turbineRotor.prepareCalculation();
            turbineRotor.doCalculation();
        } else {
            turbineInertia.setInitialEffort(3000);
        }

        setpointTurbineSpeed.run();
        
        
        if (!generatorSynched) {
            double tVel = turbineVelocity.getEffort();
            if (tVel >= 2900) {
                // sum up using discrete steps of 0.1 s
                syncAngle += tVel * 0.10471975512 * 0.1;
                // limit between -pi and +pi
                syncAngle = syncAngle % (2 * Math.PI);
            } else {
                syncAngle = 0.0;
            }
        } else {
            syncAngle = 0.0;
        }

        alarmUpdater.invokeAll();

        // Send the TPS state to controller
        if (tps != oldTps) {
            controller.propertyChange(new PropertyChangeEvent(
                    this, "Turbine#TPSState", oldTps, tps));
            oldTps = tps;
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

        outputValues.setParameterValue("Turbine#Speed",
                turbineVelocity.getEffort());
        
        outputValues.setParameterValue("Generator#SyncAngle", syncAngle);

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
        }
        setpointTurbineSpeed.handleAction(ac);
    }

    public void init() {
        setpointTurbineSpeed.initName("Turbine#SpeedSetpoint");

        setpointTurbineSpeed.setLowerLimit(0);
        setpointTurbineSpeed.setUpperLimit(3100);
        setpointTurbineSpeed.setMaxRate(30);

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
        double holdPower = 12.0; // in units of setShaftPower
        double turnResistance = 3000.0 / holdPower;
        turbineFriction.setResistanceParameter(turnResistance);

        // There could be a fancy calculation on how to get the time constand 
        // but this number was found by trying some and getting a nice spin up
        // dynamic behavior.
        turbineInertia.setTimeConstant(0.08);

        // Set up a solver for this network
        turbineRotor.addNetwork(turbineVelocity);

        // Initial conditions
        turbineTurningGear.setFlow(0.0);
        turbineMomentum.setFlow(0.0);

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

    }

    /**
     * Called from model setup in ThermalLayout, this prepares the part that
     * will be managed here.
     */
    public void initConnections() {
        // make connection: origin - node - effort source - node
        // for each of the four effort sources
        for (int idx = 0; idx < 4; idx++) {
            thermalOriginSteam[idx].connectToVia(thermalSteamTemperature[idx],
                    thermalNodeSteamGnd[idx]);
            thermalSteamTemperature[idx].connectTo(
                    thermalNodeSteamTemperature[idx]);
        }
    }

    public void initElementProperties() {

    }

    /**
     * Sets the transferred power from the turbine steam layout to this class.
     * Note that the number is totally made up but in the units of megawatts. We
     * use it as a momentum to spin up the turbine here.
     *
     * @param power
     */
    public void setShaftPower(double power) {
        // 11 bar, 6 kg/s, no reheater: 5.3 MW (HD 47.6, ND 38.3)
        // 11 bar, 6 kg/s, reheat 3 kg/s: 7 Mw (HD 47, ND 120)

        turbineMomentum.setFlow(power);
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
        // Checks that will only be performed when not switchting the RPS back 
        // on during already running operator. Those are designed to make sure
        // some other state must be reached first befroe able to reset the RPS
        if (!oldTps.equals(ControlCommand.MANUAL_OPERATION)) {
            if (turbineVelocity.getEffort() > 500) {
                return false; // turbine needs to spin down first
            }
        }
        return true;
    }

    @Override
    public void registerController(ModelListener controller) {
        // Will be called after init() - note that it is the other way round in
        // the ThermalLayout!
        super.registerController(controller);
        // Todo for whatever elements
    }

    @Override
    public void registerParameterOutput(ValueHandler output) {
        super.registerParameterOutput(output);
        setpointTurbineSpeed.registerParameterHandler(output);
    }

    /**
     * Returns a reference to one of the thermal effort sources representing the
     * temperature of the steam volume inside the turbine.
     *
     * @param idx
     * @return EffortSource in Thermal Domain
     */
    public EffortSource getThermalEffortSource(int idx) {
        return thermalSteamTemperature[idx];
    }

    public void registerThermalLayout(ThermalLayout process) {
        this.process = process;
    }
}
