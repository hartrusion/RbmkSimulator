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

import com.hartrusion.control.SetpointIntegrator;

/**
 * Describes and calculates a control rod entity. Holds information about what
 * kind of rod it is and also manages the position setting with a setpoint
 * integrator to emulate the drives behavior. An absorption can be retrieved
 * depending on the current rod position.
 *
 * @author Viktor Alexander Hartung
 */
public class ControlRod extends ReactorElement implements Runnable {

    private double absorption;

    private final ChannelType rodType;

    /**
     * Available manual movement rod speeds which can be selected with the
     * rodSpeedIndex variable.
     */
    private final double[] rodSpeeds = {0.1, 0.2, 0.3, 0.331};
    private int rodSpeedIndex = 1;

    public ChannelType getRodType() {
        return rodType;
    }

    /**
     * Setpoint integrator which represents rod position inside the core in
     * meters where 0 is the upper end stop with its output.
     */
    private final SetpointIntegrator swi = new SetpointIntegrator();

    /**
     * Get instance of the controlling setpoint integrator which defines the
     * control rod position.
     *
     * @return
     */
    public SetpointIntegrator getSwi() {
        return swi;
    }

    private boolean selected;

    private boolean automatic;

    public ControlRod(int x, int y, ChannelType rodType) {
        super(x, y);
        this.rodType = rodType;

        // initialize fully inserted
        if (rodType == ChannelType.SHORT_CONTROLROD) {
            swi.forceOutputValue(2.5);
        } else {
            swi.forceOutputValue(7.4);
        }
        swi.setMaxRate(rodSpeeds[rodSpeedIndex]);
        swi.setLowerLimit(0.0);
        swi.setUpperLimit(8.1);
    }

    @Override
    public void run() {
        swi.run();
        calculateAbsorption();
    }

    /**
     * Current absorption depending on the current position. This function
     * defines the behavior of the absorption in relation to the insertion
     * position.
     *
     * @return Value between 0.0 and 1.0
     */
    public double getAbsorption() {
        return absorption;
    }

    private void calculateAbsorption() {
        double position = swi.getOutput();
        if (rodType == ChannelType.SHORT_CONTROLROD) {
            // Short rods go the other way round and make a maximum of 0.6
            if (position <= 3.0) {
                absorption = 0.6; // small rod is fully pulled up inside core
                return;
            }
            if (position >= 7.2) {
                absorption = 0.0; // rod hanging below core, no more absorption
                return;
            } // interpolate between 3/0.6 and 7.2/0
            // y = (y2-y1) / (x2-x1) * (x-x1) + y1);
            absorption = (0.0 - 0.6) / (7.2 - 3.0) * (position - 3.0) + 0.6;
            return;
        }
        if (rodType == ChannelType.MANUAL_CONTROLROD) {
            // Manual control rods have a positive effect when they are fully
            // inserted, it's not much but it is there. This is used to trigger 
            // the accident.
            if (position <= 0.4) { // pos value here
                // dangerous area: this goes into wrong direction when inserting
                // until we hit pos meters. interpolate between 0/0.12 and pos/0
                absorption = 0.12 - position * (0.12/0.4);
                return;
            }
            if (position >= 7.3) {
                absorption = 1.0; // full insert
                return;
            }
            // interp 0.4/0 and 7.3/1
            // y = (y2-y1) / (x2-x1) * (x-x1) + y1);
            absorption = 1.0 / (7.3 - 0.4) * (position - 0.4);
            return;
        }
        if (rodType == ChannelType.AUTOMATIC_CONTROLROD) {
            // Those auto control rods do not have that effect, otherwise the
            // controller might get stuck in a positive loop in output 
            // mode, this would require the operator to do something that has 
            // only reasons in the way how the model is build and this is not
            // wanted.
            if (position <= 0) {
                absorption = 0.0; // fully withdrawn
                return;
            }
            if (position >= 7.3) {
                absorption = 1.0; // full insert
                return;
            }
            absorption = 1.0 / 7.3 * position;
            return;
        }
    }

    /**
     * The maximum possible absorption value for this control rod. Used to
     * determine the maximum absorption over all rods.
     *
     * @return value between 0..1
     */
    public double getMaxAbsorption() {
        if (rodType == ChannelType.SHORT_CONTROLROD) {
            return 0.6;
        }
        return 1.0;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    public void rodSpeedIncrease() {
        if (rodSpeedIndex < rodSpeeds.length - 1) {
            rodSpeedIndex++;
        }
        swi.setMaxRate(rodSpeeds[rodSpeedIndex]);
    }

    public void rodSpeedDecrease() {
        if (rodSpeedIndex > 1) {
            rodSpeedIndex--;
        }
        swi.setMaxRate(rodSpeeds[rodSpeedIndex]);
    }
    
    public void rodSpeedMax() {
        rodSpeedIndex = rodSpeeds.length - 1;
        swi.setMaxRate(rodSpeeds[rodSpeedIndex]);
    }
}
