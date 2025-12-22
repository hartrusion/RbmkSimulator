/*
 * Copyright (C) 2025 Viktor Alexander Hartung
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
 *
 * @author Viktor Alexander Hartung
 */
public class XenonModel implements Runnable {

    /**
     * Step time in Seconds
     */
    private double stepTime = 0.1;

    /**
     * Time constant for iodine buildup. A realistic value would be 4 hours but
     * that would be ridiculously for a simulator. 5 Minutes however seem to be
     * more reasonable for a simulator.
     */
    private static final double T_IODINE = 300;

    // Iodine 136 half life: 6.6 Hours
    // Xenon 136 half life: 9.4 hours -> 9.4/6.6 = 1.384
    private static final double T_XENON = T_IODINE * 1.4;
    
    /**
     * Time constant for burning xenon at 100 % neutron flux as percentage of
     * the Xenon time constant.
     */
    private static final double T_BURN = T_XENON * 0.2;
    
    private double uNeutronFlux;

    private double xIodine135 = 0;
    private double xXenon135 = 0;

    private double yXenon;
    private double yIodine;
    
    public void setInititalState(double xIodine135, double xXenon135) {
        this.xIodine135 = xIodine135;
        this.xXenon135 = xXenon135;
    }

    public void setInputs(double uNeutronFlux) {
        this.uNeutronFlux = uNeutronFlux;
    }

    @Override
    public void run() {
        double dXIodine135;
        double dXXenon135;

        // Calculate state space variable derivatives
        dXIodine135 = uNeutronFlux / T_IODINE
                - xIodine135 / T_IODINE;

        dXXenon135 = xIodine135 * (1 / T_XENON + 1 / T_BURN)
                - xXenon135 / T_XENON
                - xXenon135 / T_BURN * uNeutronFlux / 100; // weighted fast burn

        // Forward euler
        xIodine135 = xIodine135 + dXIodine135 * stepTime;
        xXenon135 = xXenon135 + dXXenon135 * stepTime;

        // Update Output variables
        yIodine = xIodine135;
        yXenon = xXenon135;
    }

    public double getYXenon() {
        return yXenon;
    }

    public double getYIodine() {
        return yIodine;
    }
    
    public void setStepTime(double stepTime) {
       this.stepTime = stepTime;
    }
}
