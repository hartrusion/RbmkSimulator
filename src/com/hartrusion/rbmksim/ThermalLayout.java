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
import com.hartrusion.control.PControl;
import com.hartrusion.control.PIControl;
import com.hartrusion.control.SerialRunner;
import com.hartrusion.control.Setpoint;
import com.hartrusion.modeling.PhysicalDomain;
import com.hartrusion.modeling.assemblies.HeatControlledFlowSource;
import com.hartrusion.modeling.assemblies.HeatExchanger;
import com.hartrusion.modeling.assemblies.HeatFluidPump;
import com.hartrusion.modeling.assemblies.HeatFluidPumpSimple;
import com.hartrusion.modeling.assemblies.HeatValve;
import com.hartrusion.modeling.assemblies.HeatValveControlled;
import com.hartrusion.modeling.assemblies.PhasedCondenser;
import com.hartrusion.modeling.assemblies.PhasedValve;
import com.hartrusion.modeling.assemblies.PhasedValveControlled;
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
import com.hartrusion.modeling.phasedfluid.PhasedExpandingThermalExchanger;
import com.hartrusion.modeling.phasedfluid.PhasedNode;
import com.hartrusion.modeling.phasedfluid.PhasedPropertiesWater;
import com.hartrusion.modeling.phasedfluid.PhasedSimpleFlowResistance;
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

    // private final If97Wrapper steamTable = new If97Wrapper();
    private final PhasedPropertiesWater phasedWater
            = new PhasedPropertiesWater();

    // <editor-fold defaultstate="collapsed" desc="Model elements declaration and array instantiation">
    // Cold condensate storage
    private final HeatFluidTank makeupStorage;
    private final HeatNode makeupStorageDrainCollector;
    private final HeatNode makeupStorageOut;
    private final HeatFluidPump[] makeupToDAPumps
            = new HeatFluidPump[2];

    private final PhasedNode[] mainSteamDrumNode = new PhasedNode[2];
    private final PhasedValve[] mainSteamShutoffValve = new PhasedValve[2];
    private final PhasedNode[] mainSteam = new PhasedNode[2];

    private final PhasedClosedSteamedReservoir[] loopSteamDrum
            = new PhasedClosedSteamedReservoir[2];
    private final PhasedNode[] loopNodeDrumWaterOut = new PhasedNode[2];
    private final PhasedNode[] loopNodeDrumFromReactor = new PhasedNode[2];
    private final PhasedHeatFluidConverter[] loopFromDrumConverter
            = new PhasedHeatFluidConverter[2];
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
    private final HeatVolumizedFlowResistance[] blowdownFromLoop
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
    private final HeatExchanger blowdownRegenerator;
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
    private final PhasedValveControlled[] auxCondSteamValve = new PhasedValveControlled[2];
    private final PhasedCondenser[] auxCondensers = new PhasedCondenser[2];
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
    private final HeatValve auxCondValveToHotwell;
    private final HeatValve auxCondValveToDrain;

    // Steam Dump
    private final PhasedValveControlled[] steamDump
            = new PhasedValveControlled[2];

    // Condensation
    private final PhasedCondenser hotwell;
    private final PhasedHeatFluidConverter hotwellOutConverter;
    private final HeatNode hotwellOutNode;
    private final HeatFluidPump[] condensationPump = new HeatFluidPump[3];
    private final HeatNode condensationPumpOut;
    private final HeatVolumizedFlowResistance condensationEjectorDummy;
    private final HeatNode condensationBoosterPumpIn;
    private final HeatFluidPump[] condensationBoosterPump
            = new HeatFluidPump[3];
    private final HeatNode condensationBoosterPumpOut;
    private final HeatValveControlled[] condensationValveToDA
            = new HeatValveControlled[2];
    private final HeatNode[] condensationValveOut = new HeatNode[2];
    private final PhasedHeatFluidConverter[] condensationToDeaeratorConverter
            = new PhasedHeatFluidConverter[2];

    // </editor-fold>
    private final Setpoint[] setpointDrumLevel = new Setpoint[2];
    private final Setpoint[] setpointDAPressure = new Setpoint[2];
    private final Setpoint[] setpointDALevel = new Setpoint[2];
    private final Setpoint[] setpointAuxCondLevel = new Setpoint[2];
    private final Setpoint setpointDrumPressure;

    private final DomainAnalogySolver solver = new DomainAnalogySolver();
    private final SerialRunner runner = new SerialRunner();
    private final SerialRunner alarmUpdater = new SerialRunner();

    private final AbstractController blowdownBalanceControlLoop
            = new PControl();

    private int plotUpdateCount;

    private double voiding = 0;
    private double coreTemp = 200;
    private final double[] thermalPower = new double[]{24e6, 24e6};

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
            makeupToDAPumps[idx] = new HeatFluidPump();
            makeupToDAPumps[idx].initName("MakeupStorage" + (idx + 1)
                    + "#ToDAPumps");
        }

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
            loopNodeDrumFromReactor[idx] = new PhasedNode();
            loopNodeDrumFromReactor[idx].setName("Loop" + (idx + 1)
                    + "#NodeDrumFromReactor");
            loopFromDrumConverter[idx]
                    = new PhasedHeatFluidConverter(phasedWater);
            loopFromDrumConverter[idx].setName("Loop" + (idx + 1)
                    + "#FromDrumConverter");
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
            blowdownFromLoopNode[idx] = new HeatNode();
            blowdownFromLoopNode[idx].setName(
                    "Blowdown#FromloopNode" + (idx + 1));
            blowdownFromLoop[idx] = new HeatVolumizedFlowResistance();
            blowdownFromLoop[idx].setName("Blowdown#FromLoop" + (idx + 1));
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
        blowdownRegenerator = new HeatExchanger();
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
            auxCondensers[idx] = new PhasedCondenser(phasedWater);
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
        auxCondValveToHotwell = new HeatValve();
        auxCondValveToHotwell.initName("AuxCond#ToHotwell");
        auxCondValveToDrain = new HeatValve();
        auxCondValveToDrain.initName("AuxCond#ToDrain");

        // Steam Dump
        for (int idx = 0; idx < steamDump.length; idx++) {
            steamDump[idx] = new PhasedValveControlled();
            steamDump[idx].initName("SteamDump");
        }

        // Condensation
        hotwell = new PhasedCondenser(phasedWater);
        hotwell.initName("Hotwell");
        hotwell.initGenerateNodes();
        hotwellOutConverter = new PhasedHeatFluidConverter(phasedWater);
        hotwellOutConverter.setName("HotwellOutConverter");
        hotwellOutNode = new HeatNode();
        hotwellOutNode.setName("HotwellOutNode");
        for (int idx = 0; idx < 3; idx++) {
            condensationPump[idx] = new HeatFluidPump();
            condensationPump[idx].initName(
                    "Condensation" + (idx + 1) + "Pump");
        }
        condensationPumpOut = new HeatNode();
        condensationPumpOut.setName("CondensationPumpOut");
        condensationEjectorDummy = new HeatVolumizedFlowResistance();
        condensationEjectorDummy.setName("CondensationEjectorDummy");
        condensationBoosterPumpIn = new HeatNode();
        condensationBoosterPumpIn.setName("CondensationBoosterPumpIn");
        for (int idx = 0; idx < 3; idx++) {
            condensationBoosterPump[idx] = new HeatFluidPump();
            condensationBoosterPump[idx].initName(
                    "Condensation" + (idx + 1) + "BoosterPump");
        }
        condensationBoosterPumpOut = new HeatNode();
        condensationBoosterPumpOut.setName("CondensationBoosterPumpOut");
        for (int idx = 0; idx < 2; idx++) {
            condensationValveToDA[idx] = new HeatValveControlled();
            condensationValveToDA[idx].initName(
                    "Condensation" + (idx + 1) + "ValveToDA");
            condensationValveOut[idx] = new HeatNode();
            condensationValveOut[idx].setName(
                    "Condensation" + (idx + 1) + "ValveOut");
            condensationToDeaeratorConverter[idx]
                    = new PhasedHeatFluidConverter(phasedWater);
            condensationToDeaeratorConverter[idx].setName(
                    "Condensation" + (idx + 1) + "ToDeaeratorConverter");
        }

        //</editor-fold>      
        blowdownBalanceControlLoop.setName("Blowdown#BalanceControl");

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
        setpointDrumPressure = new Setpoint();
        setpointDrumPressure.initName("LoopPressureSetpoint");
    }

    public void init() {
        // <editor-fold defaultstate="collapsed" desc="Init listeners">
        // Attach controller to monitor elements. Those monitor elements are
        // part of assemblies and send events to the controller, like a valve
        // open state reached.
        for (int idx = 0; idx < 2; idx++) {
            makeupToDAPumps[idx].registerSignalListener(controller);
        }

        for (int idx = 0; idx < 2; idx++) {
            mainSteamShutoffValve[idx].registerSignalListener(controller);
            deaeratorSteamInRegValve[idx].registerSignalListener(controller);
            deaeratorSteamInRegValve[idx].registerParameterHandler(outputValues);
            deaeratorSteamFromMain[idx].registerSignalListener(controller);
            deaeratorSteamFromMain[idx].registerParameterHandler(outputValues);
            for (int jdx = 0; jdx < 4; jdx++) {
                loopAssembly[idx][jdx].registerSignalListener(controller);
            }
            loopBypass[idx].registerSignalListener(controller);
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
            feedwaterStartupReductionValve[idx].registerSignalListener(controller);
            for (int jdx = 0; jdx < 3; jdx++) {
                feedwaterShutoffValve[idx][jdx].registerSignalListener(controller);
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

        // Attach Signal Listeners or Handlers to Control elements
        for (int idx = 0; idx < 2; idx++) {
            setpointDrumLevel[idx].registerParameterHandler(outputValues);
            setpointDAPressure[idx].registerParameterHandler(outputValues);
            setpointDALevel[idx].registerParameterHandler(outputValues);
            setpointAuxCondLevel[idx].registerParameterHandler(outputValues);
        }
        setpointDrumPressure.registerParameterHandler(outputValues);
        // </editor-fold>  
        // <editor-fold defaultstate="collapsed" desc="Describe dynamic model">
        // Cold condensate storage
        makeupStorage.connectTo(makeupStorageDrainCollector);
        makeupStorage.connectTo(makeupStorageOut);
        for (int idx = 0; idx < 2; idx++) {
            makeupToDAPumps[idx].getSuctionValve().connectTo(makeupStorageOut);
            makeupToDAPumps[idx].getDischargeValve()
                    .connectTo(condensationBoosterPumpOut);
        }

        // Define the primary loop from drum through mcps and reactor and back.
        for (int idx = 0; idx < 2; idx++) {
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

        // Build the blowdown system: There's a connection to a large pipe
        // that goes to a trim valve connected to each
        // distributor (thats the part that distributes coolant to fuel 
        // channels), connected to a common node.
        for (int idx = 0; idx < 2; idx++) {
            blowdownFromLoop[idx].connectBetween(
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
        blowdownValveDrain.getValveElement().connectBetween(blowdownTreatedOutNode, makeupStorageDrainCollector);
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
            auxCondCoolantSource[idx].connectToVia(auxCondCoolantValve[idx].getFlowSource(),
                    auxCondCoolantSourceNode[idx]);
            auxCondCoolantValve[idx].getFlowSource().connectTo(
                    auxCondensers[idx].getHeatNode(
                            PhasedCondenser.SECONDARY_IN));
            auxCondCoolantSink[idx].connectTo(auxCondensers[idx].getHeatNode(
                    PhasedCondenser.SECONDARY_OUT));
            // Steam into condenser
            auxCondSteamValve[idx].getValveElement().connectBetween(
                    mainSteam[idx],
                    auxCondensers[idx].getPhasedNode(
                            PhasedCondenser.PRIMARY_IN));
            // Condeser out: Convert to heat fluid domain and then into reg valv
            auxCondOutConverter[idx].connectBetween(
                    auxCondensers[idx].getPhasedNode(
                            PhasedCondenser.PRIMARY_OUT),
                    auxCondCondenserOutNode[idx]);
            auxCondCondensateValve[idx].getValveElement().connectBetween(
                    auxCondCondenserOutNode[idx], auxCondCondInNode);
            // Also connect one of 2 pumps here, no separate loop for this.
            auxCondPumps[idx].getPumpEffortSource().connectTo(
                    auxCondCondInNode);
            // Connect to collector node:
            auxCondPumps[idx].getDischargeValve().connectTo(auxCondCollectorNode);
        }
        // Bypass parallel to those 2 pumps
        auxCondBypass.getValveElement().connectTo(auxCondCondInNode);
        auxCondBypass.getValveElement().connectTo(auxCondCollectorNode);
        // Todo: Hotwell connection (no hotwell yet)
        // Drain to cold condensate storage
        auxCondValveToDrain.getValveElement().connectBetween(
                auxCondCollectorNode, makeupStorageDrainCollector);

        // Steam Dump
        for (int idx = 0; idx < 2; idx++) {
            steamDump[idx].getValveElement().connectBetween(mainSteam[idx],
                    hotwell.getPhasedNode(PhasedCondenser.PRIMARY_IN));
        }

        // Condensation: Hotwell to Deaerators
        hotwellOutConverter.connectBetween(
                hotwell.getPhasedNode(PhasedCondenser.PRIMARY_OUT),
                hotwellOutNode);
        for (int idx = 0; idx < condensationPump.length; idx++) {
            condensationPump[idx].getSuctionValve().connectTo(hotwellOutNode);
            condensationPump[idx].getDischargeValve().connectTo(
                    condensationPumpOut);
        }
        condensationEjectorDummy.connectBetween(
                condensationPumpOut, condensationBoosterPumpIn);
        for (int idx = 0; idx < condensationBoosterPump.length; idx++) {
            condensationBoosterPump[idx].getSuctionValve().connectTo(
                    condensationBoosterPumpIn);
            condensationBoosterPump[idx].getDischargeValve().connectTo(
                    condensationBoosterPumpOut);
        }
        for (int idx = 0; idx < 2; idx++) {
            condensationValveToDA[idx].getValveElement().connectBetween(
                    condensationBoosterPumpOut,
                    condensationValveOut[idx]);
            condensationToDeaeratorConverter[idx].connectBetween(
                    condensationValveOut[idx], deaeratorInNode[idx]);
        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Set element properties">
        makeupStorage.setTimeConstant(100 / 9.81);
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
                loopAssembly[idx][jdx].initCharacteristic(2.5e6, 2.03e6, 1933.3);
                loopTrimValve[idx][jdx].initCharacteristic(5.51724, 80.0);
            }
            loopDownflow[idx].setResistanceParameter(12.6);
            loopDownflow[idx].setInnerThermalMass(100);
            loopChannelFlowResistance[idx].setInnerThermalMass(100);
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
        for (int idx = 0; idx < 2; idx++) {
            loopSteamDrum[idx].setBaseArea(40);
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
            blowdownFromLoop[idx].setResistanceParameter(2000);
            blowdownFromLoop[idx].setInnerThermalMass(400);
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
        // amounts of water against closed valves
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
        // Set the valves to have 30 kg/s with 10 bar of pressure (1e6 Pa)
        // so can will be R = 1e6 Pa / 30 kg/s = 3.3e4 Pa/kg*s for steam valve.
        // 30 bar is roughly saturation temperature of 500 Kelvin. Lets assume 
        // condensation tempearture of 310 Kelvin so this is 190 K temperature 
        // 190 K * 4200 J/kg/K + 2100000 J/kg = 2898000 J/kg = 2.9e6 J/kg
        // Assuming 30 kg/s per side that will be a total heat transfer of
        // 2.9e6 * 30 * J/kg*kg/s = 8.7e7 J/s (= 87 MW). Assuming the temp
        // diff between condensation and coolant will be 10 K or so we will
        // kA = 8.7e7 W / 10 K = 8.7e6 W/K (k times A is basically that).
        for (int idx = 0; idx < 2; idx++) {
            auxCondSteamValve[idx].initCharacteristic(2e4, 5);
            auxCondensers[idx].initCharacteristic(25, 200,
                    4000, 8.7e6, 4.0, 6.0, 1e5);
            // No ambient pressure for condensation, always steam pressure.
            auxCondensers[idx].getPrimarySideReservoir()
                    .setAmbientPressure(0.0);
            // we use flow source as valve for coolant flow
            auxCondCoolantValve[idx].initCharacteristic(600, 6);
        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Set Initial conditions">
        // Makeup storage has 2 meters fill level initially, quite low:
        makeupStorage.setInitialEffort(2.0 * 997 * 9.81); // p = h * rho * g
        for (int idx = 0; idx <= 1; idx++) {
            // See notes above, try to init with 0 cm fill level
            loopSteamDrum[idx].setInitialState(40000, 45 + 273.15);

            loopEvaporator[idx].setInitialState(6.0, 1e5,
                    273.5 + 45, 273.5 + 45); // 45 or 90
        }
        for (int idx = 0; idx <= 1; idx++) {
            loopDownflow[idx].getHeatHandler()
                    .setInitialTemperature(370); // was 314.63, then 370
            loopAssembly[idx][1].setInitialCondition(true, true, true);
            for (int jdx = 0; jdx < 4; jdx++) {
                // All trim valves open az 70 %
                loopTrimValve[idx][jdx].initOpening(70);
            }
            loopChannelFlowResistance[idx].getHeatHandler()
                    .setInitialTemperature(370); //also 314.63 or 370
        }
        for (int idx = 0; idx < 2; idx++) {
            blowdownFromLoop[idx].getHeatHandler().setInitialTemperature(
                    51.7 + 273.5);
            blowdownReturn[idx].getHeatHandler().setInitialTemperature(
                    38 + 273.5);
            blowdownReturnValve[idx].initOpening(80);
            blowdownValveFromLoop[idx].initOpening(95);
        }
        blowdownToRegeneratorFirstResistance.getHeatHandler()
                .setInitialTemperature(298.15);
        blowdownToRegeneratorSecondResistance.getHeatHandler()
                .setInitialTemperature(298.15);
        blowdownCooldown.getPrimarySide().getHeatHandler()
                .setInitialTemperature(304.93);
        blowdownCooldown.getSecondarySide().getHeatHandler()
                .setInitialTemperature(306.91);
        // Blowdown/Cooldown system is active and cooling the reactor
        blowdownCooldownPumps[1].setInitialCondition(true, true, true);
        blowdownValvePumpsToCooler.initOpening(100);
        blowdownValveTreatmentBypass.initOpening(100);
        blowdownValveCoolant.initFlow(800);
        for (int idx = 0; idx <= 1; idx++) {
            // try to have a fill level of 100 cm (normal level)
            deaerator[idx].setInitialState(40000, 35 + 273.15);
        }

        // Todo: Something that makes more sense here.
        for (int idx = 0; idx < 2; idx++) {
            auxCondensers[idx].initConditions(320, 320, 0.8);
        }
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
            runner.submit(makeupToDAPumps[idx]);
        }
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(mainSteamShutoffValve[idx]);
            for (int jdx = 0; jdx < 4; jdx++) {
                runner.submit(loopAssembly[idx][jdx]);
                runner.submit(loopTrimValve[idx][jdx]);
            }
            runner.submit(loopBypass[idx]);
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
        setpointDrumPressure.setLowerLimit(0.0);
        setpointDrumPressure.setUpperLimit(70.0);
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
        blowdownBalanceControlLoop.setMaxOutput(20);
        blowdownBalanceControlLoop.setMinOutput(-20);

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
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 1; jdx < 3; jdx++) {
                ((PIControl) feedwaterFlowRegulationValve[idx][jdx]
                        .getController()).setParameterK(20);
                ((PIControl) feedwaterFlowRegulationValve[idx][jdx]
                        .getController()).setParameterTN(20);
            }
        }
        for (int idx = 0; idx < 2; idx++) { // startup valves
            ((PIControl) feedwaterFlowRegulationValve[idx][0]
                    .getController()).setParameterK(20);
            ((PIControl) feedwaterFlowRegulationValve[idx][0]
                    .getController()).setParameterTN(5);
        }

        for (int idx = 0; idx < 2; idx++) {
            setpointDAPressure[idx].setLowerLimit(1.0);
            setpointDAPressure[idx].setUpperLimit(10.0);
            setpointDAPressure[idx].setMaxRate(1.0);
        }

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
        auxCondCondensateValve[0].getController().addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                // negative: open valve to decrease level.
                return auxCondensers[0].getPrimarySideReservoir()
                        .getFillHeight() * 100 // m to cm
                        - setpointAuxCondLevel[0].getOutput();
            }
        });
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Alarm Definitions">
        // Alarm monitors are defined here and stored in the alarmUpdater only,
        // there is no need to have a class field for them.
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
                for (int jdx = 0; jdx < 3; jdx++) {
                    core.triggerAutoShutdown();
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
                blowdownValveFromLoop[0].operateCloseValve();
            }
        });

        am.addAlarmAction(new AlarmAction(AlarmState.MIN2) {
            @Override
            public void run() {
                for (int jdx = 0; jdx < 4; jdx++) {
                    loopAssembly[0][jdx].operateStopPump();
                    loopAssembly[0][jdx].operateCloseDischargeValve();
                }
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
                for (int jdx = 0; jdx < 3; jdx++) {
                    core.triggerAutoShutdown();
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
                blowdownValveFromLoop[1].operateCloseValve();
            }
        });

        am.addAlarmAction(new AlarmAction(AlarmState.MIN2) {
            @Override
            public void run() {
                for (int jdx = 0; jdx < 4; jdx++) {
                    loopAssembly[1][jdx].operateStopPump();
                    loopAssembly[1][jdx].operateCloseDischargeValve();
                }
            }
        });

        am.registerAlarmManager(alarmManager);
        alarmUpdater.submit(am);
        // </editor-fold>
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
        for (int idx = 0; idx < 2; idx++) {
            fuelThermalSource[idx].setFlow(thermalPower[idx] * 1e6);
        }

        // Write Control Outputs to model if necessary (some controllers are
        // already integrated into the elements)
        if (!blowdownBalanceControlLoop.isManualMode()) {
            blowdownReturnValve[0].operateSetOpening(80
                    + blowdownBalanceControlLoop.getOutput());
            blowdownReturnValve[1].operateSetOpening(80
                    - blowdownBalanceControlLoop.getOutput());
        }

        // Reset and solve (update) the whole thermal layout one cycle.
        solver.prepareCalculation();
        solver.doCalculation();

        // Generate core temperature value (avg deg celsius value)
        coreTemp = (fuelThermalOut[0].getEffort()
                + fuelThermalOut[1].getEffort()) / 2 - 273.5;

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

        // Update Alarms
        alarmUpdater.invokeAll();

        // <editor-fold defaultstate="collapsed" desc="Gain measurement data and set it to parameter out handler">
        outputValues.setParameterValue("MakeupStorageLevel",
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
                    mainSteamShutoffValve[idx].getValveElement().getFlow());

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
                            feedwaterFlowRegulationValve[idx][0].getValveElement())
                    - loopFeedwaterIn[idx].getFlow(
                            feedwaterFlowRegulationValve[idx][1].getValveElement())
                    - loopFeedwaterIn[idx].getFlow(
                            feedwaterFlowRegulationValve[idx][2].getValveElement()));

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
            outputValues.setParameterValue("Deaerator" + (idx + 1) + "#FeedFlow",
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
        // </editor-fold>

        // Save values to plot manager
        if (plotUpdateCount == 0) {
            for (int idx = 0; idx < 2; idx++) {
                // use relative fill height in cm
                plotData.insertValue("Loop" + (idx + 1) + "#DrumLevel",
                        (float) (loopSteamDrum[idx].getFillHeight() - 1.15) * 100);
                plotData.insertValue("Loop" + (idx + 1) + "#DrumPressure",
                        (float) (loopNodeDrumFromReactor[idx].getEffort() / 100000 - 1.0));
                plotData.insertValue("Loop" + (idx + 1)
                        + "#DrumTemperature",
                        (float) (loopSteamDrum[idx].getTemperature() - 273.5));
            }
            // Feedwater control loops
            for (int idx = 0; idx < 2; idx++) {
                plotData.insertValue("Loop" + (idx + 1) + "#DrumLevelSetpoint",
                        (float) setpointDrumLevel[idx].getOutput());
                for (int jdx = 0; jdx < 3; jdx++) {
                    plotData.insertValue("Feedwater" + (idx + 1) + "#FlowRegulationValve" + (jdx + 1),
                            (float) feedwaterFlowRegulationValve[idx][jdx].getOpening());
                }
            }

            plotUpdateCount = plotData.getCountDiv() - 1;
        } else {
            plotUpdateCount--;
        }
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
                if (blowdownValveFromLoop[idx].handleAction(ac)) {
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
                    } else if (ac.getValue().equals(ControlCommand.MANUAL_OPERATION)) {
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
        } else {
            // Main Steam shutoff valve commands from GUI
            switch (ac.getPropertyName()) {
                case "Main1#SteamShutoffValve" ->
                    mainSteamShutoffValve[0].handleAction(ac);
                case "Main2#SteamShutoffValve" ->
                    mainSteamShutoffValve[1].handleAction(ac);
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
        // Add the 48 MW idle (24 per side) power here
        thermalPower[loop] = power + 24;
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
    
    public void registerReactor(ReactorCore core) {
        this.core = core;
    }
}
