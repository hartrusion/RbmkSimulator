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
import com.hartrusion.modeling.general.FlowSource;
import com.hartrusion.modeling.general.GeneralNode;
import com.hartrusion.modeling.general.LinearDissipator;
import com.hartrusion.modeling.general.OpenOrigin;
import com.hartrusion.modeling.general.SelfCapacitance;
import java.util.List;

/**
 * Has an affection which is a value how much this fuel rod is affected by
 * nearby control rods.
 * <p>
 * Manages the thermal model that generates the thermal energy. Serves as some
 * kind of container which has the elements and some nodes, they get connected
 * from this in the thermal model on initialization there.
 * <p>
 * Note that the private property of fields here is explicitly used to shield
 * the fuel thermal part from the evaporator part which is an extension of this
 * class.
 *
 * @author Viktor Alexander Hartung
 */
public class FuelElement extends ReactorElement {

    private double stepTime = 0.1;

    /**
     * Besides decay heat, the core will always produce the set amount of heat.
     * A value of 5.6 MW was decided to be fine, however, this will take a long
     * time to heat up things even with the way smaller masses here. A higher
     * value is chosen to get a better simulation experience. This allows
     * pressure buildup to be observed even without any neutron flux. It was 48
     * MW at some point but, this also comes with the downside that the aux
     * condensers are too small for 48 MW idle heat.
     * <p>
     * This number is for the full core, given in Megawatts.
     */
    public static final double IDLE_HEAT = 5.6;

    /**
     * Power in Megawatts when having full neutron flux of 100 %
     */
    public static final double FULL_FLUX_POWER = 3200;

    /**
     * Conversion factor from flux (0..100 %N) to thermal power in MWth
     */
    private final double FLUX_TO_POWER;

    /**
     * Conversion factor from flux (0..100 %N) to thermal power in MWth, but
     * only for displayed power. The display shall hide the idle power that is
     * not from decay heat.
     */
    private final double FLUX_TO_DISPLAY_POWER;
    
    /**
     * Idle heat (in Megawatts) produced by this specific fuel element.
     */
    protected final double LOCAL_IDLE_POWER;

    /**
     * Fraction of the neutron flux that is distributed among the fuel elements
     * using their affection value. The remaining fraction (1 -
     * DISTRIBUTED_FLUX) is applied directly as fission power, independent of
     * the affection. The affection is therefore only used to redistribute this
     * fraction of the power without changing the overall amount.
     */
    private static final double DISTRIBUTED_FLUX = 0.15;

    /**
     * Weight factor applied to the fuel elements located within
     * {@link #RADIAL_INNER_RADIUS} of the core center. Inside this radius every
     * element receives this same (maximum) weight; from the radius outwards the
     * weight decreases linearly towards the core edge (reaching 1.0 at the
     * outermost element before normalization). A value of 1.0 disables the
     * radial peaking and results in a flat radial distribution, larger values
     * concentrate more power in the center of the core.
     */
    private static final double RADIAL_INNER_WEIGHT = 1.4;

    /**
     * Inner radius (in channel pitch units, i.e. channel numbers) around the
     * core center within which the radial weight stays constant at
     * {@link #RADIAL_INNER_WEIGHT}. Outside of it the weight decreases linearly
     * towards the outermost fuel element.
     */
    private static final double RADIAL_INNER_RADIUS = 3.0;

    /**
     * Manipulates the time the decay heat goes down so the decay heat will be
     * available much longer. This allows less waiting for full load and at the
     * same time causes problems when having a coolant problem accident, making
     * the heat not disappear that fast and cooling of the reactor is required
     * for a way longer period of time.
     */
    private static final double DECAY_DOWN_MODIFIER = 0.07;

    /**
     * Fraction of thermal power that will be delayed as it occurs by delayed
     * decay instead of the uranium fission. This will be the part that is still
     * there and slowly decays after scram.
     */
    public static final double P_DECAY = 0.062;

    /**
     * Time constant (seconds) for the delayed thermal heat production.
     */
    private final double T_DECAY = 120;

