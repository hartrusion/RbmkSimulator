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
 * Describes the neutron flux as a state space representation model with the
 * reactivity present in the reactor as an input. The model has two inputs, both
 * are reactivities but with one of them, the absorber rods, being treated a
 * little different in terms of dynamic behavior.
 * <p>
 * Does not take into account how the reactivity is formed, it is a integral
 * system controlled by reactivities and some non-linear effects. Features a
 * prompt neutron excursion.
 * <p>
 * The difference between the input reactivities is multiplied by K_REACTIVITY
 * and then 1 is added to get the K_eff value.
 *
 * @author Viktor Alexander Hartung
 */
public class NeutronFluxModel implements Runnable {

    /**
     * Step time in Seconds
     */
    private double stepTime = 0.1;

    /**
     * Fraction of neutrons which are delayed neutrons as factor (not
     * percentage), expected is 0.7 % (0.007) but due to plutonium in the core
     * it happened to be 0.5 %. Defines when the prompt neutrons cause the
     * criticality accident.
     */
    private double beta = 0.005;

    /**
     * Speed multiplier for power rise as soon as kEff exceeds 1 + beta.
     */
    private final double PROMPT_EXCURSION_FACTOR = 9000;

    /**
     * Below 1 - beta, there are not enough neutrons to sustain any chain
     * reaction (I made this up!) and there will be a rapid power decrease
     * happening. This is to allow a sudden power drop.
     */
    private final double DECAY_FACTOR = 400;

    /**
     * The return value of criticalityFunction will be multiplied by this,
     * defining the integration speed of the neutron flux.
     */
    private final double K_INTEGRAL = 104.166666666667;

    /**
     * Factor of rod movement derivative part which will be added to the
     * effective neutron multiplication factor, just to add some weird behavior
     * to the reactor controls. I made this up. 0.9 seems a funny value
     */
    private final double K_DIFF_RODS = 0.9;

    /**
     * Time constant for the DT1 part of the control rod movement.
     */
    private final double T_DIFF_RODS = 5.0;

    /**
     * Fraction of reactivity that will be instantly available as neutrons,
     * directly changing the effective neutron multiplication factor. The other
     * part will be delayed with T_DELAYED_REACTIVITY. This is not to be
     * confused with the beta value, this will affect the dynamics of the system
     * and the reaction to the input.
     */
    private final double P_INSTANT = 0.8;

    /**
     * Time constant for the reactivity part 1-P_INSTANT which will be available
     * with a delay.
     */
    private final double T_DELAYED_REACTIVITY = 16.0;

    /**
     * The difference between uReactivity and uAbsorberRods will be multiplied
     * with this factor, ultimately resulting in the effective neutron
     * multiplication factor K_eff.
     */
    private final double K_REACTIVITY = 0.0001;

    /**
     * Fraction of thermal power that will be delayed as it occurs by delayed
     * decay instead of the uranium fission. This will be the part that is still
     * there and slowly decays after scram.
     */
    private final double P_DECAY = 0.062;

    /**
     * Time constant (seconds) for the delayed thermal heat production.
     */
    private final double T_DECAY = 100;

    /**
     * Reacitivity removed by the absorber rods. Will get a kinky DT1 effect
     * added to mess up the control system.
     */
    private double uAbsorberRods;

    /**
     * Reactivity added or removed, this can be fuel, xenon or voidings. As the
     * whole system is of an open integrator type, there is no need to make it
     * relative to anything.
     */
    private double uReactivity;

    /**
     * Modifies the heat production to one or the other side by just shifting it
     * depending on the inserted rods. 0: Equal heat, 1: full output of
     * undelayed heat on side 1, -1: everything on side 2. Should be a very low
     * value like -0.02 or so.
     */
    private double uSkew = 0.0;

    /**
     * State space variable. This is the neutron flux, designed to be a
     * percentage value between 0 and 100 %, with 100 % beeing 3200 MW thermal.
     */
    private double xNeutronFlux;

    /**
     * State space variable. Delayed part of the criticality.
     */
    private double xDelayedCriticality = (1 - P_INSTANT);

    /**
     * State space variable. Helper for realizing the DT1 function.
     */
    private double xDeltaRods;

    /**
     * State space variable. Represents the thermal power from decay that will
     * occur later
     */
    private double xDelayedThermalPower;

    /**
     * State space variable. Helper for realizing the T2 function on decay heat.
     */
    private double xFirstDelay;

    private double yNeutronFlux;
    private double yK;
    private double yNeutronRate;
    private double yThermalPower1, yThermalPower2; // In Megawatts
    private double yThermalPower;

    /**
     * For displaying small flux changes, we have this value that displays
     * log10(Flux/100). Other way round will be Flux = 10^(value+1).
     */
    private double yNeutronFluxLog;

