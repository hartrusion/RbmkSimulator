# Control loop architecture

Control loops are made of
* A Setpoint object that holds the current setpoint. These extend 
  SetpointIntegrator so they can change the setpoint using a gradient.
* A control element (PI or P control) which extends an abstract class
* An actuator that does something with the control output.

Sending commands is always handled through the MVC class. A GUI sends 
actionCommands to the MVC controller, which will allow the main loop to fire 
off calls on all elements of the main loop. No calculations are done at the
view / GUI level.

The ControlCommand holds a list of commands that are used in both directions.

## Assembly classes
Some assemblies already contain a model element and provide means to initialize 
and manage multiple objects at once to keep the codebase small.

### HeatValveControlled
This extends a HeatValve class which is itself an assembly that already 
provides means to control a valve element with commands.

A HeatValveControlled class with the Name **cmp** knows an instance to a 
controller (it can be created only to be known to this class). It will:

* Handle ActionCommands which are named **cmpControlCommand**, those are 
  output increase/decrease/stop for button usage and automatic and manual 
  operation commands.
* Fire PropertyChangeEvents which are named **cmpControlState**, those 
  indicate if we run in auto or manual mode.
* Send its valve opening named **cmp** to a ParameterHandler
* Send its valve opening events as PropertyChangeEvent **cmp_Pos** (OPEN/CLOSED)

The control input has to be coded with a DoubleSupplier as input provider. This
can be used to make the setpoint known.

### Setpoint
A Setpoint class with the Name **cmp** will do this:

* Send the setpoint value named **cmp** to a ParameterHandler
* Handle ActionCommands which are named **cmp** to handle button press 
  actions increase/decrease/stop.

Setpoint Control GUI class will send those setpoint commands and display the 
setpoint value. It is obvious that the setpoint must be named as such as it 
does not add a prefix to the component designator.

## GUI Classes
Gui classes can get their component String identifier set by a property in GUI
builders.

###ControlLoopValve
This thing has 4 buttons and one gauge and is known to be used as feedwater 
control. If the component is named **cmp**, it wil:

* Receive **cmpControlState** PropertyChangeEvents with AUTOMATIC and
MANUAL_OPERATION ControlCommand
* Receive **cmp** primitive double value and display it on the gauge
* Send **cmpControlCommand** with AUTOMATIC/MANUAL and OUTPUT INCREASE/DECREASE
/CONTINUE on button actions.

###ControlLoop
A common widget with 3 gauges for setpoint, current value and output value 
and some buttons. Allows full control and display of a control loop.
* Send **cmpControlCommand** for auto, manual and output increase/decrease
* Send **setpoint** for setpoint increase/decrease/stop
* Receive **cmp** primitive value and display it as Output Value
* Receive **cmpControlState** for lighting up a small bulb if on auto mode
* Receive **feedback** primitive value as Current value
* Receive **setpoint** as primitie value for current setpoint value.

Due to the nameing of cmpSetpoint, the Setpoint class has to be named with
the Setpoint-suffix in the main loop. This allows displaying the value and 
controlling the setpoint integrator with the same commands as the other classes.

## Example
Deaerator pressure gets regulated with a valve from main steam. This valve is a
PhasedValveControlled, that's the same as a HeatValveControlled but with a 
different PhysicalDomain.

There are 2 of those but to simplify, we will assume there is only one. 
Essential steps are:

mainSteamToDAValve.initController(new PIControl());
mainSteamToDAValve.initName("Main#SteamToDAValve");
mainSteamToDAValve.initSignalListener(controller);
mainSteamToDAValve.initParameterHandler(outputValues);

Valve Opening will be known in ParameterHandler as Main#SteamToDAValve. There
will be Main#SteamToDAValveControlCommand received as ActionCommands and
Main#SteamToDAValveControlState property change event will be send.

A setpoint instance will be used to set the pressure setpoint.

setpointDAPressure = new Setpoint();
setpointDAPressure.initName("Deaerator#PressureSetpoint");
setpointDAPressure.initParameterHandler(outputValues);

The Setpoint will be known in ParameterHandler as Deaerator#PressureSetpoint.
ActionCommands will be received also as Deaerator#PressureSetpoint to modify
the setpoint via the SWI.

On the GUI, a ControlLoop widget will be used. The properties to identify the
components in the main loop have to be set accordingly to the naming of those:
* **Deaerator#PressureSetpoint**
* **Main#SteamToDAValve**
* **Deaerator#Pressure**

This will make the widget send and process the correct commands and values.

The ControlLoop widget must be initialized and get a instance to a MVC 
controller object and the updateComponent method calls have to be redirected
to that widget.