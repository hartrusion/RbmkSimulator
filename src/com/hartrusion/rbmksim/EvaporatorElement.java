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

import com.hartrusion.modeling.PhysicalDomain;
import com.hartrusion.modeling.converters.PhasedHeatFluidConverter;
import com.hartrusion.modeling.general.GeneralNode;
import com.hartrusion.modeling.heatfluid.HeatEffortSource;
import com.hartrusion.modeling.heatfluid.HeatFrictionedFlowResistance;
import com.hartrusion.modeling.heatfluid.HeatNode;
import com.hartrusion.modeling.heatfluid.HeatSimpleFlowResistance;
import com.hartrusion.modeling.heatfluid.HeatVolumizedFlowResistance;
import com.hartrusion.modeling.phasedfluid.PhasedClosedSteamedReservoir;
import com.hartrusion.modeling.phasedfluid.PhasedExpandingThermalExchanger;
import com.hartrusion.modeling.phasedfluid.PhasedExpandingThermalVolumeHandler;
import com.hartrusion.modeling.phasedfluid.PhasedNode;
import com.hartrusion.modeling.phasedfluid.Water;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends the fuel element to an element that supports the water steam part
 * with the evaporator capabilities. It is still a fuel element with the same
 * thermal model as all the other fuel elements but additionally holds the water
 * steam part which is exclusive to those elements.
 * <p>
 * Some notes on how this was developed: First, only two evaporators were used
 * but this had the downside that no accurate solution for thermal power or
 * critical power ratio was available per fuel channel. Also, having a leak, the
 * full evaporator on one side would need to operate in reverse flow mode which
 * has huge calculation issues. So the full core was simulated with 376
 * evaporator channels and 376 possible leaks. This hit the limit of 70 ms
 * calculation time and was too complex, it did not bring the wanted benefit for
 * the simulation. This step now reduces the number of evaporators to 48,
 * allowing the leak situation to be unnoted by 1 of 48 evaporators running in
 * reverse mode.
 *
 * @author Viktor Alexander Hartung
 */
public class EvaporatorElement extends FuelElement {

    private double flow;

    private double criticalPowerRatio;

    private double thermalLiftPressure;

    /**
     * Steam void, saved as variable to have only one call on the calculation of
     * the voiding (it is a bit expensive). Will be updated after the thermal
     * layout is calculated by the runProcessResults call.
     */
    private double voiding;

    // Network part for the hydraulic part
    private final HeatFrictionedFlowResistance flowResistance
            = new HeatFrictionedFlowResistance();
    private final HeatNode afterResistance = new HeatNode();
    private final HeatVolumizedFlowResistance channelMass
            = new HeatVolumizedFlowResistance();
    private final HeatNode afterChannelMass = new HeatNode();
    private final HeatEffortSource thermalLift = new HeatEffortSource();
    private final HeatNode afterThermalLift = new HeatNode();
    private final PhasedHeatFluidConverter toReactorConverter;
    private final PhasedHeatFluidConverter toPoolConverter;
    private final PhasedNode evaporatorIn = new PhasedNode();
    private final PhasedExpandingThermalExchanger evaporator;
    private final PhasedNode evapToDrumNode = new PhasedNode();

    // Node connected to the evaporators 
    private final GeneralNode thermalInNode = new GeneralNode(PhysicalDomain.THERMAL);

    private final HeatSimpleFlowResistance channelLeak
            = new HeatSimpleFlowResistance();
    private final PhasedNode channelLeakNode = new PhasedNode();
    private final HeatEffortSource channelLeakGravity
            = new HeatEffortSource();
    private final HeatNode leakOut = new HeatNode();
    private final HeatNode leakOutGrav = new HeatNode();

    private final String propertyFlow;
    private final String propertyVoiding;
    private final String propertyCpr;

    /**
     * Reference to the array that holds the temperature of the downcomers which
     * is used to generate the thermal lift value.
     */
    private double[] downcomerTemperature;

    /**
     * The calculation element used in the evaporator element.
     */
    private PhasedExpandingThermalVolumeHandler evapHandler;

    /**
     * Holds all fuel element (including this one) objects which are connected
     * to this evaporator element.
     */
    private final List<FuelElement> fuelElements = new ArrayList<>();

    /**
     * Reference to the steam drum where this evaporator is connected to
     */
    private PhasedClosedSteamedReservoir steamDrum;