    @Override
    public void run() {
        double dXNeutronFlux;
        double dXDelayedCriticality;
        double dXDeltaRods;
        double dXFirstDelay;
        double dXDelayedThermalPower;

        // Calculate state space variable derivatives
        dXNeutronFlux = K_INTEGRAL * criticalityFunction(xDelayedCriticality
                + (K_REACTIVITY * (uReactivity - uAbsorberRods) + 1 * P_INSTANT)
                - Math.min(0, // only rod out movement will cause a postiive DT1
                        K_REACTIVITY * K_DIFF_RODS
                        * (uAbsorberRods - xDeltaRods)));

        dXDelayedCriticality = ((uReactivity - uAbsorberRods) * K_REACTIVITY
                + 1.0) * (1 - P_INSTANT) / T_DELAYED_REACTIVITY
                - xDelayedCriticality / T_DELAYED_REACTIVITY;

        dXDeltaRods = (uAbsorberRods - xDeltaRods) / T_DIFF_RODS;

        dXFirstDelay = (xNeutronFlux * P_DECAY * 15.76 - xFirstDelay) / T_DECAY;

        dXDelayedThermalPower = (xFirstDelay - xDelayedThermalPower) / T_DECAY;

        // Forward Euler
        xNeutronFlux = Math.min(
                Math.max( // There will be no negative neutron flux.
                        xNeutronFlux + dXNeutronFlux * stepTime, 0),
                937.5); // Limit to 937.5 MW (833.3 percent of 3200 MW)
        xDelayedCriticality += dXDelayedCriticality * stepTime;
        xDeltaRods += dXDeltaRods * stepTime;
        xFirstDelay += dXFirstDelay * stepTime;
        xDelayedThermalPower += dXDelayedThermalPower * stepTime;

        // Update Output variables
        yK = xDelayedCriticality // hide the DT-part, just for fun.
                + (K_REACTIVITY * (uReactivity - uAbsorberRods) + 1.0)
                * P_INSTANT;

        yNeutronFlux = xNeutronFlux;
        if (yNeutronFlux > 0.0) {
            yNeutronRate = dXNeutronFlux * 10;
        } else { // no negative rate if flux reached 0.0
            yNeutronRate = 0;
        }
        // Limit the neutron flux log output, 1e-4 with fluxlog = -6 seemed
        // fine, the rxmodel used -5.28, lets go for -5.3 here.
        // it is 10^-5.3 * 100 % is 5.0118723e-4 %
        if (xNeutronFlux < 5.0118723e-4) {
            yNeutronFluxLog = -5.3;
        } else {
            yNeutronFluxLog = Math.log10(xNeutronFlux / 100);
        }

        yThermalPower1 = yNeutronFlux * (1 - P_DECAY) * 16 * (uSkew + 1)
                + xDelayedThermalPower;
        yThermalPower2 = yNeutronFlux * (1 - P_DECAY) * 16 * (uSkew + 1)
                + xDelayedThermalPower;
        yThermalPower = yThermalPower1 + yThermalPower2;
    }

    /**
     * A function that manipulates k, which is the criticality, and returns the
     * integrator input (yet without the time constant). If k exceeds 1 + beta,
     * the return value will be multiplied with PROMPT_EXCURSION_FACTOR which
     * will increase the integrator dramatically, usually ending the simulation
     * and causing an accident.
     *
     * @param k Effective neutron multiplication factor, 1.0 for steady state.
     * @return Value to be integrated, 0 at steady state.
     */
    private double criticalityFunction(double k) {
        // y = m * (x - x0) + y0
        if (k < (1 - beta)) {
            // chain reaction dies below this factor if too many prompt neutrons
            // are absorbed, but not as fast as the power surge.
            return DECAY_FACTOR * (k - (1 - beta)) - beta;
        } else if (k > (1 + beta)) {
            // Prompt neutron power excursion
            return PROMPT_EXCURSION_FACTOR * (k - (1 + beta)) + beta;
        } else {
            // Power regulation with delayed neutrons
            return k - 1;
        }
    }

    /**
     * Inputs of the state space model. Both have to be set with one method as
     * they all have to be set each cycle.
     *
     * @param uAbsorberRods Negative reactivity absorbed by absorber rods as a
     * value of 0..100%
     * @param uReactivity Positive reactivity present in the reactor, has to be
     * calculated accodingly as fuel - xenon + voids.
     */
    public void setInputs(double uAbsorberRods, double uReactivity) {
        this.uAbsorberRods = uAbsorberRods;
        this.uReactivity = uReactivity;
    }

    public double getYNeutronFlux() {
        return yNeutronFlux;
    }

    public double getYK() {
        return yK;
    }

    public double getYNeutronRate() {
        return yNeutronRate;
    }

    public double getYNeutronFluxLog() {
        return yNeutronFluxLog;
    }

    public double getYThermalPower1() {
        return yThermalPower1;
    }

    public double getYThermalPower2() {
        return yThermalPower2;
    }

    public double getYThermalPower() {
        return yThermalPower;
    }

    public void setStepTime(double stepTime) {
        this.stepTime = stepTime;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }
}
