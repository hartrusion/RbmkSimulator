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
     * Counting of rods starts with this number.
     */
    public static final int MIN_NUMBER = 20;

    /**
     * Counting of rods ends with this number.
     */
    public static final int MAX_NUMBER = 42;

    /**
     * Total number of elements possible in one direction
     */
    public static final int LENGTH = 23;

    /**
     * Returns the type of channel for a given coordinate.
     *
     * @param idx first number, Y, from bottom to top. 20 to 42.
     * @param jdx second number, X, from left to right 20 to 42.
     *
     * @return
     */
    public static ChannelType getChannelType(int idx, int jdx) {
        if (idx < MIN_NUMBER || idx > MAX_NUMBER
                || jdx < MIN_NUMBER || jdx > MAX_NUMBER) {
            // Outside defined range:
            return ChannelType.VOID;
        }
        // Only the lower quarter is described, rest is done using symmetry.
        // Generate symmetry:
        if (idx > 31) {
            idx = 62 - idx;
        }
        if (jdx > 31) {
            jdx = 62 - jdx;
        }
        switch (idx) {
            case 20:
                if (jdx >= 29) {
                    return ChannelType.FUEL;
                } else {
                    return ChannelType.VOID;
                }
            case 21:
                if (jdx >= 26) {
                    return ChannelType.FUEL;
                } else {
                    return ChannelType.VOID;
                }
            case 22:
                if (jdx >= 24) {
                    if (jdx == 28 || jdx == 31) {
                        return ChannelType.MANUAL_CONTROLROD;
                    } else {
                        return ChannelType.FUEL;
                    }
                } else {
                    return ChannelType.VOID;
                }
            case 23:
                if (jdx >= 23) {
                    return ChannelType.FUEL;
                } else {
                    return ChannelType.VOID;
                }
            case 24:
                if (jdx >= 22) {
                    return ChannelType.FUEL;
                } else {
                    return ChannelType.VOID;
                }
            case 25:
                if (jdx >= 22) {
                    return switch (jdx) {
                        case 25, 28 ->
                            ChannelType.MANUAL_CONTROLROD;
                        case 31 ->
                            ChannelType.AUTOMATIC_CONTROLROD;
                        default ->
                            ChannelType.FUEL;
                    };
                } else {
                    return ChannelType.VOID;
                }
            case 26, 27:
                if (jdx >= 21) {
                    return ChannelType.FUEL;
                } else {
                    return ChannelType.VOID;
                }
            case 28:
                if (jdx >= 21) {
                    return switch (jdx) {
                        case 22, 25, 31 ->
                            ChannelType.MANUAL_CONTROLROD;
                        case 28 ->
                            ChannelType.SHORT_CONTROLROD;
                        default ->
                            ChannelType.FUEL;
                    };
                } else {
                    return ChannelType.VOID;
                }
            case 29, 30:
                return ChannelType.FUEL;
            case 31:
                return switch (jdx) {
                    case 22, 28 ->
                        ChannelType.MANUAL_CONTROLROD;
                    case 25, 31 ->
                        ChannelType.AUTOMATIC_CONTROLROD;
                    default ->
                        ChannelType.FUEL;
                };
        }
        return ChannelType.VOID;
    }
}
