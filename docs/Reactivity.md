# Reactivity

The simulation consideres various effects for reactivity. It simply sums up all
the effects an that is the input value for the neutron flux model. As the focus 
is on the dynamic behavior and influence of each effect on the system, no 
calculation of neutrons is done, instead, effects are made with readable values 
between 0 and 100 %.

## Control Rod absorption

An absorption value is generated that is between 0 and 100 % depending on the
control rods positions.

Short Rods: 6.78 %
Auto Rods: 14.12 %
Manual Rods: 76.23 % fully withdrawn

Note that the sum of this is only 97.13, the remaining 2.87 % come from a rod
tip effect. A fully removed manual control rod makes 2.74 %.

## Voiding

Voiding is given as a percentage that compares the mass present in the fuel 
channels with the mass at boiling point on a reference pressure (which is 1 
barabs and 100 °C here). 0 % means no voids, 100 % would be empty. The value is
generated as an average on all fuel chanels

* Shutdown state: 0.0 %
* Full load with 1 MCP: 24.11 %V
* Full load with 2 MCP: 22.27 %V
* Full load with 3 MCP: 21.61 %V
* Full load with 4 MCP: 21.29 %V
* Startup before Turbine, 3 MPC on 40 %, 16.4 bar: 5.17 %V 
* 800 MWth, 3 MCP on 40 %: 19.3 %V

The voiding will be modified with a function to have a more agressive effect
beginning from 15 %V, below that, there is no voding effect. It will be 

kV = 1.2 * (v - 12.5)^2

so on the full load with 3 MCP the kV will be 100 %. Switching off one MCP will
ramp up to roughly kV = 115 %.

## Xenon

Xenon is given as mass percentage related to full load equilibrium. On full 
load, 16 % will decay to Iodine-135 and 84 % is burned off. Due the non linear
state space model, the static values are also non linear. On full load, Xenon 
will be 100 %Xe, on 50 % load it will be still go to 86 %Xe as steady state. 5 % 
load has a steady state of about 24 %Xe.

The Xenon is modified with a linear function y = 8/9 x - 100/3 beginning from 
60 %Xe so the output on full load is y(100%Xe) = 55.555 %. Below 60%Xe, a 3rd
degree polyom is used that goes through 0/0 with a gradient of 0 and meets at 
60 %Xe with same gradient, it is described as y(x) = x^3 / 16200 + x^2 / 540.
This function removes the xenon impact on lower power and during startup and 
makes a 150 %Xe value to be y(150 %Xe) = 100 %. When running down on low load, 
y will go grom 56 % on full load to 83 % which is the iodine pit.

## Graphite effect

As xenon itself would not be sufficient to force the reactor operator to have a
massive drop in reactivity and the Xenon was also not a key factor during the 
incident, the simulation here does have an artificial effect included that is 
called the graphite effect. The graphite stack temperature and gas composition 
played a major role in the behavior so the effect is named after that. 

* The graphite effect only happens on neutron flux below 40 %. 
* A hidden value is accumulated when running the reactor between 20 % and 70 %
  power. 
* The hidden value vanishes on power levels above 40 % with a fast time constant
  but between 0.1 and 20 % load, no decay of the hidden value will happen.

Having the reactor run on 40 % load will make the value accumulate, raising 
power again or shutting the reactor off completely will decay the value quite 
fast. However, holding the power on low level after a long period on half load,
the effect will kick in after the Xenon is decayed and eat all the reactivitiy, 
almost terminating the chain reaction. The time constants are quite similar.

Running on 50 % Load for long time, the Graphite Effect will be present on a 
level of around 30 % after the xenon decays. Going up, the effect will vanish 
fast, so be careful when rasing power. But going down to 1 % power will make 
the effect rise up to 110 %, this can be used to terminate the chain reaction 
until the reactor recovers itself. It will be difficult to raise the power from
that level.

## Temperature

The temperature of the core model outputs those values:
* 310.8 K on cold start
* 490.5 K on turbine warmup start
* 848.0 K on full power with 3 MCP

To have a small effect on startup only, an exponential function is used as 
follows:

kT = 100 * (1 - e^(-(T-310) / 100))

This will be at 80 % on 470 K already and have only minor effects on full load 
so the temperature coefficent will only be present on startup.

## Triggering the accident

Even though a rod tip effect is included, the rod tip effect will not be the 
major effect making the reactor explode. It was there an noticeable by operators 
but could be handled by control systems as well as the positive voiding 
coefficient. Those effects are only present to make you handle the control rods 
in a certain way to maintain power level and simulate positive feedback effects.

The accident itself is triggered by inserting a large number of manual rods by
pressing the AZ-5 switch in a situation that required all the rods to be 
removed. This will trigger another effect called the displacer boost, this ramps 
up the reactivity to a value that can't be handled anymore. The effect is a 
function of position of a rod and added for all rods, they all have to spike 
this at the same time to go over a certain value.