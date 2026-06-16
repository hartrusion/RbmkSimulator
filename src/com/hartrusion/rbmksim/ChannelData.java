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
 * Defines the reactor layout.
 *
 * @author Viktor Alexander Hartung
 */
public final class ChannelData {

    /**
     * Counting of rods starts with this number. The start number can be a 
     * higher number than 10 to have some numbers more centered between 1 and 10
     * so it does not looks like the rod count suddenly ends. More of a 
     * psychological effect.
     */
    public static final int MIN_NUMBER = 20;

    /**
     * Counting of rods ends with this number.
     */
    public static final int MAX_NUMBER = 42;

    /**
     * Total number of elements possible in one direction. This must be an odd 
     * number, so that there is a center element.
     */
    public static final int LENGTH = 23;

    /**
     * Center index of the reactor core. Row and column indices are symmetric
     * around this value.
     */
    public static final int CENTER = (MIN_NUMBER + MAX_NUMBER) / 2;

    /**
     * Returns the type of channel for a given coordinate. So far this is hand-
     * coded to match a certain pattern which can be found in the docs as svg.
     *
     * @param idx first number, Y, from bottom to top. MIN_NUMBER to MAX_NUMBER.
     * @param jdx second number, X, from left to right MIN_NUMBER to MAX_NUMBER.
     *
     * @return the type of channel at the given coordinates
     */
    public static ChannelType getChannelType(int idx, int jdx) {
        if (idx < MIN_NUMBER || idx > MAX_NUMBER
                || jdx < MIN_NUMBER || jdx > MAX_NUMBER) {
            // Outside defined range:
            return ChannelType.VOID;
        }
        // Only the lower quarter is described, rest is done using symmetry.
        // Generate symmetry:
        if (idx > CENTER) {
            idx = MIN_NUMBER + MAX_NUMBER - idx;
        }
        if (jdx > CENTER) {
            jdx = MIN_NUMBER + MAX_NUMBER - jdx;
        }
        switch (idx) {
            case MIN_NUMBER:
                if (jdx >= CENTER - 2) {
                    return ChannelType.FUEL;
                } else {
                    return ChannelType.VOID;
                }
            case MIN_NUMBER + 1:
                if (jdx >= CENTER - 5) {
                    return ChannelType.FUEL;
                } else {
                    return ChannelType.VOID;
                }
            case MIN_NUMBER + 2:
                if (jdx >= CENTER - 7) {
                    if (jdx == CENTER - 3 || jdx == CENTER) {
                        return ChannelType.MANUAL_CONTROLROD;
                    } else {
                        return ChannelType.FUEL;
                    }
                } else {
                    return ChannelType.VOID;
                }
            case MIN_NUMBER + 3:
                if (jdx >= CENTER - 8) {
                    return ChannelType.FUEL;
                } else {
                    return ChannelType.VOID;
                }
            case MIN_NUMBER + 4:
                if (jdx >= CENTER - 9) {
                    return ChannelType.FUEL;
                } else {
                    return ChannelType.VOID;
                }
            case MIN_NUMBER + 5:
                if (jdx >= CENTER - 9) {
                    return switch (jdx) {
                        case CENTER - 6, CENTER - 3 ->
                            ChannelType.MANUAL_CONTROLROD;
                        case CENTER ->
                            ChannelType.AUTOMATIC_CONTROLROD;
                        default ->
                            ChannelType.FUEL;
                    };
                } else {
                    return ChannelType.VOID;
                }
            case MIN_NUMBER + 6, MIN_NUMBER + 7:
                if (jdx >= CENTER - 10) {
                    return ChannelType.FUEL;
                } else {
                    return ChannelType.VOID;
                }
            case MIN_NUMBER + 8:
                if (jdx >= CENTER - 10) {
                    return switch (jdx) {
                        case CENTER - 9, CENTER - 6, CENTER ->
                            ChannelType.MANUAL_CONTROLROD;
                        case CENTER - 3 ->
                            ChannelType.SHORT_CONTROLROD;
                        default ->
                            ChannelType.FUEL;
                    };
                } else {
                    return ChannelType.VOID;
                }
            case MIN_NUMBER + 9, MIN_NUMBER + 10:
                return ChannelType.FUEL;
            case CENTER:
                return switch (jdx) {
                    case CENTER - 9, CENTER - 3 ->
                        ChannelType.MANUAL_CONTROLROD;
                    case CENTER - 6, CENTER ->
                        ChannelType.AUTOMATIC_CONTROLROD;
                    default ->
                        ChannelType.FUEL;
                };
        }
        return ChannelType.VOID;
    }

    /**
     * Gets the total number of channels with type {@link ChannelType#FUEL}.
     *
     * @return Number of FUEL channels in the reactor layout
     */
    public static int getNumberOfFuelChannels() {
        int count = 0;
        for (int idx = MIN_NUMBER; idx <= MAX_NUMBER; idx++) {
            for (int jdx = MIN_NUMBER; jdx <= MAX_NUMBER; jdx++) {
                if (getChannelType(idx, jdx) == ChannelType.FUEL) {
                    count++;
                }
            }
        }
        return count;
    }

   /**
     * Returns the assignment to a loop (1 or 2) for given coordinates. The 
     * original layout has an even number of channels with everything just 
     * shifted by one number (you can see that there is one row additionally
     * on two sides), but the control rods look like they would be placed in 
     * the center of the core. This would be more noticeable on this simulation
     * with the way smaller layout so the number is odd here and one part in 
     * center is assigned to loop 1 and the other part to loop 2.
     *
     * @param idx first number, Y, from bottom to top. MIN_NUMBER to MAX_NUMBER.
     * @param jdx second number, X, from left to right MIN_NUMBER to MAX_NUMBER.
     *
     * @return Loop number (1 or 2). If the coordinates are outside the defined 
     * range, -1 is returned.
     */

    public static int getLoop(int idx, int jdx) {
        if (idx < MIN_NUMBER || idx > MAX_NUMBER
                || jdx < MIN_NUMBER || jdx > MAX_NUMBER) {
            // Outside defined range:
            return -1;
        }
        if (jdx < CENTER) {
            return 1;
        } else if (jdx > CENTER) {
            return 2;
        }
        // else: Center row: decide with odd or even.
        if (jdx % 2 == 0) {
            return 1;
        } else {
            return 2;
        }
    }
}
