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

import com.hartrusion.control.Integrator;
import com.hartrusion.modeling.initial.AbstractAC;
import com.hartrusion.modeling.initial.AbstractIC;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the full current state of the simulator with the ability to
 * restore and save it.
 *
 * @author Viktor Alexander Hartung
 */
public class SaveGame implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDateTime timestamp;

    /**
     * Maps a DomainAnalogySolver name to its list of initial conditions. Each
     * entry represents one solver instance.
     */
    private final LinkedHashMap<String, List<AbstractIC>> networkIC;

    /**
     * Maps a SerialRunner name to its list of automation conditions. Each entry
     * represents one automation runner instance.
     */
    private final LinkedHashMap<String, List<AbstractAC>> automationConditions;

    /**
     * Holds the object that describes the state of the reactor component. This
     * container includes the state space variables for the core state space
     * models.
     */
    private ReactorState reactorState;

    /**
     * Saved as a single variable as there is nothing like this system, no
     * specialized class for this.
     */
    private double condenserVacuum;

    private boolean blowdownBalanceActive;

    private boolean coreOnlySimulation;

    private double turbineHPOutSatTemp;

    private boolean startupPressureSetpointActive;

    private final double[][] mcpCavitaionState = new double[2][4];

    /**
     * Holds the object that describes the state of the turbine component.
     */
    private TurbineState turbineState;

    private double channelLeak1Upper, channelLeak2Upper, 
            channelLeak1Lower, channelLeak2Lower;

    public SaveGame() {
        this.timestamp = LocalDateTime.now();
        this.networkIC = new LinkedHashMap<>();
        this.automationConditions = new LinkedHashMap<>();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void addSolverState(String solverName, List<AbstractIC> states) {
        networkIC.put(solverName, new ArrayList<>(states));
    }

    public List<AbstractIC> getSolverState(String solverName) {
        return networkIC.get(solverName);
    }

    public Map<String, List<AbstractIC>> getAllSolverStates() {
        return networkIC;
    }

    public void addRunnerState(String runnerName, List<AbstractAC> states) {
        automationConditions.put(runnerName, new ArrayList<>(states));
    }

    public List<AbstractAC> getRunnerState(String runnerName) {
        return automationConditions.get(runnerName);
    }

    public Map<String, List<AbstractAC>> getAllRunnerStates() {
        return automationConditions;
    }

    public ReactorState getReactorState() {
        return reactorState;
    }

    public void addReactorState(ReactorState reactorState) {
        this.reactorState = reactorState;
    }

    public double getCondenserVacuum() {
        return condenserVacuum;
    }

    public void setCondenserVacuum(double condenserVacuum) {
        this.condenserVacuum = condenserVacuum;
    }

    /**
     * Gets the cavitation state value from all integrators.
     *
     * @param cavitationState ref to array with integrators
     */
    public void saveCavitationState(Integrator[][] cavitationState) {
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 4; jdx++) {
                mcpCavitaionState[idx][jdx]
                        = cavitationState[idx][jdx].getOutput();
            }
        }
    }

    /**
     * Writes the saved cavitation state back to the integrators
     *
     * @param cavitationState ref to array with integrators
     */
    public void writeCavitationState(Integrator[][] cavitationState) {
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 4; jdx++) {
                cavitationState[idx][jdx].forceOutputValue(
                        mcpCavitaionState[idx][jdx]);
            }
        }
    }

    public boolean isBlowdownBalanceActive() {
        return blowdownBalanceActive;
    }

    public void setBlowdownBalanceActive(boolean blowdownBalanceActive) {
        this.blowdownBalanceActive = blowdownBalanceActive;
    }

    public boolean isCoreOnlySimulation() {
        return coreOnlySimulation;
    }

    public void setCoreOnlySimulation(boolean coreOnlySimulation) {
        this.coreOnlySimulation = coreOnlySimulation;
    }

    public double getTurbineHPOutSatTemp() {
        return turbineHPOutSatTemp;
    }

    public void setTurbineHPOutSatTemp(double turbineHPOutSatTemp) {
        this.turbineHPOutSatTemp = turbineHPOutSatTemp;
    }

    public TurbineState getTurbineState() {
        return turbineState;
    }

    public void addTurbineState(TurbineState turbineState) {
        this.turbineState = turbineState;
    }

    public boolean isStartupPressureSetpointActive() {
        return startupPressureSetpointActive;
    }

    public void setStartupPressureSetpointActive(boolean startupPressureSetpointActive) {
        this.startupPressureSetpointActive = startupPressureSetpointActive;
    }
    
    public double getChannelLeak1Upper() {
        return channelLeak1Upper;
    }

    public void setChannelLeak1Upper(double channelLeak1Upper) {
        this.channelLeak1Upper = channelLeak1Upper;
    }

    public double getChannelLeak2Upper() {
        return channelLeak2Upper;
    }

    public void setChannelLeak2Upper(double channelLeak2Upper) {
        this.channelLeak2Upper = channelLeak2Upper;
    }

    public double getChannelLeak1Lower() {
        return channelLeak1Lower;
    }

    public void setChannelLeak1Lower(double channelLeak1Lower) {
        this.channelLeak1Lower = channelLeak1Lower;
    }

    public double getChannelLeak2Lower() {
        return channelLeak2Lower;
    }

    public void setChannelLeak2Lower(double channelLeak2Lower) {
        this.channelLeak2Lower = channelLeak2Lower;
    }
}
