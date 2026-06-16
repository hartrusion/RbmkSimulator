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
import com.hartrusion.modeling.automated.PhasedValve;
import com.hartrusion.modeling.converters.PhasedHeatFluidConverter;
import com.hartrusion.modeling.general.FlowSource;
import com.hartrusion.modeling.general.GeneralNode;
import com.hartrusion.modeling.general.LinearDissipator;
import com.hartrusion.modeling.general.OpenOrigin;
import com.hartrusion.modeling.general.SelfCapacitance;
import com.hartrusion.modeling.heatfluid.HeatEffortSource;
import com.hartrusion.modeling.heatfluid.HeatFrictionedFlowResistance;
import com.hartrusion.modeling.heatfluid.HeatNode;
import com.hartrusion.modeling.heatfluid.HeatVolumizedFlowResistance;
import com.hartrusion.modeling.phasedfluid.PhasedClosedSteamedReservoir;
import com.hartrusion.modeling.phasedfluid.PhasedEffortSource;
import com.hartrusion.modeling.phasedfluid.PhasedExpandingThermalExchanger;
import com.hartrusion.modeling.phasedfluid.PhasedNode;
import com.hartrusion.modeling.phasedfluid.Water;

/**
 * Has an affection which is a value how much this fuel rod is affected by
 * nearby control rods.
 * <p>
 * Manages the thermal model that generates the thermal energy and the modeling
 * of the evaporation. Serves as some kind of container which has the elements
 * and some nodes, they get connected from this in the thermal model on
 * initialization there.
 *
 * @author Viktor Alexander Hartung
 */
public class FuelElement extends ReactorElement {

    private double maxSumOfAffections = 0.0;

    private double sumOfAffections = 0.0;

    /**
     * Normalized value between 0..1
     */
    private double affection = 0.0;

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
    private final PhasedNode evaporatorIn = new PhasedNode();
    private final PhasedExpandingThermalExchanger evaporator;
    private final PhasedNode evapToDrumNode = new PhasedNode();

    private final PhasedValve channelLeak = new PhasedValve();
    private final PhasedNode channelLeakNode = new PhasedNode();
    private final PhasedEffortSource channelLeakGravity
            = new PhasedEffortSource();

    // Thermal system describing the fuel thermal heat flow
    private final GeneralNode thermalGroundNode = new GeneralNode(PhysicalDomain.THERMAL);
    private final OpenOrigin thermalGround = new OpenOrigin(PhysicalDomain.THERMAL);
    private final FlowSource thermalFlowSource = new FlowSource(PhysicalDomain.THERMAL);
    private final SelfCapacitance thermalCapacity
            = new SelfCapacitance(PhysicalDomain.THERMAL);
    private final GeneralNode thermalCapacityNode
            = new GeneralNode(PhysicalDomain.THERMAL);
    private final LinearDissipator thermalResistance
            = new LinearDissipator(PhysicalDomain.THERMAL);
    private final GeneralNode thermalOutNode = new GeneralNode(PhysicalDomain.THERMAL);

    /**
     * Coolant loop this channel belongs to (1 or 2).
     */
    private int loop;

    /**
     * Used to generate various signal names and so on
     */
    private String prefix;