    private double maxSumOfAffections = 0.0;

    private double sumOfAffections = 0.0;

    /**
     * Normalized value between 0..1
     */
    private double affection = 0.0;

    /**
     * Global neutron flux value, it is the same for all fuel elements so a
     * static variable is used. From 0 to 100 %
     * <p>
     * Gets set from ReactorCore to value neutronFluxModel.getYNeutronFlux()
     */
    private static double globalFlux;

    /**
     * Local neutron flux for this element in the same range of the global flux,
     * it consideres the affection distribution and the total number of rods in
     * the core. The sum of all localFlux values is globalFlux.
     */
    private double localFlux;

    private double localAffection;

    /**
     * Generated heat power on this fuel rod in 0..100 %
     */
    private double rodHeatGeneration;

    /**
     * Average affection over all fuel elements. It is used to normalize the
     * affection based distribution so the affection only redistributes power
     * without changing the overall amount. It is the same for all elements so a
     * static variable is used.
     */
    private static double averageAffection;

    /**
     * Static, geometry based weight that redistributes fission power towards
     * the core center. Normalized over all fuel elements so its average equals
     * 1.0, which keeps the overall power unchanged and only reshapes the radial
     * distribution. Initialized by {@link #initRadialWeights(java.util.List)}.
     */
    private double radialWeight = 1.0;

    private double xFirstDelay;

    private double xDelayedPower;

    /**
     * Fission power given in % in same unit as neutron flux. This is already
     * considering the affection value.
     */
    private double fissionPower;

    /**
     * Fission power for display - this does not consider the idle power, it
     * will make the power display hide the value.
     */
    private double fissionPowerDisplay;

    /**
     * Saves the value of the fuel temperature as the value from the previous
     * cycle is used here to determine if the thermal power has to be limited to
     * have the fuel rod melt, but not evaporate.
     * <p>
     * Given in Degrees Celsius.
     */
    private double fuelTemperature;

    private boolean ruptured;

    // Thermal system describing the fuel thermal heat flow
    private final GeneralNode thermalGroundNode = new GeneralNode(PhysicalDomain.THERMAL);
    private final OpenOrigin thermalGround = new OpenOrigin(PhysicalDomain.THERMAL);
    private final FlowSource thermalFlowSource = new FlowSource(PhysicalDomain.THERMAL);
    private final SelfCapacitance thermalCapacity
            = new SelfCapacitance(PhysicalDomain.THERMAL);
    private final GeneralNode thermalCapacityNode
            = new GeneralNode(PhysicalDomain.THERMAL);
    private final LinearDissipator thermalInnerResistance
            = new LinearDissipator(PhysicalDomain.THERMAL);
    private final GeneralNode thermalCoreNode = new GeneralNode(PhysicalDomain.THERMAL);
    private final LinearDissipator thermalToEvapResistance
            = new LinearDissipator(PhysicalDomain.THERMAL);

    /**
     * Coolant loop this channel belongs to (1 or 2).
     */
    protected int loop;

    private final String propertyTemperature;

    private final String propertyAffectionValue;
    private final String propertyFissionPower;

