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

import com.hartrusion.control.ControlCommand;
import com.hartrusion.control.SetpointIntegrator;
import com.hartrusion.util.ArraysExt;
import java.util.ArrayList;
import java.util.List;

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

    private double displacerBoost;

    private final ChannelType rodType;

    /**
     * Available manual movement rod speeds which can be selected with the
     * rodSpeedIndex variable.
     */
    private final double[] rodSpeeds = {0.1, 0.2, 0.3, 0.331};
    private int rodSpeedIndex = 1;

    /**
     * Holds a list of all fuel elements that are driven by this control rod
     */
    private final List<FuelElement> affectedFuel = new ArrayList<>();

    /**
     * Holds the multiplier which is applied to affectedFuel.get(index).
     */
    private double[] affectedFuelMultiplier = new double[8];

    /**
     * Radius in number of rods which determines if a fuel element is affected
     * by this rod and sets the strength of affection towards the element.
     */
    private double affectionRadius = 2.2;

    private double maxAbsorption = 1.0;

    /**
     * Sets a radius in which other surrounding fuel elements will be affected
     * by this control rod.
     *
     * @param affectionRadius value in rod numbers
     */
    public void setAffectionRadius(double affectionRadius) {
        this.affectionRadius = affectionRadius;
    }

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

    /**
     * The rod is selected - this is more of a tag. Selections are done on the
     * rod control panel but they are happening at the rods, not at the view
     * level.
     */
    private boolean selected;

    private ControlCommand automatic = ControlCommand.MANUAL_OPERATION;
    private ControlCommand oldAutomatic = null;

    /**
     * Name of the parameter that is this rods position
     */
    private final String positionParameter;

    /**
     * Property Change Event description which describes the control state.
     */
    private final String controlStateProperty;

    public ControlRod(int x, int y, ChannelType rodType) {
        super(x, y);
        this.rodType = rodType;

        if (rodType == ChannelType.SHORT_CONTROLROD) {
            maxAbsorption = 0.6;
        } else {
            maxAbsorption = 1.0;
        }

        // initialize fully inserted
        if (rodType == ChannelType.SHORT_CONTROLROD) {
            swi.forceOutputValue(2.5);
        } else {
            swi.forceOutputValue(7.4);
        }
        swi.setMaxRate(rodSpeeds[rodSpeedIndex]);
        swi.setLowerLimit(0.0);
        swi.setUpperLimit(8.1);

        // Generate a designator for the rod positions parameter. identifier 
        // was written in super call of this constructor.
        positionParameter = "Reactor#RodPosition" + identifier;
        controlStateProperty = "Reactor#RodControl" + identifier;
    }

    @Override
    public void run() {
        swi.run();
        calculateAbsorption();
        calculateDisplacerBoost();
        distributeAffection();

        // Send the current position as parameter
        outputValues.setParameterValue(positionParameter, swi.getOutput());

        // Send the control state to registered listeners
        if (automatic != oldAutomatic) {
            pcs.firePropertyChange(controlStateProperty,
                    oldAutomatic, automatic);
            oldAutomatic = automatic;
        }
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

    /**
     * The displacer boost is a value that is used to trigger the accident. It
     * will be a spike between 0.75 and 1.25 with a value of 1.0 at 1.0. For
     * positions below 0.75 and 1.25 it will return 0.0.
     *
     * @return Value between 0.0 and 1.0
     */
    public double getDisplacerBoost() {
        return displacerBoost;
    }

    private void calculateAbsorption() {
        double position = swi.getOutput();
        if (rodType == ChannelType.SHORT_CONTROLROD) {
            // Short rods go the other way round and make a maximum of 0.6
            if (position <= 3.0) {
                absorption = maxAbsorption; // small rod is fully pulled up inside core
                return;
            }
            if (position >= 7.2) {
                absorption = 0.0; // rod hanging below core, no more absorption
                return;
            } // interpolate between 3/maxAbsorption and 7.2/0
            // y = (y2-y1) / (x2-x1) * (x-x1) + y1);
            absorption = (0.0 - maxAbsorption) / (7.2 - 3.0)
                    * (position - 3.0) + maxAbsorption;
            return;
        }
        if (rodType == ChannelType.MANUAL_CONTROLROD) {
            // Manual control rods have a positive effect when they are fully
            // inserted, it's not much but it is there. Initially this was ued
            // to try to trigger the accident but it would also make the reactor
            // go propmt critical when the RPS triggers due to high neutron 
            // rates. The accident trigger will be done elsewhere but the effect
            // will stay here. This makes it generally visible.
            if (position <= 0.4) { // pos value here
                // dangerous area: this goes into wrong direction when inserting
                // until we hit pos meters. interpolate between 0/0.03 and pos/0
                absorption = 0.03 - position * (0.03 / 0.4);
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
     * Calculates a displacer boost value. This is further processed elsewhere
     * and used to trigger the accident. According to dyatlovs statements, it
     * took 3 seconds between pressing AZ-5 and the explosion, as it takes 22
     * seconds for a manual rod to full insert the 7.3 meters, 3 seconds of 22
     * will mean that there is about 1.0 meter of insertion at the time of the
     * first explosion. To make it harder to trigger the explosion. only a
     * narrow gap of 0,5 meters will be used.
     */
    private void calculateDisplacerBoost() {
        if (rodType != ChannelType.MANUAL_CONTROLROD) {
            displacerBoost = 0.0;
            return;
        }
        double position = swi.getOutput();
        if (position >= 1.25 || position <= 0.75) {
            displacerBoost = 0.0;
            return;
        }
        if (position == 1.0) {
            displacerBoost = 1.0;
        } else if (position > 1.0) {
            displacerBoost = -4 * position + 5;
        } else { // if (position < 1.0)
            displacerBoost = 4 * position - 3;
        }
    }

    /**
     * Distributes the current affection value from this control rod to all
     * surounding fuel rods.
     */
    private void distributeAffection() {
        FuelElement f;
        double affection = (maxAbsorption - absorption) / maxAbsorption;
        if (affection > 1e-18) {
            for (int idx = 0; idx < affectedFuel.size(); idx++) {
                affectedFuel.get(idx).addAffection(
                        affection * affectedFuelMultiplier[idx]);
            }
        }
    }

    /**
     * Initializes the affection from this control rod towards nearby fuel
     * elements.
     *
     * @param fuelElements List of all FuelElements.
     */
    public void initAffection(List<FuelElement> fuelElements) {
        double xDistSq, yDistSq; // distance to the element from this, squared
        double distance;
        for (FuelElement f : fuelElements) {
            xDistSq = (double) ((f.getX() - getX()) * (f.getX() - getX()));
            yDistSq = (double) ((f.getY() - getY()) * (f.getY() - getY()));
            distance = Math.sqrt(xDistSq + yDistSq);
            if (distance > affectionRadius) {
                continue; // fuel rod is too far away from this control rod.
            }
            // Add that fuel rod to the affectedFuel list
            affectedFuel.add(f);
            affectedFuelMultiplier = ArraysExt.newArrayLength(
                    affectedFuelMultiplier, affectedFuel.size());
            // Calculate the affection value between 0..1 relative to the 
            // distance.
            affectedFuelMultiplier[affectedFuel.size() - 1]
                    = (affectionRadius - distance) / affectionRadius;
            // Sum up the value in the affected fuel rod.
            f.addMaxAffection(affectedFuelMultiplier[affectedFuel.size() - 1]);
        }
    }

    /**
     * The maximum possible absorption value for this control rod. Used to
     * determine the maximum absorption over all rods.
     *
     * @return value between 0..1
     */
    public double getMaxAbsorption() {
        return maxAbsorption;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Get the state of the control mode which is used with this rod (this value
     * is saved in the rods).
     *
     * @return true if rod is supposed to be in auto mode.
     */
    public boolean isAutomatic() {
        return automatic.equals(ControlCommand.AUTOMATIC);
    }

    public void setAutomatic(boolean automatic) {
        if (automatic) {
            this.automatic = ControlCommand.AUTOMATIC;
            rodSpeedIndex = 2; // allow fast speed for auto
            swi.setMaxRate(rodSpeeds[rodSpeedIndex]);
        } else {
            this.automatic = ControlCommand.MANUAL_OPERATION;
        }

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