    public EvaporatorElement(int x, int y) {
        super(x, y);

        propertyFlow = "Fuel" + (100 * x + y) + "#Flow";
        propertyVoiding = "Fuel" + (100 * x + y) + "#Voiding";

        propertyCpr = "Fuel" + x + "-" + y + "#CriticalPowerRatio";

        // Generate instances
        toReactorConverter = new PhasedHeatFluidConverter(Water.INSTANCE);
        toPoolConverter = new PhasedHeatFluidConverter(Water.INSTANCE);
        evaporator = new PhasedExpandingThermalExchanger(Water.INSTANCE);

        // Naming
        flowResistance.setName("FuelChannelHydraulic" + x + "-" + y + "#FlowResistance");
        afterResistance.setName("FuelChannelHydraulic" + x + "-" + y + "#AfterResistance");
        channelMass.setName("FuelChannelHydraulic" + x + "-" + y + "#ChannelMass");
        afterChannelMass.setName("FuelChannelHydraulic" + x + "-" + y + "#AfterChannelMass");
        thermalLift.setName("FuelChannelHydraulic" + x + "-" + y + "#ThermalLift");
        afterThermalLift.setName("FuelChannelHydraulic" + x + "-" + y + "#AfterThermalLift");
        toReactorConverter.setName("FuelChannelHydraulic" + x + "-" + y + "#ToReactorConverter");
        toPoolConverter.setName("FuelChannelHydraulic" + x + "-" + y + "#ToPoolConverter");
        evaporatorIn.setName("FuelChannelHydraulic" + x + "-" + y + "#EvaporatorIn");
        evaporator.setName("FuelChannelHydraulic" + x + "-" + y + "#Evaporator");

        thermalInNode.setName("FuelChannelHydraulic" + x + "-" + y + "#InNode");

        connectHydraulicModel();

        // Provide a connection to a node where all the fuel elements will
        // connect their heat transfer resistance to.
        evaporator.getInnerThermalResistanceElement().connectTo(thermalInNode);

        evapHandler = (PhasedExpandingThermalVolumeHandler) evaporator.getPhasedHandler();

        thermalLift.setEffort(5e4);
    }

    /**
     * Connects the hydraulic elements and nodes and sets some properties of the
     * hydraulic part.
     *
     * <pre>
     *     |
     *    | |
     *    | |  evaporator
     *    | |
     *     |
     *     o evaporatorIn (PhasedFluid)
     *     |
     *    [ ] toReactorConverter
     *     |
     *     o afterThermalLift (HeatFluid)
     *     |
     *    (|) thermalLift
     *     |
     *     o
     *     |
     *    | | channelMass - HeatVolumizedFlowResistance
     *     |
     * aft | channelLeak  gravity  toPoolConverter
     * res o----___----o---(-)--o---[ ]----
     *     |         leakOut  leakOutGrav
     *     |
     *    | | flowResistance - HeatFrictionedFlowResistance
     *     |
     * </pre>
     */
    private void connectHydraulicModel() {
        // Connections of the hydraulic part
        flowResistance.connectTo(afterResistance);
        channelMass.connectBetween(afterResistance, afterChannelMass);
        thermalLift.connectBetween(afterChannelMass, afterThermalLift);
        toReactorConverter.connectBetween(afterThermalLift, evaporatorIn);
        evaporator.initComponent();
        evaporator.connectTo(evaporatorIn);
        channelLeak.connectBetween(afterResistance, leakOut);
        channelLeakGravity.connectBetween(leakOut, leakOutGrav);
        toPoolConverter.connectTo(leakOutGrav);
    }

    /**
     * Called from the thermal layout during the network setup process, requires
     * this fuel element to be initialized already. It connects the elements to
     * the thermal network.
     *
     * @param distributorNode Flow in node from MCPs
     * @param steamDrum Steam drum where this evaporator is connected to
     * @param poolNode A node to the bubbler pool for leakage
     */
    public void connectTo(HeatNode distributorNode,
            PhasedClosedSteamedReservoir steamDrum, PhasedNode poolNode) {
        this.steamDrum = steamDrum;
        flowResistance.connectTo(distributorNode);
        evaporator.connectToVia(steamDrum, evapToDrumNode);
        toPoolConverter.connectTo(poolNode);
    }

    public void setDowncomerTemperatureReference(double[] temperature) {
        downcomerTemperature = temperature;
    }

    public GeneralNode getEvapInNode() {
        return thermalInNode;
    }

    /**
     * Makes all fuel elements which are considered by this evaporator known to
     * this instance. Including this one (parent class).
     *
     * @param fe FuelElement to add to the list
     */
    public void addFuelElementToEvaporator(FuelElement fe) {
        fuelElements.add(fe);
    }

