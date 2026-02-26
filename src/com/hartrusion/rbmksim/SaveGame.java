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

    /**
     * Holds the object that describes the state of the turbine component.
     */
    private TurbineState turbineState;

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

    public TurbineState getTurbineState() {
        return turbineState;
    }

    public void addTurbineState(TurbineState turbineState) {
        this.turbineState = turbineState;
    }
}
