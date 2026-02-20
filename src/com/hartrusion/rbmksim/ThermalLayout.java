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

import com.hartrusion.alarm.AlarmAction;
import com.hartrusion.alarm.AlarmState;
import com.hartrusion.alarm.ValueAlarmMonitor;
import com.hartrusion.control.AbstractController;
import com.hartrusion.control.ControlCommand;
import com.hartrusion.control.Integrator;
import com.hartrusion.control.PControl;
import com.hartrusion.control.PIControl;
import com.hartrusion.control.SerialRunner;
import com.hartrusion.control.Setpoint;
import com.hartrusion.modeling.PhysicalDomain;
import com.hartrusion.modeling.automated.HeatControlledFlowSource;
import com.hartrusion.modeling.assemblies.HeatExchanger;
import com.hartrusion.modeling.assemblies.HeatExchangerNoMass;
import com.hartrusion.modeling.automated.HeatFluidPump;
import com.hartrusion.modeling.automated.HeatFluidPumpSimple;
import com.hartrusion.modeling.automated.HeatValve;
import com.hartrusion.modeling.automated.HeatValveControlled;
import com.hartrusion.modeling.assemblies.PhasedCondenserNoMass;
import com.hartrusion.modeling.assemblies.PhasedSuperheater;
import com.hartrusion.modeling.automated.PhasedValve;
import com.hartrusion.modeling.automated.PhasedValveControlled;
import com.hartrusion.modeling.converters.PhasedHeatFluidConverter;
import com.hartrusion.modeling.general.FlowSource;
import com.hartrusion.modeling.general.GeneralNode;
import com.hartrusion.modeling.general.LinearDissipator;
import com.hartrusion.modeling.general.OpenOrigin;
import com.hartrusion.modeling.general.SelfCapacitance;
import com.hartrusion.modeling.heatfluid.HeatEffortSource;
import com.hartrusion.modeling.heatfluid.HeatFluidTank;
import com.hartrusion.modeling.heatfluid.HeatNode;
import com.hartrusion.modeling.heatfluid.HeatOrigin;
import com.hartrusion.modeling.heatfluid.HeatSimpleFlowResistance;
import com.hartrusion.modeling.heatfluid.HeatVolumizedFlowResistance;
import com.hartrusion.modeling.phasedfluid.PhasedClosedSteamedReservoir;
import com.hartrusion.modeling.phasedfluid.PhasedEffortSource;
import com.hartrusion.modeling.phasedfluid.PhasedExpandingThermalExchanger;
import com.hartrusion.modeling.phasedfluid.PhasedLimitedPhaseSimpleFlowResistance;
import com.hartrusion.modeling.phasedfluid.PhasedNode;
import com.hartrusion.modeling.phasedfluid.PhasedPropertiesWater;
import com.hartrusion.modeling.phasedfluid.PhasedSimpleFlowResistance;
import com.hartrusion.modeling.phasedfluid.PhasedThermalExchanger;
import com.hartrusion.modeling.solvers.DomainAnalogySolver;
import com.hartrusion.mvc.ActionCommand;
import java.util.function.DoubleSupplier;

/**
 * Describes the thermal process layout of the plant.
 *
 * <p>
 * Pump assembly: Define, init signal listener, connect suction and discharge
 * valves by getting the valve elements, call initCharacteristic to set
 * properties, set initial conditions if necessary, submit runnable to runner,
 * redirect received user actions to assembly.
 * <p>
 * Control Loops: Define as P or PI or whatever, submit runnable to runner, add
 * new DataProvider to define method that gets input value, set parameters,
 *
 *
 * @author Viktor Alexander Hartung
 */
public class ThermalLayout extends Subsystem implements Runnable {

    // Reference to the used reactor core part
    private ReactorCore core;

    // Reference to the turbine class that holds the network elements and 
    // controls for the turbine that are not part of the steam flow.
    private Turbine turbine;

    private final PhasedPropertiesWater phasedWater
            = new PhasedPropertiesWater();

    // <editor-fold defaultstate="collapsed" desc="Model elements declaration and array instantiation">
    // Cold condensate storage
    private final HeatFluidTank makeupStorage;
    private final HeatNode makeupStorageDrainCollector;
    private final HeatNode makeupStorageOut;
    private final HeatFluidPump[] makeupPumps
            = new HeatFluidPump[2];
    private final HeatNode makeupPumpsOut;

    private final PhasedNode[] mainSteamDrumNode = new PhasedNode[2];
    private final PhasedValve[] mainSteamShutoffValve = new PhasedValve[2];
    private final PhasedNode[] mainSteam = new PhasedNode[2];

    private final PhasedClosedSteamedReservoir[] loopSteamDrum
            = new PhasedClosedSteamedReservoir[2];
    private final PhasedNode[] loopNodeDrumWaterOut = new PhasedNode[2];
    private final PhasedNode[] loopNodeDrumBlowdownOut = new PhasedNode[2];
    private final PhasedNode[] loopNodeDrumFromReactor = new PhasedNode[2];
    private final PhasedHeatFluidConverter[] loopFromDrumConverter
            = new PhasedHeatFluidConverter[2];
    private final PhasedHeatFluidConverter[] loopFromDrumBlowdownConverter
            = new PhasedHeatFluidConverter[2];
    private final HeatNode[] loopNodeDrumBlowdownHeatOut = new HeatNode[2];
    private final HeatNode[] loopFeedwaterIn = new HeatNode[2];
    private final HeatVolumizedFlowResistance[] loopDownflow
            = new HeatVolumizedFlowResistance[2];
    private final HeatNode[] loopCollector = new HeatNode[2];
    private final HeatFluidPump[][] loopAssembly
            = new HeatFluidPump[2][4];
    private final HeatNode[][] loopTrimNode = new HeatNode[2][4];
    private final HeatValve[][] loopTrimValve = new HeatValve[2][4];
    private final HeatValve[] loopBypass = new HeatValve[2];
    private final HeatNode[] loopDistributor = new HeatNode[2]; // to channels
    private final HeatVolumizedFlowResistance[] loopChannelFlowResistance
            = new HeatVolumizedFlowResistance[2];
    private final HeatNode[] loopAfterResistance = new HeatNode[2];
    private final HeatEffortSource[] loopThermalLift = new HeatEffortSource[2];
    private final HeatNode[] loopAfterThermalLift = new HeatNode[2];
    private final PhasedHeatFluidConverter[] loopToReactorConverter
            = new PhasedHeatFluidConverter[2];
    private final PhasedNode[] loopEvaporatorIn = new PhasedNode[2];
    private final PhasedExpandingThermalExchanger[] loopEvaporator
            = new PhasedExpandingThermalExchanger[2];

    // Thermal system describing the fuel thermal heat flow
    private final GeneralNode fuelGroundNode;
    private final OpenOrigin fuelGround;
    private final FlowSource[] fuelThermalSource = new FlowSource[2];
    private final SelfCapacitance[] fuelThermalCapacity
            = new SelfCapacitance[2];
    private final GeneralNode[] fuelThermalOut = new GeneralNode[2];
    private final LinearDissipator[] fuelThermalResistance
            = new LinearDissipator[2];
    private final GeneralNode[] fuelEvaporatorNode = new GeneralNode[2];

    // Blowdown and cooldown system
    private final HeatValve[] blowdownValveFromDrum = new HeatValve[2];
    private final HeatVolumizedFlowResistance[] blowdownPipeFromMcp
            = new HeatVolumizedFlowResistance[2];
    private final HeatNode[] blowdownFromLoopNode = new HeatNode[2];
    private final HeatValve[] blowdownValveFromLoop = new HeatValve[2];
    private final HeatNode blowdownInCollectorNode; // lower line
    private final HeatFluidPump[] blowdownCooldownPumps = new HeatFluidPump[2];
    private final HeatNode blowdownPumpCollectorNode; // line above pumps
    // Placed before the first input into regenerator above the valve from pumps
    private final HeatVolumizedFlowResistance blowdownToRegeneratorFirstResistance;
    // Placed after the water treatment towards the regenerator
    private final HeatVolumizedFlowResistance blowdownToRegeneratorSecondResistance;
    private final HeatNode blowdownRegeneratorInCollectorNode;
    private final HeatValve blowdownValvePassiveFlow;
    private final HeatValve blowdownValvePumpsToRegenerator;
    private final HeatValve blowdownValvePumpsToCooler;
    private final HeatExchangerNoMass blowdownRegenerator;
    private final HeatValve blowdownValveRegeneratorToCooler;
    private final HeatValve blowdownValveTreatmentBypass;
    private final HeatNode blowdownToCooldownNode;
    private final HeatSimpleFlowResistance blowdownCooldownResistance;
    private final HeatExchanger blowdownCooldown;
    // There is no coolant loop yet, just a flow source
    private final HeatOrigin blowdownCoolantSource;
    private final HeatNode blowdownCoolantSourceNode;
    private final HeatControlledFlowSource blowdownValveCoolant;
    private final HeatOrigin blowdownCoolantSink;
    // water treatment is just a large volume for now
    private final HeatVolumizedFlowResistance blowdownTreatment;
    private final HeatNode blowdownTreatedOutNode;
    private final HeatValve blowdownValveRegeneratedToDrums;
    private final HeatValve blowdownValveDrain;
    private final HeatNode blowdownOutNode;
    private final HeatValve[] blowdownReturnValve = new HeatValve[2];
    private final HeatNode[] blowdownReturnNode = new HeatNode[2];
    private final HeatVolumizedFlowResistance[] blowdownReturn
            = new HeatVolumizedFlowResistance[2];

    // Deaerators
    private final PhasedClosedSteamedReservoir[] deaerator
            = new PhasedClosedSteamedReservoir[2];
    private final PhasedNode[] deaeratorOutNode = new PhasedNode[2];
    private final PhasedNode[] deaeratorInNode = new PhasedNode[2];
    private final PhasedHeatFluidConverter[] deaeratorToFeedwaterConverter
            = new PhasedHeatFluidConverter[2];
    private final HeatNode[] deaeratorFeedwaterOutHeatNode = new HeatNode[2];
    private final PhasedValveControlled[] deaeratorSteamInRegValve
            = new PhasedValveControlled[2];
    private final PhasedNode[] deaeratorSteamInNode = new PhasedNode[2];
    private final PhasedValve[] deaeratorSteamFromMain = new PhasedValve[2];
    private final PhasedNode deaeratorSteamMiddle;
    private final PhasedSimpleFlowResistance[] deaeratorSteamDistribution
            = new PhasedSimpleFlowResistance[2];
    private final PhasedHeatFluidConverter[] deaeratorToDrainConverter
            = new PhasedHeatFluidConverter[2];
    private final HeatNode[] deaeratorDrainOutHeatNode = new HeatNode[2];
    private final HeatValve[] deaeratorDrain = new HeatValve[2];

    // Feedwater pumps system
    // Two feedwater pumps per side
    private final HeatFluidPump[][] feedwaterPump
            = new HeatFluidPump[2][2];
    // the spare pump can be used on both sides so it has some valves allowing 
    // to connect it to both sides.
    private final HeatValve[] feedwaterSparePumpInValve = new HeatValve[2];
    private final HeatNode feedwaterSparePumpIn;
    private final HeatFluidPump feedwaterPump3;
    private final HeatNode feedwaterSparePumpOut;
    private final HeatValve[] feedwaterSparePumpOutValve = new HeatValve[2];
    // Main feed pumps go to this nodes:
    private final HeatNode[] feedwaterPumpCollectorNodes = new HeatNode[2];

    // There are three flow regulation valves with shutoff valves before them,
    // the third one is used for startup and has higher flow resistance. 
    // The startup valve has another flow reduction valve before to achieve even
    // lower flow rates by using both valves to reduce flow.
    private final HeatValve[] feedwaterStartupReductionValve = new HeatValve[2];
    private final HeatNode[] feedwaterAfterStartupNode = new HeatNode[2];
    private final HeatValve[][] feedwaterShutoffValve = new HeatValve[2][3];
    private final HeatNode[][] feedwaterIntoFlowRegNode = new HeatNode[2][3];
    private final HeatValveControlled[][] feedwaterFlowRegulationValve
            = new HeatValveControlled[2][3];

    // Auxiliary Condensation
    private final PhasedValveControlled[] auxCondSteamValve
            = new PhasedValveControlled[2];
    private final PhasedCondenserNoMass[] auxCondensers = new PhasedCondenserNoMass[2];
    // Coolant flow with simple flow source for now
    private final HeatOrigin[] auxCondCoolantSource = new HeatOrigin[2];
    private final HeatNode[] auxCondCoolantSourceNode = new HeatNode[2];
    private final HeatControlledFlowSource[] auxCondCoolantValve
            = new HeatControlledFlowSource[2];
    private final HeatOrigin[] auxCondCoolantSink = new HeatOrigin[2];
    private final PhasedHeatFluidConverter[] auxCondOutConverter
            = new PhasedHeatFluidConverter[2];
    private final HeatNode[] auxCondCondenserOutNode = new HeatNode[2];
    private final HeatValveControlled[] auxCondCondensateValve
            = new HeatValveControlled[2];
    private final HeatNode auxCondCondInNode;
    private final HeatValve auxCondBypass;
    private final HeatFluidPumpSimple[] auxCondPumps
            = new HeatFluidPumpSimple[2];
    private final HeatNode auxCondCollectorNode;
    private final HeatEffortSource auxCondHeightDifference;
    private final HeatNode auxCondDistributorNode;
    private final HeatValve auxCondValveToHotwell;
    private final HeatNode auxCondValveToHotwellHeatNode;
    private final PhasedHeatFluidConverter auxCondValveToHotwellConverter;
    private final PhasedNode auxCondValveToHotwellPhasedNode;
    private final HeatValve auxCondValveToDrain;

    // Steam Dump
    private final PhasedValveControlled[] mainSteamDump
            = new PhasedValveControlled[2];

    // Condensation
    private final PhasedCondenserNoMass hotwell;
    private final PhasedHeatFluidConverter hotwellOutConverter;
    private final HeatNode hotwellOutNode;
    private final HeatFluidPump[] condensationHotwellPump
            = new HeatFluidPump[3];
    private final HeatNode condensationPumpOut;
    // private final HeatVolumizedFlowResistance condensationEjectorDummy;
    private final HeatNode condensationBoosterPumpIn;
    private final HeatFluidPump[] condensationCondensatePump
            = new HeatFluidPump[3];
    private final HeatValveControlled[] condensationValveToDA
            = new HeatValveControlled[2];
    private final HeatNode[] condensationValveOut = new HeatNode[2];
    private final PhasedHeatFluidConverter[] condensationToDeaeratorConverter
            = new PhasedHeatFluidConverter[2];

    // Hotwell fill and drain valves with converters (less model complexity)
    private final PhasedHeatFluidConverter hotwellFillValveConverter;
    private final HeatNode hotwellFillNode;
    private final HeatValveControlled hotwellFillValve;
    private final HeatValveControlled hotwellDrainValve;

    // Main Coolant Loop for Condenser - for now, just a constant flow, 
    // always on, no vacuum stuff for now.
    private final HeatOrigin condenserCoolantSource;
    private final HeatNode condenserCoolantSourceNode;
    private final HeatControlledFlowSource condenserCoolant;
    private final HeatOrigin condenserCoolantSink;

    // Ejectors (both startup and main)
    private final PhasedNode ejectorTurbineTapNode;
    private final PhasedValve[] ejectorStartup
            = new PhasedValve[2];
    private final PhasedValve[] ejectorMainSteamValve
            = new PhasedValve[3];
    private final PhasedCondenserNoMass[] ejectorMain = new PhasedCondenserNoMass[3];
    private final PhasedHeatFluidConverter[] ejectorMainCondensate
            = new PhasedHeatFluidConverter[3];
    private final HeatNode[] ejectorMainCondensateOut = new HeatNode[3];
    // to rause pressure a bit and allow flow without any pump:
    private final HeatEffortSource[] ejectorLevel = new HeatEffortSource[3];
    private final HeatNode[] ejectorMainCondensateUp = new HeatNode[3];
    private final HeatValveControlled[] ejectorMainCondensateValve
            = new HeatValveControlled[3];
    private final HeatValve[] ejectorMainFlowIn = new HeatValve[3];
    private final HeatSimpleFlowResistance[] ejectorMainFlowReistance
            = new HeatSimpleFlowResistance[3];
    private final HeatNode[] ejectorMainFlowReistanceNode = new HeatNode[3];
    private final HeatValve[] ejectorMainFlowOut = new HeatValve[3];
    private final HeatValve ejectorMainBypass;
    private final HeatNode ejectorToHotwellHeatNode;
    private final PhasedHeatFluidConverter ejectorToHotwellConverter;
    private final PhasedNode ejectorToHotwellPhasedNode;

    private final PhasedCondenserNoMass[] preheater
            = new PhasedCondenserNoMass[3];
    private final HeatSimpleFlowResistance[] preheaterPiping
            = new HeatSimpleFlowResistance[2];
    private final PhasedValveControlled[] preheaterCondensateValve
            = new PhasedValveControlled[3];

    // Valves to turbine inlet
    private final PhasedValve[] turbineTripValve
            = new PhasedValve[2];
    private final PhasedNode[] turbineSteamIn = new PhasedNode[2];
    private final PhasedValveControlled[] turbineStartupSteamValve
            = new PhasedValveControlled[2];
    private final PhasedValveControlled[] turbineMainSteamValve
            = new PhasedValveControlled[2];
    private final PhasedValveControlled[] turbineReheaterSteamValve
            = new PhasedValveControlled[2];
    private final PhasedNode turbineHighPressureIn;
    private final PhasedThermalExchanger turbineHighPressureInMass;
    private final PhasedNode turbineHighPressureMidIn;
    private final PhasedLimitedPhaseSimpleFlowResistance turbineHighPressure;
    private final PhasedNode turbineHighPressureMidOut;
    private final PhasedThermalExchanger turbineHighPressureOutMass;
    private final PhasedNode turbineReheaterSteam;
    private final PhasedValve turbineReheaterTripValve;
    private final PhasedNode turbineReheaterPriValvesMidNode;
    private final PhasedValve turbineReheaterTrimValve;
    private final PhasedSuperheater turbineReheater;
    private final PhasedValveControlled[] turbineReheaterCondensateValve
            = new PhasedValveControlled[2];
    private final PhasedNode[] turbineReheaterCondensateNode
            = new PhasedNode[2];
    private final PhasedValveControlled turbineReheaterCondensateDrain;
    private final PhasedNode turbineReheaterCondensateDrainOut;
    private final PhasedEffortSource turbineReheaterCondensateHeight;
    private final PhasedValve turbineLowPressureTripValve;
    private final PhasedNode turbineLowPressureIn;
    private final PhasedThermalExchanger turbineLowPressureInMass;
    private final PhasedNode turbineLowPressureMidIn;
    private final PhasedLimitedPhaseSimpleFlowResistance[] turbineLowPressureStage
            = new PhasedLimitedPhaseSimpleFlowResistance[6];
    private final PhasedNode[] turbineLowPressureStageOut
            = new PhasedNode[5];
    private final PhasedLimitedPhaseSimpleFlowResistance[] turbineLowPressureTap
            = new PhasedLimitedPhaseSimpleFlowResistance[5];
    private final PhasedNode[] turbineLowPressureTapOut
            = new PhasedNode[5];
    private final PhasedValve[] turbineLowPressureTapValve = new PhasedValve[5];
    private final PhasedNode turbineLowPressureMidOut;
    private final PhasedThermalExchanger turbineLowPressureOutMass;

    // </editor-fold>
    private final Setpoint[] setpointDrumLevel = new Setpoint[2];
    private final Setpoint[] setpointDAPressure = new Setpoint[2];
    private final Setpoint[] setpointDALevel = new Setpoint[2];
    private final Setpoint[] setpointAuxCondLevel = new Setpoint[2];
    private final Setpoint setpointDrumPressure;
    private final Setpoint setpointHotwellUpperLevel;
    private final Setpoint setpointHotwellLowerLevel;
    private final Setpoint setpointTurbineReheaterTemperature;
    private final Setpoint setpointTurbineReheaterLevel;

    private final DomainAnalogySolver solver = new DomainAnalogySolver();
    private final SerialRunner runner = new SerialRunner();
    private final SerialRunner alarmUpdater = new SerialRunner();

    private final AbstractController blowdownBalanceControlLoop
            = new PControl();

    private double[] steamOutToTurbine = new double[2];

    private double voiding = 0;
    private double coreTemp = 200;
    private final double[] thermalPower = new double[]{2.4e6, 2.4e6};

    private double turbineShaftPower = 0.0;
    private double reheaterOutTemperature = 22.5;
    private double reheaterOutQuality = 0.0;

    /**
     * Holds the vacuum value for the condenser. This is made up from the
     * expected behavior and not actually modeled by any physical behavior.
     */
    private final Integrator condenserVacuum = new Integrator();

    /**
     * Setting this to true will disconnect the thermal loop simulation,
     * allowing to operate the reactor without feedback from the thermal system.
     * There will be no voiding and the temperature will be set estimated.
     */
    private boolean noReactorInput;

    /**
     * Poor mans debugging: This temperature is added everywhere to be able to
     * start the simulation with pressurized reactor. Set to 150 for example to
     * have some steam inside the core, 245 is operating power
     */
    private double debugAddInitTemp = 0;

