/*
 * Copyright (C) 2026 Viktor Alexander Hartung
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
 * Has an affection which is a value how much this fuel rod is affected by
 * nearby control rods.
 *
 * @author Viktor Alexander Hartung
 */
public class FuelElement extends ReactorElement {

    private double maxSumOfAffections = 0.0;

    private double sumOfAffections = 0.0;

    /**
     * Normalized value between 0..1
     */
    private double affection = 0.0;

    public FuelElement(int x, int y) {
        super(x, y);
    }

    /**
     * Adds an affection and sums it up. Each fuel element then knows the total
     * affection that can be applied to it at any time.
     *
     * @param affection
     */
    public void addMaxAffection(double affection) {
        maxSumOfAffections += affection;
    }

    /**
     * Called by each control rod, it will add its part to this fuel element.
     *
     * @param affection
     */
    public void addAffection(double affection) {
        sumOfAffections += affection;
    }

    /**
     * Called before next calculation to prepare sum up of the values
     */
    public void prepareAffectionCalculation() {
        sumOfAffections = 0;
    }

    /**
     * Makes the affection available, to be called after all rods added their
     * value to this fuel element.
     */
    public void finalizeAffection() {
        affection = sumOfAffections / maxSumOfAffections;
    }

    public double getAffection() {
        return affection;
    }

}
