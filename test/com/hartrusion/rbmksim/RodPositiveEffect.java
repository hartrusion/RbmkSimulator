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

import static com.hartrusion.plot.VisualizeData.*;

/**
 *
 * @author Viktor Alexander Hartung
 */
public class RodPositiveEffect {

    ControlRod rod;

    private double[] xPosition;
    private double[] yAbsorption;
    private double[] yDisplacerBoost;

    RodPositiveEffect() {
        rod = new ControlRod(1, 1, ChannelType.MANUAL_CONTROLROD);
        rod.rodSpeedMax();
    }

    public void run() {
        final double stepSize = 0.1;
        int steps = (int) (7.3 / stepSize) + 1;
        xPosition = new double[steps];
        yDisplacerBoost = new double[steps];
        yAbsorption = new double[steps];

        // initial rod position is 7.3 m - set to 0.0 to insert
        rod.getSwi().setInputMin();
        for (int idx = 0; idx < steps; idx++) {
            rod.getSwi().forceOutputValue(stepSize * (double) idx);
            rod.run(); // updates output for first time
            xPosition[idx] = (double) rod.getSwi().getOutput();
            yAbsorption[idx] = rod.getAbsorption();
            yDisplacerBoost[idx] = rod.getDisplacerBoost();
        }

        plot(xPosition, yAbsorption);
        xlabel("Rod Position (m)");
        ylabel("Absorption coefficient");
        
        figure();
        plot(xPosition, yDisplacerBoost);
        xlabel("Rod Position (m)");
        ylabel("Displacer Boost Value");
    }

    public static void main(String[] args) {
        RodPositiveEffect app = new RodPositiveEffect();
        app.run();
    }
}
