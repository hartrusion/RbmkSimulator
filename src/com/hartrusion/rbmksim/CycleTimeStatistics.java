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
 * minimum, the maximum and the average over the most recent cycles. The
 * simulation has to finish each cycle within the 100 ms cycle period, so these
 * values show how much time is left on the machine that runs the simulation.
 * The cycle times are kept in a ring buffer of a fixed number of cycles, in
 * which a circulating index overwrites the oldest entry with each new cycle, so
 * that the values are evaluated again after every cycle and always describe the
 * last cycles. The results are written as output parameters into the
 * ValueReceiver of MainLoop, so that they are part of the output snapshot that
 * is sent to the controller with every cycle and reach the GUI and the network
 * clients like all other measured values of the simulator, where ServerView and
 * SimulationCoreInfo display them.
 * <p>
 * LLM code generated with Claude Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public class CycleTimeStatistics {

    /**
     * Output parameter name of the shortest cycle time of the recorded cycles
     * in milliseconds.
     */
    public static final String PARAMETER_MIN = "MainLoop#CycleTimeMin";

    /**
     * Output parameter name of the longest cycle time of the recorded cycles
     * in milliseconds.
     */
    public static final String PARAMETER_MAX = "MainLoop#CycleTimeMax";

    /**
     * Output parameter name of the average cycle time of the recorded cycles
     * in milliseconds.
     */
    public static final String PARAMETER_AVERAGE = "MainLoop#CycleTimeAverage";

    /**
     * Number of cycles that the ring buffer holds.
     */
    public static final int BUFFER_CYCLES = 100;

    private static final double NANOSECONDS_PER_MILLISECOND = 1e6;

    /**
     * Receiver of the calculated values, which is the output ValueHandler of
     * MainLoop.
     */
    private final ValueReceiver output;

    /**
     * Ring buffer with the cycle times of the most recent cycles in
     * nanoseconds.
     */
    private final long[] cycleNanos = new long[BUFFER_CYCLES];

    /**
     * Index of the ring buffer entry that the next cycle overwrites.
     */
    private int nextIndex;

    /**
     * Number of valid entries in the ring buffer, which is lower than its
     * length only until the buffer was filled once after the start of the
     * MainLoop.
     */
    private int validCount;

    /**
     * @param output receiver that gets the calculated values after each cycle
     */
    public CycleTimeStatistics(ValueReceiver output) {
        this.output = output;
    }

    /**
     * Writes the execution time of one cycle into the ring buffer and writes
     * minimum, maximum and average of all valid entries to the output. Called
     * at the end of each cycle of MainLoop.
     *
     * @param cycleTimeNanos execution time of the cycle in nanoseconds
     */
    public void addCycle(long cycleTimeNanos) {
        cycleNanos[nextIndex] = cycleTimeNanos;
        nextIndex = (nextIndex + 1) % BUFFER_CYCLES;
        if (validCount < BUFFER_CYCLES) {
            validCount++;
        }

        long minNanos = Long.MAX_VALUE;
        long maxNanos = 0;
        long sumNanos = 0;
        for (int idx = 0; idx < validCount; idx++) {
            minNanos = Math.min(minNanos, cycleNanos[idx]);
            maxNanos = Math.max(maxNanos, cycleNanos[idx]);
            sumNanos += cycleNanos[idx];
        }
        output.setParameterValue(PARAMETER_MIN,
                minNanos / NANOSECONDS_PER_MILLISECOND);
        output.setParameterValue(PARAMETER_MAX,
                maxNanos / NANOSECONDS_PER_MILLISECOND);
        output.setParameterValue(PARAMETER_AVERAGE,
                sumNanos / (double) validCount / NANOSECONDS_PER_MILLISECOND);
    }
}
