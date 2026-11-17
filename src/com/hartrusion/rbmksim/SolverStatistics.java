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

import com.hartrusion.modeling.solvers.DomainAnalogySolver;
import com.hartrusion.values.ValueReceiver;

/**
 * Writes the structural information about the DomainAnalogySolver instances of
 * the model into the output values, which is the number of nodes and elements
 * that each solver handles, the number of networks that it solves in parallel
 * or in series and the number of threads that it keeps busy at the same time.
 * These values do not change after the model was built, so they are written
 * only once for each solver by MainLoop after the initialization. Because the
 * output ValueHandler keeps all values, they are still part of every output
 * snapshot, so that they reach the GUI and network clients that connect later
 * in the same way as all other measured values, where SimulationCoreInfo
 * displays them as a table.
 * <p>
 * The parameter names are built from PARAMETER_PREFIX, the name of the solver
 * and one of the suffixes, so that SimulationCoreInfo can find all solvers in
 * the snapshot without knowing them in advance. The names of the solvers must
 * therefore be unique, which is given by setString() of each solver.
 * <p>
 * LLM code generated with Claude Opus 5.5
 *
 * @author Viktor Alexander Hartung
 */
public class SolverStatistics {

    /**
     * Prefix of all solver parameter names, which is followed by the name of
     * the solver and one of the suffixes.
     */
    public static final String PARAMETER_PREFIX = "Solver#";

    /**
     * Output parameter name of the size of the thread pool that all solvers
     * share, which is 0 if the solvers do not calculate in parallel.
     */
    public static final String PARAMETER_THREAD_POOL_SIZE
            = "SolverPool#ThreadPoolSize";

    public static final String SUFFIX_NODES = "#Nodes";

    public static final String SUFFIX_ELEMENTS = "#Elements";

    /**
     * Suffix of the number of networks that the solver hands to the thread
     * pool in each cycle.
     */
    public static final String SUFFIX_PARALLEL_TASKS = "#ParallelTasks";

    /**
     * Suffix of the number of networks that the solver calculates in series
     * on the thread of MainLoop.
     */
    public static final String SUFFIX_SERIAL_NETWORKS = "#SerialNetworks";

    /**
     * Suffix of the number of threads that the solver keeps busy at the same
     * time, including the thread of MainLoop.
     */
    public static final String SUFFIX_THREADS = "#Threads";

    /**
     * Receiver of the values, which is the output ValueHandler of MainLoop.
     */
    private final ValueReceiver output;

    /**
     * @param output receiver that gets the solver values
     */
    public SolverStatistics(ValueReceiver output) {
        this.output = output;
    }

    /**
     * Writes the structural information of one solver and the size of the
     * shared thread pool into the output. The solver must have its network
     * already added, which is the case after the init() method of the
     * subsystem that owns it.
     *
     * @param solver solver with a unique name set by setString()
     */
    public void addSolver(DomainAnalogySolver solver) {
        String baseName = PARAMETER_PREFIX + solver.toString();
        output.setParameterValue(baseName + SUFFIX_NODES,
                solver.getNodeCount());
        output.setParameterValue(baseName + SUFFIX_ELEMENTS,
                solver.getElementCount());
        output.setParameterValue(baseName + SUFFIX_PARALLEL_TASKS,
                solver.getParallelTaskCount());
        output.setParameterValue(baseName + SUFFIX_SERIAL_NETWORKS,
                solver.getSerialNetworkCount());
        output.setParameterValue(baseName + SUFFIX_THREADS,
                solver.getUsedThreadCount());
        output.setParameterValue(PARAMETER_THREAD_POOL_SIZE,
                DomainAnalogySolver.getThreadPoolSize());
    }
}
