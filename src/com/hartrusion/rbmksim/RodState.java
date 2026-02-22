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

/**
 * Holds the state for a specific control rod to allow saving and loading.
 *
 * @author Viktor Alexander Hartung
 */
public class RodState implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int rodSpeedIndex;
    private double currentPosition;
    private double targetPosition;
    private ControlCommand autoState;
    private boolean selected;

    public int getRodSpeedIndex() {
        return rodSpeedIndex;
    }

    public void setRodSpeedIndex(int rodSpeedIndex) {
        this.rodSpeedIndex = rodSpeedIndex;
    }

    public double getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(double currentPosition) {
        this.currentPosition = currentPosition;
    }

    public double getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(double targetPosition) {
        this.targetPosition = targetPosition;
    }

    public ControlCommand getAutoState() {
        return autoState;
    }

    public void setAutoState(ControlCommand autoState) {
        this.autoState = autoState;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