    public FuelElement(int x, int y) {
        super(x, y);

        prefix = "Fuel" + x + "-" + y;

        // Assign loop by given coordinates.
        loop = ChannelData.getLoop(x, y);

        // Generate instances
        toReactorConverter = new PhasedHeatFluidConverter(Water.INSTANCE);
        evaporator = new PhasedExpandingThermalExchanger(Water.INSTANCE);

        // Naming
        flowResistance.setName("FuelChannelHydraulic" + x + "-" + y + "#FlowResistance");
        afterResistance.setName("FuelChannelHydraulic" + x + "-" + y + "#AfterResistance");
        channelMass.setName("FuelChannelHydraulic" + x + "-" + y + "#ChannelMass");
        afterChannelMass.setName("FuelChannelHydraulic" + x + "-" + y + "#AfterChannelMass");
        thermalLift.setName("FuelChannelHydraulic" + x + "-" + y + "#ThermalLift");
        afterThermalLift.setName("FuelChannelHydraulic" + x + "-" + y + "#AfterThermalLift");
        toReactorConverter.setName("FuelChannelHydraulic" + x + "-" + y + "#ToReactorConverter");
        evaporatorIn.setName("FuelChannelHydraulic" + x + "-" + y + "#EvaporatorIn");
        evaporator.setName("FuelChannelHydraulic" + x + "-" + y + "#Evaporator");

        thermalGroundNode.setName("FuelChannelThermal" + x + "-" + y + "Fuel#GroundNode");
        thermalGround.setName("FuelChannelThermal" + x + "-" + y + "#Ground");
        thermalFlowSource.setName("FuelChannelThermal" + x + "-" + y + "#FlowSource");
        thermalCapacity.setName("FuelChannelThermal" + x + "-" + y + "#Capacity");
        thermalCapacityNode.setName("FuelChannelThermal" + x + "-" + y + "#CapacityNode");
        thermalResistance.setName("FuelChannelThermal" + x + "-" + y + "#Resistance");
        thermalOutNode.setName("FuelChannelThermal" + x + "-" + y + "#OutNode");

        // Connections of the hydraulic part
        flowResistance.connectTo(afterResistance);
        channelMass.connectBetween(afterResistance, afterChannelMass);
        thermalLift.connectBetween(afterChannelMass, afterThermalLift);
        toReactorConverter.connectBetween(afterThermalLift, evaporatorIn);
        evaporator.initComponent();
        evaporator.connectTo(evaporatorIn);
        channelLeak.getValveElement().connectBetween(
                evaporatorIn, channelLeakNode);
        channelLeakGravity.connectTo(channelLeakNode);

        // Connections of the thermal part
        thermalGround.connectTo(thermalGroundNode);
        thermalFlowSource.connectBetween(thermalGroundNode, thermalOutNode);
        // Add a capacitance for modeling the fuels thermal capacity
        thermalCapacity.connectTo(thermalCapacityNode);
        thermalResistance.connectBetween(thermalCapacityNode, thermalOutNode);
        evaporator.getInnerThermalResistanceElement().connectTo(thermalOutNode);

        // There is 188 Channels per side. The total resistance for one loop
        // is 293.1 so per Channel it will be 55102.8.
        // This has to be split onto two elements to have the nodal analysis
        // work with norton transform.
        flowResistance.setResistanceParameter(25102.8);
        channelMass.setInnerThermalMass(300);
        channelMass.setResistanceParameter(30000);
        // Manipulate the specific heat capacity here to make the heatup 
        // from the MCP circulation much more intense. Default is 4200, the
        // heat increase is delta_p / (density * specHeatCap)
        flowResistance.setFrictionHeatupParameters(1000, 2000);

        // 192 Tons (96 per side) of fuel in reactor. Specific heat capacity
        // of uranium dioxide: 270 J/kg/K
        // Thermal capacity: m * c = 96000 kg * 270 J/kg/K = 2.6e7 J/K per side
        // Per channel: 138298 J/K
        thermalCapacity.setTimeConstant(2.6e7);
        thermalResistance.setResistanceParameter(1.78e-7);

        // 20 m³ volume in evaporator per side is way too slow for 
        // mcp loss accident.
        // Fuel model: Full thermal power per side is 1.6e9 Watts with fuel
        // temperature of 570 °C (843 K) and recirc out temp of 284°/557 K.
        // . Resistance: R = DeltaT / P_th = (843-557) / 1.6e9 
        // = 1.78e-7 K/(J*s). As G: 1/1.8e-7 is about 5.5e6.
        // For loss of circulation: The evaporator will slowly start to boil
        // and loose its mass. It should met at 2800 °C (3073 K) and we just
        // randomly define 4000 K and 50 MW when running empty, so it is
        // G = P_th / DeltaT = 50e6 J/s / 4000 K = 1.25e4 when almost empty.
        evaporator.setThermalDimension(14.0, 200, 5.5e6,
                10000, 1.0e4, 4000);

        // Channel leakage: Represented with a valve to be able to set an amount
        // of leakage with the given characteristic. Worst leakage will be
        // on 64e5 Pa with 500 kg/s: 
        channelLeak.initCharacteristicSimple(12800);
        channelLeak.getIntegrator().setMaxRate(50);
        channelLeakGravity.setEffort(1e5);

        // Initial State
        thermalCapacity.setInitialEffort(273.15 + 38);

        evaporator.setInitialState(1e5,
                273.5 + 25.3, 273.5 + 36.8);

        channelMass.getHeatHandler()
                .setInitialTemperature(273.15 + 25.3);
    }

    /**
     * Adds an affection and sums it up. Each fuel element then knows the total
     * affection that can be applied to it at any time.
     *
     * @param affection
     */
    public void addMaxAffection(double affection) {
        maxSumOfAffections += affection;
    }

    /**
     * Called by each control rod, it will add its part to this fuel element.
     *
     * @param affection
     */
    public void addAffection(double affection) {
        sumOfAffections += affection;
    }

    /**
     * Called before next calculation to prepare sum up of the values
     */
    public void prepareAffectionCalculation() {
        sumOfAffections = 0;
    }

    /**
     * Makes the affection available, to be called after all rods added their
     * value to this fuel element.
     */
    public void finalizeAffection() {
        affection = sumOfAffections / maxSumOfAffections;
    }

    /**
     * Normalized value between 0 and 1 that describes how much this fuel rod is
     * affected in total by its surrounding control rods.
     *
     * @return 0.0 .. 1.0 double
     */
    public double getAffection() {
        return affection;
    }

    public PhasedExpandingThermalExchanger getEvaporator() {
        return evaporator;
    }

    public int getLoop() {
        return loop;
    }

    /**
     * Called from the thermal layout during the network setup process, requires
     * this fuel element to be initialized already. It connects the elements 
     * to the thermal network.
     * 
     * @param distributorNode
     * @param drumNode
     * @param poolNode 
     */
    public void connectTo(HeatNode distributorNode,
            PhasedClosedSteamedReservoir steamDrum, PhasedNode poolNode) {
        flowResistance.connectTo(distributorNode);
        evaporator.connectToVia(steamDrum, evapToDrumNode);
        channelLeakGravity.connectTo(poolNode);
    }

    /**
     * Sets the current neutron flux as an input value, it will directly
     * generate some of the heat and also will be applied to the delayed heat
     * generation that is per-channel simulated (flux is a global, scalar value)
     *
     * @param flux Neutron Flux between 0..100 %
     */
    public void applyNeutronFlux(double flux) {
        // Todo: Set to fuel thermal source
    }

    /**
     * Get the temperature on this fuel element.
     *
     * @return Temperature in Kelvin
     */
    public double getTemperature() {
        return 298.15;
    }

    public void applyThermalLift() {
        thermalLift.setEffort(5e4); // todo, just a constant for now
    }

    public void sendMeasuremetData() {
        // Todo: Send Temperature, maybe other things also additonally.
    }

}
