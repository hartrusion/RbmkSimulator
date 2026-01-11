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
 * Provides a boolean value for each fuel cell and control rod. It is up to this
 * class to define how that value is build. It can get the state from the
 * reactor and provides boolean values on each element.
 * <p>
 * It is designed to be the measurement device that is connected between the
 * core and the core activity panel
 *
 * @author Viktor Alexander Hartung
 */
public class CoreIndicator {

    private ReactorCore core;
    private NeutronFluxModel neutrons;

    /**
     * Saves if that display on the specific tile is lit up or not. Index starts
     * with 0, so number 21-24 is [1][4].
     */
    private final boolean[][] active = new boolean[23][23];

    public void setCore(ReactorCore core) {
        this.core = core;
        this.neutrons = core.getNeutronModel();
    }

    public void run() {
        int idx, jdx, kdx, ldx;
        ControlRod rod;
        FuelElement fuel;

        for (idx = ChannelData.MIN_NUMBER; idx <= ChannelData.MAX_NUMBER; idx++) {
            for (jdx = ChannelData.MIN_NUMBER; jdx <= ChannelData.MAX_NUMBER; jdx++) {
                // get array index for channel coordinate thingy
                kdx = idx - ChannelData.MIN_NUMBER;
                ldx = jdx - ChannelData.MIN_NUMBER;
                switch (ChannelData.getChannelType(idx, jdx)) {
                    case FUEL:
                        fuel = (FuelElement) core.getElement(idx, jdx);
                        active[kdx][ldx]
                                = (neutrons.getYNeutronFluxLog() + fuel.getAffection() * 3 > -3.5);
                        break;
                    case VOID:
                        break;
                    case MANUAL_CONTROLROD:
                    case AUTOMATIC_CONTROLROD:
                        rod = (ControlRod) core.getElement(idx, jdx);
                        active[kdx][ldx] = rod.getAbsorption() < 0.99;
                        break;
                    case SHORT_CONTROLROD:
                        rod = (ControlRod) core.getElement(idx, jdx);
                        active[kdx][ldx] = rod.getAbsorption() < 0.59;
                        break;

                }
            }
        }
    }

    /**
     * To be called from the GUI, to determine if the given element should be
     * highlighted or not.
     *
     * @param idx
     * @param jdx
     * @return boolean
     */
    public boolean isHighlited(int idx, int jdx) {
        int kdx = idx - ChannelData.MIN_NUMBER;
        int ldx = jdx - ChannelData.MIN_NUMBER;
        return active[kdx][ldx];
    }
}