    public FuelElement(int x, int y) {
        super(x, y);

        propertyTemperature = "Fuel" + (100 * x + y) + "#Temperature";

        propertyAffectionValue = "Fuel" + (100 * x + y) + "#Affection";
        propertyFissionPower = "Fuel" + (100 * x + y) + "#FissionPower";
        // Assign loop by given coordinates.
        loop = ChannelData.getLoop(x, y);

        // Calculate factors for megawatt out of flux.
        FLUX_TO_POWER = (FULL_FLUX_POWER - IDLE_HEAT) / 100;
        FLUX_TO_DISPLAY_POWER = FULL_FLUX_POWER / 100;
        LOCAL_IDLE_POWER = IDLE_HEAT / 376;

        thermalGroundNode.setName("FuelChannelThermal" + x + "-" + y + "Fuel#GroundNode");
        thermalGround.setName("FuelChannelThermal" + x + "-" + y + "#Ground");
        thermalFlowSource.setName("FuelChannelThermal" + x + "-" + y + "#FlowSource");
        thermalCapacity.setName("FuelChannelThermal" + x + "-" + y + "#Capacity");
        thermalCapacityNode.setName("FuelChannelThermal" + x + "-" + y + "#CapacityNode");
        thermalInnerResistance.setName("FuelChannelThermal" + x + "-" + y + "#InnerResistance");
        thermalCoreNode.setName("FuelChannelThermal" + x + "-" + y + "#CoreNode");
        thermalToEvapResistance.setName("FuelChannelThermal" + x + "-" + y + "#EvapResistance");

        // Connections of the thermal part
        thermalGround.connectTo(thermalGroundNode);
        thermalFlowSource.connectBetween(thermalGroundNode, thermalCoreNode);
        // Add a capacitance for modeling the fuels thermal capacity
        thermalCapacity.connectTo(thermalCapacityNode);
        thermalInnerResistance.connectBetween(thermalCapacityNode, thermalCoreNode);

        // Only one side gets connected here, the other will be the node that 
        // already exists in the evaporator element.
        thermalToEvapResistance.connectTo(thermalCoreNode);

        // See notes in EvaporatorElement.java for details on how this was
        // calculated.
        thermalToEvapResistance.setConductanceParameter(1e5);

        // 192 Tons (96 per side) of fuel in reactor. Specific heat capacity
        // of uranium dioxide: 270 J/kg/K
        // Thermal capacity: m * c = 96000 kg * 270 J/kg/K = 2.6e7 J/K per side
        // Per channel: 138298 J/K
        thermalCapacity.setTimeConstant(138298);
        // We will not have a inner heat transfer resistance, instead, we just
        // do Tau = R * C to R = Tau/C to get a fancy time constant that 
        // gets the dynamics we want. Lets use Tau of 10 s so it will be 
        // R = 10/138298
        thermalInnerResistance.setResistanceParameter(7.2e-5);

        // Initial State
        thermalCapacity.setInitialEffort(273.15 + 38);
    }

    /**
     * Used to connect the thermal models from corresponding fuel elements to
     * the evaporator. Called during model building, for each fuel thermal model
     * part (even for the the part that is extended in this class).
     *
     * @param evapElementInNode The outNode of the evaporator (thermal) from the
     * EvaporatorElement
     */
    public void connectToEvaporator(GeneralNode evapElementInNode) {
        thermalToEvapResistance.connectTo(evapElementInNode);
    }

    /**
     * Adds an affection and sums it up. Each fuel element then knows the total
     * affection that can be applied to it at any time.
     * <p>
     * This is called during initialization from multiple loops, generating the
     * total value at the end of the initialization.
     *
     * @param affection
     */
    public void addMaxAffection(double affection) {
        maxSumOfAffections += affection;
    }

    /**
     * Called before next calculation to prepare sum up of the values
     */
    public void prepareAffectionCalculation() {
        sumOfAffections = 0;
    }

    /**
     * Called by each control rod that affects this element, it will add its
     * part to this fuel element.
     *
     * @param affection
     */
    public void addAffection(double affection) {
        sumOfAffections += affection;
    }