    /**
     * Has to be called during initialization after it is known how many
     * FuelElements are assigned to this evaporator, it calculates and sets the
     * properties accordingly.
     */
    public void initEvaporator() {
        double sizeFactor = (double) fuelElements.size();

        // There is 188 Channels per side. The total resistance for one loop
        // is 293.1 so per Channel it will be 55102.8.
        // This has to be split onto two elements to have the nodal analysis
        // work with norton transform. The split between 10000 and 45102.8 is 
        // designed in a way we get a decent flow out on channel rupture on 
        // idle as well as in full load conditions.
        flowResistance.setResistanceParameter(45102.8 / sizeFactor);
        channelMass.setInnerThermalMass(100 * sizeFactor);
        channelMass.setResistanceParameter(10000 / sizeFactor);
        // Manipulate the specific heat capacity here to make the heatup 
        // from the MCP circulation much more intense. Default is 4200, the
        // heat increase is delta_p / (density * specHeatCap)
        flowResistance.setFrictionHeatupParameters(1000, 2000);

        // Side note here: The temperature of the fuel in the previous two-
        // evaporator model was 38 °C when starting the sim, with a very low
        // flow of about 25 kg/s through one side when blowdown is shut. The 
        // flow was driven by the temperature diff between downcomer and evap
        // element.
        // 20 m³ volume in evaporator per side is way too slow for 
        // mcp loss accident.        
        // For loss of circulation: The evaporator will slowly start to boil
        // and loose its mass. It should met at 2800 °C (3073 K) and we just
        // randomly define 4000 K and 50 MW when running empty, so it is
        // G = P_th / DeltaT = 50e6 J/s / 4000 K = 1.25e4 when almost empty.
        // evaporator.setThermalDimension(14.0, 200, 
        //        5.5e6, 10000,
        //        1.0e4, 4000);
        // =====================================================================
        // New calculation with 188 fuel channels per side:
        // Each of the 376 channels transfers a certain amount of heat, on full
        // load that will be 8525531 Watts.
        // On full load, there shall be a fuel temperature of
        // of 570 °C (843 K) and recirc out temp of 284°/557 K
        // Resistance: R = DeltaT / P_th = 286 / 8525531 - G = 1/R = 29809.5
        // But, more simple: Just all the values divided by 188
        // evaporator.setThermalDimension(0.0745, 1.064,
        //         29255, 53.191, // Full Conductance
        //         54, 21.3); // Empty conductance
        // =====================================================================
        // New approach that uses multiple fuel elements per evaporator.
        //     
        //  Each fuel elements own
        //  conductance (FuelElement.java):
        //
        //      ----XXXXX----o---------XXXXX--------o
        //                   |        variable      |   Thermal Source in 
        //      ----XXXXX----o        conductance  (|)  evaporator (steam/water
        //                   |        inside        |   temperature)
        //      ----XXXXX-----        evaporator    |
        //     (these are new)                     _|_
        //       
        // From the previous per-fuel-channel simulation we had one resistor
        // for thermal energy transfer only (the variable one) which was set
        // to G = 29255 on normal operation. Now, multiple fuel elements are
        // conencted to one evaportor.
        // R = 1 / 29255 = 3.4182e-5 in total, so we split this to have 
        // R = 1e-5 on the new resistor which is in fuel element and set as
        // G = 1/1e-5 = 100000) and the rest, R = 2.418e-5 will be G = 41354 
        // on the variable conductance here.
        // Reduce to 32000 to have a more conveniant temperature (about 730 °C
        // inside the fuel elements) during operation.
        double totalVolume = 0.0745 * sizeFactor;
        double staticMass = 1.064 * sizeFactor;
        double fullConductance = 32000 * sizeFactor;
        double fullMass = 53.191 * sizeFactor;
        double emptyConductance = 54 * sizeFactor;
        double emptyMass = 21.3 * sizeFactor;
        evaporator.setThermalDimension(totalVolume, staticMass,
                fullConductance, fullMass, // Full Conductance
                emptyConductance, emptyMass); // Empty conductance

        // Channel leakage: On 64e5 Pa, leakage will be 12 kg/s per fuel rod, 
        // which is already pretty excessive. This means the resistance value
        // will be 5.3e5 when channel is ruptured, this will be set if the 
        // rupture did happen.
        channelLeakGravity.setEffort(1e5);

        // Initial conditions
        evaporator.setInitialState(1e5,
                273.5 + 25.3, 273.5 + 36.8);
        channelLeak.setOpenConnection();
        channelMass.getHeatHandler()
                .setInitialTemperature(273.15 + 25.3);
    }

