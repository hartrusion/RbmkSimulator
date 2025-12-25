# Operation Manual

The simulation starts with the reactor control panel. Additional control panels 
and the mnemonic display views can be opened in the menu. Those windows can be 
arranged as needed.

# Reactor operation

There are a total of 28 manual control rods, 5 automatic control rods and 4 
short rods from below.

Some words on that reactor controls. Things on the lower left do not work, 
I forgot to disable them. Those 5 switches below "Automatic Rods" select which 
rods should be used by the global control. 

![Image](images/screenshot-preview-controls.png)

The global control has a target setpoint and a setpoint for the rod control 
loop (upper left gauge). That loop setpoint ramps up to the target setpoint 
when the target and transient switches are on and the ramp can be set with the 
gradient switches. That allows to pause the transient. To start global control,
it needs to be enabled, rods need to be selected and then that "auto" button 
needs to be pressed to start controlling them. On the lower right with the two 
light bulbs (which do not work) is a manual override that will temporarily 
move all auto rods when pressed.

I have no idea if it worked like this but the power plant i worked in had a 
similar power setpoint control structure, just way more comfortable to use.

"File" - "Set core only mode" will disconnect the reactor model from the thermal
layout so nothing heats up and you can raise the neutron flux to 100 %. That 
also means no voiding feedback from the thermal model