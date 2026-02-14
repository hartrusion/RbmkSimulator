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

/**
 * Represents the turbine and its systems. Holds a network model with the
 * thermal part that represents the masses that get heated up. The model is
 * connected and solved in the ThermalLayout class, the instantiation of the
 * elements is done here.
 *
 * @author Viktor Alexander Hartung
 */
public class Turbine extends Subsystem implements Runnable {

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

    private final DomainAnalogySolver turbineRotor = new DomainAnalogySolver();
    
    private final SerialRunner alarmUpdater = new SerialRunner();

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
        // is open
        turbineRotor.prepareCalculation();
        turbineRotor.doCalculation();

        setpointTurbineSpeed.run();

        // Send target value back, used for the control panel lights
        outputValues.setParameterValue("Turbine#SpeedSetpointTarget",
                targetTurbineSpeed);

        outputValues.setParameterValue("Turbine#Speed",
                turbineVelocity.getEffort());
        
        alarmUpdater.invokeAll();
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
        turbineFriction.setResistanceParameter(3000.0 / 12.0);

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
        am.setName("Turbine#Speed");
        am.addInputProvider(() -> turbineVelocity.getEffort());
        am.defineAlarm(3100.0, AlarmState.MAX1);
        am.defineAlarm(3030.0, AlarmState.HIGH2);
        am.defineAlarm(3015.0, AlarmState.HIGH1);
        am.defineAlarm(50.0, AlarmState.LOW1);
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

    public void triggerTurbineTrip() {

    }

    @Override
    public void updateNotification(String propertyName) {

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
}
