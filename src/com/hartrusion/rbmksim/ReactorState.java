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

import com.hartrusion.control.ControlCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the current state of the reactor core for saving and loading the
 * simulation state. The state space models of the core will get their state
 * space variables loaded and assigned using arrays.
 *
 * @author Viktor Alexander Hartung
 */
public class ReactorState implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private boolean coreOnlySimulation;
    private double setpointTargetNeutronFluxInput;
    private double setpointTargetNeutronFluxOutput;
    private double setpointPowerGradientInput;
    private double setpointPowerGradientOutput;
    private double setpointNeutronFluxInput;
    private double setpointNeutronFluxOutput;
    private final double[] xNeutronFluxModel = new double[6];
    private final double[] xXenonModel = new double[2];
    private final double[] xGraphiteModel = new double[2];
    private ControlCommand rps;
    private boolean rpsActive;
    private boolean globalControlEnabled;
    private boolean globalControlActive;
    private boolean globalControlTransient;
    private boolean globalControlTarget;

    private final List<RodState> rodStates = new ArrayList<>();

    public boolean isCoreOnlySimulation() {
        return coreOnlySimulation;
    }

    public void setCoreOnlySimulation(boolean coreOnlySimulation) {
        this.coreOnlySimulation = coreOnlySimulation;
    }

    public double getSetpointTargetNeutronFluxInput() {
        return setpointTargetNeutronFluxInput;
    }

    public void setSetpointTargetNeutronFluxInput(double setpointTargetNeutronFluxInput) {
        this.setpointTargetNeutronFluxInput = setpointTargetNeutronFluxInput;
    }

    public double getSetpointTargetNeutronFluxOutput() {
        return setpointTargetNeutronFluxOutput;
    }

    public void setSetpointTargetNeutronFluxOutput(double setpointTargetNeutronFluxOutput) {
        this.setpointTargetNeutronFluxOutput = setpointTargetNeutronFluxOutput;
    }

    public double getSetpointPowerGradientInput() {
        return setpointPowerGradientInput;
    }

    public void setSetpointPowerGradientInput(double setpointPowerGradientInput) {
        this.setpointPowerGradientInput = setpointPowerGradientInput;
    }

    public double getSetpointPowerGradientOutput() {
        return setpointPowerGradientOutput;
    }

    public void setSetpointPowerGradientOutput(double setpointPowerGradientOutput) {
        this.setpointPowerGradientOutput = setpointPowerGradientOutput;
    }

    public double getSetpointNeutronFluxInput() {
        return setpointNeutronFluxInput;
    }

    public void setSetpointNeutronFluxInput(double setpointNeutronFluxInput) {
        this.setpointNeutronFluxInput = setpointNeutronFluxInput;
    }

    public double getSetpointNeutronFluxOutput() {
        return setpointNeutronFluxOutput;
    }

    public void setSetpointNeutronFluxOutput(double setpointNeutronFluxOutput) {
        this.setpointNeutronFluxOutput = setpointNeutronFluxOutput;
    }

    public double getxNeutronFluxModel(int idx) {
        return xNeutronFluxModel[idx];
    }

    public void setxNeutronFluxModel(double x, int idx) {
        this.xNeutronFluxModel[idx] = x;
    }

    public double getxXenonModel(int idx) {
        return xXenonModel[idx];
    }

    public void setxXenonModel(double x, int idx) {
        this.xXenonModel[idx] = x;
    }

    public double getxGraphiteModel(int idx) {
        return xGraphiteModel[idx];
    }

    public void setxGraphiteModel(double x, int idx) {
        this.xGraphiteModel[idx] = x;
    }

    public ControlCommand getRps() {
        return rps;
    }

    public void setRps(ControlCommand rps) {
        this.rps = rps;
    }

    public boolean isRpsActive() {
        return rpsActive;
    }

    public void setRpsActive(boolean rpsActive) {
        this.rpsActive = rpsActive;
    }

    public boolean isGlobalControlEnabled() {
        return globalControlEnabled;
    }

    public void setGlobalControlEnabled(boolean globalControlEnabled) {
        this.globalControlEnabled = globalControlEnabled;
    }

    public boolean isGlobalControlActive() {
        return globalControlActive;
    }

    public void setGlobalControlActive(boolean globalControlActive) {
        this.globalControlActive = globalControlActive;
    }

    public boolean isGlobalControlTransient() {
        return globalControlTransient;
    }

    public void setGlobalControlTransient(boolean globalControlTransient) {
        this.globalControlTransient = globalControlTransient;
    }

    public boolean isGlobalControlTarget() {
        return globalControlTarget;
    }

    public void setGlobalControlTarget(boolean globalControlTarget) {
        this.globalControlTarget = globalControlTarget;
    }

    /**
     * Access the list of rod states, this is a sorted array list that matches
     * the controlRods list in the reactor class.
     *
     * @return ArrayList object of type RodStates
     */
    public ArrayList<RodState> getRodStates() {
        return (ArrayList<RodState>) rodStates;
    }

}
