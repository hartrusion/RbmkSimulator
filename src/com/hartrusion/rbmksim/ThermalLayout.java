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

import com.hartrusion.control.AbstractController;
import com.hartrusion.control.ControlCommand;
import com.hartrusion.control.FloatSeriesVault;
import com.hartrusion.control.PControl;
import com.hartrusion.control.PIControl;
import com.hartrusion.control.ParameterHandler;
import com.hartrusion.control.SerialRunner;
import com.hartrusion.control.Setpoint;
import com.hartrusion.modeling.PhysicalDomain;
import com.hartrusion.modeling.assemblies.HeatControlledFlowSource;
import com.hartrusion.modeling.assemblies.HeatExchanger;
import com.hartrusion.modeling.assemblies.HeatFluidPump;
import com.hartrusion.modeling.assemblies.HeatFluidPumpRealSwitching;
import com.hartrusion.modeling.assemblies.HeatValve;
import com.hartrusion.modeling.assemblies.HeatValveControlled;
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
import com.hartrusion.modeling.solvers.DomainAnalogySolver;
import com.hartrusion.mvc.ActionCommand;
import com.hartrusion.mvc.ModelListener;
import com.hartrusion.mvc.ModelManipulation;
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
public class ThermalLayout implements Runnable, ModelManipulation {

    // private final If97Wrapper steamTable = new If97Wrapper();
    private final PhasedPropertiesWater phasedWater
            = new PhasedPropertiesWater();

    // <editor-fold defaultstate="collapsed" desc="Model elements declaration and array instantiation">
    private final HeatFluidTank makeupStorage;
    private final HeatNode makeupStorageNode;

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
    private final HeatFluidPumpRealSwitching[][] loopAssembly
            = new HeatFluidPumpRealSwitching[2][4];
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
    private final HeatControlledFlowSource blowdownCoolantFlow;
    private final HeatOrigin blowdownCoolantSink;
    // water treatment is just a large volume for now
    private final HeatVolumizedFlowResistance blowdownTreatment;
    private final HeatNode blowdownTreatedOutNode;
    private final HeatValve blowdownValveRegeneratedToDrums;
    private final HeatValve blowdownValveDrain;
    private final HeatNode blowdownOutNode;
    private final HeatValve[] blowdownReturnValve = new HeatValve[2];

    // Deaerators
    private final PhasedClosedSteamedReservoir[] deaerator
            = new PhasedClosedSteamedReservoir[2];
    private final PhasedNode[] deaeratorOutNode = new PhasedNode[2];
    private final PhasedNode[] deaeratorInNode = new PhasedNode[2];
    private final PhasedHeatFluidConverter[] deaeratorToFeedwaterConverter
            = new PhasedHeatFluidConverter[2];
    private final HeatNode[] deaeratorOutHeatNode = new HeatNode[2];
    private final PhasedValveControlled[] mainSteamToDAValve
            = new PhasedValveControlled[2];

    // Feedwater pumps system
    // Two feedwater pumps per side
    private final HeatFluidPumpRealSwitching[][] feedwaterPump
            = new HeatFluidPumpRealSwitching[2][2];
    // the spare pump can be used on both sides so it has some valves allowing 
    // to connect it to both sides.
    private final HeatValve[] feedwaterSparePumpInValve = new HeatValve[2];
    private final HeatNode feedwaterSparePumpIn;
    private final HeatFluidPumpRealSwitching feedwaterPump3;
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
    // </editor-fold>

    private final Setpoint[] setpointDrumLevel = new Setpoint[2];
    private final Setpoint[] setpointDAPressure = new Setpoint[2];
    private final Setpoint[] setpointDALevel = new Setpoint[2];

    private final DomainAnalogySolver solver = new DomainAnalogySolver();
    private final SerialRunner runner = new SerialRunner();

    private final AbstractController blowdownBalanceControlLoop
            = new PControl();

    private ModelListener controller;

