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
     * Fixed rate as soon as reactivity exceeds beta once. Will be multiplied
     * with K_INTEGRAL to get the integrator input.
     */
    private final double PROMPT_EXCURSION_RATE = 0.8;

    /**
     * Below 1 - beta, there are not enough neutrons to sustain any chain
     * reaction (I made this up!) and there will be a rapid power decrease
     * happening. This is to allow a sudden power drop.
     */
    private final double DECAY_FACTOR = 200;

    /**
     * As we will have prompt neutron excursion on k = 1 + beta, there should be
     * fast neutron decay on k = 1 - beta to have a faster scram effect. I made
     * this up (as stated above) but to have the chain reaction collapse even
     * faster, we use k = 1 - beta * f to make the scram button work better.
     */
    private final double NEGATIVE_BETA_FACTOR = 0.7;

    /**
     * The difference between uReactivity and uAbsorberRods will be multiplied
     * with this factor, ultimately resulting in the reactivity of the reactor.
     */
    private final double K_REACTIVITY = 0.0005;

    /**
     * The return value of criticalityFunction will be multiplied by this,
     * defining the integration speed of the neutron flux. This is directly
     * affecting how the neutronFluxRate value is calculated from K_eff.
     * <p>
     * The K_INTEGRAL value multiplied with the beta value will indicate the
     * neutron rate in %/s.
     */
    private final double K_INTEGRAL = 480;

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
     * Time constant for filtering the neutron rate output, there is a need for
     * a filtered output for the control feedback.
     */
    private final double T_RATEFILTER = 1.8;

    /**
     * The positive feedback path of the neutron flux to its own grow rate. This
     * number is "a" of 1-e^(-a*x).
     */
    private final double A_POSITIVE_FEEDBACK = 0.4;

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
    private double xDelayedCriticality = 0.0;

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

    /**
     * Sets the model to a steady state for the given initial conditions
     *
     * @param uAbsorberRods Negative reactivity absorbed by absorber rods as a
     * value of 0..100%
     * @param uReactivity Positive reactivity present in the reactor, has to be
     * calculated accordingly as fuel - xenon + voids.
     */
    public void setInitialConditions(double uAbsorberRods, double uReactivity,
            double neutronFlux) {
        // limits: see run method, its described there.
        xNeutronFlux = Math.min(Math.max(neutronFlux, 5.0118723e-4), 937.5);
        xDeltaRods = uAbsorberRods;
        xDelayedCriticality = (uReactivity - uAbsorberRods) * K_REACTIVITY
                * (1 - P_INSTANT);
    }

    @Override
    public void run() {
        double dXNeutronFlux;
        double dXDelayedCriticality;
        double dXDeltaRods;
        double dXFirstDelay;
        double dXDelayedThermalPower;
        double dXNeutronRateDelay;
        double reactivity, dynamisedReactivity, critFunctionResult,
                posFeedbackMultiplier;
        boolean zeroPower = false;
        // Now, first, we will have some values valvulated from the current
        // state variables and the inputs.
        // The reactivity coefficient rho:
        reactivity = (uReactivity - uAbsorberRods) * K_REACTIVITY;

        // Input value of the criticality function which adds the prompt 
        // excursion and also a fast neutron death. This is the third summizer
        // on the svg schematic.
        dynamisedReactivity = xDelayedCriticality
                // prompt path:
                + reactivity * P_INSTANT
                // DT1 rod lift part:
                - Math.min(0, // only rod out movement will cause a postiive DT1
                        K_REACTIVITY * K_DIFF_RODS
                        * (uAbsorberRods - xDeltaRods));

        // Apply the criticality rate 
        critFunctionResult = criticalityFunction(dynamisedReactivity);

        if (!promptExcursion) {
            // This is the input on the multiplier block that is used to generate 
            // the positive feedback behavior in the beginning
            posFeedbackMultiplier = 1.0 - Math.exp(
                    -A_POSITIVE_FEEDBACK * xNeutronFlux);
        } else {
            // Allow fast runaway even for very small neutron flux values
            // immediately
            posFeedbackMultiplier = 1.0;
        }

        // Generate the diff inputs for the integral blocks
        dXNeutronFlux = K_INTEGRAL * posFeedbackMultiplier * critFunctionResult;

        dXDelayedCriticality = reactivity
                * (1 - P_INSTANT) / T_DELAYED_REACTIVITY
                - xDelayedCriticality / T_DELAYED_REACTIVITY;

        dXDeltaRods = (uAbsorberRods - xDeltaRods) / T_DIFF_RODS;

        dXFirstDelay = (xNeutronFlux * P_DECAY * 15.76 - xFirstDelay) / T_DECAY;

        dXDelayedThermalPower = (xFirstDelay - xDelayedThermalPower) / T_DECAY;

        dXNeutronRateDelay = (dXNeutronFlux - xNeutronRateDelay) / T_RATEFILTER;

        // Forward Euler
        xNeutronFlux = xNeutronFlux + dXNeutronFlux * stepTime;
        if (xNeutronFlux >= 1200) {
            // Limit to 12 times the normal flux value.
            // the megawatt-value will be limited elsewhere. This happens on
            // prompt excursion and is irreversible anyway.
            xNeutronFlux = 1200;
        } else if (xNeutronFlux <= 1e-4) {
            // Limit the neutron flux. The lower limit is defined using the log
            // value. RXModel had something like -5.28, we use -6.0 here as this
            // is set as the low end of the plot view scale.
            // 10^-5.3 * 100 % is 5.0118723e-4 %
            // 10^-6 * 100 % = 1e-4 %
            xNeutronFlux = 1e-4;
            zeroPower = true;
        }
        xDelayedCriticality += dXDelayedCriticality * stepTime;
        xDeltaRods += dXDeltaRods * stepTime;
        xFirstDelay += dXFirstDelay * stepTime;
        if (!promptExcursion) {
            xDelayedThermalPower += dXDelayedThermalPower * stepTime;
            // freeze this effect on explosion to stop vales from moving.
        }
        xNeutronRateDelay += dXNeutronRateDelay * stepTime;

        // Update Output variables
        if (!promptExcursion) {
            yReactivity = reactivity;
            yK = - 1 / (reactivity - 1);
            // Those values are freezed to their last value on prompt neutron
            // excursion.
        }

        yNeutronFlux = Math.min(xNeutronFlux, 937.5); // put old limit here

        // Neutron Rate is given in 10%/s
        if (zeroPower) {
            yNeutronRate = 0;
            yNeutronRateFiltered = 0;
        } else {
            yNeutronRate = dXNeutronFlux * 10;
            yNeutronRateFiltered = xNeutronRateDelay * 10;
        }

        yNeutronFluxLog = Math.log10(xNeutronFlux / 100);

        // Limit the total thermal power output to 30.000 Megawatts
        yThermalPower1 = Math.min(15000,
                xNeutronFlux * (1 - P_DECAY) * 16 * (uSkew + 1)
                + xDelayedThermalPower);
        yThermalPower2 = Math.min(15000,
                xNeutronFlux * (1 - P_DECAY) * 16 * (uSkew + 1)
                + xDelayedThermalPower);
        yThermalPower = yThermalPower1 + yThermalPower2;
    }

    /**
     * A function that manipulates the manipulated, dynamic rho value and
     * returns the integrator input (yet without the time constant). If rho
     * exceeds beta, the return value will be PROMPT_EXCURSION_RATE which will
     * increase the integrator dramatically, usually ending the simulation and
     * causing an accident. It is a hard switch that can't be stopped.
     *
     * @param rho Effective neutron multiplication factor, 0.0 for steady state.
     * @return Value to be integrated, 0 at steady state.
     */
    private double criticalityFunction(double rho) {
        if (promptExcursion) {
            return PROMPT_EXCURSION_RATE;
        } else if (rho < (-beta * NEGATIVE_BETA_FACTOR)) {
            // chain reaction dies below this factor if too many prompt neutrons
            // are absorbed, but not as fast as the power surge.
            // y = m * (x - x0) + y0
            return DECAY_FACTOR * (rho - (-beta * NEGATIVE_BETA_FACTOR))
                    - beta * NEGATIVE_BETA_FACTOR;
        } else if (rho > beta) {
            // Prompt neutron power excursion. Unstoppable.
            promptExcursion = true;
            return PROMPT_EXCURSION_RATE;
        } else {
            // unmodified input value
            return rho;
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

    /**
     * Thermal power for loop 1, including the decay heat.
     *
     * @return Power in Megawatts.
     */
    public double getYThermalPower1() {
        return yThermalPower1;
    }

    /**
     * Thermal power for loop 2, including the decay heat.
     *
     * @return Power in Megawatts.
     */
    public double getYThermalPower2() {
        return yThermalPower2;
    }

    /**
     * Thermal power (sum), including the decay heat.
     *
     * @return Power in Megawatts.
     */
    public double getYThermalPower() {
        return yThermalPower;
    }

    public void setStepTime(double stepTime) {
        this.stepTime = stepTime;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public boolean isReactorIntact() {
        return (xNeutronFlux < 500);
    }

    public void setStateSpaceVariable(int idx, double x) {
        switch (idx) {
            case 0 -> {
                xNeutronFlux = x;
                yNeutronFlux = xNeutronFlux;
            }
            case 1 ->
                xDelayedCriticality = x;
            case 2 ->
                xDeltaRods = x;
            case 3 ->
                xDelayedThermalPower = x;
            case 4 ->
                xFirstDelay = x;
            case 5 ->
                xNeutronRateDelay = x;
            case 6 ->
                uAbsorberRods = x;
            case 7 ->
                uReactivity = x;
            case 8 ->
                uSkew = x;
        }
    }

    public double getStateSpaceVariable(int idx) {
        switch (idx) {
            case 0:
                return xNeutronFlux;
            case 1:
                return xDelayedCriticality;
            case 2:
                return xDeltaRods;
            case 3:
                return xDelayedThermalPower;
            case 4:
                return xFirstDelay;
            case 5:
                return xNeutronRateDelay;
            case 6:
                return uAbsorberRods;
            case 7:
                return uReactivity;
            case 8:
                return uSkew;
        }
        throw new IllegalArgumentException("Provided index not exsisting.");
    }
}
