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

import com.hartrusion.values.ValueReceiver;

/**
 * Collects the execution times of the cycles of MainLoop and calculates the
 * minimum, the maximum and the average over a window of a fixed number of
 * cycles. The simulation has to finish each cycle within the 100 ms cycle
 * period, so these values show how much time is left on the machine that runs
 * the simulation. Each completed window is written as output parameters into
 * the ValueReceiver of MainLoop, so that the values are part of the output
 * snapshot and reach the GUI and the network clients like all other measured
 * values of the simulator, where ServerView displays them. The window is
 * restarted afterwards, so the values are updated once per window and not on
 * every cycle.
 * <p>
 * LLM code generated with Claude Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public class CycleTimeStatistics {

    /**
     * Output parameter name of the shortest cycle time of the last window in
     * milliseconds.
     */
    public static final String PARAMETER_MIN = "MainLoop#CycleTimeMin";

    /**
     * Output parameter name of the longest cycle time of the last window in
     * milliseconds.
     */
    public static final String PARAMETER_MAX = "MainLoop#CycleTimeMax";

    /**
     * Output parameter name of the average cycle time of the last window in
     * milliseconds.
     */
    public static final String PARAMETER_AVERAGE = "MainLoop#CycleTimeAverage";

    /**
     * Number of cycles that one window contains.
     */
    public static final int WINDOW_CYCLES = 100;

    private static final double NANOSECONDS_PER_MILLISECOND = 1e6;

    /**
     * Receiver of the calculated values, which is the output ValueHandler of
     * MainLoop.
     */
    private final ValueReceiver output;

    /**
     * Number of cycles that were added to the current window.
     */
    private int cycleCount;

    /**
     * Sum of the cycle times of the current window in nanoseconds.
     */
    private long sumNanos;

    /**
     * Shortest cycle time of the current window in nanoseconds.
     */
    private long minNanos;

    /**
     * Longest cycle time of the current window in nanoseconds.
     */
    private long maxNanos;

    /**
     * @param output receiver that gets the values of each completed window
     */
    public CycleTimeStatistics(ValueReceiver output) {
        this.output = output;
        resetWindow();
    }

    /**
     * Adds the execution time of one cycle to the current window and writes
     * minimum, maximum and average to the output when the window is complete.
     * Called at the end of each cycle of MainLoop.
     *
     * @param cycleNanos execution time of the cycle in nanoseconds
     */
    public void addCycle(long cycleNanos) {
        cycleCount++;
        sumNanos += cycleNanos;
        minNanos = Math.min(minNanos, cycleNanos);
        maxNanos = Math.max(maxNanos, cycleNanos);
        if (cycleCount < WINDOW_CYCLES) {
            return;
        }
        output.setParameterValue(PARAMETER_MIN,
                minNanos / NANOSECONDS_PER_MILLISECOND);
        output.setParameterValue(PARAMETER_MAX,
                maxNanos / NANOSECONDS_PER_MILLISECOND);
        output.setParameterValue(PARAMETER_AVERAGE,
                sumNanos / (double) cycleCount / NANOSECONDS_PER_MILLISECOND);
        resetWindow();
    }

    private void resetWindow() {
        cycleCount = 0;
        sumNanos = 0;
        minNanos = Long.MAX_VALUE;
        maxNanos = 0;
    }
}