    /**
     * Stores all output values as string value pair. Values from model will be
     * written into this handler.
     */
    private ParameterHandler outputValues;

    private FloatSeriesVault plotData;

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
        makeupStorageNode = new HeatNode();
        makeupStorageNode.setName("MakeupStorageNode");

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
                loopAssembly[idx][jdx] = new HeatFluidPumpRealSwitching();
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
        blowdownCoolantFlow = new HeatControlledFlowSource();
        blowdownCoolantFlow.initName("Blowdown#CoolantFlow");
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
            blowdownReturnValve[idx].initName("Blowdown#ReturnValve" + (idx + 1));
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
            deaeratorOutHeatNode[idx] = new HeatNode();
            deaeratorOutHeatNode[idx].setName(
                    "Deaerator" + (idx + 1) + "#OutHeatNode");
            mainSteamToDAValve[idx] = new PhasedValveControlled();
            mainSteamToDAValve[idx].initController(new PIControl());
            mainSteamToDAValve[idx].initName(
                    "Main" + (idx + 1) + "#SteamToDAValve");
        }

        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 2; jdx++) {
                feedwaterPump[idx][jdx] = new HeatFluidPumpRealSwitching();
                feedwaterPump[idx][jdx].initName(
                        "Feedwater" + (idx + 1) + "#Pump" + (jdx + 1));
            }
            feedwaterSparePumpInValve[idx] = new HeatValve();
            feedwaterSparePumpInValve[idx].initName(
                    "Feedwater" + (idx + 1) + "#SparePumpInValve");
        }
        feedwaterSparePumpIn = new HeatNode();
        feedwaterSparePumpIn.setName("Feedwater#SparePumpIn");
        feedwaterPump3 = new HeatFluidPumpRealSwitching();
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
                feedwaterFlowRegulationValve[idx][jdx].initController(
                        new PIControl());
                feedwaterFlowRegulationValve[idx][jdx]
                        .initName("Feedwater" + (idx + 1)
                                + "#FlowRegulationValve" + (jdx + 1));
            }
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
        }
    }

    public void init() {
        // Attach controller to monitor elements. Those monitor elements are
        // part of assemblies and send events to the controller, like a valve
        // open state reached.
        for (int idx = 0; idx < 2; idx++) {
            mainSteamShutoffValve[idx].initSignalListener(controller);
            mainSteamToDAValve[idx].initSignalListener(controller);
            mainSteamToDAValve[idx].initParameterHandler(outputValues);
            for (int jdx = 0; jdx < 4; jdx++) {
                loopAssembly[idx][jdx].initSignalListener(controller);
            }
            loopBypass[idx].initSignalListener(controller);
            blowdownValveFromLoop[idx].initSignalListener(controller);
            blowdownCooldownPumps[idx].initSignalListener(controller);
        }
        blowdownValvePassiveFlow.initSignalListener(controller);
        blowdownValvePumpsToRegenerator.initSignalListener(controller);
        blowdownValvePumpsToCooler.initSignalListener(controller);
        blowdownValveRegeneratorToCooler.initSignalListener(controller);
        blowdownValveTreatmentBypass.initSignalListener(controller);
        blowdownValveRegeneratedToDrums.initSignalListener(controller);
        blowdownValveDrain.initSignalListener(controller);
        blowdownCoolantFlow.initSignalListener(controller);
        for (int idx = 0; idx < 2; idx++) {
            blowdownReturnValve[idx].initSignalListener(controller);
        }
        blowdownBalanceControlLoop.addPropertyChangeListener(controller);
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 2; jdx++) {
                feedwaterPump[idx][jdx].initSignalListener(controller);
            }
            feedwaterSparePumpInValve[idx].initSignalListener(controller);
        }
        feedwaterPump3.initSignalListener(controller);
        for (int idx = 0; idx < 2; idx++) {
            feedwaterSparePumpOutValve[idx].initSignalListener(controller);
            feedwaterStartupReductionValve[idx].initSignalListener(controller);
            for (int jdx = 0; jdx < 3; jdx++) {
                feedwaterShutoffValve[idx][jdx].initSignalListener(controller);
                feedwaterFlowRegulationValve[idx][jdx]
                        .initSignalListener(controller);
                feedwaterFlowRegulationValve[idx][jdx]
                        .initParameterHandler(outputValues);
            }
        }

        // Attach Signal Listeners or Handlers to Control elements
        for (int idx = 0; idx < 2; idx++) {
            setpointDrumLevel[idx].initParameterHandler(outputValues);
            setpointDAPressure[idx].initParameterHandler(outputValues);
            setpointDALevel[idx].initParameterHandler(outputValues);
        }

        // <editor-fold defaultstate="collapsed" desc="Describe dynamic model">
        // Define the primary loop from drum through mcps and reactor and back.
        makeupStorage.connectTo(makeupStorageNode);
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
            mainSteamShutoffValve[idx].getValveElement().connectBetween(
                    mainSteamDrumNode[idx], mainSteam[idx]);
        }

        // Build the blowdown system: There's a trim valve connected to each
        // distributor (thats the part that distributes coolant to fuel 
        // channels), connected to a common node.
        for (int idx = 0; idx < 2; idx++) {
            blowdownValveFromLoop[idx].getValveElement().connectBetween(
                    loopDistributor[idx], blowdownInCollectorNode);
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
                blowdownTreatedOutNode, makeupStorageNode);
        // Secondary coolant flow, just a flow source for forcing the flow.
        blowdownCoolantSource.connectTo(blowdownCoolantSourceNode);
        blowdownCoolantFlow.getFlowSource()
                .connectBetween(blowdownCoolantSourceNode,
                        blowdownCooldown.getSecondarySide().getNode(1));
        blowdownCoolantSink.connectTo(
                blowdownCooldown.getSecondarySide().getNode(0));
        // Connect blowdown out valves to the feedwater collector
        for (int idx = 0; idx < 2; idx++) {
            blowdownReturnValve[idx].getValveElement().connectTo(
                    blowdownOutNode);
            blowdownReturnValve[idx].getValveElement().connectTo(
                    loopFeedwaterIn[idx]);
        }

        // Deaerators
        for (int idx = 0; idx < 2; idx++) {
            // DA has two nodes for in and outflow. The Steam for heating it up
            // will simply be added to the inNode.
            deaerator[idx].connectTo(deaeratorOutNode[idx]);
            deaerator[idx].connectTo(deaeratorInNode[idx]);
            // Heat fluid will leave DA, place a conveter after
            deaeratorToFeedwaterConverter[idx].connectBetween(
                    deaeratorOutNode[idx],
                    deaeratorOutHeatNode[idx]);
            mainSteamToDAValve[idx].getValveElement().connectBetween(
                    mainSteam[idx], deaeratorInNode[idx]);
        }
        // Feedwater Pumps
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 2; jdx++) {
                // 2 Pumps on both sides
                feedwaterPump[idx][jdx].getSuctionValve().connectTo(
                        deaeratorOutHeatNode[idx]);
                feedwaterPump[idx][jdx].getDischargeValve().connectTo(
                        feedwaterPumpCollectorNodes[idx]);
            }
            // Valve from both DAs to spare pump in
            feedwaterSparePumpInValve[idx].getValveElement().connectBetween(
                    deaeratorOutHeatNode[idx], feedwaterSparePumpIn);
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
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Set element properties">
        makeupStorage.setTimeConstant(100 / 9.81);
        // The main steam shutoff valve can be seen more as a cheat to keep the
        // model stable, it will be operated automatically. Randomly setting it 
        // to 200 for a little pressure loss.
        for (int idx = 0; idx < 2; idx++) {
            mainSteamShutoffValve[idx].initCharacteristic(200, -1.0);
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

        // Steam Drum: Use behaviour of RXModel simulator here. Experiments show
        // that there is approximately 1 ton or 1 m^3 water in ech drum per cm
        // level. -18 cm will trip the MCPs so we assume that -20 cm will be an 
        // empty drum. The RXModel uses 0 cm as nominal level, and we will just
        // do the same here.
        for (int idx = 0; idx < 2; idx++) {
            loopSteamDrum[idx].setBaseArea(100);
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
            blowdownValveFromLoop[idx].initCharacteristic(400, 20.0);
            blowdownReturnValve[idx].initCharacteristic(400, 20.0);
            blowdownCooldownPumps[idx].initCharacteristic(16e5, 12e5, 900);
        }
        blowdownValvePassiveFlow.initCharacteristic(3000, -1.0);
        blowdownValvePumpsToRegenerator.initCharacteristic(500, -1.0);
        blowdownValvePumpsToCooler.initCharacteristic(500, -1.0);
        blowdownValveRegeneratorToCooler.initCharacteristic(500, -1.0);
        blowdownCooldownResistance.setResistanceParameter(2000);
        blowdownValveTreatmentBypass.initCharacteristic(500, -1.0);
        blowdownValveRegeneratedToDrums.initCharacteristic(500, -1.0);
        blowdownValveDrain.initCharacteristic(400, 20.0);
        blowdownToRegeneratorFirstResistance.setResistanceParameter(1000);
        blowdownToRegeneratorSecondResistance.setResistanceParameter(1000);
        blowdownCooldown.initCharacteristic(3000, 1500, 7e6);

        for (int idx = 0; idx < 2; idx++) {
            // RXmodel has a base area of 40, use this value here
            deaerator[idx].setBaseArea(40);
        }

        // Feedwater
        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 2; jdx++) {
                // Todo: Proper pump characteristic
                feedwaterPump[idx][jdx].initCharacteristic(16e5, 12e5, 900);
            }
        }
        feedwaterPump3.initCharacteristic(30e5, 22e5, 900);
        // Todo: Feedwater valves characteristic
        for (int idx = 0; idx < 2; idx++) {
            feedwaterSparePumpInValve[idx].initCharacteristic(20, -1.0);
            feedwaterSparePumpOutValve[idx].initCharacteristic(20, -1.0);
            feedwaterStartupReductionValve[idx].initCharacteristic(500, -1.0);
            for (int jdx = 0; jdx < 3; jdx++) {
                feedwaterShutoffValve[idx][jdx].initCharacteristic(500, -1.0);
                feedwaterFlowRegulationValve[idx][jdx].initCharacteristic(500, -1.0);
            }
        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Set Initial conditions">
        // Makeup storage has 2 meters fill level initially, quite low:
        makeupStorage.setInitialEffort(2.0 * 997 * 9.81); // p = h * rho * g
        for (int idx = 0; idx <= 1; idx++) {
            // See notes above, try to init with 0 cm fill level
            loopSteamDrum[idx].setInitialState(10000, 45 + 273.15);

            loopEvaporator[idx].setInitialState(6.0, 1e5,
                    273.5 + 45, 273.5 + 45);
        }
        for (int idx = 0; idx <= 1; idx++) {
            loopDownflow[idx].getHeatHandler()
                    .setInitialTemperature(370); // was 314.63
            loopAssembly[idx][1].setInitialCondition(true, true, true);
            for (int jdx = 0; jdx < 4; jdx++) {
                // All trim valves open az 70 %
                loopTrimValve[idx][jdx].initOpening(70);
            }
            loopChannelFlowResistance[idx].getHeatHandler()
                    .setInitialTemperature(370); //also 314.63
        }
        for (int idx = 0; idx < 2; idx++) {
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
        blowdownCoolantFlow.initFlow(800);
        for (int idx = 0; idx <= 1; idx++) {
            // try to have a fill level of 100 cm (normal level)
            deaerator[idx].setInitialState(40000, 35 + 273.15);
        }
        // </editor-fold>

        // Initialize solver and build model. This is only a small line of code,
        // but it triggers a huge step of building up all the network and
        // calculation of the thermal layout.
        solver.addNetwork(blowdownOutNode);

        // Add assemblies to runner instance, this way they get their run 
        // method called each cycle (this sets valve movements, fires events
        // and so on).
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
        runner.submit(blowdownCoolantFlow);
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(blowdownReturnValve[idx]);
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
            runner.submit(mainSteamToDAValve[idx]);
        }

        // Add Solo control loops
        runner.submit(blowdownBalanceControlLoop);

        // Add setpoint instances
        for (int idx = 0; idx < 2; idx++) {
            runner.submit(setpointDrumLevel[idx]);
            runner.submit(setpointDAPressure[idx]);
            runner.submit(setpointDALevel[idx]);
        }

        // Control Loop configuration
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

        blowdownBalanceControlLoop.addInputProvider(new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                // Try to keep differences between drum levels equal.
                // Setpoint is cm, level is m
                return (setpointDrumLevel[0].getOutput() * 0.5
                        - loopSteamDrum[0].getFillHeight() * 100)
                        - (setpointDrumLevel[1].getOutput() * 0.5
                        - loopSteamDrum[1].getFillHeight() * 100);
            }
        });
        ((PControl) blowdownBalanceControlLoop).setParameterK(20);
        blowdownBalanceControlLoop.setMaxOutput(20);
        blowdownBalanceControlLoop.setMinOutput(-20);

        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 3; jdx++) {
                ((PIControl) feedwaterFlowRegulationValve[idx][jdx]
                        .getController()).setParameterK(10);
                ((PIControl) feedwaterFlowRegulationValve[idx][jdx]
                        .getController()).setParameterTN(5);
            }
        }

        for (int jdx = 0; jdx < 3; jdx++) {
            feedwaterFlowRegulationValve[0][jdx].getController()
                    .addInputProvider(new DoubleSupplier() {
                        @Override
                        public double getAsDouble() {
                            return setpointDrumLevel[0].getOutput()
                                    - loopSteamDrum[0].getFillHeight();
                        }
                    });
            feedwaterFlowRegulationValve[1][jdx].getController()
                    .addInputProvider(new DoubleSupplier() {
                        @Override
                        public double getAsDouble() {
                            return setpointDrumLevel[1].getOutput()
                                    - loopSteamDrum[1].getFillHeight();
                        }
                    });
        }

        for (int idx = 0; idx < 2; idx++) {
            for (int jdx = 0; jdx < 3; jdx++) {
                ((PIControl) feedwaterFlowRegulationValve[idx][jdx]
                        .getController()).setParameterK(10);
                ((PIControl) feedwaterFlowRegulationValve[idx][jdx]
                        .getController()).setParameterTN(5);
            }
        }

        for (int idx = 0; idx < 2; idx++) {
            setpointDAPressure[idx].setLowerLimit(1.0);
            setpointDAPressure[idx].setUpperLimit(10.0);
            setpointDAPressure[idx].setMaxRate(1.0);
        }

        mainSteamToDAValve[0].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return setpointDAPressure[0].getOutput() // this is bar
                        - deaerator[0].getEffort() * 1e-5 + 1.0; // as rel. bar
            }
        });
        mainSteamToDAValve[1].getController().addInputProvider(
                new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return setpointDAPressure[1].getOutput() // this is bar
                        - deaerator[1].getEffort() * 1e-5 + 1.0; // as rel. bar
            }
        });

        for (int idx = 0; idx < 2; idx++) {
            ((PIControl) mainSteamToDAValve[idx].getController())
                    .setParameterK(1.0);
            ((PIControl) mainSteamToDAValve[idx].getController())
                    .setParameterTN(20);
        }
    }

    @Override
    public void run() {
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
                + fuelThermalOut[0].getEffort()) / 2 - 273.5;

        // Generate thermal lift for next cycle
        for (int idx = 0; idx < 2; idx++) {
            if (loopBypass[idx].getOpening() > 1.0) {
                loopThermalLift[idx].setEffort(
                        (loopEvaporator[idx].getTemperature()
                        - loopDownflow[idx].getHeatHandler().getTemperature())
                        * 1000); // its a coincidentce that this is 1000
            }
        }

        // <editor-fold defaultstate="collapsed" desc="Gain measurement data and set it to parameter out handler">
        outputValues.setParameterValue("MakeupStorageLevel",
                makeupStorage.getEffort() * 1.0224e-4); // Pa in meters
        for (int idx = 0; idx < 2; idx++) {
            // -20 cm = 0 kg, 0 cm = 10.000 kg - as with RxModel
            outputValues.setParameterValue("Loop" + (idx + 1) + "#DrumLevel",
                    loopSteamDrum[idx].getStoredMass() * 2e-3 - 20);
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
                            blowdownReturnValve[idx].getValveElement()));

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
                blowdownCoolantFlow.getFlowSource().getFlow());

        for (int idx = 0; idx < 2; idx++) {
            outputValues.setParameterValue("Blowdown#ValveFromLoop" + (idx + 1),
                    blowdownValveFromLoop[idx].getValveElement().getOpening());
            outputValues.setParameterValue("Blowdown#ReturnValve" + (idx + 1),
                    blowdownReturnValve[idx].getValveElement().getOpening());
        }
        for (int idx = 0; idx < 2; idx++) {
            outputValues.setParameterValue(
                    "Feedwater" + (idx + 1) + "#Temperature",
                    feedwaterPumpCollectorNodes[idx].getTemperature() - 273.5);
            outputValues.setParameterValue(
                    "Feedwater" + (idx + 1) + "#Pressure",
                    feedwaterPumpCollectorNodes[idx]
                            .getEffort() / 100000 - 1.0);
        }

        // </editor-fold>
    }

    @Override
    public void updateNotification(String propertyName) {

    }

    @Override
    public void handleAction(ActionCommand ac) {
        // <editor-fold defaultstate="collapsed" desc="Receive and process control commands from controller (GUI)">
        if (ac.getPropertyName().startsWith("Loop")) {
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
                    switch ((int) ac.getValue()) {
                        case -1 ->
                            blowdownCoolantFlow.setToMinFlow();
                        case +1 ->
                            blowdownCoolantFlow.setToMaxFlow();
                        default ->
                            blowdownCoolantFlow.setStopAtCurrentFlow();
                    }
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
        } else {
            // Main Steam shutoff valve commands from GUI
            switch (ac.getPropertyName()) {
                case "Main1#SteamShutoffValve" ->
                    mainSteamShutoffValve[0].handleAction(ac);
                case "Main2#SteamShutoffValve" ->
                    mainSteamShutoffValve[1].handleAction(ac);
                case "Deaerator1#PressureSetpoint" ->
                    setpointDAPressure[0].handleAction(ac);
                case "Deaerator2#PressureSetpoint" ->
                    setpointDAPressure[1].handleAction(ac);
                case "Deaerator1#LevelSetpoint" ->
                    setpointDALevel[0].handleAction(ac);
                case "Deaerator2#LevelSetpoint" ->
                    setpointDALevel[1].handleAction(ac);
            }
            if (ac.getPropertyName().startsWith("Main1#SteamToDAValve")) {
                mainSteamToDAValve[0].handleAction(ac);
            } else if (ac.getPropertyName().startsWith("Main2#SteamToDAValve")) {
                mainSteamToDAValve[1].handleAction(ac);
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
    public void setThermalPower(int loop, double power) {
        thermalPower[loop] = power;
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

    @Override
    public void registerController(ModelListener controller) {
        this.controller = controller;
    }

    public void registerParameterOutput(ParameterHandler output) {
        this.outputValues = output;
    }

    public void registerPlotDataVault(FloatSeriesVault plotData) {
        this.plotData = plotData;
    }
}
