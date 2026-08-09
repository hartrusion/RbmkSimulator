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
 * <p>
 * Elements (Fuel channels or control rods) are described with two numbers, like
 * 23-38 or 29-31, in coordinates as Y-X. The coordinates begin with MIN_NUMBER
 * to have a certain nice looking distribution of numbers. While the original
 * RBMK has an even number of elements in one direction, this simulation does
 * not have such a thing.
 * <p>
 * Some of the functions were written with the help of Claude Code using Opus 5.
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
     * shifted by one number (you can see that there is one row additionally on
     * two sides), but the control rods look like they would be placed in the
     * center of the core. This would be more noticeable on this simulation with
     * the way smaller layout. The assignment is done with the same rotation 
     * symmetry as the evaporator assignments.
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
        // else (center row): Lower part belongs to lower left, upper row to
        // the upper right quadrant.
        if (idx <= 30) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Defines if the channel is a so called evaporator channel - those are the
     * channels which simulate the steam evaporation.
     * <p>
     * Only the lower left quarter is described by
     * {@link #isEvaporatorChannelLowerLeft(int, int)}, the remaining three
     * quarters are generated by rotating them around the center element in
     * steps of 90 degrees. Mirroring is not used here because the pattern is
     * not symmetric to the axes - it is a pinwheel like arrangement, so the
     * core is divided into four quarters which each get rotated back onto the
     * defined one:
     * <ul>
     * <li>lower right (rotated by 90 degrees counterclockwise),</li>
     * <li>upper right (rotated by 180 degrees),</li>
     * <li>upper left (rotated by 270 degrees counterclockwise).</li>
     * </ul>
     * The center element itself belongs to none of the rotated quarters and is
     * looked up directly in the definition.
     *
     * @param idx first number, Y, from bottom to top. MIN_NUMBER to MAX_NUMBER.
     * @param jdx second number, X, from left to right MIN_NUMBER to MAX_NUMBER.
     *
     * @return true if the channel at the given coordinates is an evaporator
     * channel, false otherwise or if the coordinates are outside the core.
     */
    public static boolean isEvaporatorChannel(int idx, int jdx) {
        if (idx < MIN_NUMBER || idx > MAX_NUMBER
                || jdx < MIN_NUMBER || jdx > MAX_NUMBER) {
            // Outside defined range:
            return false;
        }
        // Distance to the center element, negative means below respectively
        // left of the center:
        int deltaY = idx - CENTER;
        int deltaX = jdx - CENTER;
        if (deltaX > 0 && deltaY <= 0) {
            // Lower right quarter, rotate by 90 degrees clockwise:
            return isEvaporatorChannelLowerLeft(
                    MIN_NUMBER + MAX_NUMBER - jdx, idx);
        } else if (deltaX >= 0 && deltaY > 0) {
            // Upper right quarter, rotate by 180 degrees:
            return isEvaporatorChannelLowerLeft(
                    MIN_NUMBER + MAX_NUMBER - idx,
                    MIN_NUMBER + MAX_NUMBER - jdx);
        } else if (deltaX < 0 && deltaY >= 0) {
            // Upper left quarter, rotate by 270 degrees clockwise:
            return isEvaporatorChannelLowerLeft(
                    jdx, MIN_NUMBER + MAX_NUMBER - idx);
        }
        // Lower left quarter (and the center element), used as defined:
        return isEvaporatorChannelLowerLeft(idx, jdx);
    }

    /**
     * Hand-coded definition of the evaporator channels in the lower left
     * quarter of the core, which goes from Y = MIN_NUMBER to CENTER - 1 and
     * from X = MIN_NUMBER to CENTER. The rest of the core is derived from this
     * quarter by rotation, see {@link #isEvaporatorChannel(int, int)}.
     *
     * @param idx first coordinate, Y, from bottom to top. MIN_NUMBER to CENTER.
     * @param jdx second coord., X, from left to right MIN_NUMBER to CENTER.
     *
     * @return true if the channel at the given coordinates is an evaporator
     * channel, false otherwise.
     */
    private static boolean isEvaporatorChannelLowerLeft(int idx, int jdx) {
        if (idx == 20) {
            if (jdx == 30) {
                return true;
            }
        } else if (idx == 23) {
            if (jdx == 24 || jdx == 27 || jdx == 30) {
                return true;
            }
        } else if (idx == 26 || idx == 29) {
            if (jdx == 21 || jdx == 24 || jdx == 27 || jdx == 30) {
                return true;
            }
        }
        return false;
    }

    /**
     * Each fuel channel that has no evaporator for the water steam part gets
     * assigned to the channel with the evaporator element, see
     * {@link #isEvaporatorChannel(int, int)}.
     * <p>
     * Only the lower left quarter is described by
     * {@link #getEvaporatorChannelLowerLeft(int, int)}, the remaining three
     * quarters are generated with the same rotation around the center element
     * as used for {@link #isEvaporatorChannel(int, int)}. The given coordinates
     * are rotated back onto the defined quarter, and the returned evaporator
     * coordinates are rotated forward again into the quarter the request came
     * from.
     *
     * @param idx first number, Y, from bottom to top. MIN_NUMBER to MAX_NUMBER.
     * @param jdx second number, X, from left to right MIN_NUMBER to MAX_NUMBER.
     *
     * @return Identifier number (merged channel coordinate, for example, idx =
     * 25 and jdx = 30 which is "25-30" has the number 2530. Zero is returned if
     * no evaporator channel is assigned or if the coordinates are outside the
     * core.
     */
    public static int getEvaporatorChannel(int idx, int jdx) {
        if (idx < MIN_NUMBER || idx > MAX_NUMBER
                || jdx < MIN_NUMBER || jdx > MAX_NUMBER) {
            // Outside defined range:
            return 0;
        }
        // Distance to the center element, negative means below respectively
        // left of the center:
        int deltaY = idx - CENTER;
        int deltaX = jdx - CENTER;
        int identifier;
        if (deltaX > 0 && deltaY <= 0) {
            // Lower right quarter, rotate by 90 degrees clockwise:
            identifier = getEvaporatorChannelLowerLeft(
                    MIN_NUMBER + MAX_NUMBER - jdx, idx);
            // Rotate the result back by 90 degrees counterclockwise:
            return rotateIdentifier(identifier, getColumn(identifier),
                    MIN_NUMBER + MAX_NUMBER - getRow(identifier));
        } else if (deltaX >= 0 && deltaY > 0) {
            // Upper right quarter, rotate by 180 degrees:
            identifier = getEvaporatorChannelLowerLeft(
                    MIN_NUMBER + MAX_NUMBER - idx,
                    MIN_NUMBER + MAX_NUMBER - jdx);
            // Rotate the result back by 180 degrees:
            return rotateIdentifier(identifier,
                    MIN_NUMBER + MAX_NUMBER - getRow(identifier),
                    MIN_NUMBER + MAX_NUMBER - getColumn(identifier));
        } else if (deltaX < 0 && deltaY >= 0) {
            // Upper left quarter, rotate by 270 degrees clockwise:
            identifier = getEvaporatorChannelLowerLeft(
                    jdx, MIN_NUMBER + MAX_NUMBER - idx);
            // Rotate the result back by 270 degrees counterclockwise:
            return rotateIdentifier(identifier,
                    MIN_NUMBER + MAX_NUMBER - getColumn(identifier),
                    getRow(identifier));
        }
        // Lower left quarter (and the center element), used as defined:
        return getEvaporatorChannelLowerLeft(idx, jdx);
    }

    /**
     * Splits the row (Y) coordinate out of a merged channel identifier, for
     * example 25 out of 2530.
     *
     * @param identifier merged channel coordinate
     *
     * @return first coordinate, Y, from bottom to top.
     */
    public static int getRow(int identifier) {
        return identifier / 100;
    }

    /**
     * Splits the column (X) coordinate out of a merged channel identifier, for
     * example 30 out of 2530.
     *
     * @param identifier merged channel coordinate
     *
     * @return second coordinate, X, from left to right.
     */
    public static int getColumn(int identifier) {
        return identifier % 100;
    }

    /**
     * Merges the given rotated coordinates back into one identifier, keeping
     * the "no evaporator assigned" identifier zero untouched.
     *
     * @param identifier the identifier before the rotation, only used to detect
     * the unassigned case.
     * @param idx rotated first coordinate, Y, from bottom to top.
     * @param jdx rotated second coordinate, X, from left to right.
     *
     * @return merged rotated channel coordinate, or zero if no evaporator
     * channel is assigned.
     */
    private static int rotateIdentifier(int identifier, int idx, int jdx) {
        if (identifier == 0) {
            // No evaporator channel assigned, nothing to rotate:
            return 0;
        }
        return idx * 100 + jdx;
    }

    /**
     * Hand-coded definition of the evaporator channel assignment in the lower
     * left quarter of the core. There is not much logic behind this, it is just
     * drawn on paper to distribute the channels and hardcoded here. The rest of
     * the core is derived from this quarter by rotation, see
     * {@link #getEvaporatorChannel(int, int)}.
     *
     * @param idx first coordinate, Y, from bottom to top. MIN_NUMBER to CENTER.
     * @param jdx second coord., X, from left to right MIN_NUMBER to CENTER.
     *
     * @return Identifier number (merged channel coordinate, for example, idx =
     * 25 and jdx = 30 which is "25-30" has the number 2530.
     */
    private static int getEvaporatorChannelLowerLeft(int idx, int jdx) {
        if (idx >= 28 && idx <= 30) {
            if (idx == 28 && jdx == 20) {
                return 0;
            } else if (jdx >= 20 && jdx <= 22) {
                return 2921;
            } else if (jdx >= 23 && jdx <= 25) {
                return 2924;
            } else if (jdx >= 26 && jdx <= 28) {
                return 2927;
            } else if (jdx >= 29 && jdx <= 31) {
                return 2930;
            }
        } else if (idx == 24 && (jdx == 22 || jdx == 23)) {
            return 2621; // 2 elements from the lower row picked here
        } else if (idx >= 25 && idx <= 27) {
            if (jdx >= 21 && jdx <= 22) {
                return 2621;
            } else if (jdx >= 23 && jdx <= 25) {
                return 2624;
            } else if (jdx >= 26 && jdx <= 28) {
                return 2627;
            } else if (jdx >= 29 && jdx <= 31) {
                return 2630;
            }
        } else if (idx == 23 && jdx == 23) {
            return 2324;
        } else if (idx >= 22 && idx <= 24 && jdx >= 24) {
            if (jdx >= 24 && jdx <= 25) {
                return 2324;
            } else if (jdx >= 26 && jdx <= 28) {
                return 2327;
            } else if (jdx >= 29 && jdx <= 31) {
                return 2330;
            }
        } else if (idx == 21 && jdx == 26) {
            return 2324;
        } else if (idx == 21 && (jdx == 27 || jdx == 28)) {
            return 2030;
        } else if (idx <= 21) {
            if (jdx >= 29 && jdx <= 31) {
                return 2030;
            }
        }
        return 0;
    }
}