    ThermalLayout() {
        // <editor-fold defaultstate="collapsed" desc="Model elements instantiation">
        // Generate all instances and name them. This is done here and not in
        // variables declaration so we can both instanciate single and array
        // elements in a common way.
        makeupStorage = new HeatFluidTank();
        makeupStorage.setName("MakeupStorage");
        makeupStorageDrainCollector = new HeatNode();
        makeupStorageDrainCollector.setName("MakeupStorage#DrainCollector");
        makeupStorageOut = new HeatNode();
        makeupStorageOut.setName("MakeupStorage#Out");
        for (int idx = 0; idx < 2; idx++) {
            makeupPumps[idx] = new HeatFluidPump();
            makeupPumps[idx].initName("Makeup" + (idx + 1)
                    + "#Pumps");
        }
        makeupPumpsOut = new HeatNode();
        makeupPumpsOut.setName("Makeup#PumpsOut");

        for (int idx = 0; idx < 2; idx++) {
            mainSteamDrumNode[idx] = new PhasedNode();
            mainSteamDrumNode[idx].setName("Main" + (idx + 1)
                    + "#SteamDrumNode");
            mainSteamShutoffValve[idx] = new PhasedValve();
            mainSteamShutoffValve[idx].initName("Main" + (idx + 1)
                    + "#SteamShutoffValve");
            mainSteam[idx] = new PhasedNode();
            mainSteam[idx].setName("Main" + (idx + 1) + "Steam");
        }

        for (int idx = 0; idx < 2; idx++) {
            loopSteamDrum[idx] = new PhasedClosedSteamedReservoir(phasedWater);
            loopSteamDrum[idx].setName("Loop" + (idx + 1) + "SteamDrum");
            loopNodeDrumWaterOut[idx] = new PhasedNode();
            loopNodeDrumWaterOut[idx].setName("Loop" + (idx + 1)
                    + "#NodeDrumWaterOut");
            loopNodeDrumBlowdownOut[idx] = new PhasedNode();
            loopNodeDrumBlowdownOut[idx].setName("Loop" + (idx + 1)
                    + "#NodeDrumBlowdownOut");
            loopNodeDrumFromReactor[idx] = new PhasedNode();
            loopNodeDrumFromReactor[idx].setName("Loop" + (idx + 1)
                    + "#NodeDrumFromReactor");
            loopFromDrumConverter[idx]
                    = new PhasedHeatFluidConverter(phasedWater);
            loopFromDrumConverter[idx].setName("Loop" + (idx + 1)
                    + "#FromDrumConverter");
            loopFromDrumBlowdownConverter[idx]
                    = new PhasedHeatFluidConverter(phasedWater);
            loopFromDrumBlowdownConverter[idx].setName("Loop" + (idx + 1)
                    + "#FromDrumBlowdownConverter");
            loopNodeDrumBlowdownHeatOut[idx] = new HeatNode();
            loopNodeDrumBlowdownHeatOut[idx].setName("Loop" + (idx + 1)
                    + "#NodeDrumBlowdownHeatOut");
            loopDownflow[idx] = new HeatVolumizedFlowResistance();
            loopDownflow[idx].setName("Loop" + (idx + 1) + "#Downdflow");
            loopFeedwaterIn[idx] = new HeatNode();
            loopFeedwaterIn[idx].setName("Loop" + (idx + 1) + "#FeedwaterIn");
            loopCollector[idx] = new HeatNode();
            loopCollector[idx].setName("Loop" + (idx + 1) + "#Collector");
            for (int jdx = 0; jdx < 4; jdx++) {
                loopAssembly[idx][jdx] = new HeatFluidPump();
                // Will be named something like Loop1#mcp3DischargeValve
                loopAssembly[idx][jdx].initName("Loop" + (idx + 1)
                        + "#mcp" + (jdx + 1));
                loopTrimNode[idx][jdx] = new HeatNode();
                loopTrimNode[idx][jdx].setName("Loop" + (idx + 1)
                        + "#TrimNode" + (jdx + 1));
                loopTrimValve[idx][jdx] = new HeatValve();
                loopTrimValve[idx][jdx].initName("Loop" + (idx + 1)
                        + "#mcp" + (jdx + 1) + "TrimValve");
            }
            loopBypass[idx] = new HeatValve();
            loopBypass[idx].initName("Loop" + (idx + 1) + "#Bypass");
            loopDistributor[idx] = new HeatNode();
            loopDistributor[idx].setName("Loop" + (idx + 1) + "#Distributor");
            loopChannelFlowResistance[idx] = new HeatVolumizedFlowResistance();
            loopChannelFlowResistance[idx].setName("Loop" + (idx + 1)
                    + "#ChannelFlowResistance");
            loopAfterResistance[idx] = new HeatNode();
            loopAfterResistance[idx].setName("Loop" + (idx + 1)
                    + "#AfterResistance");
            loopThermalLift[idx] = new HeatEffortSource();
            loopThermalLift[idx].setName("Loop" + (idx + 1) + "#ThermalLift");
            loopAfterThermalLift[idx] = new HeatNode();
            loopAfterThermalLift[idx].setName("Loop" + (idx + 1)
                    + "#AfterThermalLift");
            loopToReactorConverter[idx]
                    = new PhasedHeatFluidConverter(phasedWater);
            loopToReactorConverter[idx].setName("Loop" + (idx + 1)
                    + "+ToReactorConverter");
            loopEvaporatorIn[idx] = new PhasedNode();
            loopEvaporatorIn[idx].setName("Loop" + (idx + 1) + "#EvaporatorIn");
            loopEvaporator[idx]
                    = new PhasedExpandingThermalExchanger(phasedWater);
            loopEvaporator[idx].setName("Loop" + (idx + 1) + "#Evaporator");
        }

        fuelGroundNode = new GeneralNode(PhysicalDomain.THERMAL);
        fuelGroundNode.setName("Fuel#GroundNode");
        fuelGround = new OpenOrigin(PhysicalDomain.THERMAL);
        fuelGroundNode.setName("Fuel#Ground");
        for (int idx = 0; idx < 2; idx++) {
            fuelThermalSource[idx] = new FlowSource(PhysicalDomain.THERMAL);
            fuelThermalSource[idx].setName("Fuel#ThermalSource" + (idx + 1));
            fuelThermalCapacity[idx]
                    = new SelfCapacitance(PhysicalDomain.THERMAL);
            fuelThermalCapacity[idx].setName(
                    "Fuel#ThermalCapacity" + (idx + 1));
            fuelThermalOut[idx] = new GeneralNode(PhysicalDomain.THERMAL);
            fuelThermalOut[idx].setName("Fuel#ThermalOut" + (idx + 1));
            fuelThermalResistance[idx]
                    = new LinearDissipator(PhysicalDomain.THERMAL);
            fuelThermalResistance[idx].setName("Fuel#ThermalResistance"
                    + (idx + 1));
            fuelEvaporatorNode[idx] = new GeneralNode(PhysicalDomain.THERMAL);
            fuelEvaporatorNode[idx].setName("Fuel#EvaporatorNode" + (idx + 1));
        }

        for (int idx = 0; idx < 2; idx++) {
            blowdownValveFromDrum[idx] = new HeatValve();
            blowdownValveFromDrum[idx].initName(
                    "Blowdown#ValveFromDrum" + (idx + 1));
            blowdownFromLoopNode[idx] = new HeatNode();
            blowdownFromLoopNode[idx].setName(
                    "Blowdown#FromloopNode" + (idx + 1));
            blowdownPipeFromMcp[idx] = new HeatVolumizedFlowResistance();
            blowdownPipeFromMcp[idx].setName(
                    "Blowdown#PipeFromMcp" + (idx + 1));
            blowdownValveFromLoop[idx] = new HeatValve();
            blowdownValveFromLoop[idx].initName(
                    "Blowdown#ValveFromLoop" + (idx + 1));
        }
        blowdownInCollectorNode = new HeatNode();
        blowdownInCollectorNode.setName("Blowdown#InCollectorNode");
        for (int idx = 0; idx < 2; idx++) {
            blowdownCooldownPumps[idx] = new HeatFluidPump();
            blowdownCooldownPumps[idx].initName(
                    "Blowdown#CooldownPump" + (idx + 1));
        }
        blowdownOutNode = new HeatNode();
        blowdownOutNode.setName("Blowdown#OutNode");
        blowdownPumpCollectorNode = new HeatNode();
        blowdownPumpCollectorNode.setName("Blowdown#PumpCollectorNode");
        blowdownToRegeneratorFirstResistance
                = new HeatVolumizedFlowResistance();
        blowdownToRegeneratorFirstResistance.setName(
                "Blowdown#RegeneratorFirstResistance");
        blowdownToRegeneratorSecondResistance
                = new HeatVolumizedFlowResistance();
        blowdownToRegeneratorSecondResistance.setName(
                "Blowdown#RegeneratorSecondResistance");
        blowdownRegeneratorInCollectorNode = new HeatNode();
        blowdownRegeneratorInCollectorNode.setName(
                "Blowdown#RegeneratorInCollectorNode");
        blowdownValvePassiveFlow = new HeatValve();
        blowdownValvePassiveFlow.initName(
                "Blowdown#ValvePassiveFlow");
        blowdownValvePumpsToRegenerator = new HeatValve();
        blowdownValvePumpsToRegenerator.initName(
                "Blowdown#ValvePumpsToRegenerator");
        blowdownValvePumpsToCooler = new HeatValve();
        blowdownValvePumpsToCooler.initName("Blowdown#ValvePumpsToCooler");
        blowdownRegenerator = new HeatExchangerNoMass();
        blowdownRegenerator.initGenerateNodes();
        blowdownRegenerator.initName("Blowdown#Regenerator");
        blowdownValveRegeneratorToCooler = new HeatValve();
        blowdownValveRegeneratorToCooler.initName(
                "Blowdown#ValveRegeneratorToCooler");
        blowdownValveTreatmentBypass = new HeatValve();
        blowdownValveTreatmentBypass.initName("Blowdown#ValveTreatmentBypass");
        blowdownToCooldownNode = new HeatNode();
        blowdownToCooldownNode.setName(
                "Blowdown#ToCooldownNode");
        blowdownCooldownResistance = new HeatSimpleFlowResistance();
        blowdownCooldownResistance.setName(
                "Blowdown#CooldownResistance");
        blowdownCooldown = new HeatExchanger();
        blowdownCooldown.initGenerateNodes();
        blowdownCooldown.initName("Blowdown#Cooldown");
        blowdownCoolantSource = new HeatOrigin();
        blowdownCoolantSource.setName("Blowdown#CoolantSource");
        blowdownCoolantSourceNode = new HeatNode();
        blowdownCoolantSourceNode.setName("Blowdown#CoolantSourceNode");
        blowdownValveCoolant = new HeatControlledFlowSource();
        blowdownValveCoolant.initName("Blowdown#ValveCoolant");
        blowdownCoolantSink = new HeatOrigin();
        blowdownCoolantSink.setName("Blowdown#CoolantSink");
        blowdownTreatment = new HeatVolumizedFlowResistance();
        blowdownTreatment.setName("Blowdown#Treatment");
        blowdownTreatedOutNode = new HeatNode();
        blowdownTreatedOutNode.setName("Blowdown#TreatedOutNode");
        blowdownValveRegeneratedToDrums = new HeatValve();
        blowdownValveRegeneratedToDrums.initName(
                "Blowdown#ValveRegeneratedToDrums");
        blowdownValveDrain = new HeatValve();
        blowdownValveDrain.initName("Blowdown#ValveDrain");
        for (int idx = 0; idx < 2; idx++) {
            blowdownReturnValve[idx] = new HeatValve();
            blowdownReturnValve[idx].initName(
                    "Blowdown#ReturnValve" + (idx + 1));
            blowdownReturnNode[idx] = new HeatNode();
            blowdownReturnNode[idx].setName("Blowdown#ReturnNode" + (idx + 1));
            blowdownReturn[idx] = new HeatVolumizedFlowResistance();
            blowdownReturn[idx].setName("Blowdown#Return" + (idx + 1));
        }

        for (int idx = 0; idx < 2; idx++) {
            deaerator[idx] = new PhasedClosedSteamedReservoir(phasedWater);
            deaerator[idx].setName("Deaerator" + (idx + 1));
            deaeratorOutNode[idx] = new PhasedNode();
            deaeratorOutNode[idx].setName(
                    "Deaerator" + (idx + 1) + "#OutNode");
            deaeratorInNode[idx] = new PhasedNode();
            deaeratorInNode[idx].setName(
                    "Deaerator" + (idx + 1) + "#InNode");
            deaeratorToFeedwaterConverter[idx]
                    = new PhasedHeatFluidConverter(phasedWater);
            deaeratorToFeedwaterConverter[idx].setName(
                    "Deaerator" + (idx + 1) + "#ToFeedwaterConverter");
            deaeratorFeedwaterOutHeatNode[idx] = new HeatNode();
            deaeratorFeedwaterOutHeatNode[idx].setName(
                    "Deaerator" + (idx + 1) + "#FeedwaterOutHeatNode");
            deaeratorSteamInRegValve[idx] = new PhasedValveControlled();
            deaeratorSteamInRegValve[idx].registerController(new PIControl());
            deaeratorSteamInRegValve[idx].initName(
                    "Deaerator" + (idx + 1) + "#SteamInRegValve");
            deaeratorSteamInNode[idx] = new PhasedNode();
            deaeratorSteamInNode[idx].setName(
                    "Deaerator" + (idx + 1) + "#SteaminNode");
            deaeratorSteamFromMain[idx] = new PhasedValve();
            deaeratorSteamFromMain[idx].initName(
                    "Deaerator" + (idx + 1) + "#SteamFromMain");
        }
        deaeratorSteamMiddle = new PhasedNode();
        deaeratorSteamMiddle.setName("Deaerator#SteamMiddle");
        for (int idx = 0; idx < 2; idx++) {
            deaeratorSteamDistribution[idx]
                    = new PhasedSimpleFlowResistance();
            deaeratorSteamDistribution[idx].setName(
                    "Deaerator" + (idx + 1) + "#SteamDistribution");
            deaeratorToDrainConverter[idx]
                    = new PhasedHeatFluidConverter(phasedWater);
            deaeratorToDrainConverter[idx].setName(
                    "Deaerator" + (idx + 1) + "#ToDrainConverter");
            deaeratorDrainOutHeatNode[idx] = new HeatNode();
            deaeratorDrainOutHeatNode[idx].setName(
                    "Deaerator" + (idx + 1) + "#DrainOutHeatNode");
            deaeratorDrain[idx] = new HeatValve();
            deaeratorDrain[idx].initName("Deaerator" + (idx + 1) + "#Drain");
        }

        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 2; jdx++) {
                feedwaterPump[idx][jdx] = new HeatFluidPump();
                feedwaterPump[idx][jdx].initName(
                        "Feedwater" + (idx + 1) + "#Pump" + (jdx + 1));
            }
            feedwaterSparePumpInValve[idx] = new HeatValve();
            feedwaterSparePumpInValve[idx].initName(
                    "Feedwater" + (idx + 1) + "#SparePumpInValve");
        }
        feedwaterSparePumpIn = new HeatNode();
        feedwaterSparePumpIn.setName("Feedwater#SparePumpIn");
        feedwaterPump3 = new HeatFluidPump();
        feedwaterPump3.initName("Feedwater#Pump3");
        feedwaterSparePumpOut = new HeatNode();
        feedwaterSparePumpOut.setName("Feedwater#SparePumpOut");
        for (int idx = 0; idx < 2; idx++) {
            feedwaterSparePumpOutValve[idx] = new HeatValve();
            feedwaterSparePumpOutValve[idx].initName(
                    "Feedwater" + (idx + 1) + "#SparePumpOutValve");
            feedwaterPumpCollectorNodes[idx] = new HeatNode();
            feedwaterPumpCollectorNodes[idx].setName(
                    "Feedwater" + (idx + 1) + "#PumpCollectorNodes");
            feedwaterStartupReductionValve[idx] = new HeatValve();
            feedwaterStartupReductionValve[idx].initName(
                    "Feedwater" + (idx + 1) + "#StartupReductionValve");
            feedwaterAfterStartupNode[idx] = new HeatNode();
            feedwaterAfterStartupNode[idx].setName(
                    "Feedwater" + (idx + 1) + "#AfterStartupNode");
            for (int jdx = 0; jdx < 3; jdx++) {
                feedwaterShutoffValve[idx][jdx] = new HeatValve();
                feedwaterShutoffValve[idx][jdx]
                        .initName("Feedwater" + (idx + 1)
                                + "#ShutoffValve" + (jdx + 1));
                feedwaterIntoFlowRegNode[idx][jdx] = new HeatNode();
                feedwaterIntoFlowRegNode[idx][jdx].setName(
                        "Feedwater" + (idx + 1)
                        + "#IntoFlowRegNode" + (jdx + 1));
                feedwaterFlowRegulationValve[idx][jdx]
                        = new HeatValveControlled();
                // Name has to match the designator on the GUI element. like:
                // Feedwater1#FlowRegulationValve3
                feedwaterFlowRegulationValve[idx][jdx].registerController(
                        new PIControl());
                feedwaterFlowRegulationValve[idx][jdx]
                        .initName("Feedwater" + (idx + 1)
                                + "#FlowRegulationValve" + (jdx + 1));
            }
        }

        // Auxiliary Condensation
        for (int idx = 0; idx < 2; idx++) {
            auxCondSteamValve[idx] = new PhasedValveControlled();
            auxCondSteamValve[idx].registerController(new PIControl());
            auxCondSteamValve[idx].initName(
                    "AuxCond" + (idx + 1) + "#SteamValve");
            auxCondensers[idx] = new PhasedCondenserNoMass(phasedWater);
            auxCondensers[idx].initGenerateNodes();
            auxCondensers[idx].initName("AuxCond" + (idx + 1) + "#Condenser");
            auxCondCoolantSource[idx] = new HeatOrigin();
            auxCondCoolantSource[idx].setName(
                    "AuxCond" + (idx + 1) + "#CoolantSource");
            auxCondCoolantSourceNode[idx] = new HeatNode();
            auxCondCoolantSourceNode[idx].setName(
                    "AuxCond" + (idx + 1) + "#CoolantSourceNode");
            auxCondCoolantValve[idx] = new HeatControlledFlowSource();
            auxCondCoolantValve[idx].initName(
                    "AuxCond" + (idx + 1) + "#CoolantValve");
            auxCondCoolantSink[idx] = new HeatOrigin();
            auxCondCoolantSink[idx].setName(
                    "AuxCond" + (idx + 1) + "#CoolantSink");
            auxCondOutConverter[idx]
                    = new PhasedHeatFluidConverter(phasedWater);
            auxCondOutConverter[idx].setName(
                    "AuxCond" + (idx + 1) + "#CondOutConverter");
            auxCondCondenserOutNode[idx] = new HeatNode();
            auxCondCondenserOutNode[idx].setName(
                    "AuxCond" + (idx + 1) + "#CondenserOutNode");
            auxCondCondensateValve[idx] = new HeatValveControlled();
            auxCondCondensateValve[idx].initName(
                    "AuxCond" + (idx + 1) + "#CondensateValve");
        }
        auxCondCondInNode = new HeatNode();
        auxCondCondInNode.setName("AuxCond#CondInNode");
        auxCondBypass = new HeatValve();
        auxCondBypass.initName("AuxCond#Bypass");
        for (int idx = 0; idx < 2; idx++) {
            auxCondPumps[idx] = new HeatFluidPumpSimple();
            auxCondPumps[idx].initName("AuxCond" + (idx + 1) + "#CondPumps");
        }
        auxCondCollectorNode = new HeatNode();
        auxCondCollectorNode.setName("AuxCond#ConllectorNode");
        auxCondHeightDifference = new HeatEffortSource();
        auxCondHeightDifference.setName("AuxCond#HeightDifference");
        auxCondDistributorNode = new HeatNode();
        auxCondDistributorNode.setName("AuxCond#DistributorNode");
        auxCondValveToHotwell = new HeatValve();
        auxCondValveToHotwell.initName("AuxCond#ToHotwell");
        auxCondValveToHotwellHeatNode = new HeatNode();
        auxCondValveToHotwellHeatNode.setName("AuxCond#ToHotwellHeatNode");
        auxCondValveToHotwellConverter = new PhasedHeatFluidConverter(phasedWater);
        auxCondValveToHotwellConverter.setName("AuxCond#ToHotwellConverter");
        auxCondValveToHotwellPhasedNode = new PhasedNode();
        auxCondValveToHotwellPhasedNode.setName("AuxCond#ToHotwellPhasedNode");
        auxCondValveToDrain = new HeatValve();
        auxCondValveToDrain.initName("AuxCond#ToDrain");

        // Steam Dump
        for (int idx = 0; idx < mainSteamDump.length; idx++) {
            mainSteamDump[idx] = new PhasedValveControlled();
            mainSteamDump[idx].registerController(new PIControl());
            mainSteamDump[idx].initName(
                    "Main" + (idx + 1) + "#SteamDump");
        }

        // Condensation
        hotwell = new PhasedCondenserNoMass(phasedWater);
        hotwell.initName("Hotwell");
        hotwell.initGenerateNodes();
        hotwellOutConverter = new PhasedHeatFluidConverter(phasedWater);
        hotwellOutConverter.setName("Hotwell#OutConverter");
        hotwellOutNode = new HeatNode();
        hotwellOutNode.setName("Hotwell#OutNode");
        for (int idx = 0; idx < 3; idx++) {
            condensationHotwellPump[idx] = new HeatFluidPump();
            condensationHotwellPump[idx].initName(
                    "Condensation" + (idx + 1) + "#HotwellPump");
        }
        condensationPumpOut = new HeatNode();
        condensationPumpOut.setName("Condensation#PumpOut");
        // condensationEjectorDummy = new HeatVolumizedFlowResistance();
        // condensationEjectorDummy.setName("Condensation#EjectorDummy");
        condensationBoosterPumpIn = new HeatNode();
        condensationBoosterPumpIn.setName("Condensation#BoosterPumpIn");
        for (int idx = 0; idx < 3; idx++) {
            condensationCondensatePump[idx] = new HeatFluidPump();
            condensationCondensatePump[idx].initName(
                    "Condensation" + (idx + 1) + "#CondensatePump");
        }
        for (int idx = 0; idx < 2; idx++) {
            condensationValveToDA[idx] = new HeatValveControlled();
            condensationValveToDA[idx].registerController(new PIControl());
            condensationValveToDA[idx].initName(
                    "Condensation" + (idx + 1) + "#ValveToDA");
            condensationValveOut[idx] = new HeatNode();
            condensationValveOut[idx].setName(
                    "Condensation" + (idx + 1) + "#ValveOut");
            condensationToDeaeratorConverter[idx]
                    = new PhasedHeatFluidConverter(phasedWater);
            condensationToDeaeratorConverter[idx].setName(
                    "Condensation" + (idx + 1) + "#ToDeaeratorConverter");
        }

        hotwellFillValveConverter = new PhasedHeatFluidConverter(phasedWater);
        hotwellFillValveConverter.setName("Hotwell#FillValveConverter");
        hotwellFillNode = new HeatNode();
        hotwellFillNode.setName("Hotwell#FillNode");
        hotwellFillValve = new HeatValveControlled();
        hotwellFillValve.registerController(new PIControl());
        hotwellFillValve.initName("Hotwell#FillValve");
        hotwellDrainValve = new HeatValveControlled();
        hotwellDrainValve.registerController(new PIControl());
        hotwellDrainValve.initName("Hotwell#DrainValve");

        condenserCoolantSource = new HeatOrigin();
        condenserCoolantSource.setName("Condenser#CoolantSource");
        condenserCoolantSourceNode = new HeatNode();
        condenserCoolantSourceNode.setName("Condenser#CoolantSourceNode");
        condenserCoolant = new HeatControlledFlowSource();
        condenserCoolant.initName("Condenser#Coolant");
        condenserCoolantSink = new HeatOrigin();
        condenserCoolantSink.setName("Condenser#CoolantSource");

        // Ejectors (both startup and main)
        ejectorTurbineTapNode = new PhasedNode();
        ejectorTurbineTapNode.setName("EjectorDummyTurbineTap");
        for (int idx = 0; idx < 2; idx++) {
            ejectorStartup[idx] = new PhasedValve();
            ejectorStartup[idx].initName(
                    "EjectorStartup" + (idx + 1));
        }
        for (int idx = 0; idx < 3; idx++) {
            ejectorMainSteamValve[idx] = new PhasedValve();
            ejectorMainSteamValve[idx].initName(
                    "EjectorMain" + (idx + 1) + "#SteamValve");
            ejectorMain[idx] = new PhasedCondenserNoMass(phasedWater);
            ejectorMain[idx].initGenerateNodes();
            ejectorMain[idx].initName("EjectorMain" + (idx + 1));
            ejectorMainCondensate[idx]
                    = new PhasedHeatFluidConverter(phasedWater);
            ejectorMainCondensate[idx].setName(
                    "EjectorMain" + (idx + 1) + "#Condensate");
            ejectorMainCondensateOut[idx] = new HeatNode();
            ejectorMainCondensateOut[idx].setName(
                    "EjectorMain" + (idx + 1) + "#CondensateOut");
            ejectorLevel[idx] = new HeatEffortSource();
            ejectorLevel[idx].setName("EjectorMain" + (idx + 1) + "#Level");
            ejectorMainCondensateUp[idx] = new HeatNode();
            ejectorMainCondensateUp[idx].setName(
                    "EjectorMain" + (idx + 1) + "#CondensateUp");
            ejectorMainCondensateValve[idx] = new HeatValveControlled();
            ejectorMainCondensateValve[idx].registerController(new PIControl());
            ejectorMainCondensateValve[idx].initName("EjectorMain"
                    + (idx + 1) + "#CondensateValve");
            ejectorMainFlowIn[idx] = new HeatValve();
            ejectorMainFlowIn[idx].initName(
                    "EjectorMain" + (idx + 1) + "#FlowIn");
            ejectorMainFlowReistance[idx] = new HeatSimpleFlowResistance();
            ejectorMainFlowReistance[idx].setName(
                    "EjectorMain" + (idx + 1) + "#FlowResistance");
            ejectorMainFlowReistanceNode[idx] = new HeatNode();
            ejectorMainFlowReistanceNode[idx].setName(
                    "EjectorMain" + (idx + 1) + "#FlowResistanceNode");
            ejectorMainFlowOut[idx] = new HeatValve();
            ejectorMainFlowOut[idx].initName(
                    "EjectorMain" + (idx + 1) + "#FlowOut");
        }
        ejectorMainBypass = new HeatValve();
        ejectorMainBypass.initName("EjectorMain#Bypass");
        ejectorToHotwellHeatNode = new HeatNode();
        ejectorToHotwellHeatNode.setName("Ejector#ToHotwellHeatNode");
        ejectorToHotwellConverter = new PhasedHeatFluidConverter(phasedWater);
        ejectorToHotwellConverter.setName("Ejector#ToHotwellConverter");
        ejectorToHotwellPhasedNode = new PhasedNode();
        ejectorToHotwellPhasedNode.setName("Ejector#ToHotwellPhasedNode");

        for (int idx = 0; idx < 3; idx++) {
            preheater[idx] = new PhasedCondenserNoMass(phasedWater);
            preheater[idx].initGenerateNodes();
            preheater[idx].initName(
                    "Preheater" + (idx + 1));
        }
        for (int idx = 0; idx < 2; idx++) {
            preheaterPiping[idx] = new HeatSimpleFlowResistance();
            preheaterPiping[idx].setName("Preheater"
                    + (idx + 1) + "#Piping");
        }
        for (int idx = 0; idx < 3; idx++) {
            preheaterCondensateValve[idx]
                    = new PhasedValveControlled();
            preheaterCondensateValve[idx]
                    .registerController(new PIControl());
            preheaterCondensateValve[idx].initName(
                    "Preheater" + (idx + 1) + "#CondensateValve");
        }

        // Valves to turbine inlet
        for (int idx = 0; idx < 2; idx++) {
            turbineTripValve[idx] = new PhasedValve();
            turbineTripValve[idx].initName(
                    "Turbine" + (idx + 1) + "#TripValve");
            turbineSteamIn[idx] = new PhasedNode();
            turbineSteamIn[idx].setName(
                    "Turbine" + (idx + 1) + "#SteamIn");
            turbineStartupSteamValve[idx] = new PhasedValveControlled();
            turbineStartupSteamValve[idx].registerController(new PIControl());
            turbineStartupSteamValve[idx].initName(
                    "Turbine" + (idx + 1) + "#StartupSteamValve");
            turbineMainSteamValve[idx] = new PhasedValveControlled();
            turbineMainSteamValve[idx].registerController(new PIControl());
            turbineMainSteamValve[idx].initName(
                    "Turbine" + (idx + 1) + "#MainSteamValve");
            turbineReheaterSteamValve[idx] = new PhasedValveControlled();
            turbineReheaterSteamValve[idx].registerController(new PIControl());
            turbineReheaterSteamValve[idx].initName(
                    "Turbine" + (idx + 1) + "#ReheaterSteamValve");
        }
        turbineHighPressureIn = new PhasedNode();
        turbineHighPressureIn.setName("Turbine#HighPressureIn");
        turbineHighPressureInMass = new PhasedThermalExchanger(phasedWater);
        turbineHighPressureInMass.setName("Turbine#HighPressureInMass");
        turbineHighPressureMidIn = new PhasedNode();
        turbineHighPressureMidIn.setName("Turbine#HighPressureMidIn");
        turbineHighPressure = new PhasedLimitedPhaseSimpleFlowResistance(phasedWater);
        turbineHighPressure.setName("Turbine#HighPressure");
        turbineHighPressureMidOut = new PhasedNode();
        turbineHighPressureMidOut.setName("Turbine#HighPressureMidOut");
        turbineHighPressureOutMass = new PhasedThermalExchanger(phasedWater);
        turbineHighPressureOutMass.setName("Turbine#HighPressureOutMass");
        turbineReheaterSteam = new PhasedNode();
        turbineReheaterSteam.setName("Turbine#ReheaterSteam");
        turbineReheaterTripValve = new PhasedValve();
        turbineReheaterTripValve.initName("Turbine#ReheaterTripValve");
        turbineReheaterPriValvesMidNode = new PhasedNode();
        turbineReheaterPriValvesMidNode.setName(
                "Turbine#ReheaterPriValvesMidNode");
        turbineReheaterTrimValve = new PhasedValve();
        turbineReheaterTrimValve.initName("Turbine#ReheaterTrimValve");
        turbineReheater = new PhasedSuperheater(phasedWater);
        turbineReheater.initGenerateNodes();
        turbineReheater.initName("Turbine#Superheater");
        for (int idx = 0; idx < 2; idx++) {
            turbineReheaterCondensateValve[idx] = new PhasedValveControlled();
            turbineReheaterCondensateValve[idx].registerController(
                    new PIControl());
            turbineReheaterCondensateValve[idx].initName(
                    "Turbine" + (idx + 1) + "#ReheaterCondensateValve");
            turbineReheaterCondensateNode[idx] = new PhasedNode();
            turbineReheaterCondensateNode[idx].setName(
                    "Turbine" + (idx + 1) + "#ReheaterCondensateNode");
        }
        turbineReheaterCondensateDrain = new PhasedValveControlled();
        turbineReheaterCondensateDrain.registerController(new PIControl());
        turbineReheaterCondensateDrain.initName("Turbine#ReheaterCondensateDrain");
        turbineReheaterCondensateDrainOut = new PhasedNode();
        turbineReheaterCondensateDrainOut.setName("Turbine#ReheaterCondensateDrainOut");
        turbineReheaterCondensateHeight = new PhasedEffortSource();
        turbineReheaterCondensateHeight.setName("Turbine#ReheaterCondensateHeight");
        turbineLowPressureTripValve = new PhasedValve();
        turbineLowPressureTripValve.initName("Turbine#LowPressureTripValve");
        turbineLowPressureIn = new PhasedNode();
        turbineLowPressureIn.setName("Turbine#LowPressureIn");
        turbineLowPressureInMass = new PhasedThermalExchanger(phasedWater);
        turbineLowPressureInMass.setName("Turbine#LowPressureInMass");
        turbineLowPressureMidIn = new PhasedNode();
        turbineLowPressureMidIn.setName("Turbine#LowPressureMidIn");
        for (int idx = 0; idx < 6; idx++) {
            turbineLowPressureStage[idx]
                    = new PhasedLimitedPhaseSimpleFlowResistance(phasedWater);
            turbineLowPressureStage[idx].setName(
                    "Turbine" + (idx + 1) + "#LowPressureStage");
        }
        for (int idx = 0; idx < 5; idx++) {
            turbineLowPressureStageOut[idx] = new PhasedNode();
            turbineLowPressureStageOut[idx].setName(
                    "Turbine" + (idx + 1) + "#LowPressureStageOut");
            turbineLowPressureTap[idx]
                    = new PhasedLimitedPhaseSimpleFlowResistance(phasedWater);
            turbineLowPressureTap[idx].setName(
                    "Turbine" + (idx + 1) + "#LowPressureTap");
            turbineLowPressureTapOut[idx] = new PhasedNode();
            turbineLowPressureTapOut[idx].setName(
                    "Turbine" + (idx + 1) + "#LowPressureTapOut");
            turbineLowPressureTapValve[idx] = new PhasedValve();
            turbineLowPressureTapValve[idx].initName(
                    "Turbine" + (idx + 1) + "#LowPressureTapValve");
        }
        turbineLowPressureMidOut = new PhasedNode();
        turbineLowPressureMidOut.setName("Turbine#LowPressureMidOut");
        turbineLowPressureOutMass = new PhasedThermalExchanger(phasedWater);
        turbineLowPressureOutMass.setName("Turbine#LowPressureOutMass");

        //</editor-fold>      
        blowdownBalanceControlLoop.setName("Blowdown#BalanceControl");

        // <editor-fold defaultstate="collapsed" desc="Control Setpoint instances">
        for (int idx = 0; idx < 2; idx++) {
            setpointDrumLevel[idx] = new Setpoint();
            setpointDrumLevel[idx].initName(
                    "Loop" + (idx + 1) + "#DrumLevelSetpoint");
            setpointDAPressure[idx] = new Setpoint();
            setpointDAPressure[idx].initName(
                    "Deaerator" + (idx + 1) + "#PressureSetpoint");
            setpointDALevel[idx] = new Setpoint();
            setpointDALevel[idx].initName(
                    "Deaerator" + (idx + 1) + "#LevelSetpoint");
            setpointAuxCondLevel[idx] = new Setpoint();
            setpointAuxCondLevel[idx].initName(
                    "AuxCond" + (idx + 1) + "#LevelSetpoint");
        }
        setpointHotwellUpperLevel = new Setpoint();
        setpointHotwellUpperLevel.initName("Hotwell#UpperSetpoint");
        setpointHotwellLowerLevel = new Setpoint();
        setpointHotwellLowerLevel.initName("Hotwell#LowerSetpoint");
        setpointDrumPressure = new Setpoint();
        setpointDrumPressure.initName("LoopPressureSetpoint");
        setpointTurbineReheaterTemperature = new Setpoint();
        setpointTurbineReheaterTemperature.initName(
                "Turbine#SetpointReheaterTemperature");
        setpointTurbineReheaterLevel = new Setpoint();
        setpointTurbineReheaterLevel.initName(
                "Turbine#SetpointReheaterLevel");

        // </editor-fold>
    }

    public void init() {
        // <editor-fold defaultstate="collapsed" desc="Init listeners">
        // Attach controller to monitor elements. Those monitor elements are
        // part of assemblies and send events to the controller, like a valve
        // open state reached.
        for (int idx = 0; idx < 2; idx++) {
            makeupPumps[idx].registerSignalListener(controller);
        }

        for (int idx = 0; idx < 2; idx++) {
            mainSteamShutoffValve[idx].registerSignalListener(controller);
            deaeratorSteamInRegValve[idx].registerSignalListener(controller);
            deaeratorSteamInRegValve[idx].registerParameterHandler(
                    outputValues);
            deaeratorSteamFromMain[idx].registerSignalListener(controller);
            deaeratorSteamFromMain[idx].registerParameterHandler(outputValues);
            for (int jdx = 0; jdx < 4; jdx++) {
                loopAssembly[idx][jdx].registerSignalListener(controller);
            }
            loopBypass[idx].registerSignalListener(controller);
            blowdownValveFromDrum[idx].registerSignalListener(controller);
            blowdownValveFromDrum[idx].registerParameterHandler(outputValues);
            blowdownValveFromLoop[idx].registerSignalListener(controller);
            blowdownValveFromLoop[idx].registerParameterHandler(outputValues);
            blowdownCooldownPumps[idx].registerSignalListener(controller);
        }
        blowdownValvePassiveFlow.registerSignalListener(controller);
        blowdownValvePumpsToRegenerator.registerSignalListener(controller);
        blowdownValvePumpsToCooler.registerSignalListener(controller);
        blowdownValveRegeneratorToCooler.registerSignalListener(controller);
        blowdownValveTreatmentBypass.registerSignalListener(controller);
        blowdownValveRegeneratedToDrums.registerSignalListener(controller);
        blowdownValveDrain.registerSignalListener(controller);
        blowdownValveCoolant.registerSignalListener(controller);
        blowdownValveCoolant.registerParameterHandler(outputValues);
        for (int idx = 0; idx < 2; idx++) {
            blowdownReturnValve[idx].registerSignalListener(controller);
            blowdownReturnValve[idx].registerParameterHandler(outputValues);
        }
        blowdownBalanceControlLoop.addPropertyChangeListener(controller);
        for (int idx = 0; idx < 2; idx++) {
            deaeratorDrain[idx].registerSignalListener(controller);
            deaeratorDrain[idx].registerParameterHandler(outputValues);
        }
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 2; jdx++) {
                feedwaterPump[idx][jdx].registerSignalListener(controller);
            }
            feedwaterSparePumpInValve[idx].registerSignalListener(controller);
        }
        feedwaterPump3.registerSignalListener(controller);
        for (int idx = 0; idx < 2; idx++) {
            feedwaterSparePumpOutValve[idx].registerSignalListener(controller);
            feedwaterStartupReductionValve[idx].registerSignalListener(
                    controller);
            for (int jdx = 0; jdx < 3; jdx++) {
                feedwaterShutoffValve[idx][jdx].registerSignalListener(
                        controller);
                feedwaterFlowRegulationValve[idx][jdx]
                        .registerSignalListener(controller);
                feedwaterFlowRegulationValve[idx][jdx]
                        .registerParameterHandler(outputValues);
            }
        }
        for (int idx = 0; idx < 2; idx++) {
            auxCondSteamValve[idx].registerSignalListener(controller);
            auxCondSteamValve[idx].registerParameterHandler(outputValues);
            auxCondCoolantValve[idx].registerSignalListener(controller);
            auxCondCoolantValve[idx].registerParameterHandler(outputValues);
            auxCondCondensateValve[idx].registerController(new PIControl());
            auxCondCondensateValve[idx].registerSignalListener(controller);
            auxCondCondensateValve[idx].registerParameterHandler(outputValues);
        }
        auxCondBypass.registerSignalListener(controller);
        auxCondBypass.registerParameterHandler(outputValues);
        for (int idx = 0; idx < 2; idx++) {
            auxCondPumps[idx].registerSignalListener(controller);
        }
        auxCondValveToHotwell.registerSignalListener(controller);
        auxCondValveToHotwell.registerParameterHandler(outputValues);
        auxCondValveToDrain.registerSignalListener(controller);
        auxCondValveToDrain.registerParameterHandler(outputValues);
        for (int idx = 0; idx < mainSteamDump.length; idx++) {
            mainSteamDump[idx].registerSignalListener(controller);
            mainSteamDump[idx].registerParameterHandler(outputValues);
        }
        for (int idx = 0; idx < 3; idx++) {
            condensationHotwellPump[idx].registerSignalListener(controller);
            condensationCondensatePump[idx].registerSignalListener(controller);
        }
        for (int idx = 0; idx < 2; idx++) {
            condensationValveToDA[idx].registerParameterHandler(outputValues);
            condensationValveToDA[idx].registerSignalListener(controller);
        }
        hotwellFillValve.registerParameterHandler(outputValues);
        hotwellFillValve.registerSignalListener(controller);
        hotwellDrainValve.registerParameterHandler(outputValues);
        hotwellDrainValve.registerSignalListener(controller);
        for (int idx = 0; idx < 2; idx++) {
            ejectorStartup[idx].registerParameterHandler(outputValues);
            ejectorStartup[idx].registerSignalListener(controller);
        }
        for (int idx = 0; idx < 3; idx++) {
            ejectorMainSteamValve[idx].registerParameterHandler(outputValues);
            ejectorMainSteamValve[idx].registerSignalListener(controller);
            ejectorMainCondensateValve[idx].registerParameterHandler(outputValues);
            ejectorMainCondensateValve[idx].registerSignalListener(controller);
            ejectorMainFlowIn[idx].registerParameterHandler(outputValues);
            ejectorMainFlowIn[idx].registerSignalListener(controller);
            ejectorMainFlowOut[idx].registerParameterHandler(outputValues);
            ejectorMainFlowOut[idx].registerSignalListener(controller);
        }
        ejectorMainBypass.registerParameterHandler(outputValues);
        ejectorMainBypass.registerSignalListener(controller);

        for (int idx = 0; idx < 3; idx++) {
            preheaterCondensateValve[idx].registerParameterHandler(outputValues);
            preheaterCondensateValve[idx].registerSignalListener(controller);
        }

        for (int idx = 0; idx < 2; idx++) {
            turbineTripValve[idx].registerParameterHandler(outputValues);
            turbineTripValve[idx].registerSignalListener(controller);
            turbineStartupSteamValve[idx].registerParameterHandler(outputValues);
            turbineStartupSteamValve[idx].registerSignalListener(controller);
            turbineMainSteamValve[idx].registerParameterHandler(outputValues);
            turbineMainSteamValve[idx].registerSignalListener(controller);
            turbineReheaterSteamValve[idx].registerParameterHandler(outputValues);
            turbineReheaterSteamValve[idx].registerSignalListener(controller);
        }
        turbineLowPressureTripValve.registerParameterHandler(outputValues);
        turbineLowPressureTripValve.registerSignalListener(controller);
        turbineReheaterTripValve.registerParameterHandler(outputValues);
        turbineReheaterTripValve.registerSignalListener(controller);
        turbineReheaterTrimValve.registerParameterHandler(outputValues);
        turbineReheaterTrimValve.registerSignalListener(controller);
        for (int idx = 0; idx < 2; idx++) {
            turbineReheaterCondensateValve[idx].registerParameterHandler(outputValues);
            turbineReheaterCondensateValve[idx].registerSignalListener(controller);
        }
        turbineReheaterCondensateDrain.registerParameterHandler(outputValues);
        turbineReheaterCondensateDrain.registerSignalListener(controller);
        for (int idx = 0; idx < 5; idx++) {
            turbineLowPressureTapValve[idx].registerParameterHandler(outputValues);
            turbineLowPressureTapValve[idx].registerSignalListener(controller);
        }

        // Attach Signal Listeners or Handlers to Control elements
        for (int idx = 0; idx < 2; idx++) {
            setpointDrumLevel[idx].registerParameterHandler(outputValues);
            setpointDAPressure[idx].registerParameterHandler(outputValues);
            setpointDALevel[idx].registerParameterHandler(outputValues);
            setpointAuxCondLevel[idx].registerParameterHandler(outputValues);
        }
        setpointDrumPressure.registerParameterHandler(outputValues);
        setpointHotwellUpperLevel.registerParameterHandler(outputValues);
        setpointHotwellLowerLevel.registerParameterHandler(outputValues);
        setpointTurbineReheaterTemperature.registerParameterHandler(outputValues);
        setpointTurbineReheaterLevel.registerParameterHandler(outputValues);
        // </editor-fold>  
        turbine.initConnections();
        // <editor-fold defaultstate="collapsed" desc="Node-element connections">
        // Cold condensate storage
        makeupStorage.connectTo(makeupStorageDrainCollector);
        makeupStorage.connectTo(makeupStorageOut);
        for (int idx = 0; idx < 2; idx++) {
            makeupPumps[idx].getSuctionValve().connectTo(makeupStorageOut);
            makeupPumps[idx].getDischargeValve()
                    .connectTo(makeupPumpsOut);
        }

        // Define the primary loop from drum through mcps and reactor and back.
        for (int idx = 0; idx < 2; idx++) {
            // Steam Drum Blowdown (will be continued later at blowdown system)
            loopSteamDrum[idx].connectToVia(
                    loopFromDrumBlowdownConverter[idx],
                    loopNodeDrumBlowdownOut[idx]);
            loopFromDrumBlowdownConverter[idx].connectTo(
                    loopNodeDrumBlowdownHeatOut[idx]);
            // From steam drum through converter down to mcp suction header:
            loopSteamDrum[idx].connectToVia(
                    loopFromDrumConverter[idx], loopNodeDrumWaterOut[idx]);
            loopFromDrumConverter[idx].connectToVia(
                    loopDownflow[idx], loopFeedwaterIn[idx]);
            loopDownflow[idx].connectTo(loopCollector[idx]);
            // Place 4 MCPs parallel, each has a trim valve after it,
            for (int jdx = 0; jdx < 4; jdx++) {
                loopAssembly[idx][jdx].getSuctionValve()
                        .connectTo(loopCollector[idx]);
                loopAssembly[idx][jdx].getDischargeValve().connectToVia(
                        loopTrimValve[idx][jdx].getValveElement(),
                        loopTrimNode[idx][jdx]);
                loopTrimValve[idx][jdx].getValveElement()
                        .connectTo(loopDistributor[idx]);
            }
            // Place bypass valve parallel to the MPCs
            loopBypass[idx].getValveElement().connectBetween(
                    loopCollector[idx], loopDistributor[idx]);
            // Add a flow resistance element and an effort source that will
            // add pressure difference for natural ciruclation on open bypass
            loopChannelFlowResistance[idx].connectBetween(
                    loopDistributor[idx], loopAfterResistance[idx]);
            loopThermalLift[idx].connectTo(loopAfterResistance[idx]);
            loopThermalLift[idx].connectTo(loopAfterThermalLift[idx]);
            loopToReactorConverter[idx].connectBetween(
                    loopAfterThermalLift[idx], loopEvaporatorIn[idx]);
            loopEvaporator[idx].initComponent();
            loopEvaporator[idx].connectTo(loopEvaporatorIn[idx]);
            loopEvaporator[idx].connectToVia(
                    loopSteamDrum[idx], loopNodeDrumFromReactor[idx]);
        }
        // Define the fuel thermal model and connect it to the loop.
        fuelGround.connectTo(fuelGroundNode);
        for (int idx = 0; idx < 2; idx++) {
            fuelThermalSource[idx].connectTo(fuelGroundNode);
            fuelThermalSource[idx].connectTo(fuelThermalOut[idx]);
            // fuelThermalCapacity[idx].connectTo(fuelThermalOut[idx]);
            fuelThermalResistance[idx].connectBetween(
                    fuelThermalOut[idx], fuelEvaporatorNode[idx]);
            // Connect to the Evaporators Temperature source
            loopEvaporator[idx].getInnerThermalEffortSource().connectTo(
                    fuelEvaporatorNode[idx]);
        }
        // Connect steam shutoff valves to drum 
        for (int idx = 0; idx < 2; idx++) {
            loopSteamDrum[idx].connectTo(mainSteamDrumNode[idx]);
            loopSteamDrum[idx].setSteamOut(mainSteamDrumNode[idx]);
            mainSteamShutoffValve[idx].getValveElement().connectBetween(
                    mainSteamDrumNode[idx], mainSteam[idx]);
        }

        // Build the blowdown system: 
        // 1st: Each steam drum has a designated blowdown out.
        // 2nd: a connection to a large pipe that goes to a trim valve 
        // connected to each distributor (thats the part that distributes 
        // coolant to fuel channels), connected to a common node.
        for (int idx = 0; idx < 2; idx++) {
            blowdownValveFromDrum[idx].getValveElement().connectBetween(
                    loopNodeDrumBlowdownHeatOut[idx], blowdownInCollectorNode);

            // From MCPs:
            blowdownPipeFromMcp[idx].connectBetween(
                    loopDistributor[idx], blowdownFromLoopNode[idx]);
            blowdownValveFromLoop[idx].getValveElement().connectBetween(
                    blowdownFromLoopNode[idx], blowdownInCollectorNode);
        }
        // Passive flow without pumps
        blowdownValvePassiveFlow.getValveElement().connectBetween(
                blowdownInCollectorNode, blowdownRegeneratorInCollectorNode);
        // Two parallel cooldown pumps are connected there
        for (int idx = 0; idx < 2; idx++) {
            blowdownCooldownPumps[idx].getSuctionValve().connectTo(
                    blowdownInCollectorNode);
            blowdownCooldownPumps[idx].getDischargeValve().connectTo(
                    blowdownPumpCollectorNode);
        }
        // valve on the left lower side
        blowdownValvePumpsToRegenerator.getValveElement().connectBetween(
                blowdownRegeneratorInCollectorNode, blowdownPumpCollectorNode);
        // Resistance on the input side of the regenerator
        blowdownToRegeneratorFirstResistance.connectTo(
                blowdownRegeneratorInCollectorNode);
        blowdownToRegeneratorFirstResistance.connectTo(
                blowdownRegenerator.getPrimarySide().getNode(0));
        blowdownValveRegeneratorToCooler.getValveElement().connectTo(
                blowdownRegenerator.getPrimarySide().getNode(1));
        blowdownValveRegeneratorToCooler.getValveElement().connectTo(
                blowdownToCooldownNode);
        // Valve richt to the pump collector node
        blowdownValvePumpsToCooler.getValveElement().connectBetween(
                blowdownPumpCollectorNode,
                blowdownToCooldownNode);
        blowdownCooldownResistance.connectBetween(blowdownToCooldownNode,
                blowdownCooldown.getPrimarySide().getNode(0));
        // Treatment bypass
        blowdownValveTreatmentBypass.getValveElement().connectBetween(
                blowdownCooldown.getPrimarySide().getNode(1),
                blowdownOutNode);
        // Flow path from cooldown out through regenerator to out node        
        blowdownTreatment.connectBetween(
                blowdownCooldown.getPrimarySide().getNode(1),
                blowdownTreatedOutNode);
        blowdownToRegeneratorSecondResistance.connectBetween(
                blowdownTreatedOutNode,
                blowdownRegenerator.getSecondarySide().getNode(0));
        blowdownValveRegeneratedToDrums.getValveElement().connectBetween(
                blowdownRegenerator.getSecondarySide().getNode(1),
                blowdownOutNode);
        // Add the drain back to makeup storage
        blowdownValveDrain.getValveElement().connectBetween(
                blowdownTreatedOutNode, makeupStorageDrainCollector);
        // Secondary coolant flow, just a flow source for forcing the flow.
        blowdownCoolantSource.connectTo(blowdownCoolantSourceNode);
        blowdownValveCoolant.getFlowSource()
                .connectBetween(blowdownCoolantSourceNode,
                        blowdownCooldown.getSecondarySide().getNode(1));
        blowdownCoolantSink.connectTo(
                blowdownCooldown.getSecondarySide().getNode(0));
        // Connect blowdown out valves to the feedwater collector via pipes
        for (int idx = 0; idx < 2; idx++) {
            blowdownReturnValve[idx].getValveElement().connectTo(
                    blowdownOutNode);
            blowdownReturnValve[idx].getValveElement().connectTo(
                    blowdownReturnNode[idx]);
            blowdownReturn[idx].connectBetween(
                    blowdownReturnNode[idx], loopFeedwaterIn[idx]);
        }

        // Deaerators
        for (int idx = 0; idx < 2; idx++) {
            // DA has two nodes for in and outflow. The Steam for heating it up
            // will simply be added to the inNode.
            deaerator[idx].connectTo(deaeratorOutNode[idx]);
            deaerator[idx].connectTo(deaeratorInNode[idx]);
            // only heat fluid will leave DA, place a conveter after
            deaeratorToFeedwaterConverter[idx].connectBetween(
                    deaeratorOutNode[idx],
                    deaeratorFeedwaterOutHeatNode[idx]);
            // Drain gets its own converter, this allows the model to be split 
            // to its own path for a separate subnet solution. Otherwise model
            // complexity would explode
            deaeratorToDrainConverter[idx].connectBetween(
                    deaeratorOutNode[idx],
                    deaeratorDrainOutHeatNode[idx]);
            deaeratorDrain[idx].getValveElement().connectBetween(
                    deaeratorDrainOutHeatNode[idx],
                    makeupStorageDrainCollector);
            // Steam in from main
            deaeratorSteamFromMain[idx].getValveElement().connectBetween(
                    mainSteam[idx], deaeratorSteamInNode[idx]);
            deaeratorSteamInRegValve[idx].getValveElement().connectBetween(
                    deaeratorSteamInNode[idx], deaeratorInNode[idx]);
            // Middle connection with flow resistance to common
            // mid point,
            deaeratorSteamDistribution[idx].connectBetween(
                    deaeratorSteamInNode[idx], deaeratorSteamMiddle);
            // The turbine tap gets connected between both steam valves.
            // Todo
        }
        // Feedwater Pumps
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 2; jdx++) {
                // 2 Pumps on both sides
                feedwaterPump[idx][jdx].getSuctionValve().connectTo(
                        deaeratorFeedwaterOutHeatNode[idx]);
                feedwaterPump[idx][jdx].getDischargeValve().connectTo(
                        feedwaterPumpCollectorNodes[idx]);
            }
            // Valve from both DAs to spare pump in
            feedwaterSparePumpInValve[idx].getValveElement().connectBetween(
                    deaeratorFeedwaterOutHeatNode[idx], feedwaterSparePumpIn);
        }
        feedwaterPump3.getSuctionValve().connectTo(feedwaterSparePumpIn);
        feedwaterPump3.getDischargeValve().connectTo(feedwaterSparePumpOut);
        // Pumps to drum connection, this is a bit messed up. It gets 
        // distributed from collector node as follows:
        // - StartRed - aftSNode - ShutoffValve[0] - intoFlowRegN[0] - flowRegV
        // - ShutoffValve[1] - intoFlowRegN[1] - flowRegV[2]
        // - ShutoffValve[2] - intoFlowRegN[2] - flowRegV[2]
        for (int idx = 0; idx < 2; idx++) { // idx is still loop side btw.
            // Distribution to both sides with out valves:
            feedwaterSparePumpOutValve[idx].getValveElement().connectBetween(
                    feedwaterPumpCollectorNodes[idx], feedwaterSparePumpOut);
            // Connect 1 Startup and 2 Main flow valves:
            feedwaterStartupReductionValve[idx].getValveElement()
                    .connectBetween(feedwaterPumpCollectorNodes[idx],
                            feedwaterAfterStartupNode[idx]);
            // Statup shutoff after first flow reduction valve
            feedwaterShutoffValve[idx][0].getValveElement().connectBetween(
                    feedwaterAfterStartupNode[idx],
                    feedwaterIntoFlowRegNode[idx][0]);
            for (int jdx = 1; jdx < 3; jdx++) {
                // Remaining two shutoff valves
                feedwaterShutoffValve[idx][jdx].getValveElement()
                        .connectBetween(feedwaterPumpCollectorNodes[idx],
                                feedwaterIntoFlowRegNode[idx][jdx]);
            }
            for (int jdx = 0; jdx < 3; jdx++) {
                // flow regulator valves to drum feedwater in
                feedwaterFlowRegulationValve[idx][jdx].getValveElement()
                        .connectBetween(feedwaterIntoFlowRegNode[idx][jdx],
                                loopFeedwaterIn[idx]);
            }
        }

        // Auxiliary Condenser
        for (int idx = 0; idx < 2; idx++) {
            // Build coolant supply: source - flow source - heat exch - sink
            auxCondCoolantSource[idx].connectToVia(
                    auxCondCoolantValve[idx].getFlowSource(),
                    auxCondCoolantSourceNode[idx]);
            auxCondCoolantValve[idx].getFlowSource().connectTo(
                    auxCondensers[idx].getHeatNode(
                            PhasedCondenserNoMass.SECONDARY_IN));
            auxCondCoolantSink[idx].connectTo(auxCondensers[idx].getHeatNode(
                    PhasedCondenserNoMass.SECONDARY_OUT));
            // Steam into condenser
            auxCondSteamValve[idx].getValveElement().connectBetween(
                    mainSteam[idx],
                    auxCondensers[idx].getPhasedNode(
                            PhasedCondenserNoMass.PRIMARY_IN));
            // Condeser out: Convert to heat fluid domain and then into reg valv
            auxCondOutConverter[idx].connectBetween(
                    auxCondensers[idx].getPhasedNode(
                            PhasedCondenserNoMass.PRIMARY_OUT),
                    auxCondCondenserOutNode[idx]);
            auxCondCondensateValve[idx].getValveElement().connectBetween(
                    auxCondCondenserOutNode[idx], auxCondCondInNode);
            // Also connect one of 2 pumps here, no separate loop for this.
            auxCondPumps[idx].getPumpEffortSource().connectTo(
                    auxCondCondInNode);
            // Connect to collector node:
            auxCondPumps[idx].getDischargeValve().connectTo(
                    auxCondCollectorNode);
        }
        // Bypass parallel to those 2 pumps
        auxCondBypass.getValveElement().connectTo(auxCondCondInNode);
        auxCondBypass.getValveElement().connectTo(auxCondCollectorNode);
        // height difference as pressure diff source
        auxCondHeightDifference.connectBetween(auxCondCollectorNode,
                auxCondDistributorNode);
        // To hotwell: Connect to a converter for this purpose, this allows
        // better network simplification and performance.
        auxCondValveToHotwell.getValveElement().connectBetween(
                auxCondDistributorNode, auxCondValveToHotwellHeatNode);
        auxCondValveToHotwellConverter.connectBetween(
                auxCondValveToHotwellHeatNode, auxCondValveToHotwellPhasedNode);
        hotwell.getPrimarySideReservoir().connectTo(
                auxCondValveToHotwellPhasedNode);

        // Drain to cold condensate storage
        auxCondValveToDrain.getValveElement().connectBetween(
                auxCondDistributorNode, makeupStorageDrainCollector);

        // Steam Dump
        for (int idx = 0; idx < 2; idx++) {
            mainSteamDump[idx].getValveElement().connectBetween(mainSteam[idx],
                    hotwell.getPhasedNode(PhasedCondenserNoMass.PRIMARY_IN));
        }

        // Condensation: Hotwell to Deaerators
        hotwellOutConverter.connectBetween(
                hotwell.getPhasedNode(PhasedCondenserNoMass.PRIMARY_OUT),
                hotwellOutNode);
        for (int idx = 0; idx < condensationHotwellPump.length; idx++) {
            condensationHotwellPump[idx].getSuctionValve().connectTo(
                    hotwellOutNode);
            condensationHotwellPump[idx].getDischargeValve().connectTo(
                    condensationPumpOut);
        }

        // The main ejectors are here between those pumps, but that is handled
        // a bit later in code.
        for (int idx = 0; idx < condensationCondensatePump.length; idx++) {
            condensationCondensatePump[idx].getSuctionValve().connectTo(
                    condensationBoosterPumpIn);
            condensationCondensatePump[idx].getDischargeValve().connectTo(
                    preheater[0].getHeatNode(
                            PhasedCondenserNoMass.SECONDARY_IN));
        }

        // Condensate gets pumped through secondary side of low pressure 
        // preheater and heated up there. Connect the preheaters with a 
        // resistance element inbetween them.
        preheaterPiping[0].connectBetween(
                preheater[0].getHeatNode(
                        PhasedCondenserNoMass.SECONDARY_OUT),
                preheater[1].getHeatNode(
                        PhasedCondenserNoMass.SECONDARY_IN));
        preheaterPiping[1].connectBetween(
                preheater[1].getHeatNode(
                        PhasedCondenserNoMass.SECONDARY_OUT),
                preheater[2].getHeatNode(
                        PhasedCondenserNoMass.SECONDARY_IN));

        // Valve to deaerator
        for (int idx = 0; idx < 2; idx++) {
            condensationValveToDA[idx].getValveElement().connectBetween(
                    preheater[2].getHeatNode(
                            PhasedCondenserNoMass.SECONDARY_OUT),
                    condensationValveOut[idx]);
            condensationToDeaeratorConverter[idx].connectBetween(
                    condensationValveOut[idx], deaeratorInNode[idx]);
        }

        // Hotwell fill and drain valves: Those get connected via their own
        // converter elements so the transfer subnets can be set up as separate
        // networks by the solver.
        hotwellFillValveConverter.connectBetween(
                hotwell.getPhasedNode(PhasedCondenserNoMass.PRIMARY_OUT),
                hotwellFillNode);

        // Fill valve is connected to makup pumps out
        hotwellFillValve.getValveElement().connectBetween(
                makeupPumpsOut, hotwellFillNode);

        // Drain valve is placed after the first condensate pumps and the 
        // condensate is pumped back into the makeup storage tank.
        hotwellDrainValve.getValveElement().connectBetween(
                condensationPumpOut, makeupStorageDrainCollector);

        // Coolant loop, so far just a constant flow source
        condenserCoolantSource.connectTo(condenserCoolantSourceNode);
        condenserCoolant.getFlowSource().connectBetween(
                condenserCoolantSourceNode,
                hotwell.getHeatNode(PhasedCondenserNoMass.SECONDARY_IN));
        condenserCoolantSink.connectTo(
                hotwell.getHeatNode(PhasedCondenserNoMass.SECONDARY_OUT));

        // Startup ejector is modeled as a simple valve to hotwell, exactly 
        // like the main steam bypass.
        for (int idx = 0; idx < 2; idx++) {
            ejectorStartup[idx].getValveElement().connectBetween(mainSteam[idx],
                    hotwell.getPhasedNode(PhasedCondenserNoMass.PRIMARY_IN));
        }

        // Main ejectors: The condensate will be pumped through the secondary 
        // side, this will warm it up a bit. It is already converted to heat
        // fluid domain. The primary sides are connected to a turbine tap.
        for (int idx = 0; idx < 3; idx++) {
            ejectorMainSteamValve[idx].getValveElement().connectBetween(
                    ejectorTurbineTapNode,
                    ejectorMain[idx].getPhasedNode(PhasedCondenserNoMass.PRIMARY_IN));
            ejectorMainCondensate[idx].connectBetween(
                    ejectorMain[idx].getPhasedNode(PhasedCondenserNoMass.PRIMARY_OUT),
                    ejectorMainCondensateOut[idx]);
            // add an effort source after condensate out to increase pressure
            // towards hotwell. Lets just assume its on a higher position
            // in the building so the flow works even if everything is cooled 
            // down.
            ejectorLevel[idx].connectBetween(
                    ejectorMainCondensateOut[idx],
                    ejectorMainCondensateUp[idx]);
            ejectorMainCondensateValve[idx].getValveElement().connectBetween(
                    ejectorMainCondensateUp[idx], ejectorToHotwellHeatNode);
            // Coolant loop is connected between condensate pumps:
            ejectorMainFlowIn[idx].getValveElement().connectBetween(
                    condensationPumpOut,
                    ejectorMain[idx].getHeatNode(PhasedCondenserNoMass.SECONDARY_IN));
            ejectorMainFlowReistance[idx].connectBetween(
                    ejectorMain[idx].getHeatNode(PhasedCondenserNoMass.SECONDARY_OUT),
                    ejectorMainFlowReistanceNode[idx]);
            ejectorMainFlowOut[idx].getValveElement().connectBetween(
                    ejectorMainFlowReistanceNode[idx],
                    condensationBoosterPumpIn);
        }
        ejectorMainBypass.getValveElement().connectBetween(
                condensationPumpOut, condensationBoosterPumpIn);
        // use a separate converter back to hotwell for a separate flow path.
        ejectorToHotwellConverter.connectBetween(
                ejectorToHotwellHeatNode, ejectorToHotwellPhasedNode);
        hotwell.getPrimarySideReservoir().connectTo(
                ejectorToHotwellPhasedNode);

        // Low pressure preheaters condensate flow: This is pressure based and
        // the preheater will just push the condensate to the previous one
        preheaterCondensateValve[2].getValveElement().connectBetween(
                preheater[2].getPhasedNode(PhasedCondenserNoMass.PRIMARY_OUT),
                preheater[1].getPhasedNode(PhasedCondenserNoMass.PRIMARY_OUT));
        preheaterCondensateValve[1].getValveElement().connectBetween(
                preheater[1].getPhasedNode(PhasedCondenserNoMass.PRIMARY_OUT),
                preheater[0].getPhasedNode(PhasedCondenserNoMass.PRIMARY_OUT));
        preheaterCondensateValve[0].getValveElement().connectBetween(
                preheater[0].getPhasedNode(PhasedCondenserNoMass.PRIMARY_OUT),
                hotwell.getPhasedNode(PhasedCondenserNoMass.PRIMARY_OUT));

        // Turbine
        // Turbine valves from steam drums to common collector node
        // NOTE: The turbine is NOT connected to the steam out valve from the 
        // drum. This would be more correct but crashes the solver so far, so 
        // it is connected directly to the durm, allowing more simple networks
        // to be compiled. It is not noticeable to the end user as the trip 
        // valves can't be opened when the main steam drum shutoff is closed.
        for (int idx = 0; idx < 2; idx++) {
            // Main steam valve:
            turbineTripValve[idx].getValveElement().connectBetween(
                    mainSteamDrumNode[idx], turbineSteamIn[idx]);
            // Startup path:
            turbineStartupSteamValve[idx].getValveElement().connectBetween(
                    turbineSteamIn[idx], turbineHighPressureIn);
            // Main steam valves parallel to startup valve
            turbineMainSteamValve[idx].getValveElement().connectBetween(
                    turbineSteamIn[idx], turbineHighPressureIn);
            // Reheater Steam Valve
            turbineReheaterSteamValve[idx].getValveElement().connectBetween(
                    mainSteamDrumNode[idx], turbineReheaterSteam);
        }
        // There is a thermal volume at the beginning and at the end of the
        // turbine which exchanges heat with the rotor and the stator.
        // Between these volumes is the limiting element that represents the
        // enthaly loss and transfer to the turbine blades.
        turbineHighPressureInMass.connectBetween(turbineHighPressureIn,
                turbineHighPressureMidIn);
        turbineHighPressure.connectBetween(turbineHighPressureMidIn,
                turbineHighPressureMidOut);
        // High pressure ends in the secondary loop of the superheater
        turbineHighPressureOutMass.connectBetween(turbineHighPressureMidOut,
                turbineReheater.getPhasedNode(PhasedSuperheater.SECONDARY_IN));
        // Connect both thermal exchangers to the turbine thermal model which
        // is managed in a different object.
        turbineHighPressureInMass.setInnerThermalEffortSource(
                turbine.getThermalEffortSource(0));
        turbineHighPressureOutMass.setInnerThermalEffortSource(
                turbine.getThermalEffortSource(1));

        // Superheater goes to a shutoff valve - this is quite non realistic 
        // but why not.
        turbineLowPressureTripValve.getValveElement().connectBetween(
                turbineReheater.getPhasedNode(PhasedSuperheater.SECONDARY_OUT),
                turbineLowPressureIn);
        // Superheater primary side
        turbineReheaterTripValve.getValveElement()
                .connectBetween(turbineReheaterSteam,
                        turbineReheaterPriValvesMidNode);
        turbineReheaterTrimValve.getValveElement()
                .connectBetween(turbineReheaterPriValvesMidNode,
                        turbineReheater.getPhasedNode(
                                PhasedSuperheater.PRIMARY_IN));
        // condensate to deaerators. This implies that the condensate has a 
        // higher temperature and steam pressure than the deaerators.
        for (int idx = 0; idx < 2; idx++) {
            turbineReheaterCondensateValve[idx].getValveElement()
                    .connectBetween( // Condensate to hotwell / condenser:
                            turbineReheater.getPhasedNode(
                                    PhasedSuperheater.PRIMARY_OUT),
                            turbineReheaterCondensateNode[idx]);
            deaerator[idx].connectTo(turbineReheaterCondensateNode[idx]);
        }
        // Drain path from reheater to hotwell, this goes via some effort source
        // to ensure proper flow anr represents the height difference.
        turbineReheaterCondensateDrain.getValveElement()
                .connectBetween(turbineReheater.getPhasedNode(
                        PhasedSuperheater.PRIMARY_OUT),
                        turbineReheaterCondensateDrainOut);
        // Connect the drain to the hotwell reservoir directly. This will make
        // the model way easier as this is a direct connection between two
        // self capacitances then. Otherwise the solver will crash unfortunately
        turbineReheaterCondensateHeight.connectBetween(
                turbineReheaterCondensateDrainOut,
                hotwell.getPhasedNode(PhasedCondenserNoMass.PRIMARY_INNER));

        // Turbine Low pressure path. First, a mass element that is also used
        // for thermal exchange like in the high pressure part
        turbineLowPressureInMass.connectBetween(turbineLowPressureIn,
                turbineLowPressureMidIn);
        turbineLowPressureStage[0].connectTo(turbineLowPressureMidIn); // 1st stage
        for (int idx = 0; idx < 6; idx++) {
            if (idx >= 1) {
                turbineLowPressureStage[idx].connectTo(
                        turbineLowPressureStageOut[idx - 1]);
            }
            if (idx <= 4) {
                turbineLowPressureStage[idx].connectTo(
                        turbineLowPressureStageOut[idx]);
            }
        }
        // Last stage again into a thermal mass, but that will not do much 
        // of dynamics as it will likely say cold there. just to get the value.
        turbineLowPressureStage[5].connectTo(turbineLowPressureMidOut);
        // last mass goes straight into hotwell
        turbineLowPressureOutMass.connectBetween(turbineLowPressureMidOut,
                hotwell.getPhasedNode(PhasedCondenserNoMass.PRIMARY_IN));

        // make connections to the thermal model
        turbineLowPressureInMass.setInnerThermalEffortSource(
                turbine.getThermalEffortSource(2));
        turbineLowPressureOutMass.setInnerThermalEffortSource(
                turbine.getThermalEffortSource(3));

        // connect turbine taps, also represented as resistors. Each ends on a
        // node that will be connected to other components later.
        for (int idx = 0; idx < 5; idx++) {
            turbineLowPressureTap[idx].connectBetween(
                    turbineLowPressureStageOut[idx],
                    turbineLowPressureTapOut[idx]);
            turbineLowPressureTapValve[idx].getValveElement().connectTo(
                    turbineLowPressureTapOut[idx]);
        }
        // Connect the out sides of the turbine tap valves:
        turbineLowPressureTapValve[0].getValveElement().connectTo(
                preheater[2].getPhasedNode(PhasedCondenserNoMass.PRIMARY_IN));
        turbineLowPressureTapValve[1].getValveElement().connectTo(
                deaeratorSteamMiddle);
        turbineLowPressureTapValve[2].getValveElement().connectTo(
                preheater[1].getPhasedNode(PhasedCondenserNoMass.PRIMARY_IN));
        turbineLowPressureTapValve[3].getValveElement().connectTo(
                ejectorTurbineTapNode);
        turbineLowPressureTapValve[4].getValveElement().connectTo(
                preheater[0].getPhasedNode(PhasedCondenserNoMass.PRIMARY_IN));

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Element properties">
        makeupStorage.setTimeConstant(100 / 9.81);

        // Makup storage pumps: 4 bar on 200 kg/s
        for (int idx = 0; idx < 2; idx++) {
            makeupPumps[idx].initCharacteristic(5e5, 4e5, 200);
        }

        // The main steam shutoff valve can be seen more as a cheat to keep the
        // model stable, it will be operated automatically. Randomly setting it 
        // to 200 for a little pressure loss.
        for (int idx = 0; idx < 2; idx++) {
            mainSteamShutoffValve[idx].initCharacteristic(200, -1.0);
            mainSteamShutoffValve[idx].getIntegrator().setMaxRate(50);
        }

        // Main circulation pumps: 8000 m^3/h per pump nominal, is reduced down
        // to 6000 m^3 by trim valves on low power. Max head: 1.56 - 2.0 MPa 
        // Assuming a density of 870 kg/m³, we will use 0.24167 kg/s per m^3/h.
        // Full load will be 6 active pumps with total of 48000 m^3/h = 
        // 11600.16 kg/s circulation, making a total of 5800 kg/s per side.
        // I will assume 1.7e6 Pa (17 bar) pressure drop through the channels
        // and 7e4 Pa through the down pipes to the MCPs, being a total of 2e6
        // Pa pressure drop. We will ignore the thermic and use trim valves
        // to trim this down to the 5800. Per pump: 5800/3 = 1933.3 kg/s
        // Resistance is always linear here so we use ohms law R = p/q
        // R downflow = 7e4 Pa / 5800 kg/s = 51.72 Pa/kg*s
        // R fuelchannels = 1.7e6 / 5800 kg/s = 293.1 Pa/kg*s
        // Full open trim valve will have 3e4 Pa pressure loss, making it 
        // R trimvalve = 3e4 Pa& 5800 kg/s = 5.51724 Pa/kg*s
        // This will be a pressure of 2.03e6 Pa. Assume that the zero flow head
        // Assume a pressure of 2.5e6Pa if all valves are closed.
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 4; jdx++) {
                loopAssembly[idx][jdx].initCharacteristic(
                        2.5e6, 2.03e6, 1933.3);
                loopTrimValve[idx][jdx].initCharacteristic(5.51724, 80.0);
            }
            loopDownflow[idx].setResistanceParameter(12.6);
            loopDownflow[idx].setInnerThermalMass(50); // initial: 100
            loopChannelFlowResistance[idx].setInnerThermalMass(50);
            loopChannelFlowResistance[idx].setResistanceParameter(293.1);
        }

        // Steam Drum: to compare with RXmodel simulator: Experiments show
        // that there is approximately 1 ton or 1 m^3 water in ech drum per cm
        // level. -18 cm will trip the MCPs so we assume that -20 cm will be an 
        // empty drum. The RXModel uses 0 cm as nominal level, and we will just
        // do the same here.
        // Real world values, gathered from some sources: around 30 m length,
        // inner diameter 2.6 m, 2 such drums per side. A circle with 2.6 m 
        // diameter has same area than a square with 2.3 m sides. So with 2 
        // times 30 * 2.6 we'll have about 156 m² base area. We choose 40 for 
        // way lower time constants (this is not intended to be real time sim)
        // It will be definded here that 1.15 m fill height is normal and 2.3 m
        // is flooded. 0 will be empty, which will terminate the simulation.
        // So in cm, lower limit is -115 and upper is 230. but above 180 or so
        // There will be massive problems with steam separation so let#s define:
        // MAX2: 140
        // MAX1: 100 close all feedwater valves and blowdown return
        // HIGH2: 60
        // HIGH1: 30
        // LOW1: -20
        // LOW2: -40
        // MIN1: -60 shut off MPC
        // MIN2: -80 close all blowdown valves
        // Base area: A value of 40 is quite realistic but it does not make a 
        // Fast simulation, it takes long to heat up and build up pressure. so
        // the value is set to a different one.
        for (int idx = 0; idx < 2; idx++) {
            loopSteamDrum[idx].setBaseArea(20);
        }

        // Bypass valves: Not yet proper configured. Todo. Just randomly used
        // 10 Pa/kg/s with the default shut valve characteristic.
        for (int idx = 0; idx < 2; idx++) {
            loopBypass[idx].initCharacteristic(10.0, -1);
        }

        // Fuel model: Full thermal power per side is 1.6e9 Watts with fuel
        // temperature of 570 °C (843 K) and recirc out temp of 284 °C (557 K).
        // Resistance: R = DeltaT / P = (843-557) / 1.6e9 = 1.78e-7 K/(J*s)
        // 192 Tons (96 per side) of fuel in reactor. Specific heat capacity
        // of uranium dioxide: 270 J/kg/K
        // Thermal capacity: m * c = 96000 kg * 270 J/kg/K = 2.6e7 J/K
        for (int idx = 0; idx < 2; idx++) {
            fuelThermalCapacity[idx].setTimeConstant(2.6e7);
            fuelThermalResistance[idx].setResistanceParameter(1.78e-7);
        }

        // Blowdown System
        for (int idx = 0; idx < 2; idx++) {
            // Two large pipes from and to reactor
            blowdownPipeFromMcp[idx].setResistanceParameter(2000);
            blowdownPipeFromMcp[idx].setInnerThermalMass(400);
            blowdownReturn[idx].setResistanceParameter(2400);
            blowdownReturn[idx].setInnerThermalMass(400);
            // Valves connected to those
            blowdownValveFromLoop[idx].initCharacteristic(400, 20.0);
            blowdownReturnValve[idx].initCharacteristic(400, 20.0);
            blowdownCooldownPumps[idx].initCharacteristic(16e5, 12e5, 900);
        }
        blowdownValvePassiveFlow.initCharacteristic(3000, -1.0);
        blowdownValvePumpsToRegenerator.initCharacteristic(500, -1.0);
        blowdownValvePumpsToCooler.initCharacteristic(100, -1.0);
        blowdownValveRegeneratorToCooler.initCharacteristic(500, -1.0);
        blowdownCooldownResistance.setResistanceParameter(500); // war: 2000
        blowdownValveTreatmentBypass.initCharacteristic(100, -1.0);
        blowdownValveRegeneratedToDrums.initCharacteristic(100, -1.0);
        blowdownValveDrain.initCharacteristic(400, 20.0);
        blowdownToRegeneratorFirstResistance.setResistanceParameter(500);
        blowdownToRegeneratorSecondResistance.setResistanceParameter(500);
        blowdownCooldown.initCharacteristic(3000, 1500, 7e6);
        // Flow source for coolant
        blowdownValveCoolant.initCharacteristic(1000, 6);

        // Temperature in Deaerators is supposed to be 165 °C / 8 barabs
        for (int idx = 0; idx < 2; idx++) {
            // RXmodel has a base area of 40, use this value here
            deaerator[idx].setBaseArea(40);

            // Todo: Proper values for DA valves, just some rough estimates 
            deaeratorSteamInRegValve[idx].initCharacteristic(1000, -1.0);
            deaeratorSteamFromMain[idx].initCharacteristic(200, -1.0);
            deaeratorDrain[idx].initCharacteristic(50, -1.0);
        }

        // Feedwater
        // Steam generation is about 5600 t/h which is 1555.56 kg/s so per side
        // there must be 777.775 kg/s Feedwater flow. Expected pressure in 
        // steam drum is 69 bar and 284 °C. All safety valves will open at 75.5
        // bar so there should be no need to have a pump that can press huge 
        // amounts of water against closed valves.
        // The 4th square root formula will put out 65 bar abs for 284 °C.
        // It is assumed that we need two pumps per side and two flow regulation
        // valves per side so it's all in use on full power. The real plant has
        // more valves on the feed side but the simulation core cannot calculate
        // this. So per pump it will be 388.88 kg/s.
        // Assume another pressure drop on the regulation valves (those include 
        // the piping in the model) of 10 bar if full opened
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 2; jdx++) {
                // Operating pressure is 69 + 10 - 8 = 71 bar = 7.1e6 on 389 kgs
                feedwaterPump[idx][jdx].initCharacteristic(8e6, 7.1e6, 388.8);
            }
        }
        feedwaterPump3.initCharacteristic(8e6, 6.6e6, 388.8);
        // Shutoff valves do not add a considerable amount of resistance.
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 3; jdx++) {
                feedwaterShutoffValve[idx][jdx]
                        .initCharacteristic(500, -1.0);
            }
        }
        // Startup valves
        for (int idx = 0; idx < 2; idx++) {
            // This thing reduces to 100 kg/s at no pressure on startupt so it
            // has a huge pressure drop of about 6e5 -> 6e3
            feedwaterStartupReductionValve[idx]
                    .initCharacteristic(6000, 20);
            feedwaterFlowRegulationValve[idx][0]
                    .initCharacteristic(800, 200);
        }
        // Full power valves
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 1; jdx < 3; jdx++) {
                feedwaterFlowRegulationValve[idx][jdx]
                        .initCharacteristic(800, 20);
            }
        }

        // For startup will have an additional valve that will allow to have
        // up to 200 kg/s on full pressure drop as we do not have any pressure
        // from the steam on low temperatures. This has to work against max
        // pump pressure of 8e6 Pa.
        // 8e6 / 200 = 4e4 -> 180 kg/s reduce to 3e4
        for (int idx = 0; idx < 2; idx++) {
            feedwaterStartupReductionValve[idx].initCharacteristic(3e4, 20);
        }

        // Those from middle pump do not have much resistance, just a shutoff.
        for (int idx = 0; idx < 2; idx++) {
            feedwaterSparePumpInValve[idx].initCharacteristic(200, -1.0);
            feedwaterSparePumpOutValve[idx].initCharacteristic(200, -1.0);
        }

        // Auxiliary condensers (БРУ–ТК) each can condense about 100 t/h 
        // steam which is 28 kg/s. It is designed to be used when there is no
        // vacuum so it will propably be operated in low pressure regions only.
        // Set the valves to have 30 kg/s with 5 bar of pressure (5e5 Pa)
        // so it will be R = 5e5 Pa / 30 kg/s = 1.2e4 Pa/kg*s for steam valve.
        // Tbh, had to play around with those values for some time to get some
        // proper behavior.
        // 30 bar is roughly saturation temperature of 500 Kelvin. Lets assume 
        // condensation tempearture of 310 Kelvin so this is 190 K temperature 
        // 190 K * 4200 J/kg/K + 2100000 J/kg = 2898000 J/kg = 2.9e6 J/kg
        // Assuming 30 kg/s per side that will be a total heat transfer of
        // 2.9e6 * 30 * J/kg*kg/s = 8.7e7 J/s (= 87 MW). Assuming the temp
        // diff between condensation and coolant will be 10 K or so we will
        // kA = 8.7e7 W / 10 K = 8.7e6 W/K (k times A is basically that).
        for (int idx = 0; idx < 2; idx++) {
            auxCondSteamValve[idx].initCharacteristic(1.8e4, -1);
            auxCondensers[idx].initCharacteristic(4.0, 500, 5e5, 1e5, 4.0);
            // No ambient pressure for condensation, always steam pressure.
            auxCondensers[idx].getPrimarySideReservoir()
                    .setAmbientPressure(0.0);
            // we use flow source as valve for coolant flow
            auxCondCoolantSource[idx].setOriginTemperature(273.15 + 21.1);
            auxCondCoolantValve[idx].initCharacteristic(200, 6);
        }
        // Assume there would be about 15 meters height difference and apply
        // this to the pressure source to allow passive flow. 
        auxCondHeightDifference.setEffort(1.5e5);
        // to need at least one pump for those total 56 kg/s, lets make the 
        // valves in a way they only have about 20 kg/s only without a pump. So 
        // that will be a total reistance of R = 1.5e5 Pa / 20 kg/s = 7500.
        // we use 3000 on the auxCondSteamValve and 2500 on the valves to
        // the hotwell or makeup storage, 2000 on the aux bypass valve.
        for (int idx = 0; idx < 2; idx++) {
            auxCondCondensateValve[idx].initCharacteristic(3e3, 20);
            auxCondPumps[idx].initCharacteristic(5e5, 4e5, 60.0);
        }
        auxCondBypass.initCharacteristic(2000, -1);
        auxCondValveToDrain.initCharacteristic(2500, 20);
        auxCondValveToHotwell.initCharacteristic(2500, 20);

        // The steam dump or turbine bypass will not be able to handle the full
        // reactor load, only a bit more than 50 % of it roughly, but it needs
        // to operate on lower pressures also. Lets just assume on a pressure of
        // 50 bar we'll get those 400 kg/s of steam throuh the valve if it is 
        // opened to 100 %: 5e6 Pa / 400 kg/s = 12500 Pa/kg*s
        for (int idx = 0; idx < 2; idx++) {
            mainSteamDump[idx].initCharacteristic(12500, -1.0);
        }

        // Todo: Get proper values, those are completely made up here
        // There are 4 condensers with a total of 40480 m² surface, 10 Kelvin
        // coolant temperature rise and about 224500 kg/s coolant water flow. 
        hotwell.initCharacteristic(60, 5000, 1e5, 0.0, 4.0);

        // Condensate pumps have 2 stages, we need 2 of 3 for full load.
        // full condensation flow is 3111,11 kg/s, div by 2 is 1555.56 kg/s
        // Todo: Proper values, those are just made up to have something.
        for (int idx = 0; idx < condensationHotwellPump.length; idx++) {
            condensationHotwellPump[idx].initCharacteristic(10e5, 8e5, 1556);
        }
        // same here
        for (int idx = 0; idx < condensationHotwellPump.length; idx++) {
            condensationCondensatePump[idx].initCharacteristic(10e5, 6e5, 1556);
        }
        // 3 bar diff on full opening at 1555 kg/s
        // this was 800, reduced to 400 and added 400 on preheater piping.
        for (int idx = 0; idx < 2; idx++) {
            condensationValveToDA[idx].initCharacteristic(400, -1);
        }

        // Condenser startup ejectors do actually not use much steam, they will
        // have an operating flow of 1 kg/s.
        for (int idx = 0; idx < 2; idx++) {
            ejectorStartup[idx].initCharacteristic(2e6, -1);
        }

        // Todo: make some proper resistance values on the preheaters
        preheaterPiping[0].setResistanceParameter(200);
        preheaterPiping[1].setResistanceParameter(200);

        // The makup storage pump makes 200 kg/s at 4.0 bars
        hotwellFillValve.initCharacteristic(2000, 10);
        hotwellDrainValve.initCharacteristic(2000, 20);

        // Condenser Coolant
        condenserCoolantSource.setOriginTemperature(273.15 + 22.5);
        condenserCoolant.initCharacteristic(44000, 5.0);

        // Main Ejectors
        for (int idx = 0; idx < 3; idx++) {
            // Dummy resistor: 6 bar diff on half flow -> 6e5 / 1500
            ejectorMainFlowReistance[idx].setResistanceParameter(95);
        }
        ejectorMainBypass.initCharacteristic(200, -1);

        // Turbine: fresh steam from drums is 1555 kg/s with 4_440_030 J/kg
        // as we have 69 bars with 284 °C with X=1.0 from drums.
        // After HD turbine: x=0.85 3.5 bar 137 °C - 3_507_630 J/kg
        // Reheated to 263 °C: 4_351_830 J/kg, Delta: 844_201 J/kg
        // Condensation after Reheater, assumed: 4.4 bar 145 °C
        // this is 1_756_230 J/kg which was fresh steam before. Transfered
        // in reheater is then 2_683_800 J/kg.
        // Energy balance in reheater: m1*844_201 = m2*2_683_800, with
        // m1 + m2 = 1555 it is m1=1183 and m2=372 kg/s flows.
        // Therefore resistance of HD: (69e5 Pa-3.5e5 Pe) / 1183 kg/s
        // is R_HD = 5536.7 Pa/kg*s 
        turbineHighPressure.setResistanceParameter(5537);
        turbineHighPressure.setOutVaporFraction(0.85);

        // Inlst valves: It was suggested by maha that we use 10 bar pressure 
        // to start heating up but with very low steam flow.
        for (int idx = 0; idx < 2; idx++) {
            turbineTripValve[idx].initCharacteristic(500, -1.0);
            // Fast closing valve: 200 % per second, default is 25 %/s
            turbineTripValve[idx].getIntegrator().setMaxRate(200);
            turbineStartupSteamValve[idx].initCharacteristic(8e5, -1.0);
            turbineMainSteamValve[idx].initCharacteristic(2e4, -1.0);
            turbineMainSteamValve[idx].getIntegrator().setMaxRate(8);
            turbineReheaterSteamValve[idx].initCharacteristic(2e5, -1.0);
            turbineReheaterSteamValve[idx].getIntegrator().setMaxRate(8);
        }

        // The steam mass inside the turbine is modeled constant, those are some
        // numbers that are totally non realistic but they have to be high for
        // the solution to be stable.
        turbineHighPressureInMass.getPhasedHandler()
                .setInnerHeatedMass(20);
        turbineHighPressureOutMass.getPhasedHandler()
                .setInnerHeatedMass(80);
        turbineLowPressureInMass.getPhasedHandler()
                .setInnerHeatedMass(10);
        turbineLowPressureOutMass.getPhasedHandler()
                .setInnerHeatedMass(5);

        turbineLowPressureTripValve.initCharacteristic(1.0, -1);
        turbineLowPressureTripValve.getIntegrator().setMaxRate(200);

        // ND turbine part: 3.5 bar to almost 0 at condensation with 
        // 1183 kg/s will be R = 295 Pa/kg*s which is about 60 per resistance.
        // The turbine will need a minimum pressure of 10 bars 
        turbineLowPressureStage[0].setResistanceParameter(60);
        turbineLowPressureStage[1].setResistanceParameter(60);
        turbineLowPressureStage[2].setResistanceParameter(60);
        turbineLowPressureStage[3].setResistanceParameter(60);
        turbineLowPressureStage[4].setResistanceParameter(60);
        turbineLowPressureStage[5].setResistanceParameter(60);

        turbineReheater.initCharacteristic(25.0, 500, 5e2, 0.0);

        // Resistance to Reheater in: (69e5 Pa-4.4e5 Pe) / 372 kg/s
        // both valves full open will have sum R of 17365 Pa/kg*s 
        // Reistance of Reheater condensate: 4.4 e6 Pa / 372 kg/s
        // is 1182.8 Pa/kg*s
        // Note that the used resistances on valves will be different to
        // have some possibility of controlling them left.
        turbineReheaterTripValve.getIntegrator().setMaxRate(200);
        turbineReheaterTripValve.initCharacteristic(500, -1);

        turbineReheaterTrimValve.initCharacteristic(9000, -1);
        for (int idx = 0; idx < 2; idx++) {
            turbineReheaterCondensateValve[idx].initCharacteristic(500, -1);
        }
        // 1 bar difference towards hotwell to ensure flow
        turbineReheaterCondensateHeight.setEffort(2e5);
        turbineReheaterCondensateDrain.initCharacteristic(200, -1);

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Set Initial conditions">
        // Makeup storage has 2 meters fill level initially, quite low:
        // ... but use 6.5 as long as there's no fill thing implemented:
        makeupStorage.setInitialEffort(6.5 * 997 * 9.81); // p = h * rho * g

        // Main circulation - common IC for both cases (there are 2 below...)
        for (int idx = 0; idx <= 1; idx++) { // 2 sides
            for (int jdx = 0; jdx < 4; jdx++) {
                // All trim valves open az 70 %
                loopTrimValve[idx][jdx].initOpening(70);
            }
        }

        // Initial setpoint for steam pressure
        setpointDrumPressure.forceOutputValue(64.0);

        for (int idx = 0; idx < 2; idx++) {
            blowdownReturnValve[idx].initOpening(80);
            blowdownValveFromLoop[idx].initOpening(95);
        }
        blowdownToRegeneratorFirstResistance.getHeatHandler()
                .setInitialTemperature(298.15 + debugAddInitTemp);
        blowdownToRegeneratorSecondResistance.getHeatHandler()
                .setInitialTemperature(298.15 + debugAddInitTemp);

        blowdownValvePumpsToCooler.initOpening(100);
        blowdownValveTreatmentBypass.initOpening(100);

        if (debugAddInitTemp <= 5) { // no cooldown for hot tests
            blowdownValveCoolant.initFlow(400);
        }

        // Variant 1: Forced Circ. with MPC and no Cooldown Pump:
        /* for (int idx = 0; idx <= 1; idx++) {
            loopSteamDrum[idx].setInitialState(40000, 36.6 + 273.15);
            loopEvaporator[idx].setInitialState(6.0, 1e5,
                    273.5 + 34.6, 273.5 + 36.9);
            loopDownflow[idx].getHeatHandler()
                    .setInitialTemperature(36.6 + 273.16);
            loopAssembly[idx][1].setInitialCondition(true, true, true);
            loopChannelFlowResistance[idx].getHeatHandler()
                    .setInitialTemperature(273.5 + 34.6);
            blowdownPipeFromMcp[idx].getHeatHandler().setInitialTemperature(
                    36.6 + 273.5);
            blowdownReturn[idx].getHeatHandler().setInitialTemperature(
                    26.1 + 273.5);
        }
        blowdownCooldown.getPrimarySide().getHeatHandler()
                .setInitialTemperature(299.84);
        blowdownCooldown.getSecondarySide().getHeatHandler()
                .setInitialTemperature(301.57);
        // Blowdown/Cooldown one pump is ready but inactive
        blowdownCooldownPumps[1].setInitialCondition(false, true, false);
        // Use bypass valves (MPC will push it through):
        blowdownValvePassiveFlow.initOpening(100);
        blowdownValvePumpsToRegenerator.initOpening(100); */
        // Variant 2: Natural Circulation without MCP and cooldown active:
        for (int idx = 0; idx <= 1; idx++) {
            // first value is the mass, it is the base area time 1000
            loopSteamDrum[idx].setInitialState(21000, 42.9 + 273.15 + debugAddInitTemp);
            loopEvaporator[idx].setInitialState(6.0, 1e5,
                    273.5 + 29.4 + debugAddInitTemp, 273.5 + 46.3 + debugAddInitTemp);
            loopBypass[idx].initOpening(100); // Open Bypass
            loopDownflow[idx].getHeatHandler()
                    .setInitialTemperature(273.16 + 29.44 + debugAddInitTemp);
            loopChannelFlowResistance[idx].getHeatHandler()
                    .setInitialTemperature(273.5 + 29.44 + debugAddInitTemp);

            blowdownPipeFromMcp[idx].getHeatHandler().setInitialTemperature(
                    29.44 + 273.5 + debugAddInitTemp);
            blowdownReturn[idx].getHeatHandler().setInitialTemperature(
                    26.27 + 273.5 + debugAddInitTemp);
        }
        blowdownCooldown.getPrimarySide().getHeatHandler()
                .setInitialTemperature(299.84 + debugAddInitTemp);
        blowdownCooldown.getSecondarySide().getHeatHandler()
                .setInitialTemperature(301.57 + debugAddInitTemp);
        // Blowdown/Cooldown one pump is in operation
        blowdownCooldownPumps[1].setInitialCondition(true, true, true);

        // Deaerator
        for (int idx = 0; idx <= 1; idx++) {
            // try to have a fill level of 100 cm (normal level)
            deaerator[idx].setInitialState(40000, 35 + 273.15);
        }
        for (int idx = 0; idx < 2; idx++) {
            setpointDALevel[idx].forceOutputValue(100);
        }

        // Todo: Something that makes more sense here.
        for (int idx = 0; idx < 2; idx++) {
            auxCondensers[idx].initConditions(320, 320, 0.8);
        }
        hotwell.initConditions(273.15 + 22.5, 273.15 + 22.5, 0.2);

        // start with full hotwell cooling
        condenserCoolant.initFlow(44000);

        // Turbine: Reheater
        turbineReheater.initConditions(273.15 + 22.5,
                (273.15 + 22.5) * phasedWater.getSpecificHeatCapacity(),
                0.5, 0.0);
        // Init Setpoint values
        setpointTurbineReheaterTemperature.forceOutputValue(140);
        setpointTurbineReheaterLevel.forceOutputValue(60);

        // </editor-fold>
        // Initialize solver and build model. This is only a small line of code,
        // but it triggers a huge step of building up all the network and
        // calculation of the thermal layout.
        solver.addNetwork(blowdownOutNode);
        // <editor-fold defaultstate="collapsed" desc="Submit to runner">
        // Add assemblies to runner instance, this way they get their run 
        // method called each cycle (this sets valve movements, fires events
        // and so on).
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(makeupPumps[idx]);
        }
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(mainSteamShutoffValve[idx]);
            for (int jdx = 0; jdx < 4; jdx++) {
                runner.submit(loopAssembly[idx][jdx]);
                runner.submit(loopTrimValve[idx][jdx]);
            }
            runner.submit(loopBypass[idx]);
            runner.submit(blowdownValveFromDrum[idx]);
            runner.submit(blowdownValveFromLoop[idx]);
            runner.submit(blowdownCooldownPumps[idx]);
        }
        runner.submit(blowdownValvePassiveFlow);
        runner.submit(blowdownValvePumpsToRegenerator);
        runner.submit(blowdownValvePumpsToCooler);
        runner.submit(blowdownValveRegeneratorToCooler);
        runner.submit(blowdownValveTreatmentBypass);
        runner.submit(blowdownValveRegeneratedToDrums);
        runner.submit(blowdownValveDrain);
        runner.submit(blowdownValveCoolant);
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(blowdownReturnValve[idx]);
        }
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(deaeratorSteamInRegValve[idx]);
            runner.submit(deaeratorSteamFromMain[idx]);
            runner.submit(deaeratorDrain[idx]);
        }
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 2; jdx++) {
                runner.submit(feedwaterPump[idx][jdx]);
            }
            runner.submit(feedwaterSparePumpInValve[idx]);
        }
        runner.submit(feedwaterPump3);

        for (int idx = 0; idx < 2; idx++) {
            runner.submit(feedwaterSparePumpOutValve[idx]);
            runner.submit(feedwaterStartupReductionValve[idx]);
        }
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 3; jdx++) {
                runner.submit(feedwaterShutoffValve[idx][jdx]);
            }
        }
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 3; jdx++) {
                runner.submit(feedwaterFlowRegulationValve[idx][jdx]);
            }
        }
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(auxCondSteamValve[idx]);
            runner.submit(auxCondCoolantValve[idx]);
            runner.submit(auxCondCondensateValve[idx]);
        }
        runner.submit(auxCondBypass);
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(auxCondPumps[idx]);
        }
        runner.submit(auxCondValveToHotwell);
        runner.submit(auxCondValveToDrain);
        for (int idx = 0; idx < mainSteamDump.length; idx++) {
            runner.submit(mainSteamDump[idx]);
        }
        for (int idx = 0; idx < 3; idx++) {
            runner.submit(condensationHotwellPump[idx]);
            runner.submit(condensationCondensatePump[idx]);
        }
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(condensationValveToDA[idx]);
        }
        runner.submit(hotwellFillValve);
        runner.submit(hotwellDrainValve);
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(ejectorStartup[idx]);
        }
        for (int idx = 0; idx < 3; idx++) {
            runner.submit(ejectorMainSteamValve[idx]);
            runner.submit(ejectorMainCondensateValve[idx]);
            runner.submit(ejectorMainFlowIn[idx]);
            runner.submit(ejectorMainFlowOut[idx]);
        }
        runner.submit(ejectorMainBypass);
        for (int idx = 0; idx < 3; idx++) {
            runner.submit(preheaterCondensateValve[idx]);
        }
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(turbineTripValve[idx]);
            runner.submit(turbineStartupSteamValve[idx]);
            runner.submit(turbineMainSteamValve[idx]);
            runner.submit(turbineReheaterSteamValve[idx]);
        }

        runner.submit(turbineReheaterTripValve);
        runner.submit(turbineReheaterTrimValve);
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(turbineReheaterCondensateValve[idx]);
        }
        runner.submit(turbineReheaterCondensateDrain);
        runner.submit(turbineLowPressureTripValve);

        for (int idx = 0; idx < 5; idx++) {
            runner.submit(turbineLowPressureTapValve[idx]);
        }

        // Add Solo control loops
        runner.submit(blowdownBalanceControlLoop);

        // Add setpoint instances
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(setpointDrumLevel[idx]);
            runner.submit(setpointDAPressure[idx]);
            runner.submit(setpointDALevel[idx]);
            runner.submit(setpointAuxCondLevel[idx]);
        }
        runner.submit(setpointDrumPressure);
        runner.submit(setpointHotwellUpperLevel);
        runner.submit(setpointHotwellLowerLevel);
        runner.submit(setpointTurbineReheaterTemperature);
        runner.submit(setpointTurbineReheaterLevel);

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Control Loops Configuration">
        for (int idx = 0; idx < 2; idx++) {
            setpointDrumLevel[idx].setLowerLimit(-10);
            setpointDrumLevel[idx].setUpperLimit(10);
            setpointDrumLevel[idx].setMaxRate(0.5);
        }

        for (int idx = 0; idx < 2; idx++) {
            setpointDALevel[idx].setLowerLimit(50);
            setpointDALevel[idx].setUpperLimit(220);
            setpointDALevel[idx].setMaxRate(10.0);
        }

        for (int idx = 0; idx < 2; idx++) {
            // Todo, use values that make sense after setting characteristics
            setpointAuxCondLevel[idx].setLowerLimit(50);
            setpointAuxCondLevel[idx].setUpperLimit(220);
            setpointAuxCondLevel[idx].setMaxRate(10.0);
        }

        // Pressure setpoint and controls are given in bar relative while the 
        // model itsel works on Pascal absolute. Sounds painful but is a real
        // world approach.
        setpointDrumPressure.setLowerLimit(58);
        setpointDrumPressure.setUpperLimit(72.0);
        setpointDrumPressure.setMaxRate(5.0);

        blowdownBalanceControlLoop.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                // Try to keep differences between drum levels equal.
                // Setpoint is cm, level is m
                return (setpointDrumLevel[0].getOutput() * 0.5
                        - (loopSteamDrum[0].getFillHeight() - 1.15) * 100)
                        - (setpointDrumLevel[1].getOutput() * 0.5
                        - (loopSteamDrum[1].getFillHeight() - 1.15) * 100);
            }
        });
        ((PControl) blowdownBalanceControlLoop).setParameterK(20);
        blowdownBalanceControlLoop.setMaxOutput(60);
        blowdownBalanceControlLoop.setMinOutput(-60);

        // Drum level setpoint is in cm and getFillHeight retursn meters.
        for (int jdx = 0; jdx < 3; jdx++) {
            feedwaterFlowRegulationValve[0][jdx].getController()
                    .addInputProvider(new DoubleSupplier() {
                        @Override
                        public double getAsDouble() {
                            return setpointDrumLevel[0].getOutput()
                                    - (loopSteamDrum[0].getFillHeight()
                                    - 1.15) * 100;
                        }
                    });
            feedwaterFlowRegulationValve[1][jdx].getController()
                    .addInputProvider(new DoubleSupplier() {
                        @Override
                        public double getAsDouble() {
                            return setpointDrumLevel[1].getOutput()
                                    - (loopSteamDrum[1].getFillHeight()
                                    - 1.15) * 100;
                        }
                    });
        }
        // Feedwater level control parameters
        for (int idx = 0; idx < 2; idx++) { // main valves
            for (int jdx = 1; jdx < 3; jdx++) {
                ((PIControl) feedwaterFlowRegulationValve[idx][jdx]
                        .getController()).setParameterK(20);
                ((PIControl) feedwaterFlowRegulationValve[idx][jdx]
                        .getController()).setParameterTN(80.0);
            }
        }
        for (int idx = 0; idx < 2; idx++) { // startup valves
            ((PIControl) feedwaterFlowRegulationValve[idx][0]
                    .getController()).setParameterK(10.0);
            ((PIControl) feedwaterFlowRegulationValve[idx][0]
                    .getController()).setParameterTN(30.0);
        }

        // Pressure Setpoints for Deaerator (the steam in will go for those 
        // pressure setpoints)
        for (int idx = 0; idx < 2; idx++) {
            setpointDAPressure[idx].setLowerLimit(1.0);
            setpointDAPressure[idx].setUpperLimit(10.0);
            setpointDAPressure[idx].setMaxRate(1.0);
        }

        // Steam into Deaerator
        deaeratorSteamInRegValve[0].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return setpointDAPressure[0].getOutput() // this is bar
                        - deaerator[0].getEffort() * 1e-5 + 1.0; // as rel. bar
            }
        });
        deaeratorSteamInRegValve[1].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return setpointDAPressure[1].getOutput() // this is bar
                        - deaerator[1].getEffort() * 1e-5 + 1.0; // as rel. bar
            }
        });
        for (int idx = 0; idx < 2; idx++) {
            ((PIControl) deaeratorSteamInRegValve[idx].getController())
                    .setParameterK(1.0);
            ((PIControl) deaeratorSteamInRegValve[idx].getController())
                    .setParameterTN(20);
        }

        // Condensate valves to Deaerator, this controls Deaerator levels
        condensationValveToDA[0].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return setpointDALevel[0].getOutput()
                        - deaerator[0].getFillHeight() * 100; // m to cm
            }
        }
        );
        condensationValveToDA[1].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return setpointDALevel[1].getOutput()
                        - deaerator[1].getFillHeight() * 100; // m to cm
            }
        }
        );
        for (int idx = 0; idx < 2; idx++) {
            ((PIControl) condensationValveToDA[idx].getController())
                    .setParameterK(10); // todo, needs better parameters
            ((PIControl) condensationValveToDA[idx].getController())
                    .setParameterTN(20); // todo, needs better parameters
        }

        // Aux Condensation Valves control main steam pressure if enabled.
        auxCondSteamValve[0].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                // Loop setpoint is bar relative, provided value from loop is
                // given in pascals as absolute value. Controllers will use bar.
                return loopSteamDrum[0].getEffort() * 1e-5 + 1.0
                        - setpointDrumPressure.getOutput();
            }
        });
        auxCondSteamValve[1].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                // Loop setpoint is bar relative, provided value from loop is
                // given in pascals as absolute value. Controllers will use bar.
                return loopSteamDrum[1].getEffort() * 1e-5 + 1.0
                        - setpointDrumPressure.getOutput();
            }
        });

        // Aux Condensation condensate level control
        auxCondCondensateValve[0].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                // negative: open valve to decrease level.
                return auxCondensers[0].getPrimarySideReservoir()
                        .getFillHeight() * 100 // m to cm
                        - setpointAuxCondLevel[0].getOutput();
            }
        });
        auxCondCondensateValve[1].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                // negative: open valve to decrease level.
                return auxCondensers[1].getPrimarySideReservoir()
                        .getFillHeight() * 100 // m to cm
                        - setpointAuxCondLevel[1].getOutput();
            }
        });
        for (int idx = 0; idx < 2; idx++) {
            ((PIControl) auxCondCondensateValve[idx].getController())
                    .setParameterK(5.0);
            ((PIControl) auxCondCondensateValve[idx].getController())
                    .setParameterTN(20);
        }

        // Aux condensate steam in: Those can control drum pressure
        auxCondSteamValve[0].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                if (!loopNodeDrumFromReactor[0].effortUpdated()) {
                    return 0.0;
                }
                // Negative control: Open valve to decrease pressure
                return -setpointDrumPressure.getOutput()
                        + (loopNodeDrumFromReactor[0].getEffort()
                        / 100000 - 1.0); // Pa abs to bar rel
            }
        });
        auxCondSteamValve[1].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                if (!loopNodeDrumFromReactor[1].effortUpdated()) {
                    return 0.0;
                }
                // Negative control: Open valve to decrease pressure
                return -setpointDrumPressure.getOutput()
                        + (loopNodeDrumFromReactor[1].getEffort()
                        / 100000 - 1.0); // Pa abs to bar rel
            }
        });
        for (int idx = 0; idx < 2; idx++) {
            ((PIControl) auxCondSteamValve[idx].getController())
                    .setParameterK(7.0);
            ((PIControl) auxCondSteamValve[idx].getController())
                    .setParameterTN(20);
        } // Todo: Get some parameters, those here were random numbers!

        // Steam dump or turbine bypass: Controls main drum pressure
        mainSteamDump[0].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                if (!loopNodeDrumFromReactor[0].effortUpdated()) {
                    return 0.0;
                }
                // Negative control: Open valve to decrease pressure
                return -setpointDrumPressure.getOutput()
                        + (loopNodeDrumFromReactor[0].getEffort()
                        / 100000 - 1.0); // Pa abs to bar rel
            }
        });
        mainSteamDump[1].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                if (!loopNodeDrumFromReactor[1].effortUpdated()) {
                    return 0.0;
                }
                // Negative control: Open valve to decrease pressure
                return -setpointDrumPressure.getOutput()
                        + (loopNodeDrumFromReactor[1].getEffort()
                        / 100000 - 1.0); // Pa abs to bar rel
            }
        });
        for (int idx = 0; idx < 2; idx++) {
            ((PIControl) mainSteamDump[idx].getController())
                    .setParameterK(6.0);
            ((PIControl) mainSteamDump[idx].getController())
                    .setParameterTN(10);
        } // Todo: Get some parameters, those here were random numbers!

        turbineStartupSteamValve[0].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return turbine.getTurbineSpeedDeviation();
            }
        });
        turbineStartupSteamValve[1].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return turbine.getTurbineSpeedDeviation();
            }
        });
        for (int idx = 0; idx < 2; idx++) {
            ((PIControl) turbineReheaterSteamValve[idx].getController())
                    .setParameterK(0.05);
            ((PIControl) turbineReheaterSteamValve[idx].getController())
                    .setParameterTN(10);
        } // Todo: Get some parameters, those here were random numbers!

        // Main Turbine steam valves control the drum pressure so they open
        // as soon as the pressure rises.
        turbineMainSteamValve[0].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                if (!loopNodeDrumFromReactor[0].effortUpdated()) {
                    return 0.0;
                }
                // Negative control: Open valve to decrease pressure
                return -setpointDrumPressure.getOutput()
                        + (loopNodeDrumFromReactor[0].getEffort()
                        / 100000 - 1.0); // Pa abs to bar rel
            }
        });
        turbineMainSteamValve[1].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                if (!loopNodeDrumFromReactor[1].effortUpdated()) {
                    return 0.0;
                }
                // Negative control: Open valve to decrease pressure
                return -setpointDrumPressure.getOutput()
                        + (loopNodeDrumFromReactor[1].getEffort()
                        / 100000 - 1.0); // Pa abs to bar rel
            }
        });
        for (int idx = 0; idx < 2; idx++) {
            ((PIControl) mainSteamDump[idx].getController())
                    .setParameterK(6.0);
            ((PIControl) mainSteamDump[idx].getController())
                    .setParameterTN(8);
        } // Todo: Get some parameters, those here were random numbers!

        turbineReheaterSteamValve[0].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return setpointTurbineReheaterTemperature.getOutput()
                        - reheaterOutTemperature;
            }
        });
        turbineReheaterSteamValve[1].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return setpointTurbineReheaterTemperature.getOutput()
                        - reheaterOutTemperature;
            }
        });
        for (int idx = 0; idx < 2; idx++) {
            ((PIControl) turbineReheaterSteamValve[idx].getController())
                    .setParameterK(0.2);
            ((PIControl) turbineReheaterSteamValve[idx].getController())
                    .setParameterTN(20);
        } // Todo: Get some parameters, those here were random numbers!

        turbineReheaterCondensateDrain.getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return -setpointTurbineReheaterLevel.getOutput()
                        + turbineReheater.getPrimarySideReservoir()
                                .getFillHeight() * 100;
            }
        });
        ((PIControl) turbineReheaterCondensateDrain.getController())
                .setParameterK(5);
        ((PIControl) turbineReheaterCondensateDrain.getController())
                .setParameterTN(20);

        turbineReheaterCondensateValve[0].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return -setpointTurbineReheaterLevel.getOutput()
                        + turbineReheater.getPrimarySideReservoir()
                                .getFillHeight() * 100;
            }
        });
        turbineReheaterCondensateValve[1].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return -setpointTurbineReheaterLevel.getOutput()
                        + turbineReheater.getPrimarySideReservoir()
                                .getFillHeight() * 100;
            }
        });
        for (int idx = 0; idx < 2; idx++) {
            ((PIControl) turbineReheaterCondensateValve[idx].getController())
                    .setParameterK(5);
            ((PIControl) turbineReheaterCondensateValve[idx].getController())
                    .setParameterTN(20);
        } // Todo: Get some parameters, those here were random numbers!

        // Hotwell level control
        // Alarms: 5 min, 10 low2, 15 low1, 80 high1, 100: max1
        // Suggested setpoints: Fill setpoint 35, drain setpoint 60 (20 mm diff)
        setpointHotwellUpperLevel.forceOutputValue(60);
        setpointHotwellLowerLevel.forceOutputValue(35);

        // Same behavior for both controls (display is 0..100)
        setpointHotwellUpperLevel.setMaxRate(8.0);
        setpointHotwellUpperLevel.setLowerLimit(10);
        setpointHotwellUpperLevel.setUpperLimit(90);
        setpointHotwellLowerLevel.setMaxRate(8.0);
        setpointHotwellLowerLevel.setLowerLimit(10);
        setpointHotwellLowerLevel.setUpperLimit(90);
        setpointTurbineReheaterTemperature.setMaxRate(10);
        setpointTurbineReheaterTemperature.setLowerLimit(120);
        setpointTurbineReheaterTemperature.setUpperLimit(280);
        setpointTurbineReheaterLevel.setMaxRate(10);
        setpointTurbineReheaterLevel.setLowerLimit(40);
        setpointTurbineReheaterLevel.setUpperLimit(140);

        hotwellFillValve.getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return setpointHotwellLowerLevel.getOutput()
                        - hotwell.getPrimarySideReservoir()
                                .getFillHeight() * 100; // m to cm
            }
        });
        ((PIControl) hotwellFillValve.getController()).setParameterK(2.0);
        ((PIControl) hotwellFillValve.getController()).setParameterTN(5.0);

        hotwellDrainValve.getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                // negative: open valve to decrease level.
                return hotwell.getPrimarySideReservoir()
                        .getFillHeight() * 100 // m to cm
                        - setpointHotwellLowerLevel.getOutput();
            }
        });
        // Let the controller run away to keep the valve shut.
        hotwellDrainValve.getController().setMinOutput(-5.0);

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Alarm Definitions">
        // Alarm monitors are defined here and stored in the alarmUpdater only,
        // there is no need to have a class field for them.
        // NOTE that there's this alarm thing that triggers alarms and events.
        // to prevent further actions, use either safety definitions here or
        // use the checkRpsDisengage in the reactor core. Events are one-time
        // only, so usually there is an alarm event AND some other place that
        // prevents something because certain conditions (like alarm present)
        // are met.
        ValueAlarmMonitor am;

        // Steam Drum Separator 1 Level
        am = new ValueAlarmMonitor();
        am.setName("Drum1Level");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return (loopSteamDrum[0].getFillHeight() - 1.15) * 100;
            }
        });
        am.defineAlarm(140.0, AlarmState.MAX2);
        am.defineAlarm(100.0, AlarmState.MAX1);
        am.defineAlarm(60.0, AlarmState.HIGH2);
        am.defineAlarm(30.0, AlarmState.HIGH1);
        am.defineAlarm(-20.0, AlarmState.LOW1);
        am.defineAlarm(-40.0, AlarmState.LOW2);
        am.defineAlarm(-60.0, AlarmState.MIN1);
        am.defineAlarm(-80.0, AlarmState.MIN2);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
                for (int jdx = 0; jdx < 3; jdx++) {
                    feedwaterShutoffValve[0][jdx].operateCloseValve();
                }
            }
        });
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
                blowdownBalanceControlLoop.setManualMode(true);
                blowdownReturnValve[0].operateCloseValve();
                blowdownValveFromDrum[1].operateCloseValve();
                blowdownValveFromLoop[0].operateCloseValve();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        // Steam Drum Separator 2 Level
        am = new ValueAlarmMonitor();
        am.setName("Drum2Level");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return (loopSteamDrum[1].getFillHeight() - 1.15) * 100;
            }
        });
        am.defineAlarm(140.0, AlarmState.MAX2);
        am.defineAlarm(100.0, AlarmState.MAX1);
        am.defineAlarm(60.0, AlarmState.HIGH2);
        am.defineAlarm(30.0, AlarmState.HIGH1);
        am.defineAlarm(-20.0, AlarmState.LOW1);
        am.defineAlarm(-40.0, AlarmState.LOW2);
        am.defineAlarm(-60.0, AlarmState.MIN1);
        am.defineAlarm(-80.0, AlarmState.MIN2);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
                for (int jdx = 0; jdx < 3; jdx++) {
                    feedwaterShutoffValve[1][jdx].operateCloseValve();
                }
            }
        });
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
                blowdownBalanceControlLoop.setManualMode(true);
                blowdownReturnValve[1].operateCloseValve();
                blowdownValveFromDrum[1].operateCloseValve();
                blowdownValveFromLoop[1].operateCloseValve();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        // Steam Drum Pressure
        am = new ValueAlarmMonitor();
        am.setName("Drum1Pressure");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return loopNodeDrumFromReactor[0].getEffort() / 100000 - 1.0;
            }
        });
        am.defineAlarm(75.0, AlarmState.MAX2);
        am.defineAlarm(74.0, AlarmState.MAX1);
        am.defineAlarm(72.0, AlarmState.HIGH2);
        am.defineAlarm(70.0, AlarmState.HIGH1);
        am.defineAlarm(60.0, AlarmState.LOW1);
        am.defineAlarm(58.0, AlarmState.LOW2);
        am.defineAlarm(55.0, AlarmState.MIN1);
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                turbine.triggerTurbineTrip();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("Drum2Pressure");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return loopNodeDrumFromReactor[1].getEffort() / 100000 - 1.0;
            }
        });
        am.defineAlarm(75.0, AlarmState.MAX2);
        am.defineAlarm(74.0, AlarmState.MAX1);
        am.defineAlarm(72.0, AlarmState.HIGH2);
        am.defineAlarm(70.0, AlarmState.HIGH1);
        am.defineAlarm(60.0, AlarmState.LOW1);
        am.defineAlarm(58.0, AlarmState.LOW2);
        am.defineAlarm(55.0, AlarmState.MIN1);
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                turbine.triggerTurbineTrip();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("Loop1Flow");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return Math.abs(loopChannelFlowResistance[0].getFlow());
            }
        });
        am.defineAlarm(65000.0, AlarmState.HIGH2);
        am.defineAlarm(5800.0, AlarmState.HIGH1);
        am.defineAlarm(4800.0, AlarmState.LOW1);
        am.defineAlarm(3000.0, AlarmState.LOW2);
        am.defineAlarm(2000.0, AlarmState.MIN1);
        am.defineAlarm(50.0, AlarmState.MIN2);
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
            }
        });
        // We need both alarms as the initial state can already be min2 and
        // min1 would be skipped that way.
        am.addAlarmAction(new AlarmAction(AlarmState.MIN2) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
                // Todo: Min2 trigger eccs system operation?
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("Loop2Flow");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return Math.abs(loopChannelFlowResistance[1].getFlow());
            }
        });
        am.defineAlarm(65000.0, AlarmState.HIGH2);
        am.defineAlarm(5800.0, AlarmState.HIGH1);
        am.defineAlarm(4800.0, AlarmState.LOW1);
        am.defineAlarm(3000.0, AlarmState.LOW2);
        am.defineAlarm(2000.0, AlarmState.MIN1);
        am.defineAlarm(50.0, AlarmState.MIN2);
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
            }
        });
        // We need both alarms as the initial state can already be min2 and
        // min1 would be skipped that way.
        am.addAlarmAction(new AlarmAction(AlarmState.MIN2) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
                // Todo: Min2 trigger eccs system operation
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("DA1Level");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return deaerator[0].getFillHeight() * 100;
            }
        });
        am.defineAlarm(220.0, AlarmState.MAX2);
        am.defineAlarm(210.0, AlarmState.MAX1);
        am.defineAlarm(180.0, AlarmState.HIGH2);
        am.defineAlarm(160.0, AlarmState.HIGH1);
        am.defineAlarm(60.0, AlarmState.LOW1);
        am.defineAlarm(40.0, AlarmState.LOW2);
        am.defineAlarm(20.0, AlarmState.MIN1);
        am.defineAlarm(10.0, AlarmState.MIN2);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                deaeratorSteamFromMain[0].operateCloseValve();
                deaeratorSteamFromMain[1].operateCloseValve();
                deaeratorSteamInRegValve[0].getController().setManualMode(true);
                deaeratorSteamInRegValve[0].operateCloseValve();
            }
        });
        am.addAlarmAction(new AlarmAction(AlarmState.MAX2) {
            @Override
            public void run() {
                condensationValveToDA[0].getController().setManualMode(true);
                condensationValveToDA[0].operateCloseValve();
            }
        });
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                deaeratorDrain[0].operateCloseValve();
            }
        });
        am.addAlarmAction(new AlarmAction(AlarmState.MIN2) {
            @Override
            public void run() {
                for (int jdx = 0; jdx < 2; jdx++) {
                    feedwaterPump[0][jdx].operateStopPump();
                    feedwaterPump[0][jdx].operateCloseSuctionValve();
                }
                // shut down feed pump 3 if it is currently open to this side.
                if (feedwaterSparePumpInValve[0].getOpening() > 1.0) {
                    feedwaterSparePumpInValve[0].operateCloseValve();
                    feedwaterPump3.operateCloseSuctionValve();
                    feedwaterPump3.operateStopPump();
                }
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("DA2Level");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return deaerator[1].getFillHeight() * 100;
            }
        });
        am.defineAlarm(220.0, AlarmState.MAX2);
        am.defineAlarm(210.0, AlarmState.MAX1);
        am.defineAlarm(180.0, AlarmState.HIGH2);
        am.defineAlarm(160.0, AlarmState.HIGH1);
        am.defineAlarm(60.0, AlarmState.LOW1);
        am.defineAlarm(40.0, AlarmState.LOW2);
        am.defineAlarm(20.0, AlarmState.MIN1);
        am.defineAlarm(10.0, AlarmState.MIN2);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                deaeratorSteamFromMain[0].operateCloseValve();
                deaeratorSteamFromMain[1].operateCloseValve();
                deaeratorSteamInRegValve[1].getController().setManualMode(true);
                deaeratorSteamInRegValve[1].operateCloseValve();
            }
        });
        am.addAlarmAction(new AlarmAction(AlarmState.MAX2) {
            @Override
            public void run() {
                condensationValveToDA[1].getController().setManualMode(true);
                condensationValveToDA[1].operateCloseValve();
            }
        });
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                deaeratorDrain[1].operateCloseValve();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("Feed1Pressure");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return feedwaterPumpCollectorNodes[0]
                        .getEffort() * 1e-5; // Pa to bar
            }
        });
        am.defineAlarm(70.0, AlarmState.LOW1);
        am.defineAlarm(60.0, AlarmState.LOW2);
        am.defineAlarm(50.0, AlarmState.MIN1);
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("Feed2Pressure");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return feedwaterPumpCollectorNodes[1]
                        .getEffort() * 1e-5; // Pa to bar
            }
        });
        am.defineAlarm(70.0, AlarmState.LOW1);
        am.defineAlarm(60.0, AlarmState.LOW2);
        am.defineAlarm(50.0, AlarmState.MIN1);
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("AuxCond1Level");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return auxCondensers[0].getPrimarySideReservoir()
                        .getFillHeight() * 100;
            }
        });
        am.defineAlarm(300.0, AlarmState.MAX1);
        am.defineAlarm(200.0, AlarmState.HIGH2);
        am.defineAlarm(120.0, AlarmState.HIGH1);
        am.defineAlarm(60.0, AlarmState.LOW1);
        am.defineAlarm(40.0, AlarmState.LOW2);
        am.defineAlarm(5.0, AlarmState.MIN1);
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("AuxCond2Level");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return auxCondensers[1].getPrimarySideReservoir()
                        .getFillHeight() * 100;
            }
        });
        am.defineAlarm(300.0, AlarmState.MAX1);
        am.defineAlarm(200.0, AlarmState.HIGH2);
        am.defineAlarm(120.0, AlarmState.HIGH1);
        am.defineAlarm(60.0, AlarmState.LOW1);
        am.defineAlarm(40.0, AlarmState.LOW2);
        am.defineAlarm(5.0, AlarmState.MIN1);
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("AuxCond1Temp");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return auxCondensers[0]
                        .getPrimarySideReservoir().getTemperature() - 273.15;
            }
        });
        am.defineAlarm(90.0, AlarmState.MAX1);
        am.defineAlarm(65.0, AlarmState.HIGH2);
        am.defineAlarm(55.0, AlarmState.HIGH1);
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("AuxCond2Temp");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return auxCondensers[1]
                        .getPrimarySideReservoir().getTemperature() - 273.15;
            }
        });
        am.defineAlarm(90.0, AlarmState.MAX1);
        am.defineAlarm(65.0, AlarmState.HIGH2);
        am.defineAlarm(55.0, AlarmState.HIGH1);
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("AuxCond1SteamFlow");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return auxCondSteamValve[0].getValveElement().getFlow();

            }
        });
        am.defineAlarm(30.0, AlarmState.HIGH1);
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("AuxCond2SteamFlow");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return auxCondSteamValve[1].getValveElement().getFlow();

            }
        });
        am.defineAlarm(30.0, AlarmState.HIGH1);
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("CondenserVacuum");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return 1e5 - condenserVacuum.getOutput();
            }
        });
        am.defineAlarm(9e4, AlarmState.LOW1);
        am.defineAlarm(7e4, AlarmState.LOW2);
        am.defineAlarm(5e4, AlarmState.MIN1);
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                turbine.triggerTurbineTrip();
            }
        });

        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("CondensatePumpPressure");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return condensationPumpOut.getEffort() * 1e-5; // Pa to bar
            }
        });
        am.defineAlarm(5.0, AlarmState.LOW1);
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("CondensatePumpPressure");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return preheater[0].getHeatNode(
                        PhasedCondenserNoMass.SECONDARY_IN)
                        .getEffort() * 1e-5; // Pa-bar
            }
        });
        am.defineAlarm(5.0, AlarmState.LOW1);
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("StartupEjector1Flow");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return ejectorStartup[0].getValveElement().getFlow();
            }
        });
        am.defineAlarm(1.2, AlarmState.HIGH1);
        am.defineAlarm(1.5, AlarmState.HIGH2);
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("StartupEjector2Flow");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return ejectorStartup[1].getValveElement().getFlow();
            }
        });
        am.defineAlarm(1.2, AlarmState.HIGH1);
        am.defineAlarm(1.5, AlarmState.HIGH2);
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("HotwellLevel");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return hotwell.getPrimarySideReservoir().getFillHeight() * 100;
            }
        });
        am.defineAlarm(100.0, AlarmState.MAX1);
        am.defineAlarm(80.0, AlarmState.HIGH1);
        am.defineAlarm(15.0, AlarmState.LOW1);
        am.defineAlarm(10.0, AlarmState.LOW2);
        am.defineAlarm(5.0, AlarmState.MIN1);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
                turbine.triggerTurbineTrip();
            }
        });
        am.addAlarmAction(new AlarmAction(AlarmState.MIN1) {
            @Override
            public void run() {
                core.triggerAutoShutdown();
                turbine.triggerTurbineTrip();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("ReheaterCondensateTemp");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return turbineReheater.getPrimarySideReservoir()
                        .getTemperature() - 273.15;
            }
        });
        am.defineAlarm(180.0, AlarmState.MAX1);
        am.defineAlarm(160.0, AlarmState.HIGH2);
        am.defineAlarm(150.0, AlarmState.HIGH1);
        am.addAlarmAction(new AlarmAction(AlarmState.MAX1) {
            @Override
            public void run() {
                turbine.triggerTurbineTrip();
            }
        });
        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);

        am = new ValueAlarmMonitor();
        am.setName("ReheaterCondensateLevel");
        am.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return turbineReheater.getPrimarySideReservoir()
                        .getFillHeight() * 100;
            }
        });
        am.defineAlarm(180.0, AlarmState.MAX1);
        am.defineAlarm(140.0, AlarmState.HIGH2);
        am.defineAlarm(120.0, AlarmState.HIGH1);
        am.defineAlarm(50.0, AlarmState.LOW1);
        am.defineAlarm(45.0, AlarmState.LOW2);
        am.defineAlarm(30.0, AlarmState.MIN1);

        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Safety">
        // Steam Drum Level must be above MIN2 for MCPs to run.
        for (int jdx = 0; jdx < 4; jdx++) {
            loopAssembly[0][jdx].addSafeOffProvider(()
                    -> !alarmManager.isAlarmActive(
                            "Drum1Level", AlarmState.MIN2));
        }
        for (int jdx = 0; jdx < 4; jdx++) {
            loopAssembly[1][jdx].addSafeOffProvider(()
                    -> !alarmManager.isAlarmActive(
                            "Drum2Level", AlarmState.MIN2));
        }
        // Do not allow draining the Steam drum with the blowdown system
        blowdownValveFromDrum[0].addSafeClosedProvider(()
                -> !alarmManager.isAlarmActive(
                        "Drum1Level", AlarmState.MIN1));
        blowdownValveFromDrum[1].addSafeClosedProvider(()
                -> !alarmManager.isAlarmActive(
                        "Drum2Level", AlarmState.MIN1));
        blowdownValveFromLoop[0].addSafeClosedProvider(()
                -> !alarmManager.isAlarmActive(
                        "Drum1Level", AlarmState.MIN1));
        blowdownValveFromLoop[1].addSafeClosedProvider(()
                -> !alarmManager.isAlarmActive(
                        "Drum2Level", AlarmState.MIN1));
        blowdownReturnValve[0].addSafeClosedProvider(()
                -> !alarmManager.isAlarmActive(
                        "Drum1Level", AlarmState.MIN2));
        blowdownReturnValve[1].addSafeClosedProvider(()
                -> !alarmManager.isAlarmActive(
                        "Drum2Level", AlarmState.MIN2));

        // Shut off feedwater pumps on low DA level
        for (int jdx = 0; jdx < 2; jdx++) {
            feedwaterPump[0][jdx].addSafeOffProvider(()
                    -> !alarmManager.isAlarmActive(
                            "DA1Level", AlarmState.MIN2));
        }
        for (int jdx = 0; jdx < 2; jdx++) {
            feedwaterPump[1][jdx].addSafeOffProvider(()
                    -> !alarmManager.isAlarmActive(
                            "DA2Level", AlarmState.MIN2));
        }
        // Do not allow direct connections between hotwell with spare pump
        feedwaterSparePumpInValve[0].addSafeClosedProvider(()
                -> feedwaterSparePumpInValve[1].getOpening() <= 1.0);
        feedwaterSparePumpInValve[1].addSafeClosedProvider(()
                -> feedwaterSparePumpInValve[0].getOpening() <= 1.0);

        // Close Aux Condensers Drain Valve on low level
        auxCondCondensateValve[0].addSafeClosedProvider(()
                -> !alarmManager.isAlarmActive(
                        "AuxCond1Level", AlarmState.MIN1)
        );
        auxCondCondensateValve[1].addSafeClosedProvider(()
                -> !alarmManager.isAlarmActive(
                        "AuxCond2Level", AlarmState.MIN1)
        );

        // close aux condenser steam valves on high level or high temperature
        auxCondSteamValve[0].addSafeClosedProvider(()
                -> !(alarmManager.isAlarmActive(
                        "AuxCond1Level", AlarmState.MAX1)
                || alarmManager.isAlarmActive(
                        "AuxCond1Temp", AlarmState.MAX1))
        );
        auxCondSteamValve[1].addSafeClosedProvider(()
                -> !(alarmManager.isAlarmActive(
                        "AuxCond2Level", AlarmState.MAX1)
                || alarmManager.isAlarmActive(
                        "AuxCond2Temp", AlarmState.MAX1))
        );

        // Nothing gets into condenser if no vacuum is available.
        for (int idx = 0; idx < 2; idx++) {
            mainSteamDump[idx].addSafeClosedProvider(()
                    -> !alarmManager.isAlarmActive(
                            "CondenserVacuum", AlarmState.MIN1));
        }

        // Hotwell
        hotwellDrainValve.addSafeClosedProvider(()
                -> !alarmManager.isAlarmActive(
                        "HotwellLevel", AlarmState.MIN1));
        for (int idx = 0; idx < 3; idx++) {
            condensationHotwellPump[idx].addSafeOffProvider(()
                    -> !alarmManager.isAlarmActive(
                            "HotwellLevel", AlarmState.MIN1));
            condensationCondensatePump[idx].addSafeOffProvider(()
                    -> !alarmManager.isAlarmActive(
                            "HotwellLevel", AlarmState.MIN1));
        }

        // Turbine - there is no trip logic yet but for now block all valves
        // if there is no vacuum - TODO - this will be included in turbine 
        // logic later
        for (int idx = 0; idx < 2; idx++) {
            turbineTripValve[idx].addSafeClosedProvider(()
                    -> !turbine.isTpsActive());
            turbineReheaterSteamValve[idx].addSafeClosedProvider(()
                    -> !alarmManager.isAlarmActive(
                            "CondenserVacuum", AlarmState.MIN1));

            // Turbine: Reheater gets limited by condensate level and the 
            // shut valve also gets limited by condenser vacuum
            turbineReheaterCondensateValve[idx].addSafeClosedProvider(()
                    -> !alarmManager.isAlarmActive(
                            "ReheaterCondensateLevel", AlarmState.MIN1));
        }
        turbineLowPressureTripValve.addSafeClosedProvider(()
                -> !turbine.isTpsActive());
        turbineReheaterCondensateDrain.addSafeClosedProvider(()
                -> !alarmManager.isAlarmActive(
                        "ReheaterCondensateLevel", AlarmState.MIN1));
        turbineReheaterTripValve.addSafeClosedProvider(()
                -> !turbine.isTpsActive());

        // </editor-fold>
        // Time ocnstant for condenser
        condenserVacuum.setMaxOutput(1e5); // 1 bar
        condenserVacuum.setMinOutput(0); // 0 bar
        condenserVacuum.setTi(2e-4);
        condenserVacuum.forceOutputValue(1e5); // initialize with ambient press.
    }

    @Override
    public void run() {
        setThermalPower(0, core.getThermalPower(0));
        setThermalPower(1, core.getThermalPower(1));

        // Before this run method is invoked from the MainLoop, the controller
        // will be triggered to fire all property updates (this will invoke
        // handleAction in this class here. So the first thing happening is
        // all the commands from GUI will be processed.
        // Invoke all runnable assemblies, this will for example set the valve 
        // opening values or pump effort source values to the thermal layout 
        // model.
        runner.invokeAll();

        // Apply thermal power from fuel
        if (!noReactorInput) { // for debugging and full reactor use
            for (int idx = 0; idx < 2; idx++) {
                fuelThermalSource[idx].setFlow(thermalPower[idx] * 1e6);
            }
        } else {
            for (int idx = 0; idx < 2; idx++) {
                fuelThermalSource[idx].setFlow(0.0); // this will be noticed.
            }
        }

        // Write Control Outputs to model if necessary (some controllers are
        // already integrated into the elements)
        if (!blowdownBalanceControlLoop.isManualMode()) {
            // Controller output closes one of the two valves depending on the
            // sign of the control output
            if (blowdownBalanceControlLoop.getOutput() > 0) {
                blowdownReturnValve[0].operateSetOpening(80
                        + blowdownBalanceControlLoop.getOutput() * 0.25);
                blowdownReturnValve[1].operateSetOpening(80
                        - blowdownBalanceControlLoop.getOutput());

            } else if (blowdownBalanceControlLoop.getOutput() < 0) {
                blowdownReturnValve[0].operateSetOpening(80
                        + blowdownBalanceControlLoop.getOutput());
                blowdownReturnValve[1].operateSetOpening(80
                        - blowdownBalanceControlLoop.getOutput() * 0.25);
            } else {
                blowdownReturnValve[0].operateSetOpening(80);
                blowdownReturnValve[1].operateSetOpening(80);
            }
        }

        // Apply the vacuum value to the hotwell/condenser element
        hotwell.getPrimarySideReservoir().setAmbientPressure(
                condenserVacuum.getOutput());

        // Reset and solve (update) the whole thermal layout one cycle.
        solver.prepareCalculation();
        solver.doCalculation();

        // Get those values here so we can add them later more easy. We had to
        // simplify the model in a bad, misleading way to keep it stable.
        for (int idx = 0; idx < 2; idx++) {
            steamOutToTurbine[idx]
                    = mainSteamDrumNode[idx].getFlow(
                            turbineTripValve[idx].getValveElement())
                    + mainSteamDrumNode[idx].getFlow(
                            turbineReheaterSteamValve[idx].getValveElement());
        }

        // Generate core temperature value (avg deg celsius value)
        if (!noReactorInput) {
            coreTemp = (fuelThermalOut[0].getEffort()
                    + fuelThermalOut[1].getEffort()) / 2 - 273.5;
        } else {
            coreTemp = 0.324 * (thermalPower[0] + thermalPower[1]) / 2 + 50.9;
        }

        // Generate thermal lift for next cycle
        for (int idx = 0; idx < 2; idx++) {
            if (loopBypass[idx].getOpening() > 1.0) {
                loopThermalLift[idx].setEffort(
                        (loopEvaporator[idx].getTemperature()
                        - loopDownflow[idx].getHeatHandler().getTemperature())
                        * 1000); // its a coincidentce that this is 1000
            }
        }

        // Auto-open or close main steam valves according to steam pressure
        for (int idx = 0; idx < 2; idx++) {
            if (loopSteamDrum[idx].getEffort() >= 1.3e5) {
                mainSteamShutoffValve[idx].operateOpenValve();
            } else if (loopSteamDrum[idx].getEffort() < 1.1e5) {
                mainSteamShutoffValve[idx].operateCloseValve();
            }
        }

        // Calculate turbine mechanical energy transfer
        turbineShaftPower = turbineHighPressure.getExcessPower()
                + turbineLowPressureStage[0].getExcessPower()
                + turbineLowPressureStage[1].getExcessPower()
                + turbineLowPressureStage[2].getExcessPower()
                + turbineLowPressureStage[3].getExcessPower()
                + turbineLowPressureStage[4].getExcessPower()
                + turbineLowPressureStage[5].getExcessPower();
        // and make it known to the turbine
        turbine.setShaftPower(turbineShaftPower * 1e-6);

        // Condenser Vacuum: There is no specific model for that, it is assumed
        // that certain effects do either make the vacuum go down or up. The
        // sum (or difference) of those effects will be integrated, providing a
        // value for the condensers vacuum.
        // Todo: Remember to have some initital condition value for this!
        // Make a -100 to 100 % value for integral input to describe the speed
        // of integration.
        condenserVacuum.setInput(
                // A static value that will make the vacuum disappear if no
                // ejector is active.
                0.4
                // Full flow of 1400 kg/s adds a number of "10" so it is
                // 1 / 1400 * 10 = 7.14e-3
                + hotwell.getPrimaryInFlow() * 7e-3
                // A way smaller factor will be applied to the steam dump, it
                // does not bring in that much extra air and there is no turbine
                // tap available to use the main ejectors.
                + mainSteamDump[0].getValveElement().getFlow() * 2e-3
                + mainSteamDump[1].getValveElement().getFlow() * 2e-3
                // Each startup ejector makes 1 kg/s in operation. They should
                // not be able to compensate for large steam quantities
                - ejectorStartup[0].getValveElement().getFlow()
                - ejectorStartup[1].getValveElement().getFlow()
                // Main ejectors are designed to use about 3 kg/s of steam
                // each 
                - ejectorMainSteamValve[0].getValveElement().getFlow()
        );
        condenserVacuum.run();

        // Get the temperature on the steam reheater out - problem is, that this
        // value directly calculated from other values and highly dependend on
        // flows and pressures. It is also not always available due to the 
        // bad quality of the solver and all the numeric issues.
        if (turbineReheater.getPhasedNode(
                PhasedSuperheater.SECONDARY_OUT).heatEnergyUpdated(
                        turbineLowPressureTripValve.getValveElement())) {
            if (!turbineReheater.getPhasedNode(
                    PhasedSuperheater.SECONDARY_OUT)
                    .noHeatEnergy(turbineLowPressureTripValve.getValveElement())) {
                reheaterOutTemperature
                        = phasedWater.getTemperature(
                                turbineReheater.getPhasedNode(
                                        PhasedSuperheater.SECONDARY_OUT)
                                        .getHeatEnergy(
                                                turbineLowPressureTripValve
                                                        .getValveElement()),
                                turbineReheater.getPhasedNode(
                                        PhasedSuperheater.SECONDARY_OUT)
                                        .getEffort()
                        ) - 273.15;
                reheaterOutQuality
                        = phasedWater.getVapourFraction(
                                turbineReheater.getPhasedNode(
                                        PhasedSuperheater.SECONDARY_OUT)
                                        .getHeatEnergy(
                                                turbineLowPressureTripValve
                                                        .getValveElement()),
                                turbineReheater.getPhasedNode(
                                        PhasedSuperheater.SECONDARY_OUT)
                                        .getEffort());
            }
        }

        // Update Alarms
        alarmUpdater.invokeAll();

        // <editor-fold defaultstate="collapsed" desc="Gain measurement data and set it to parameter out handler">
        outputValues.setParameterValue("MakeupStorage#Level",
                makeupStorage.getEffort() * 1.0224e-4); // Pa in meters
        for (int idx = 0; idx < 2; idx++) {
            // -20 cm = 0 kg, 0 cm = 10.000 kg - as with RxModel
            outputValues.setParameterValue("Loop" + (idx + 1) + "#DrumLevel",
                    (loopSteamDrum[idx].getFillHeight() - 1.15) * 100);
            outputValues.setParameterValue("Loop" + (idx + 1) + "#DrumPressure",
                    loopNodeDrumFromReactor[idx].getEffort() / 100000 - 1.0);
            outputValues.setParameterValue("Loop" + (idx + 1)
                    + "#DrumTemperature",
                    loopSteamDrum[idx].getTemperature() - 273.5);

            outputValues.setParameterValue("Main" + (idx + 1)
                    + "#SteamShutoffValve",
                    mainSteamShutoffValve[idx].getValveElement().getOpening());
            outputValues.setParameterValue("Main" + (idx + 1)
                    + "#SteamFromDrumFlow",
                    -mainSteamDrumNode[idx].getFlow(loopSteamDrum[idx]));

            // Trim valve percentage:
            for (int jdx = 0; jdx < 4; jdx++) {
                outputValues.setParameterValue(
                        "Loop" + (idx + 1) + "#mcp" + (jdx + 1) + "TrimValve",
                        loopTrimValve[idx][jdx].getOpening());
            }
            outputValues.setParameterValue(
                    "Loop" + (idx + 1) + "#McpInTemp",
                    loopCollector[idx].getTemperature() - 273.5);
            outputValues.setParameterValue(
                    "Loop" + (idx + 1) + "#McpInPressure",
                    loopCollector[idx].getEffort() / 100000 - 1.0);
            outputValues.setParameterValue(
                    "Loop" + (idx + 1) + "#FuelInTemp",
                    loopDistributor[idx].getTemperature() - 273.5);
            outputValues.setParameterValue(
                    "Loop" + (idx + 1) + "#FuelInPressure",
                    loopDistributor[idx].getEffort() / 100000 - 1.0);

            // The MCP cooldown value is the temperature drop before the MCP 
            // inlet. Used to prevent cavitation but below 100 °C this does not
            // matter. The MPC cavitation is calculated different to make things
            // worse.
            outputValues.setParameterValue(
                    "Loop" + (idx + 1) + "#McpCooldown",
                    loopDistributor[idx].getTemperature()
                    - loopSteamDrum[idx].getTemperature());

            outputValues.setParameterValue(
                    "Loop" + (idx + 1) + "#DownFlow",
                    loopDownflow[idx].getFlow());

            // This is some thing that should not be necessary anymore
            if (loopNodeDrumFromReactor[idx].flowUpdated(loopEvaporator[idx])) {
                outputValues.setParameterValue(
                        "Loop" + (idx + 1) + "#ReactorOutFlow",
                        -loopNodeDrumFromReactor[idx].getFlow(
                                loopEvaporator[idx]));
            } else {
                outputValues.setParameterValue(
                        "Loop" + (idx + 1) + "#ReactorOutFlow",
                        Double.NaN);
            }

            // Temperature from thermal system
            outputValues.setParameterValue(
                    "Core" + (idx + 1) + "#Temperature",
                    fuelThermalOut[idx].getEffort() - 273.5);

            outputValues.setParameterValue(
                    "Loop" + (idx + 1) + "#Voiding",
                    loopEvaporator[idx].getVoiding(1e5));

            // Flow from Blowdown into Feedwater mixture in drum
            if (loopNodeDrumFromReactor[idx].heatEnergyUpdated(
                    loopEvaporator[idx])) {
                if (!loopNodeDrumFromReactor[idx].noHeatEnergy(
                        loopEvaporator[idx])) {
                    // Getting the temperature is a bit complicated for now,
                    // we need to get the heat energy from the node in direction
                    // of the evaporator and convert the energy to a temperature
                    // with the use of the pressure on the node.
                    outputValues.setParameterValue("Loop" + (idx + 1)
                            + "#ReactorOutTemperature",
                            phasedWater.getTemperature(
                                    loopNodeDrumFromReactor[idx].getHeatEnergy(
                                            loopEvaporator[idx]),
                                    loopNodeDrumFromReactor[idx].getEffort()
                            ) - 273.15);
                }
            }

            outputValues.setParameterValue("Loop" + (idx + 1)
                    + "#BlowdownFlowToFeedwaterIn",
                    -loopFeedwaterIn[idx].getFlow( // negative = into node
                            blowdownReturn[idx]));

            // Feedwater: Sum of feed into steam drums
            outputValues.setParameterValue("Feedwater" + (idx + 1) + "#Flow",
                    -loopFeedwaterIn[idx].getFlow(
                            feedwaterFlowRegulationValve[idx][0]
                                    .getValveElement())
                    - loopFeedwaterIn[idx].getFlow(
                            feedwaterFlowRegulationValve[idx][1]
                                    .getValveElement())
                    - loopFeedwaterIn[idx].getFlow(
                            feedwaterFlowRegulationValve[idx][2]
                                    .getValveElement()));

            outputValues.setParameterValue("Feedwater" + (idx + 1)
                    + "#StartupReductionValve",
                    feedwaterStartupReductionValve[idx].getOpening());

            outputValues.setParameterValue("Deaerator" + (idx + 1) + "#Level",
                    deaerator[idx].getFillHeight() * 100); // m to cm
            outputValues.setParameterValue(
                    "Deaerator" + (idx + 1) + "#Pressure",
                    deaerator[idx].getEffort() * 1e-5 - 1.0); // Pa to bar rel.
            outputValues.setParameterValue(
                    "Deaerator" + (idx + 1) + "#Temperature",
                    deaerator[idx].getTemperature() - 273.15);

            // Feedwater from Deaerators to the pumps (this value is displayed
            // on the feedwater pumps mnemonics
            outputValues.setParameterValue("Deaerator" + (idx + 1)
                    + "#FeedFlow",
                    deaeratorFeedwaterOutHeatNode[idx].getFlow(
                            feedwaterPump[idx][0].getSuctionValve())
                    + deaeratorFeedwaterOutHeatNode[idx].getFlow(
                            feedwaterPump[idx][1].getSuctionValve())
                    + deaeratorFeedwaterOutHeatNode[idx].getFlow(
                            feedwaterSparePumpInValve[idx].getValveElement()));
        }

        // Blowdown and Cooldown system
        outputValues.setParameterValue("Blowdown#InTemperature",
                blowdownInCollectorNode.getTemperature() - 273.5);
        outputValues.setParameterValue("Blowdown#PassiveFlow",
                blowdownInCollectorNode.getFlow(
                        blowdownValvePassiveFlow.getValveElement()));
        outputValues.setParameterValue("Blowdown#RegeneratorPrimaryOutTemp",
                ((HeatNode) blowdownRegenerator.getPrimarySide()
                        .getNode(1)).getTemperature() - 273.5);
        outputValues.setParameterValue("Blowdown#CoolerPrimaryInTemp",
                ((HeatNode) blowdownCooldown.getPrimarySide()
                        .getNode(0)).getTemperature() - 273.5);
        outputValues.setParameterValue("Blowdown#CoolerPrimaryFlow",
                blowdownCooldown.getPrimarySide().getNode(0)
                        .getFlow(blowdownCooldown.getPrimarySide()));
        outputValues.setParameterValue("Blowdown#CoolerPrimaryOutTemp",
                ((HeatNode) blowdownCooldown.getPrimarySide()
                        .getNode(1)).getTemperature() - 273.5);
        outputValues.setParameterValue("Blowdown#TreatmentOutTemp",
                blowdownTreatedOutNode.getTemperature() - 273.5);
        outputValues.setParameterValue("Blowdown#RegeneratorSecondaryOutTemp",
                ((HeatNode) blowdownRegenerator.getPrimarySide()
                        .getNode(1)).getTemperature() - 273.5);
        outputValues.setParameterValue("Blowdown#CoolantOutTemp",
                ((HeatNode) blowdownCooldown.getSecondarySide()
                        .getNode(0)).getTemperature() - 273.5);
        outputValues.setParameterValue("Blowdown#CoolantOutFlow",
                blowdownValveCoolant.getFlowSource().getFlow());
        outputValues.setParameterValue("Blowdown#ValveDrain",
                blowdownValveDrain.getValveElement().getOpening());
        outputValues.setParameterValue("Blowdown#ValveDrainFlow",
                blowdownValveDrain.getValveElement().getFlow());
        outputValues.setParameterValue("Blowdown#SumFlowToDrums",
                blowdownReturnValve[0].getValveElement().getFlow()
                + blowdownReturnValve[1].getValveElement().getFlow());
        outputValues.setParameterValue("Blowdown#ReturnTemp",
                blowdownOutNode.getTemperature() - 273.5);

        for (int idx = 0; idx < 2; idx++) {
            outputValues.setParameterValue(
                    "Feedwater" + (idx + 1) + "#Temperature",
                    feedwaterPumpCollectorNodes[idx].getTemperature() - 273.5);
            outputValues.setParameterValue(
                    "Feedwater" + (idx + 1) + "#Pressure",
                    feedwaterPumpCollectorNodes[idx]
                            .getEffort() / 100000 - 1.0);
        }
        for (int idx = 0; idx < 2; idx++) {
            outputValues.setParameterValue(
                    "AuxCond" + (idx + 1) + "#Level",
                    auxCondensers[idx].getPrimarySideReservoir()
                            .getFillHeight() * 100); // m to cm
            outputValues.setParameterValue(
                    "AuxCond" + (idx + 1) + "#Temperature",
                    auxCondensers[idx].getPrimarySideReservoir()
                            .getTemperature() - 273.5);
            outputValues.setParameterValue(
                    "AuxCond" + (idx + 1) + "#SteamFlow",
                    auxCondSteamValve[idx].getValveElement().getFlow());
        }
        outputValues.setParameterValue("AuxCond#CondensateTemperature",
                auxCondCondInNode.getTemperature() - 273.5);

        for (int idx = 0; idx < 2; idx++) {
            outputValues.setParameterValue(
                    "Main" + (idx + 1) + "#BypassFlow",
                    mainSteamDump[idx].getValveElement().getFlow());
        }

        outputValues.setParameterValue("Hotwell#Level", // m to cm
                hotwell.getPrimarySideReservoir().getFillHeight() * 100);
        outputValues.setParameterValue("Hotwell#Pressure", // m to cm
                hotwell.getPhasedNode(PhasedCondenserNoMass.PRIMARY_INNER)
                        .getEffort() / 100000); // bar absolute
        outputValues.setParameterValue("Hotwell#Temperature",
                hotwell.getPrimarySideReservoir().getTemperature() - 273.15);
        for (int idx = 0; idx < 2; idx++) {
            outputValues.setParameterValue(
                    "Condensation" + (idx + 1) + "#FlowToDA",
                    condensationValveToDA[idx].getValveElement().getFlow());
        }
        outputValues.setParameterValue("Hotwell#FillFlow",
                hotwellFillValve.getValveElement().getFlow());
        outputValues.setParameterValue("Hotwell#DrainFlow",
                hotwellDrainValve.getValveElement().getFlow());
        outputValues.setParameterValue("CircCoolant#CondensorOutTemp",
                hotwell.getHeatNode(PhasedCondenserNoMass.SECONDARY_OUT)
                        .getTemperature() - 273.15);
        outputValues.setParameterValue("Condensation#HotwellPumpsPressure",
                condensationPumpOut.getEffort() / 100000 - 1.0);

        // Make a 0..100 kPa value like in the old sim game with 0 being 
        // 1 barabs and 100 kPa being 0 barabs.
        outputValues.setParameterValue("Condenser#Vacuum",
                (1e5 - condenserVacuum.getOutput()) * 1e-3);

        for (int idx = 0; idx < 2; idx++) {
            outputValues.setParameterValue(
                    "EjectorStartup" + (idx + 1) + "#Flow",
                    ejectorStartup[idx].getValveElement().getFlow());
        }

        for (int idx = 0; idx < 3; idx++) {
            outputValues.setParameterValue(
                    "Preheater" + (idx + 1) + "#Level",
                    preheater[idx].getPrimarySideReservoir()
                            .getFillHeight() * 100);
            outputValues.setParameterValue(
                    "Preheater" + (idx + 1) + "#Temperature",
                    preheater[idx].getPrimarySideReservoir()
                            .getTemperature() - 273.15);
        }

        // Flow to turbine and startup ejectors, this is displayed as main flow
        // to turbine system on turbine panel. Includes the steam dump valves.
        for (int idx = 0; idx < 2; idx++) {
            outputValues.setParameterValue(
                    "Turbine" + (idx + 1) + "#MainSteamFlow",
                    steamOutToTurbine[idx]);
        }
        // Get the HP out temperature directly from the heated steam mass
        outputValues.setParameterValue("Turbine#HPOutTemp",
                turbineHighPressureOutMass.getTemperature() - 273.15);
        outputValues.setParameterValue("Turbine#LPInTemp",
                turbineLowPressureInMass.getTemperature() - 273.15);

        outputValues.setParameterValue("Turbine#ReheaterOutTemp",
                reheaterOutTemperature);
        outputValues.setParameterValue("Turbine#ReheaterOutQuality",
                reheaterOutQuality);

        outputValues.setParameterValue("Turbine#ReheaterLevel",
                turbineReheater.getPrimarySideReservoir()
                        .getFillHeight() * 100); // m to cm
        outputValues.setParameterValue("Turbine#ReheaterCondTemp",
                turbineReheater.getPrimarySideReservoir()
                        .getTemperature() - 273.15); // K to °C
        outputValues.setParameterValue("Turbine#ReheaterSteamInFlow",
                -turbineReheater.getPrimarySideCondenser().getFlow());
        outputValues.setParameterValue("Turbine#LowPressureFlow",
                turbineLowPressureStage[0].getFlow());

        // </editor-fold>        
//        System.out.println(
//                "p_HP-In: "
//                + ((int) turbineHighPressureIn.getEffort())
//                + ", h_HP-In: "
//                + ((int) turbineHighPressureIn.getHeatEnergy())
//                + ", p_HP-MidOut: "
//                + turbineHighPressureMidOut.getEffort()
//                + ", h_HP-MidOut: "
//                + turbineHighPressureMidOut.getHeatEnergy());
    }

    @Override
    public void updateNotification(String propertyName) {

    }

    @Override
    public void handleAction(ActionCommand ac) {
        // <editor-fold defaultstate="collapsed" desc="Receive and process control commands from controller (GUI)">
        if (ac.getPropertyName().startsWith("Loop")) {
            if (setpointDrumPressure.handleAction(ac)) {
                return;
            }
            // property name is smtn like this: Loop2#mcp3ValveDischarge
            if (ac.getPropertyName().substring(6, 9).equals("mcp")) {
                int idx = Character.getNumericValue(
                        ac.getPropertyName().charAt(4)) - 1;
                int jdx = Character.getNumericValue(
                        ac.getPropertyName().charAt(9)) - 1;
                if (idx >= 0 && idx <= 3 && jdx >= 0 && jdx <= 3) {
                    String command = ac.getPropertyName()
                            .substring(10, ac.getPropertyName().length());
                    if (!loopAssembly[idx][jdx].handleAction(ac)) {
                        loopTrimValve[idx][jdx].handleAction(ac);
                    }
                }
                return;
            }
            switch (ac.getPropertyName()) {
                case "Loop1#Bypass":
                    boolean allowOpening = true;
                    // all 4 mcps must be closed to allow bypass valve to open.
                    for (int idx = 0; idx < 4; idx++) {
                        if (loopAssembly[0][idx].getDischargeValve()
                                .getOpening() > 1.0) {
                            allowOpening = false;
                            break;
                        }
                    }
                    if ((boolean) ac.getValue()) {
                        if (allowOpening) {
                            loopBypass[0].handleAction(ac);
                        }
                    } else {
                        loopBypass[0].handleAction(ac);
                    }
                    break;
                case "Loop2#Bypass":
                    allowOpening = true;
                    // all 4 mcps must be closed to allow bypass valve to open.
                    for (int idx = 0; idx < 4; idx++) {
                        if (loopAssembly[1][idx].getDischargeValve()
                                .getOpening() > 1.0) {
                            allowOpening = false;
                            break;
                        }
                    }
                    if ((boolean) ac.getValue()) {
                        if (allowOpening) {
                            loopBypass[1].handleAction(ac);
                        }
                    } else {
                        loopBypass[1].handleAction(ac);
                    }
                    break;
                case "Loop1#DrumLevelSetpoint":
                    setpointDrumLevel[0].handleAction(ac);
                    break;
                case "Loop2#DrumLevelSetpoint":
                    setpointDrumLevel[1].handleAction(ac);
                    break;
            }
        } else if (ac.getPropertyName().startsWith("Blowdown")) {
            for (int idx = 0; idx < 2; idx++) {
                if (blowdownCooldownPumps[idx].handleAction(ac)) {
                    return;
                }
                if (blowdownValveFromDrum[idx].handleAction(ac)) {
                    return;
                }
                if (blowdownValveFromLoop[idx].handleAction(ac)) {
                    return;
                }
                if (blowdownReturnValve[idx].handleAction(ac)) {
                    return;
                }
            }

            if (blowdownValvePassiveFlow.handleAction(ac)) {
                return;
            } else if (blowdownValvePumpsToRegenerator.handleAction(ac)) {
                return;
            } else if (blowdownValvePumpsToCooler.handleAction(ac)) {
                return;
            } else if (blowdownValveRegeneratorToCooler.handleAction(ac)) {
                return;
            } else if (blowdownValveTreatmentBypass.handleAction(ac)) {
                return;
            } else if (blowdownValveRegeneratorToCooler.handleAction(ac)) {
                return;
            } else if (blowdownValveRegeneratedToDrums.handleAction(ac)) {
                return;
            } else if (blowdownValveDrain.handleAction(ac)) {
                return;
            }

            switch (ac.getPropertyName()) {
                case "Blowdown#ValveCoolant":
                    blowdownValveCoolant.handleAction(ac);
                    break;

                case "Blowdown#Balance_ControlCommand":
                    if (ac.getValue().equals(ControlCommand.AUTOMATIC)) {
                        blowdownBalanceControlLoop.setManualMode(false);
                    } else if (ac.getValue().equals(
                            ControlCommand.MANUAL_OPERATION)) {
                        blowdownBalanceControlLoop.setManualMode(true);
                    }
                    break;
            }
        } else if (ac.getPropertyName().startsWith("Feedwater")) {
            //                                  01234567890123456789
            // Feedwater Pumps commands:        Feedwater1#Pump1#ValveDischarge
            if (ac.getPropertyName().substring(11, 15).equals("Pump")) {
                int idx = Character.getNumericValue(
                        ac.getPropertyName().charAt(9)) - 1;
                int jdx = Character.getNumericValue(
                        ac.getPropertyName().charAt(15)) - 1;
                if (idx >= 0 && idx <= 1 && jdx >= 0 && jdx <= 1) {
                    String command = ac.getPropertyName()
                            .substring(16, ac.getPropertyName().length());
                    if (feedwaterPump[idx][jdx].handleAction(ac)) {
                        return;
                    }
                }
            }
            if (feedwaterPump3.handleAction(ac)) {
                return;
            }
            // level regulation valves (those include control elements)
            for (int idx = 0; idx < 2; idx++) {
                for (int jdx = 0; jdx < 3; jdx++) {
                    if (feedwaterFlowRegulationValve[idx][jdx]
                            .handleAction(ac)) {
                        return;
                    }
                }
            }
            // Shutoff valves
            for (int idx = 0; idx < 2; idx++) {
                for (int jdx = 0; jdx < 3; jdx++) {
                    if (feedwaterShutoffValve[idx][jdx]
                            .handleAction(ac)) {
                        return;
                    }
                }
            }
            for (int idx = 0; idx < 2; idx++) {
                if (feedwaterSparePumpInValve[idx].handleAction(ac)) {
                    return;
                }
                if (feedwaterSparePumpOutValve[idx].handleAction(ac)) {
                    return;
                }
                if (feedwaterStartupReductionValve[idx].handleAction(ac)) {
                    return;
                }
            }
        } else if (ac.getPropertyName().startsWith("Deaerator")) {
            setpointDAPressure[0].handleAction(ac);
            setpointDAPressure[1].handleAction(ac);
            setpointDALevel[0].handleAction(ac);
            setpointDALevel[1].handleAction(ac);
            deaeratorDrain[0].handleAction(ac);
            deaeratorDrain[1].handleAction(ac);
            deaeratorSteamInRegValve[0].handleAction(ac);
            deaeratorSteamInRegValve[1].handleAction(ac);
            deaeratorSteamFromMain[0].handleAction(ac);
            deaeratorSteamFromMain[1].handleAction(ac);

        } else if (ac.getPropertyName().startsWith("AuxCond")) {
            if (ac.getPropertyName().equals("AuxCond1#CoolantSource")) {
                switch ((int) ac.getValue()) {
                    case -1 ->
                        auxCondCoolantValve[0].setToMinFlow();
                    case +1 ->
                        auxCondCoolantValve[0].setToMaxFlow();
                    default ->
                        auxCondCoolantValve[0].setStopAtCurrentFlow();
                }
                return;
            }
            if (ac.getPropertyName().equals("AuxCond2#CoolantSource")) {
                switch ((int) ac.getValue()) {
                    case -1 ->
                        auxCondCoolantValve[1].setToMinFlow();
                    case +1 ->
                        auxCondCoolantValve[1].setToMaxFlow();
                    default ->
                        auxCondCoolantValve[1].setStopAtCurrentFlow();
                }
                return;
            }
            for (int idx = 0; idx < 2; idx++) {
                auxCondSteamValve[idx].handleAction(ac);
                auxCondCondensateValve[idx].handleAction(ac);
                auxCondPumps[idx].handleAction(ac);
                setpointAuxCondLevel[idx].handleAction(ac);
            }
            auxCondBypass.handleAction(ac);
            auxCondValveToHotwell.handleAction(ac);
            auxCondValveToDrain.handleAction(ac);
            if (ac.getPropertyName().equals("AuxCond1#CoolantValve")) {
                switch ((int) ac.getValue()) {
                    case -1 ->
                        auxCondCoolantValve[0].setToMinFlow();
                    case +1 ->
                        auxCondCoolantValve[0].setToMaxFlow();
                    default ->
                        auxCondCoolantValve[0].setStopAtCurrentFlow();
                }
            }
            if (ac.getPropertyName().equals("AuxCond2#CoolantValve")) {
                switch ((int) ac.getValue()) {
                    case -1 ->
                        auxCondCoolantValve[1].setToMinFlow();
                    case +1 ->
                        auxCondCoolantValve[1].setToMaxFlow();
                    default ->
                        auxCondCoolantValve[1].setStopAtCurrentFlow();
                }
            }
        } else if (ac.getPropertyName().startsWith("Condensation")) {
            for (int idx = 0; idx < 3; idx++) {
                condensationHotwellPump[idx].handleAction(ac);
                condensationCondensatePump[idx].handleAction(ac);
            }
            for (int idx = 0; idx < 2; idx++) {
                condensationValveToDA[idx].handleAction(ac);
            }
        } else if (ac.getPropertyName().startsWith("Ejector")) {
            for (int idx = 0; idx < 2; idx++) {
                ejectorStartup[idx].handleAction(ac);
            }
            // Todo: More valves here, there are only those so far which do work
            for (int idx = 0; idx < 3; idx++) {
                ejectorMainSteamValve[idx].handleAction(ac);
                // ejectorMainCondensateValve[idx].handleAction(ac);
                ejectorMainFlowIn[idx].handleAction(ac);
                ejectorMainFlowOut[idx].handleAction(ac);
            }
            ejectorMainBypass.handleAction(ac);
        } else {
            // Main Steam shutoff valve commands from GUI
            switch (ac.getPropertyName()) {
                case "Main1#SteamShutoffValve" ->
                    mainSteamShutoffValve[0].handleAction(ac);
                case "Main2#SteamShutoffValve" ->
                    mainSteamShutoffValve[1].handleAction(ac);
            }
            mainSteamDump[0].handleAction(ac);
            mainSteamDump[1].handleAction(ac);
            hotwellFillValve.handleAction(ac);
            hotwellDrainValve.handleAction(ac);
            setpointHotwellUpperLevel.handleAction(ac);
            setpointHotwellLowerLevel.handleAction(ac);
            makeupPumps[0].handleAction(ac);
            makeupPumps[1].handleAction(ac);

            preheaterCondensateValve[0].handleAction(ac);
            preheaterCondensateValve[1].handleAction(ac);
            preheaterCondensateValve[2].handleAction(ac);

            for (int idx = 0; idx < 2; idx++) {
                turbineTripValve[idx].handleAction(ac);
                turbineStartupSteamValve[idx].handleAction(ac);
                turbineMainSteamValve[idx].handleAction(ac);
                turbineReheaterSteamValve[idx].handleAction(ac);
            }
            turbineReheaterTripValve.handleAction(ac);
            turbineReheaterTrimValve.handleAction(ac);
            turbineReheaterCondensateValve[0].handleAction(ac);
            turbineReheaterCondensateValve[1].handleAction(ac);
            turbineReheaterCondensateDrain.handleAction(ac);
            turbineLowPressureTripValve.handleAction(ac);
            for (int idx = 0; idx < 5; idx++) {
                turbineLowPressureTapValve[idx].handleAction(ac);
            }
            setpointTurbineReheaterTemperature.handleAction(ac);
            setpointTurbineReheaterLevel.handleAction(ac);

            if (ac.getPropertyName().equals("SetCoreOnly")) {
                noReactorInput = true;
            }
        }
        // </editor-fold>
    }

    /**
     * Set the current thermal power in the reactor.
     *
     * @param loop 0 or 1
     * @param power in Megawatts
     */
    private void setThermalPower(int loop, double power) {
        // Add 5.6 MW idle (2.8 per side) power here
        // Limit the thermal power to 10 Gigawatts per side, it will crash the
        // simulation anyway but that way it's not that fast.
        thermalPower[loop] = Math.min(power + 2.8, 1e4);
    }

    /**
     * Called from the turbine protection system on turbine trip action, this
     * handles the part which is defined here in the thermal layout.
     */
    public void turbineTrip() {
        turbineReheaterTrimValve.operateCloseValve();
        turbineTripValve[0].operateCloseValve();
        turbineTripValve[1].operateCloseValve();
        turbineLowPressureTripValve.operateCloseValve();
    }

    /**
     * Returns the last void value from steam generation.
     *
     * @return Value between 0..100 %
     */
    public double getVoiding() {
        return voiding;
    }

    public double getCoreTemp() {
        return coreTemp;
    }

    /**
     * Called by the turbine setpoint to check if the turbine startup valves are
     * currently in automatic mode. This is used to make a follow up on the
     * setpoint generator value.
     *
     * @return true if one of both startup valves is in auto mode
     */
    public boolean isTurbineStartupValveAutomatic() {
        return !turbineStartupSteamValve[0].getController().isManualMode()
                || !turbineStartupSteamValve[1].getController().isManualMode();
    }

    public void registerReactor(ReactorCore core) {
        this.core = core;
    }

    public void registerTurbine(Turbine turbine) {
        this.turbine = turbine;
    }
}
