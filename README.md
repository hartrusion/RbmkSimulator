# RBMK Simulator
A simulator for the chernobyl RBMK reactor. This aims to be a successor to 
the famous RXMODEL simulator from about 30 years ago but with a lot more 
detailed systems and a more accurate representation of the chernobyl plant.

**This is work in progress.**

Why Java? Java might be the worst choice for real time applications, but it
provides very convenient ways of using object orientated programming and runs on
a variety of computers, providing the same experience over different 
architectures and operating systems.

## Modeling
Focus of this project is to model the behaviour of all those pumps, valves, 
control loops and heat exchangers and the thermal layout of the plant. For now, 
the reactor is only a very simplified model that mimics a certain behaviour 
but does not have scientific derivations of equations.

The positive feedback loop that led to the accident is part of the simulator 
model. 

## Getting involved
As this project is in a very early stage, lots of architecture and design 
decisions have to be made and major refactorings do occur.

Please make yourself familiar with the rather unusual
[Code of Conduct](./CODE_OF_CONDUCT.md) first and read
[Contributing](./CONTRIBUTING.md) guidelines.

Discussions about this particular project shall be discussed here on GitHub 
only.

## Modeling engine
The simulation is based on the PhxNetMod project which implements the theory of 
having similar linear ordinary differential equations in electronics, mechanics, 
hydraulics and thermal systems. The methods of network analysis can therefore 
be applied to other domains. The PhxNetMod core is available as a separate 
project.