    /**
     * Makes the affection available, to be called after all rods added their
     * value to this fuel element.
     */
    public void finalizeAffection() {
        affection = sumOfAffections / maxSumOfAffections;
        // Send as soon as calculation is done
        outputValues.setParameterValue(propertyAffectionValue, affection);
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

    /**
     * Side of the reactor this fuel element belongs to
     *
     * @return 1 or 2
     */
    public int getLoop() {
        return loop;
    }

    /**
     * Sets the current neutron flux as an input value, it will directly
     * generate some of the heat and also will be applied to the delayed heat
     * generation that is per-channel simulated (flux is a global, scalar value)
     *
     * @param flux Neutron Flux between 0..100 %
     */
    public static void applyNeutronFlux(double flux) {
        globalFlux = flux;
    }

    /**
     * Sets the average affection over all fuel elements. This is needed to
     * normalize the affection based distribution of the fission power so the
     * affection only redistributes the power without changing the total amount.
     *
     * @param average Average affection (0..1) over all fuel elements
     */
    public static void applyAverageAffection(double average) {
        averageAffection = average;
    }

    /**
     * Euclidean distance of this fuel element to the core center in channel
     * pitch units.
     *
     * @return distance to the core center
     */
    private double getDistanceToCenter() {
        double dx = getX() - ChannelData.CENTER;
        double dy = getY() - ChannelData.CENTER;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Computes the raw (not yet normalized) radial weight of this fuel element.
     * Elements within {@link #RADIAL_INNER_RADIUS} receive
     * {@link #RADIAL_INNER_WEIGHT}, from there the weight drops linearly to 1.0
     * at the outermost fuel element.
     *
     * @param maxRadius distance of the outermost fuel element to the center
     * @return raw radial weight
     */
    private double computeRawRadialWeight(double maxRadius) {
        double d = getDistanceToCenter();
        if (d <= RADIAL_INNER_RADIUS || maxRadius <= RADIAL_INNER_RADIUS) {
            return RADIAL_INNER_WEIGHT;
        }
        if (d >= maxRadius) {
            return 1.0;
        }
        return RADIAL_INNER_WEIGHT - (RADIAL_INNER_WEIGHT - 1.0)
                * (d - RADIAL_INNER_RADIUS) / (maxRadius - RADIAL_INNER_RADIUS);
    }

    /**
     * Initializes the static radial power weights for all fuel elements. The
     * weights are normalized so their average equals 1.0, which ensures the
     * overall fission power stays unchanged and the radial profile only
     * redistributes power towards the core center. Has to be called once after
     * all fuel elements have been created.
     *
     * @param elements list of all fuel elements
     */
    public static void initRadialWeights(List<FuelElement> elements) {
        double maxRadius = 0.0;
        for (FuelElement f : elements) {
            maxRadius = Math.max(maxRadius, f.getDistanceToCenter());
        }
        double sum = 0.0;
        for (FuelElement f : elements) {
            f.radialWeight = f.computeRawRadialWeight(maxRadius);
            sum += f.radialWeight;
        }
        double average = sum / elements.size();
        for (FuelElement f : elements) {
            f.radialWeight /= average;
        }
    }

    /**
     * Current fission power of this fuel element, given in MW in the same unit
     * as the neutron flux and already considering the affection distribution.
     *
     * @return Fission power in Megawatts
     */
    public double getFissionPower() {
        return fissionPower;
    }

    /**
     * This is called on each fuel rod after the thermal layout has finished its
     * calculations and the temperature and flow data is available. All elements
     * are updated now.
     */
    public void runProcessResults() {

        fuelTemperature = thermalCapacityNode.getEffort() - 273.15;

        // Send per fuel rod values - those are intended to be debugging
        // only as they are not available in such a detail in the real plant.
        outputValues.setParameterValue(propertyTemperature, fuelTemperature);
        outputValues.setParameterValue(propertyFissionPower, fissionPower);

        // The fuel will be set as ruptured if temperature hits 850 °C. The 
        // maximum operation temperature is usually below 730 °C so it only 
        // happens if something goes very wrong.
        if (thermalCapacityNode.getEffort() - 273.15 >= 850) {
            ruptured = true;
        }
    }

    /**
     * Access to the fuel elements temperature, this describes the temperature
     * of the inner material of the fuel, not the evaporator.
     *
     * @return Temperature of the fuel in Kelvin
     */
    public double getFuelTemperature() {
        return thermalCoreNode.getEffort();
    }

    /**
     * Called from the reactor core for each rod after the core model was
     * called, it will do the calculations for the power model, which is the
     * part that generates the heat.
     * <p>
     * Called from RectorCore.run() which is invoked BEFORE the thermal layout
     * is calculated.
     */
    public void calculationStepPowerModel() {
        double dXFirstDelay, dXDelayedPower;

        // Calculate a local affection value based on distribution of control
        // rods, this will influence the power generated by each fuel rod.
        if (averageAffection > 0.0) {
            localAffection = affection / averageAffection;
        } else {
            localAffection = 1.0;
        }

        // There are 376 fuel elements in the core. Calculate the flux on this
        // fuel element considering the number of rods and the current 
        // distribution.
        // In addition to the affection based redistribution, a static radial
        // weight concentrates power towards the core center. As radialWeight is
        // normalized to an average of 1.0, the (radialWeight - 1.0) term sums
        // to zero over all elements and therefore only redistributes power
        // without changing the overall amount produced by the flux.
        localFlux = (globalFlux * (1.0 - DISTRIBUTED_FLUX)
                + globalFlux * DISTRIBUTED_FLUX * localAffection
                + globalFlux * (radialWeight - 1.0)) / 376.0;

        dXFirstDelay = (localFlux * P_DECAY - xFirstDelay) / T_DECAY;

        dXDelayedPower = (xFirstDelay - xDelayedPower) / T_DECAY;

        // Forward Euler
        xFirstDelay += dXFirstDelay * stepTime;
        if (dXDelayedPower < 0.0) {
            // decay heat will be present way longer than it takes time 
            // to build it up.
            xDelayedPower += dXDelayedPower * stepTime
                    * DECAY_DOWN_MODIFIER;
        } else {
            xDelayedPower += dXDelayedPower * stepTime;
        }

        rodHeatGeneration = localFlux * (1 - P_DECAY) + xDelayedPower;

        // Fission power consideres the idle power but does not display it,
        // it is added as an invisible energy not shown on the power display.
        // It's the value that will be set towards the thermal steam model.
        fissionPower = rodHeatGeneration * FLUX_TO_POWER + LOCAL_IDLE_POWER;

        if (fuelTemperature > 7000) {
            // Limit the possible fuel temperature and do not add any more heat
            // to keep the model in a state that still can be calculated.
            thermalFlowSource.setFlow(0.0);
        } else {
            // MW to Watt (SI)
            thermalFlowSource.setFlow(fissionPower * 1e6);
        }

        // The displayed fission power will not include the idle heat and show a
        // wrong 3200 MW display for 100 %
        fissionPowerDisplay = rodHeatGeneration * FLUX_TO_DISPLAY_POWER;
    }

    public double getFissionPowerForDisplay() {
        return fissionPowerDisplay;
    }
    
    /**
     * Combines actual flux value (considers affection) and delayed heat 
     * generation. Describes the part that generates the heat from the nuclear
     * reaction, but without idle power.
     * 
     * @return Value in same unigs as Neutron Flux (0..100 %N)
     */
    public double getRodHeatGeneration() {
        return rodHeatGeneration;
    }
    
    /**
     * Delayed power produced by this fuel element.
     * 
     * @return Value in same unigs as Neutron Flux (0..100 %N)
     */
    public double getDelayedPower() {
        return xDelayedPower;
    }

    /**
     * Saves the current state of this fuel element to a FuelState object that
     * is provided as an argument.
     *
     * @param fs FuelState object
     */
    public void writeToFuelStateObject(FuelState fs) {
        fs.setXFirstDelay(xFirstDelay);
        fs.setXDelayedPower(xDelayedPower);
        fs.setRuptured(ruptured);
    }

    /**
     * Sets this fuel element to the provided state for loading the simulation
     * state.
     *
     * @param fs FuelState object
     */
    public void applyFuelState(FuelState fs) {
        xFirstDelay = fs.getXFirstDelay();
        xDelayedPower = fs.getXDelayedPower();
        ruptured = fs.isRuptured();
    }

    /**
     * Get the rupture state of the fuel channel.
     *
     * @return true if the channel is ruptured
     */
    public boolean isRuptured() {
        return ruptured;
    }

    /**
     * Sets the rupture state to false.
     */
    public void repair() {
        ruptured = false;
    }
}
