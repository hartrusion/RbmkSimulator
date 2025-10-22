# RBMK Simulator
A simulator for the chornobyl RBMK reactor. This aims to be a successor to 
the famous RXMODEL simulator from about 30 years ago but with a lot more 
detailed systems and a more accurate representation of the chornobyl plant.

This is work in progress.

Why Java? Java might be the worst choice for real time applications, but it
provides very convenient ways of using object orientated programming and runs on
a variety of computers, providing the same experience over different 
architectures and operating systems. 

## Basics
The simulation is based on the DomainAnalogyModeling project which implements 
the theory of having similar linear ordinary differential equations in 
electonics, mechanics, hydraulics and thermal systems. The methods of network
analysis can therefore be applied to other domains. The 
DomainAnalogyModeling core is available as a separate project.

## Calculation run
The model is updated each 100 ms and the following things have to happen in 
those 100 ms:
1. Fire all actions by getting them from controller (Those are commands from 
   the GUI usually)
2. Get previous cycle values from thermal layout for core voiding and core 
   temperature and set them to core
3. Calculate one core cycle:
   * Update all control rods and calculate absorption with previously set 
     cycles xenon value.
   * Run the neutron flux state space model
   * Set neutron flux to xenon model
   * Generate output values (neutron rate, flux, ORM,...)
4. Calculate one thermal layout (process) cycle:
   * Run all assemblies (like valve movements, pump controls) which might 
     got some command from the actions we fired first, set outputs to the 
     dynamic model input variables.
   * Run all control loops, they will use the output values from model from 
     previous cycle, and update dynamic model inputs.
   * Apply thermal power from core model as input
   * Run the solver for one time step (prepare and do calculation calls)
   * Set model output values to control loops for next cycle
   * Gain measurement data and generate output values