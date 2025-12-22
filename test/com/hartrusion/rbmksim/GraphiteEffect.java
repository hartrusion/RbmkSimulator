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
public class GraphiteEffect {

    GraphiteEffectModel graphiteModel = new GraphiteEffectModel();

    private double[] xTime, uFlux, yGraphite;

    public void run() {
        final double simulationTime = 60 * 60; // 1h
        final double stepTime = 5;
        int steps = (int) (simulationTime / stepTime);

        double neutronFlux;

        uFlux = new double[steps];
        xTime = new double[steps];
        yGraphite = new double[steps];
        
        graphiteModel.setStepTime(stepTime);
        // Maybe we need initital state later

        for (int idx = 0; idx < steps; idx++) {
            xTime[idx] = (double) idx * stepTime / 60; // Minutes
            // Accident sequence
            if (xTime[idx] > 49) {
                neutronFlux = 6.3; // 200 MW
            } else if (xTime[idx] > 48) {
                // Raise to 6.3 % (200 MW) in 1 Minute
                neutronFlux = 6 * xTime[idx] - 287.7;
            } else if (xTime[idx] > 45) {
                neutronFlux = 0.9; // 30 MW
            } else if (xTime[idx] > 42) {
                // Ramp down to 0.9 % (30 MW) in 3 minutes
                neutronFlux = -16.3666666666667 * xTime[idx] + 737.4;
            } else if (xTime[idx] > 7) {
                neutronFlux = 50; // 1600 MW
            } else if (xTime[idx] > 5) {
                // Ramp down to 50 % (1600 MW) in 2 minutes
                neutronFlux = -25 * xTime[idx] + 225;
            } else {
                neutronFlux = 100;
            }
            // Planned, non-accidential seuqence withou
//            if (xTime[idx] > 19) {
//                neutronFlux = 6.3; // 200 MW
//            } else if (xTime[idx] > 18) {
//                // Raise to 6.3 % (200 MW) in 1 Minute
//                neutronFlux = 5.4 * xTime[idx] - 96.3;
//            } else if (xTime[idx] > 15) {
//                neutronFlux = 0.9; // 30 MW
//            } else if (xTime[idx] > 12) {
//                // Ramp down to 0.9 % (30 MW) in 3 minutes
//                neutronFlux = -16.3666666666667 * xTime[idx] + 246.4;
//            } else if (xTime[idx] > 7) {
//                neutronFlux = 50; // 1600 MW
//            } else if (xTime[idx] > 5) {
//                // Ramp down to 50 % (1600 MW) in 2 minutes
//                neutronFlux = -25 * xTime[idx] + 225;
//            } else {
//                neutronFlux = 100;
//            }
            uFlux[idx] = neutronFlux;
            graphiteModel.setInputs(neutronFlux);
            graphiteModel.run();
            yGraphite[idx] = graphiteModel.getYGraphie();
        }

        plot(xTime, yGraphite);
        hold("on");
        plot(xTime, uFlux);
        axis(0, 60, 0, 100);
        xlabel("Simulation Time (Minutes)");
        ylabel("Percentage");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        GraphiteEffect app = new GraphiteEffect();
        app.run();
    }
}
