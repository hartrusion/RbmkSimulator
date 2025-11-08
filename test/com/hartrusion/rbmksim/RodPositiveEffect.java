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

    private double[] xTime;
    private double[] yAbsorption;

    RodPositiveEffect() {
        rod = new ControlRod(1, 1, ChannelType.MANUAL_CONTROLROD);
    }

    public void run() {
        // It's fixed that the rods need 22 Seconds from top to bottom. 
        // Setting stepTime to 0.5 then will lead to 44 data points.
        final double stepTime = 0.5;
        int steps = (int) (25.0 / stepTime);
        xTime = new double[steps];
        yAbsorption = new double[steps];

        rod.getSwi().setStepTime(stepTime);
        
        rod.run(); // updates output for first time
        
        // initial rod position is 7.3 m - set to 0.0 to insert
        rod.getSwi().setInputMin();
        for (int idx = 0; idx < steps; idx++) {
            xTime[idx] = (double) idx * stepTime;
            yAbsorption[idx] = rod.getAbsorption();
            rod.run();
        }
        
        plot(xTime, yAbsorption);

    }

    public static void main(String[] args) {
        RodPositiveEffect app = new RodPositiveEffect();
        app.run();
    }
}
