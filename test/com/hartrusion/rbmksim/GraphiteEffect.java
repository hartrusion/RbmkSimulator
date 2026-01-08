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

    public void testDoubleDrop() {
        XenonModel xenonModel = new XenonModel();
        GraphiteEffectModel graphiteModel = new GraphiteEffectModel();
        
        final double simulationTime = 60 * 60; // 1h
        final double stepTime = 5;
        int steps = (int) (simulationTime / stepTime);

        double neutronFlux;

        double[] uFlux = new double[steps];
        double[] xTime = new double[steps];
        double[] yIodine = new double[steps];
        double[] yXenon = new double[steps];
        double[] yGraphite = new double[steps];
        
        xenonModel.setStepTime(stepTime);
        xenonModel.setInititalState(100, 100); // start at full power
        graphiteModel.setStepTime(stepTime);

        for (int idx = 0; idx < steps; idx++) {
            xTime[idx] = (double) idx * stepTime / 60; // Minutes
            // Double drop (extremely high value, set ylim to 250)
            if (xTime[idx] > 15) {
                neutronFlux = 5;
            } else  if (xTime[idx] > 10) {
                neutronFlux = 40;
            } else {
                neutronFlux = 100;
            }

            uFlux[idx] = neutronFlux;
            xenonModel.setInputs(neutronFlux);
            xenonModel.run();
            graphiteModel.setInputs(neutronFlux);
            graphiteModel.run();
            yIodine[idx] = xenonModel.getYIodine();
            yXenon[idx] = xenonModel.getYXenon();
            yGraphite[idx] = graphiteModel.getYGraphie();
        }

        figure();
        plot(xTime, yXenon);
        hold("on");
        plot(xTime, uFlux);
        plot(xTime, yIodine);
        plot(xTime, yGraphite);
        axis(0, 60, 0, 150);
        xlabel("Simulation Time (Minutes)");
        ylabel("Percentage");
    }
    
    public void testAccident() {
        XenonModel xenonModel = new XenonModel();
        GraphiteEffectModel graphiteModel = new GraphiteEffectModel();
        
        final double simulationTime = 60 * 60; // 1h
        final double stepTime = 5;
        int steps = (int) (simulationTime / stepTime);

        double neutronFlux;

        double[] uFlux = new double[steps];
        double[] xTime = new double[steps];
        double[] yIodine = new double[steps];
        double[] yXenon = new double[steps];
        double[] yGraphite = new double[steps];
        
        xenonModel.setStepTime(stepTime);
        xenonModel.setInititalState(100, 100); // start at full power
        graphiteModel.setStepTime(stepTime);

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

            uFlux[idx] = neutronFlux;
            xenonModel.setInputs(neutronFlux);
            xenonModel.run();
            graphiteModel.setInputs(neutronFlux);
            graphiteModel.run();
            yIodine[idx] = xenonModel.getYIodine();
            yXenon[idx] = xenonModel.getYXenon();
            yGraphite[idx] = graphiteModel.getYGraphie();
        }

        figure();
        plot(xTime, yXenon);
        hold("on");
        plot(xTime, uFlux);
        plot(xTime, yIodine);
        plot(xTime, yGraphite);
        axis(0, 60, 0, 150);
        xlabel("Simulation Time (Minutes)");
        ylabel("Percentage");
    }
    
    public void testPlannedTest() {
        XenonModel xenonModel = new XenonModel();
        GraphiteEffectModel graphiteModel = new GraphiteEffectModel();
        
        final double simulationTime = 60 * 60; // 1h
        final double stepTime = 5;
        int steps = (int) (simulationTime / stepTime);

        double neutronFlux;

        double[] uFlux = new double[steps];
        double[] xTime = new double[steps];
        double[] yIodine = new double[steps];
        double[] yXenon = new double[steps];
        double[] yGraphite = new double[steps];
        
        xenonModel.setStepTime(stepTime);
        xenonModel.setInititalState(100, 100); // start at full power
        graphiteModel.setStepTime(stepTime);

        for (int idx = 0; idx < steps; idx++) {
            xTime[idx] = (double) idx * stepTime / 60; // Minutes
            
            // Planned, non-accidential seuqence withou
            if (xTime[idx] > 15) {
                neutronFlux = 21.8; // 700 MW
            } else if (xTime[idx] > 12) {
                // Ramp down to 21.8 % (700 MW) in 3 minutes
                neutronFlux = -9.4 * xTime[idx] + 162.8;
            } else if (xTime[idx] > 7) {
                neutronFlux = 50; // 1600 MW
            } else if (xTime[idx] > 5) {
                // Ramp down to 50 % (1600 MW) in 2 minutes
                neutronFlux = -25 * xTime[idx] + 225;
            } else {
                neutronFlux = 100;
            }

            uFlux[idx] = neutronFlux;
            xenonModel.setInputs(neutronFlux);
            xenonModel.run();
            graphiteModel.setInputs(neutronFlux);
            graphiteModel.run();
            yIodine[idx] = xenonModel.getYIodine();
            yXenon[idx] = xenonModel.getYXenon();
            yGraphite[idx] = graphiteModel.getYGraphie();
        }

        figure();
        plot(xTime, yXenon);
        hold("on");
        plot(xTime, uFlux);
        plot(xTime, yIodine);
        plot(xTime, yGraphite);
        axis(0, 60, 0, 150);
        xlabel("Simulation Time (Minutes)");
        ylabel("Percentage");
    }
    
        public void testReducedLoad() {
        XenonModel xenonModel = new XenonModel();
        GraphiteEffectModel graphiteModel = new GraphiteEffectModel();
        
        final double simulationTime = 60 * 60; // 1h
        final double stepTime = 5;
        int steps = (int) (simulationTime / stepTime);

        double neutronFlux;

        double[] uFlux = new double[steps];
        double[] xTime = new double[steps];
        double[] yIodine = new double[steps];
        double[] yXenon = new double[steps];
        double[] yGraphite = new double[steps];
        
        xenonModel.setStepTime(stepTime);
        xenonModel.setInititalState(100, 100); // start at full power
        graphiteModel.setStepTime(stepTime);

        for (int idx = 0; idx < steps; idx++) {
            xTime[idx] = (double) idx * stepTime / 60; // Minutes
            
            // Planned power drop
            if (xTime[idx] > 45) {
                neutronFlux = 100; // 3200 MW
            } else if (xTime[idx] > 42) {
                // Ramp back up to 100 %
                neutronFlux = 16.6666666666667 * xTime[idx] - 650;
            } else if (xTime[idx] > 7) {
                neutronFlux = 50; // 1600 MW
            } else if (xTime[idx] > 5) {
                // Ramp down to 50 % (1600 MW) in 2 minutes
                neutronFlux = -25 * xTime[idx] + 225;
            } else {
                neutronFlux = 100;
            }

            uFlux[idx] = neutronFlux;
            xenonModel.setInputs(neutronFlux);
            xenonModel.run();
            graphiteModel.setInputs(neutronFlux);
            graphiteModel.run();
            yIodine[idx] = xenonModel.getYIodine();
            yXenon[idx] = xenonModel.getYXenon();
            yGraphite[idx] = graphiteModel.getYGraphie();
        }

        figure();
        plot(xTime, yXenon);
        hold("on");
        plot(xTime, uFlux);
        plot(xTime, yIodine);
        plot(xTime, yGraphite);
        axis(0, 60, 0, 150);
        xlabel("Simulation Time (Minutes)");
        ylabel("Percentage");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        GraphiteEffect app = new GraphiteEffect();
        app.testDoubleDrop();
        app.testAccident();
        app.testPlannedTest();
        app.testReducedLoad();
    }
}
