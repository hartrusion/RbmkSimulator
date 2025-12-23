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
 * The graphite effect is named after the influence of the graphite temperature
 * with its coefficient on reactivity but has nothing to do with it. It turned
 * out that xenon is not sufficient to trigger the accident so another effect
 * has to be taken into account.
 * <p>
 * This thing here models a value that will eat up reactivity after having a
 * long time on 50 % power and further reducing power after that.
 *
 * @author Viktor Alexander Hartung
 */
public class GraphiteEffectModel implements Runnable {

    /**
     * Step time in Seconds
     */
    private double stepTime = 0.1;

    /**
     * Time constant for the behavior of the whole effect in seconds. This is
     * set to a higher value compared to the neutron flux
     */
    private static final double T_GRAPHITE = 600;

    /**
     * The output value will get a T1 behavior with this time constant applied.
     */
    private static final double T_OUT = 20;

    private double uNeutronFlux;
    private double xHidden;
    private double xGraphite;

    public void setInputs(double uNeutronFlux) {
        this.uNeutronFlux = uNeutronFlux;
    }

    @Override
    public void run() {
        double dXHidden;
        double dXGraphite;

        // Calculate state space variable derivatives
        dXHidden = (fluxFilter(uNeutronFlux)
                - feedbackWeighting(uNeutronFlux) * xHidden)
                / T_GRAPHITE;
        dXGraphite = (outputWeighting(uNeutronFlux) * xHidden
                - xGraphite) / T_OUT;

        // Forward euler
        xHidden += dXHidden * stepTime;
        xGraphite += dXGraphite * stepTime;

        // Update Output variables
    }

    public void setStepTime(double stepTime) {
        this.stepTime = stepTime;
    }

    public double getHiddenValue() {
        return xHidden;
    }

    public double getYGraphie() {
        return xGraphite;
    }

    /**
     * Filters the relevant flux values. Returns 100 between 40 and 60 and
     * returns 0 below 20 and above 70. Between those there will be linear
     * interpolation. This is to have the effect build up only in a certain
     * power range.
     *
     * @param flux from 0 to 100
     * @return value between 0 and 100
     */
    private double fluxFilter(double flux) {
        if (flux <= 20 || flux > 70) {
            return 0.0;
        } else if (flux > 20 && flux <= 30) {
            return 10 * flux - 200;
        } else if (flux > 60 && flux <= 70) {
            return -10 * flux + 700;
        }
        // between 40 and 60:
        return 100;
    }

    /**
     * Weighted feedback factor that will influence the decay speed of the
     * integrated value. Between 1 and 10 % there is only a factor of 0.1.
     *
     * @param flux from 0 to 100
     * @return value between 0 and 1
     */
    private double feedbackWeighting(double flux) {
        if (flux <= 1.0) {
            return 1.0; // full decay
        } else if (flux > 1.0 && flux <= 20) {
            return 0.0; // no decay
        } else if (flux > 20 && flux < 40) {
            return 0.05 * flux - 1;
        }
        // >= 40:
        return 1.0;
    }

    /**
     * Returns a weight value to make the output only for low flux values. It
     * will simply disappear on higher flux values above 64 %.
     *
     * @param flux from 0 to 100
     * @return value between 0 and 1
     */
    private double outputWeighting(double flux) {
        if (flux <= 15) {
            return 1.0;
        } else if (flux > 15 && flux < 64) {
            return -0.0204081632653061 * flux + 1.30612244897959;
        }
        return 0;
    }

}
