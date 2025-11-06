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
* Send **cmpSetpoint** for setpoint increase/decrease/stop
* Receive **cmp** primitive value and display it as Output Value
* Receive **feedback** primitive value as Current value
* Receive **cmpSetpoint** as primitie value for current setpoint value.

Due to the nameing of cmpSetpoint, the Setpoint class has to be named with
the Setpoint-suffix in the main loop. This allows displaying the value and 
controlling the setpoint integrator with the same commands as the other classes.