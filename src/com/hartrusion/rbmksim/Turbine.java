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

import com.hartrusion.control.Setpoint;
import com.hartrusion.modeling.PhysicalDomain;
import com.hartrusion.modeling.general.EffortSource;
import com.hartrusion.modeling.general.GeneralNode;
import com.hartrusion.modeling.general.OpenOrigin;
import com.hartrusion.mvc.ActionCommand;
import com.hartrusion.mvc.ModelListener;
import static com.hartrusion.rbmksim.SpeedSelect.LOW;
import com.hartrusion.values.ValueHandler;
import java.beans.PropertyChangeEvent;

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
    // </editor-fold>

    private double targetTurbineSpeed = 0.0;
    private final Setpoint setpointTurbineSpeed;
    private SpeedSelect setpointSpeedGradient = SpeedSelect.MED;
    private SpeedSelect oldSetpointSpeedGradient = null;

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

        setpointTurbineSpeed.run();

        // Send target value back, used for the control panel lights
        outputValues.setParameterValue("Turbine#SpeedSetpointTarget",
                targetTurbineSpeed);
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

    public void setShaftPower(double power) {

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
