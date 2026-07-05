# Reactivity

The simulation considers various effects on reactivity. It simply sums all
effects, and the result is the input value for the neutron flux model. Because
the focus is on the dynamic behavior and influence of each effect on the
system, no neutron calculation is performed. Instead, the effects are expressed
as readable values between 0 and 100 %.

## Control Rod Absorption

An absorption value is generated between 0 and 100 %, depending on the control
rod positions.

* 4 Short Rods: 6.78 %
* 5 Auto Rods: 14.12 % (2.82 % per Rod)
* 28 Manual Rods: 76.23 % total

Note that the total is only 97.13 %; the remaining 2.87 % come from a rod tip
effect. **A fully withdrawn manual control rod contributes 2.74 %.**

## Voiding

Voiding is given as a percentage that compares the mass present in the fuel
channels with the mass at the boiling point at a reference pressure, which is 1
bar abs and 100 °C here. 0 % means no voids, and 100 % means empty channels.
The value is generated as an average across all fuel channels.

* Shutdown state: 0.0 %
* Full load with 1 MCP: 24.11 %V
* Full load with 2 MCP: 22.27 %V
* Full load with 3 MCP: 21.61 %V
* Full load with 4 MCP: 21.29 %V
* Startup before Turbine, 3 MPC on 40 %, 16.4 bar: 5.17 %V 
* 800 MWth, 3 MCP on 40 %: 19.3 %V

The voiding is modified with a function to make the effect more aggressive
starting at 12.5 %V. Below that, there is no voiding effect. It is defined as

kV = 1.2 * (v - 12.5)^2

so at full load with 3 MCP, kV will be 100 %. Switching off one MCP will ramp it
up to roughly kV = 115 %.

## Xenon

Xenon is given as a mass percentage relative to full-load equilibrium. At full
load, 16 % will decay to Iodine-135 and 84 % is burned off. Due to the
nonlinear state-space model, the static values are also nonlinear. At full load,
Xenon will be 100 %Xe. At 50 % load, it will still settle at 86 %Xe in steady
state. At 5 % load, the steady state is about 24 %Xe.

Xenon is modified with a linear function, y = 8/9 x - 100/3, starting from
60 %Xe, so the output at full load is y(100%Xe) = 55.555 %. Below 60 %Xe, a
third-degree polynomial is used that passes through 0/0 with a gradient of 0
and meets 60 %Xe with the same gradient. It is described as
y(x) = x^3 / 16200 + x^2 / 540.

This function removes the xenon impact at lower power and during startup, and
it maps 150 %Xe to y(150 %Xe) = 100 %. When running down at low load, y will
go from 56 % on full load to 83 %, which is the iodine pit.

## Graphite effect

Xenon alone would not be sufficient to force the reactor operator to face a
massive drop in reactivity, and xenon was also not a key factor during the
incident. Therefore, the simulation includes an artificial effect called the
graphite effect. The graphite stack temperature and gas composition played a
major role in the behavior, so the effect is named after that.

* The graphite effect only happens when neutron flux is below 40 %.
* A hidden value is accumulated when running the reactor between 20 % and 70 %
  power.
* The hidden value vanishes on power levels above 40 % with a fast time constant
  but, between 0.1 and 20 % load, no decay of the hidden value will happen.

Running the reactor at 40 % load will make the value accumulate. Raising power
again or shutting the reactor off completely will decay the value quite fast.
However, if the reactor is held at a low level after a long period at half
load, the effect will kick in after the xenon has decayed and consume most of
the reactivity, almost terminating the chain reaction. The time constants are
quite similar.

Running at 50 % load for a long time, the graphite effect will remain at around
30 % after the xenon decays. Going up, the effect will vanish quickly, so be
careful when raising power. But going down to 1 % power will make the effect
rise to 110 %, which can be used to terminate the chain reaction until the
reactor recovers. It will be difficult to raise power from that level.

## Temperature

The core model outputs the following temperatures:
* 310.8 K on cold start
* 490.5 K on turbine warmup start
* 848.0 K on full power with 3 MCP

To have only a small effect during startup, an exponential function is used as
follows:

kT = 100 * (1 - e^(-(T-310) / 100))

This will already be at 80 % at 470 K and have only minor effects on full
load, so the temperature coefficient will only be present during startup.

## Caclulation of contribution factors

The negative temperature coefficient shall consume 3 manual control rods in 
total so it is 2.87 % * 3 / 100 % = 0.0841. This consumes 8.41 % reactivity on 
full load.

The positive void coefficient shall indroduce a movement of half an automatic  
rod (1.41 %) when switching off an MPC on full power. This makes the kV value 
jump by about 15 %. So it is 1.41 % / 15 % = 0.094. This means, on full load, 
the voiding will **add** 9.4 % which is equvalent of 3.4 manual control rods.

For first startup and to somehow allow a save shutdown even with the voiding 
happening, we will require 4 manual rods withdrawn and 4 automatic rods on
about 1/3 of their position. Here, we also consider the short rods inserted on 
half of their length which is kind of the operating state when using them on 
auto mode. This will be a RodAbsorption of 82.7 %. Since no other effects are in 
place, this is the so called base reactivity.

We now know the rod equivalent for each effect on full load: 3 for temperature,
6.8 for steam voids and 4 are unavailable as they are the save shutdown margin.
It is then 28 - 3 + 3.4 - 4 = 24.4 which sounds pretty balanced. That means, 
24.4 rods can be used to tackle the graphite effect and the xenon effect, so the
reactivity of both those effects is 24.4 * 2.74 = 66.85 %. Xenon has 55 % on
full load, 20 % on half load but the peak will be 100 % after decreasing power.
The graphite effect will rise to 28 % after the 100 % peak of the xenon goes 
down to 20 % when holding power on half load. But, if power will be lowered 
after a long hold on half load, the graphite effect can go up to 100 %. So the
coefficients will be chosen in a way that 100 % of Xenon will be 55 % and 
100 % of Graphite will be 65 %, the graphite effect will kill the chain reaction
that way for a short period. That means, coefficients are 0.55 and 0.65

## Triggering the accident

Even though a rod tip effect is included, it will not be the main effect that
causes the reactor to explode. It was noticeable to operators, but it could be
handled by control systems as well as the positive voiding coefficient. Those
effects are only present to encourage a certain way of handling the control
rods in order to maintain power levels and simulate positive feedback effects.

The accident itself is triggered by inserting a large number of manual rods by
pressing the AZ-5 switch in a situation that requires all rods to be removed.
This triggers another effect called the displacer boost, which ramps up the
reactivity to a value that can no longer be handled. The effect is a function
of rod position and is added for all rods, so they all have to spike at the
same time to exceed a certain value.
