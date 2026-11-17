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

import com.hartrusion.util.JvmStatistics;
import com.hartrusion.values.ValueReceiver;

/**
 * Samples the state of the Java virtual machine that runs the simulation with
 * JvmStatistics and writes the values as output parameters into the
 * ValueReceiver of MainLoop, so that memory usage, threads, CPU load and
 * garbage collection of the simulation host reach the GUI and the network
 * clients with the output snapshot, where SimulationCoreInfo displays them.
 * MainLoop calls this after each cycle, but the values are only sampled every
 * SAMPLE_INTERVAL_CYCLES cycles, because the CPU load of the JVM is an average
 * since the previous query that only becomes meaningful over a longer interval
 * and because the values do not change fast enough to justify the sampling in
 * each cycle.
 * <p>
 * LLM code generated with Claude Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public class JvmStatisticsOutput {

    /**
     * Number of cycles between two samples, which is one second with the
     * cycle period of 100 ms.
     */
    public static final int SAMPLE_INTERVAL_CYCLES = 10;

    /**
     * Output parameter name of the used heap memory in MiB.
     */
    public static final String PARAMETER_HEAP_USED = "Jvm#HeapUsed";

    /**
     * Output parameter name of the heap memory in MiB that the JVM has
     * reserved from the operating system.
     */
    public static final String PARAMETER_HEAP_COMMITTED = "Jvm#HeapCommitted";

    /**
     * Output parameter name of the maximum heap memory in MiB, which is -1 if
     * the heap is not limited.
     */
    public static final String PARAMETER_HEAP_MAX = "Jvm#HeapMax";

    /**
     * Output parameter name of the used memory outside of the heap in MiB.
     */
    public static final String PARAMETER_NON_HEAP_USED = "Jvm#NonHeapUsed";

    /**
     * Output parameter name of the CPU load of the JVM process in percent of
     * all processors, which is negative if it is not available.
     */
    public static final String PARAMETER_PROCESS_CPU_LOAD
            = "Jvm#ProcessCpuLoad";

    /**
     * Output parameter name of the CPU load of the whole system in percent of
     * all processors, which is negative if it is not available.
     */
    public static final String PARAMETER_SYSTEM_CPU_LOAD = "Jvm#SystemCpuLoad";

    public static final String PARAMETER_THREAD_COUNT = "Jvm#ThreadCount";

    public static final String PARAMETER_PEAK_THREAD_COUNT
            = "Jvm#PeakThreadCount";

    public static final String PARAMETER_DAEMON_THREAD_COUNT
            = "Jvm#DaemonThreadCount";

    public static final String PARAMETER_AVAILABLE_PROCESSORS
            = "Jvm#AvailableProcessors";

    /**
     * Output parameter name of the number of garbage collections since the
     * start of the JVM.
     */
    public static final String PARAMETER_GC_COUNT = "Jvm#GcCount";

    /**
     * Output parameter name of the accumulated time of all garbage
     * collections since the start of the JVM in milliseconds.
     */
    public static final String PARAMETER_GC_TIME = "Jvm#GcTime";

    /**
     * Receiver of the values, which is the output ValueHandler of MainLoop.
     */
    private final ValueReceiver output;

    private final JvmStatistics jvmStatistics = new JvmStatistics();

    /**
     * Counts the cycles since the last sample, starting so that the first call
     * samples immediately and the values are available after the first cycle.
     */
    private int cyclesSinceSample = SAMPLE_INTERVAL_CYCLES;

    /**
     * @param output receiver that gets the sampled values
     */
    public JvmStatisticsOutput(ValueReceiver output) {
        this.output = output;
    }

    /**
     * Samples the JVM and writes all values to the output if
     * SAMPLE_INTERVAL_CYCLES cycles have passed since the last sample. Called
     * at the end of each cycle of MainLoop.
     */
    public void addCycle() {
        cyclesSinceSample++;
        if (cyclesSinceSample < SAMPLE_INTERVAL_CYCLES) {
            return;
        }
        cyclesSinceSample = 0;
        jvmStatistics.sample();

        output.setParameterValue(PARAMETER_HEAP_USED,
                jvmStatistics.getHeapUsedMiB());
        output.setParameterValue(PARAMETER_HEAP_COMMITTED,
                jvmStatistics.getHeapCommittedMiB());
        output.setParameterValue(PARAMETER_HEAP_MAX,
                jvmStatistics.getHeapMaxMiB());
        output.setParameterValue(PARAMETER_NON_HEAP_USED,
                jvmStatistics.getNonHeapUsedMiB());
        output.setParameterValue(PARAMETER_PROCESS_CPU_LOAD,
                jvmStatistics.getProcessCpuLoad());
        output.setParameterValue(PARAMETER_SYSTEM_CPU_LOAD,
                jvmStatistics.getSystemCpuLoad());
        output.setParameterValue(PARAMETER_THREAD_COUNT,
                jvmStatistics.getThreadCount());
        output.setParameterValue(PARAMETER_PEAK_THREAD_COUNT,
                jvmStatistics.getPeakThreadCount());
        output.setParameterValue(PARAMETER_DAEMON_THREAD_COUNT,
                jvmStatistics.getDaemonThreadCount());
        output.setParameterValue(PARAMETER_AVAILABLE_PROCESSORS,
                jvmStatistics.getAvailableProcessors());
        // The snapshot only transports int values, which is sufficient for
        // the number of collections and the time in milliseconds for about
        // 24 days of pure garbage collection.
        output.setParameterValue(PARAMETER_GC_COUNT,
                (int) Math.min(jvmStatistics.getGarbageCollectionCount(),
                        Integer.MAX_VALUE));
        output.setParameterValue(PARAMETER_GC_TIME,
                (int) Math.min(jvmStatistics.getGarbageCollectionTimeMillis(),
                        Integer.MAX_VALUE));
    }
}
