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
public class XenonBuildup {

    XenonModel xenonModel = new XenonModel();

    private double[] xTime, yFlux, yIodine, yXenon;

    public void run() {
        final double simulationTime = 60 * 60; // 1h
        final double stepTime = 40;
        int steps = (int) (simulationTime / stepTime);

        double neutronFlux;

        yFlux = new double[steps];
        xTime = new double[steps];
        yIodine = new double[steps];
        yXenon = new double[steps];
        
        xenonModel.setStepTime(stepTime);

        for (int idx = 0; idx < steps; idx++) {
            xTime[idx] = (double) idx * stepTime / 60;
            if (xTime[idx] > 35) {
                neutronFlux = 5;
            } else  if (xTime[idx] > 30) {
                neutronFlux = 40;
            } else if (xTime[idx] > 5) {
                neutronFlux = 100;
            } else {
                neutronFlux = 0;
            }
            yFlux[idx] = neutronFlux;
            xenonModel.setInputs(neutronFlux);
            xenonModel.run();
            yIodine[idx] = xenonModel.getYIodine();
            yXenon[idx] = xenonModel.getYXenon();
        }

        plot(xTime, yXenon);
        hold("on");
        plot(xTime, yFlux);
        plot(xTime, yIodine);
        axis(0, 60, 0, 250);
        xlabel("Time (Hours)");
        ylabel("Percentage");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        XenonBuildup app = new XenonBuildup();
        app.run();
    }
}
