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
 * <p>
 * The speed of the integration is determined by K_REACTIVITY * K_INTEGRAL.
 * <p>
 * To trigger the accident, a sudden increase in reactivity has to kick k_Eff
 * over 1 + beta. This is done by the control rods with a displacer effect.
 * Straight path through the model is u * K_REACTIVITY * P_INSTANT but also the
 * differential part of the rods will do its part. To get beta = 0.005 through
 * the straight path, we can have 0.005 / K_REACTIVITY / P_INSTANT = u which
 * results in 16.6 % - so a lack of 16.6% absorption will definitely trigger the
 * prompt neutron excursion. As we have also that diff part, less will be enough
 * ifs slope is sharp.
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
     * Fixed rate as soon as kEff exceeds 1 + beta. Will be multiplied with
     * K_INTEGRAL to get the integrator input.
     */
    private final double PROMPT_EXCURSION_RATE = 4.0;

    /**
     * Below 1 - beta, there are not enough neutrons to sustain any chain
     * reaction (I made this up!) and there will be a rapid power decrease
     * happening. This is to allow a sudden power drop.
     */
    private final double DECAY_FACTOR = 800;

    /**
     * As we will have prompt neutron excursion on k = 1 + beta, there should be
     * fast neutron decay on k = 1 - beta to have a faster scram effect. I made
     * this up (as stated above) but to have the chain reaction collapse even
     * faster, we use k = 1 - beta * f to make the scram button work better.
     */
    private final double NEGATIVE_BETA_FACTOR = 0.7;

    /**
     * The difference between uReactivity and uAbsorberRods will be multiplied
     * with this factor and 1.0 will be added, ultimately resulting in the
     * effective neutron multiplication factor K_eff.
     */
    private final double K_REACTIVITY = 0.0005;

    /**
     * The return value of criticalityFunction will be multiplied by this,
     * defining the integration speed of the neutron flux. This is directly
     * affecting how the neutronFluxRate value is calculated from K_eff.
     * <p>
     * The K_INTEGRAL value multiplied with the beta value will indicate the
     * neutron rate in %/s which will trigger prompt excursion. With an initial
     * value of 57.9 this was 2.9 10%/s for prompt excursion, however, this
     * takes way too much time on simulator for ramping up power. To make it a
     * better experience, a value of 85 was chosen. This will do a prompt
     * excursion with 4.25 10%/s. The lower values were taken from rxmodel but
     */
    private final double K_INTEGRAL = 85;

    /**
     * Factor of rod movement derivative part which will be added to the
     * effective neutron multiplication factor, just to add some weird behavior
     * to the reactor controls. I made this up. 0.9 seems a funny value.
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
    private final double P_INSTANT = 0.6;

    /**
     * Time constant for the reactivity part 1-P_INSTANT which will be available
     * with a delay.
     */
    private final double T_DELAYED_REACTIVITY = 16.0;

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
     * Below 1 % Neutron flux, the neutron rate used to calculate the flux by
     * integration gets multiplied with this factor. At 5 %, the full flux rate
     * is used, between 1% and 5 % it is going to be interpolated.
     */
    private final double STARTUP_FLUX_REDUCTION_FACTOR = 0.06;

    private final double STARTUP_FLUX_REDUCTION_START = 0.5;
    private final double STARTUP_FLUX_REDUCTION_END = 2.0;

    /**
     * Time constant for filtering the neutron rate output, there is a need for
     * a filtered output for the control feedback.
     */
    private final double T_RATEFILTER = 1.8;

    private final double mRed, bRed; // coefficients for linear interpolation

    /**
     * Feedback of flux to reactivity until 8 % flux is reached. Flux value gets
     * multiplied by this factor and subtracted from the value that will
     * generate k_Eff.
     */
    private final double K_FLUXFEEDB_START = 0.00012;

    private final double FLUXFEEDB_MAX = 8.0;

    /**
     * Marks the event of the prompt neutron excursion.
     */
    private boolean promptExcursion = false;

    /**
     * Reactivity removed by the absorber rods. Will get a kinky DT1 effect
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
     * depending on the inserted rods. 0: Equal heat, 1: full output of non
     * delayed heat on side 1, -1: everything on side 2. Should be a very low
     * value like -0.02 or so.
     */
    private double uSkew = 0.0;

    /**
     * State space variable. This is the neutron flux, designed to be a
     * percentage value between 0 and 100 %, with 100 % being 3200 MW thermal.
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

    private double xNeutronRateDelay;

    private double yNeutronFlux;
    private double yK;
    private double yReactivity;
    private double yNeutronRate, yNeutronRateFiltered;
    private double yThermalPower1, yThermalPower2; // In Megawatts
    private double yThermalPower;

    /**
     * For displaying small flux changes, we have this value that displays
     * log10(Flux/100). Other way round will be Flux = 10^(value+1).
     */
    private double yNeutronFluxLog;

    NeutronFluxModel() {
        // Calculate coefficients from given coefficient
        mRed = (1 - STARTUP_FLUX_REDUCTION_FACTOR)
                / (STARTUP_FLUX_REDUCTION_END - STARTUP_FLUX_REDUCTION_START);
        bRed = 1.0 - STARTUP_FLUX_REDUCTION_END * mRed;
    }

    @Override
    public void run() {
        double dXNeutronFlux;
        double dXDelayedCriticality;
        double dXDeltaRods;
        double dXFirstDelay;
        double dXDelayedThermalPower;
        double dXNeutronRateDelay;
        double reactivityDiff, kEff, neutronRate, critFunctionResult,
                limitedNeutronRate;

        // Value around 1,0 with same units as k_Eff without any delay.
        reactivityDiff = (uReactivity - uAbsorberRods) * K_REACTIVITY + 1.0
                - Math.min(FLUXFEEDB_MAX, xNeutronFlux) * K_FLUXFEEDB_START;

        // Calculate k_Eff
        // Delayed path:
        kEff = xDelayedCriticality
                // prompt path:
                + reactivityDiff * P_INSTANT
                // DT1 rod lift part:
                - Math.min(0, // only rod out movement will cause a postiive DT1
                        K_REACTIVITY * K_DIFF_RODS
                        * (uAbsorberRods - xDeltaRods));

        // Raw neutron rate without manipulating it out of k_Eff value
        critFunctionResult = criticalityFunction(kEff);
        neutronRate = K_INTEGRAL * critFunctionResult;

        // Manipulate the actually used neturon rate for startup, requiring to
        // mess around with the reactor controls for a longer time.
        if (xNeutronFlux < STARTUP_FLUX_REDUCTION_END && neutronRate >= 0.0) {
            if (xNeutronFlux < STARTUP_FLUX_REDUCTION_START) {
                dXNeutronFlux = STARTUP_FLUX_REDUCTION_FACTOR * neutronRate;
            } else {
                dXNeutronFlux = (mRed * xNeutronFlux + bRed) * neutronRate;
            }
        } else {
            dXNeutronFlux = neutronRate;
        }

        // generate a manipulated neutron rate for output that does go less than
        // 0 if there is no reaction with unit 10%/s
        if (xNeutronFlux > 0.0) {
            limitedNeutronRate = dXNeutronFlux * 10;
        } else { // no negative rate if flux reached 0.0
            limitedNeutronRate = 0;
        }

        dXDelayedCriticality = reactivityDiff
                * (1 - P_INSTANT) / T_DELAYED_REACTIVITY
                - xDelayedCriticality / T_DELAYED_REACTIVITY;

        dXDeltaRods = (uAbsorberRods - xDeltaRods) / T_DIFF_RODS;

        dXFirstDelay = (xNeutronFlux * P_DECAY * 15.76 - xFirstDelay) / T_DECAY;

        dXDelayedThermalPower = (xFirstDelay - xDelayedThermalPower) / T_DECAY;

        dXNeutronRateDelay = (limitedNeutronRate - xNeutronRateDelay) / T_RATEFILTER;

        // Forward Euler
        xNeutronFlux = Math.min(
                Math.max( // There will be no negative neutron flux.
                        xNeutronFlux + dXNeutronFlux * stepTime, 0),
                937.5); // Limit to 937.5 % (833.3 percent of 3200 MW)
        xDelayedCriticality += dXDelayedCriticality * stepTime;
        xDeltaRods += dXDeltaRods * stepTime;
        xFirstDelay += dXFirstDelay * stepTime;
        xDelayedThermalPower += dXDelayedThermalPower * stepTime;
        xNeutronRateDelay += dXNeutronRateDelay * stepTime;

        // Update Output variables
        yK = critFunctionResult + 1.0;
        yReactivity = (critFunctionResult + 1) / critFunctionResult;
                
        yNeutronFlux = xNeutronFlux;

        yNeutronRate = limitedNeutronRate;
        yNeutronRateFiltered = xNeutronRateDelay;

        // Limit the neutron flux log output, 1e-4 with fluxlog = -6 seemed
        // fine, the rxmodel used -5.28, lets go for -5.3 here.
        // it is 10^-5.3 * 100 % is 5.0118723e-4 %
//        if (xNeutronFlux < 5.0118723e-4) {
//            yNeutronFluxLog = -5.3;
//        } else {
        // use -6.0 as its the graphs bottom line
        // 10^-6.0 * 100 % is 5.0118723e-4 %
        if (xNeutronFlux < 1e-4) {
            yNeutronFluxLog = -6;
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
        if (promptExcursion) {
            return PROMPT_EXCURSION_RATE;
        } else if (k < (1 - beta * NEGATIVE_BETA_FACTOR)) {
            // chain reaction dies below this factor if too many prompt neutrons
            // are absorbed, but not as fast as the power surge.
            // y = m * (x - x0) + y0
            return DECAY_FACTOR * (k - (1 - beta * NEGATIVE_BETA_FACTOR))
                    - beta * NEGATIVE_BETA_FACTOR;
        } else if (k > (1 + beta)) {
            // Prompt neutron power excursion. Unstoppable
            promptExcursion = true;
            return PROMPT_EXCURSION_RATE;
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
     * calculated accordingly as fuel - xenon + voids.
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
    
    public double getYReactivity() {
        return yReactivity;
    }

    /**
     * Neutron Rate describes the change in the neutron flux (given in 0..100%)
     * in 10%/s.
     *
     * @return Neutron rate in 10%/s.
     */
    public double getYNeutronRate() {
        return yNeutronRate;
    }

    public double getYNeutronRateFiltered() {
        return yNeutronRateFiltered;
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