    @Override
    public void runProcessResults() {
        super.runProcessResults();

        voiding = evapHandler.getVoiding(1e5);
        flow = toReactorConverter.getFlow();

        outputValues.setParameterValue(propertyFlow, flow);
        outputValues.setParameterValue(propertyVoiding, voiding);

        // Limit the thermal loop to always have a minimum flow and not exceed 
        // a certain limit - due to the nature of the model it is otherwise 
        // possible that the flow goes reversed, there is no real gravity.
        thermalLiftPressure = Math.min(1.8e5, Math.max(1.2e4,
                (evaporator.getTemperature()
                - downcomerTemperature[loop - 1])
                * 2000)); // try-and-error obtained number

        thermalLift.setEffort(thermalLiftPressure);

        // How many fuel channels asisgned to this evaporator are in a ruptured
        // state?
        int rupturedChannels = 0;
        for (FuelElement f : fuelElements) {
            if (f.isRuptured()) {
                rupturedChannels++;
            }
        }
        if (steamDrum.getFillHeight() < 0.15 || rupturedChannels == 0) {
            // Drain protection: No more leakage below -100 cm or simulation
            // will end.
            channelLeak.setOpenConnection();
        } else {
            // This will generate a flow of 0.37 kg/s on idle and roughly 
            // 30 kg/s on full load per ruptured channel, so the flow also comes 
            // drom the steam drum and the evaporator operates in reverse flow 
            // mode at some point.
            channelLeak.setConductanceParameter(
                    5e-6 * (double) rupturedChannels);
        }

        // get power from all control rods connected to this evaporator group
        double groupFissionPower = 0.0;
        for (FuelElement f : fuelElements) {
            groupFissionPower += f.getFissionPower();
        }
        groupFissionPower = groupFissionPower * 1e6; // MW to W

        // Calculate the Critical Power Ratio - this is Skala code
        // K4N0000 according to Choronobyl Family. This describes a factor of
        // thermal power and a power that would lead to dryout.
        // K = flow * (deltaH_subcooling + X_crit * r) / Q_thermalPower
        // With X_crit = 0.25 and r = 2100000 (Water-Model)
        if (groupFissionPower > LOCAL_IDLE_POWER * 1e6 * fuelElements.size()) {
            double deltaT_subcooling
                    = (Water.INSTANCE.getSaturationTemperature(evapToDrumNode.getEffort())
                    - ((HeatNode) flowResistance.getNode(1)).getTemperature());
            double deltaH_subcooling = deltaT_subcooling * Water.INSTANCE.getSpecificHeatCapacity();

            criticalPowerRatio = Math.min(-evapToDrumNode.getFlow(evaporator)
                    * (deltaH_subcooling + 0.25 * Water.INSTANCE.getVaporizationHeatEnergy())
                    / groupFissionPower, 9.990);
        } else {
            criticalPowerRatio = 9.990;
        }

        // Send this also
        outputValues.setParameterValue(propertyCpr, criticalPowerRatio);
    }

    /**
     * Minimum Critical Power Ratio (K4N0000) of this fuel Channel. This is a
     * value that describes the ratio of maximum allowed power before dry buling
     * and the current power. It needs to stay above 1.0
     *
     * @return
     */
    public double getCriticalPowerRatio() {
        return criticalPowerRatio;
    }

    public PhasedExpandingThermalExchanger getEvaporator() {
        return evaporator;
    }

    /**
     * Access to the current voiding of the steam surrounding the fuel element.
     * Note that this returns only a saved variable to keep the call as cheap as
     * possible.
     *
     * @return
     */
    public double getSteamVoiding() {
        return voiding;
    }

    /**
     * Upward flow in channel
     *
     * @return kg/s
     */
    public double getFlow() {
        return flow;
    }

    /**
     * Saves the current state of this fuel element to a FuelState object that
     * is provided as an argument.
     *
     * @param fs FuelState object
     */
    public void writeToFuelStateObject(FuelState fs) {
        super.writeToFuelStateObject(fs);
        fs.setThermalLiftPressure(thermalLiftPressure);
    }

    /**
     * Sets this fuel element to the provided state for loading the simulation
     * state.
     *
     * @param fs FuelState object
     */
    public void applyFuelState(FuelState fs) {
        super.applyFuelState(fs);
        thermalLiftPressure = fs.getThermalLiftPressure();
        // Disable the leakage - it won't be an issue if its not there for the
        // first cycle.
        channelLeak.setOpenConnection();
    }
}
