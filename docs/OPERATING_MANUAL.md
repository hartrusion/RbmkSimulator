# Operation Manual

The simulation starts with a control panel showing the reactor controls. 
Additional control panels can be added via the _Control Panels_ menu there.
Those will be displayed inside the Control Panel window.

Mnemonic displays are opened as separate windows, those can be opened from the
menu also. 

**Please note that those widgets on the panels do look like the controls from 
the real control panel but their functionality is made up by my imagination.**
Do no use this simulator as a reference for the original plant.

_Views_ allows you to open additional windows showing the control rod positions 
and a core status view, for example.

Please arrange windows as you need them.

## Reactor operation

There are a total of 28 manual control rods, 5 automatic control rods and 4 
short rods from below.

Some words on that reactor controls. Things on the lower left do not work, 
those are not yet implemented. Those 5 switches below "Automatic Rods" select 
which rods should be used by the global control. 

![Image](images/screenshot-preview-controls.png)

The global control has a **target setpoint** and an **active setpoint** for the 
rod control loop. That loop setpoint ramps up to the target setpoint when the 
target and transient switches are on and the ramp can be set with the gradient 
switches. That allows to pause the transient.

Global control will move the selected auto rods (red) trying to keep the active 
setpoint value. You need to get the reactor in a state where this works, it is 
your job to pull out enough manual rods (green) or push them back in to keep 
the auto rods in operating range.

Pay attention to both the red and the green lights on top of the override 
switch, those do indicate that the automatic rods are reaching the top or lower 
position (an alarm will also be fired in such cases).

_File_ - _Set core only mode_ will disconnect the reactor model from the thermal
layout so nothing heats up and you can raise the neutron flux to 100 %. That 
also means no voiding feedback from the thermal model. Use this to get yourself 
familiar with the rod controls.

## Controls usage

There is a certain way on how the control panel is operated. I tried to mimic 
the behavior of such control panels with the elements that are available on 
modern computer programs. Some buttons and switches will not do anything if 
some prerequisites are not met. Switches need to be turned again to their "on"
position if something was shut down, things will never turn themselves back on.

Some controls have tooltips with a description.

### Valves

Some valves can be opened or closed only, those are operated with green and red
buttons. The button with the corresponding end position will light up as soon 
as the valve is open (red/in Operation) or closed (green/ready).

![Image](images/screenshot-preview-controlledvalve.png)

Other valves might be controlled by a control loop output (level control), there 
is a widget with four buttons for such valves. The indicator scale shows the 
current valve position. The two middle buttons switch between manual and auto 
mode, the outer buttons directly control the valves position. Active control 
loops are indicated with a lit up triangle on the mnemonic display. The valve 
control widget shows "H" on manual and "A" on auto mode.

Valves which can only be controlled manually have a smaller switch with two 
buttons and a red and green indicator above the switch. Their position has to 
be obtained from the mnemonics.

### Control Loop Widget

Sometimes, multiple valves do the same thing and have the same setpoint value.
In other cases, there might be a simple control loop with only one single valve 
and a setpoint just for that one valve. Such control loops can be controlled 
by a control loop widget.

![Image](images/screenshot-preview-integratedloop.png)

The upper gauge shows the current value (green) and the setpoint value (red)
with the same scale. The lower gauge shows the current valve position, or any 
other output elements value. The A and H buttons toggle between auto and manual 
mode. Pressing the red "S" button will toggle setpoint mode. The up and down 
buttons do control the valve manually or do a manual override. If setpoint 
mode is active, those buttons are used to modify the setpoint instead of 
the valve position.

### Switching Pumps

Centrifugal pumps have a valve on each side (suction and discharge). To turn on 
a pump, the discharge valve has to be closed. This ensures no reverse flow could
ever turn the pump when switching the motor on. This is enforced in the 
simulator. Each pump on the control panel can be found on a mnemonic display
showing the current pump state. The suction valve usually remains open and has 
to be opened before turning ont he pump.

![Image](images/screenshot-preview-pumpswitches.png)

A pump is ready to be turned on when the outline glows. Its startup sequence is 
indicated by a slight glow, a running pump is indicated with a glowing inner 
circle. Do not open the discharge valve before the pump is running.

### Alarms

Some values are attached to alarms which are triggered when above or below a 
certain level. There are **four** alarm thresholds available (not all of them 
need to be mapped to a vale) for each direction. The first two are considered 
warning and there is no consequence if they are appearing. The second two alarm 
thresholds are always connected to a certain event (valve operation, pump 
shutdown, ...).

For high values there is:

* HIGH1 - warning only
* HIGH2 - warning only
* MAX1 - Triggers an event
* MAX2 - Triggers an event

For low values there is:

* LOW1 - warning only
* LOW2 - warning only
* MIN1 - Triggers an event
* MIN2 - Triggers an event

Some elements also have a safety override implemented that prohibits opening or 
closing valves or switching on certain pumps.

This is not primarily done to ensure the plant's integrity, it is done because 
the modeling engine does not allow certain states and would otherwise crash.

## Startup 

With the current projects state, it is possible to generate some steam and build
up pressure using the following procedure:

* Open all MCP suction valves. This will make each MCP ready to be switched on, 
the green light above the switch will indicate that.
* Close the recirculation bypass valves. Note that there is now still flow 
through the core as we still have the blowdown/cooldown system sucking in the
water from the MCP pressure header.
* Turn on the level balance control on Blowdown/cooldown system. This will 
compensate level imbalances by controlling the return valves automatically.
* Switch on two MCPs on both sides each and open discharge valves afterwards.
* Switch on Makeup pumps and hotwell fill regulation.
* Switch on one condensate pump and one booster pump. Open the bypass at the 
main ejectors to have a path from hotwell to deaerators. 
* Try filling some water in the deaerators by manually opening one of the 
condensate flow valves to the DAs a little bit.
* Switch both DA flow valves to automatic.
* Turn on one feed pump per side to have pressure on the feed valves to the
steam drums.
* Open the startup valves for both sides and the reduction valves. Those are on 
the lower left of the control panel. We need small valves and an additional 
reduction valve as there is no pressure inside the steam drum but full pressure 
from the feed pumps.
* The regulator valves are controlling the steam drum level. Press the icon on 
the 4-button-valve to switch the two startup valves to automatic mode. You 
should now see the steam drum getting filled up.
* Open Coolant valves for Aux Condensation.
* Reset RPS on Reactor - the red light should disappear.
* Pull out 4 manual (green) control rods
* Open rod positions panel, core matrix and neutron flux diagram
* Select all auto rods and pull them out until reactivity reaches 0,0015. Stop
them in that position before a max reactivity alarm pops up.
* Wait for the neutrons to rise, you can see this on the flux log gauge first
* Try to keep the neutron rate inside a limit (look at the gauge). At some 
* point, it will start to rise faster. Try to get a neutron flux of 4,0 %.
* Turn on global control (press enable, enable all switches on auto rods,
enable transient and target mode and push auto button). When the control is
active, it should stabilize the neutron flux at the setpoint of 4.0 %.
* Turn on one pump at aux condensation and open the condensate valve to
hotwell. Enable both level controls.
* If you have some steam pressure in steam drums, open the steam valve on
aux condensation and it will condense there. It will be fed back into the
reactor though condensation and feedwater path.
