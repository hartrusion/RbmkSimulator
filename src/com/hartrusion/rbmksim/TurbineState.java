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
import java.io.Serializable;

/**
 * Holds current state for saving and loading of the turbine subsystem.
 *
 * @author Viktor Alexander Hartung
 */
public class TurbineState implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean tpsActive;
    private ControlCommand tps;
    private double targetTurbineSpeed;
    private SpeedSelect setpointSpeedGradient;
    private boolean generatorSynched;
    private double syncAngle;

    public boolean isTpsActive() {
        return tpsActive;
    }

    public void setTpsActive(boolean tpsActive) {
        this.tpsActive = tpsActive;
    }

    public ControlCommand getTps() {
        return tps;
    }

    public void setTps(ControlCommand tps) {
        this.tps = tps;
    }

    public double getTargetTurbineSpeed() {
        return targetTurbineSpeed;
    }

    public void setTargetTurbineSpeed(double targetTurbineSpeed) {
        this.targetTurbineSpeed = targetTurbineSpeed;
    }

    public SpeedSelect getSetpointSpeedGradient() {
        return setpointSpeedGradient;
    }

    public void setSetpointSpeedGradient(SpeedSelect setpointSpeedGradient) {
        this.setpointSpeedGradient = setpointSpeedGradient;
    }

    public boolean isGeneratorSynched() {
        return generatorSynched;
    }

    public void setGeneratorSynched(boolean generatorSynched) {
        this.generatorSynched = generatorSynched;
    }

    public double getSyncAngle() {
        return syncAngle;
    }

    public void setSyncAngle(double syncAngle) {
        this.syncAngle = syncAngle;
    }
}
