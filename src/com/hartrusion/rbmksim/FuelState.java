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

/**
 * Holds the state for a specific fuel element to allow saving and loading.
 * 
 * @author Viktor Alexander Hartung
 */
public class FuelState  implements java.io.Serializable {
    private static final long serialVersionUID = 6L;
    
    private double thermalLiftPressure;
    private double xFirstDelay;

    private double xDelayedPower;

    public double getThermalLiftPressure() {
        return thermalLiftPressure;
    }

    public void setThermalLiftPressure(double thermalLiftPressure) {
        this.thermalLiftPressure = thermalLiftPressure;
    }
    
    public double getXFirstDelay() {
        return xFirstDelay;
    }

    public void setXFirstDelay(double xFirstDelay) {
        this.xFirstDelay = xFirstDelay;
    }

    public double getXDelayedPower() {
        return xDelayedPower;
    }

    public void setXDelayedPower(double xDelayedPower) {
        this.xDelayedPower = xDelayedPower;
    }
}
