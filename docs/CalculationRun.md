# Calculation run
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