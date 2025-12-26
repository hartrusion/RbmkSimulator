# RBMK Simulator
A simulator app for the chornobyl RBMK recator. This aims to be a successor for 
the Simgenics rxmodel simulator from about 30 years ago but with a lot more 
detailed systems and a more accurate representation of the chernobyl plant.

**This is work in progress. The development is still in a very early stage.**

![Example](docs/images/screenshot-preview.png)

## Installation and Usage
To download the simulator, you need to grab the latest release from the
[Releases](https://github.com/hartrusion/RbmkSimulator/releases) section. 
Download the file  "RbmkSimulator.jar" file from _github-ci-nightly_ there.

<details>
  <summary>You also need Java installed (expand this for more info).</summary>
  
  You need a Java Runtime (at least v17.0.17) as this is ja Java application.

  To check if you already have java installed and configured on your system, 
  type
  ```
  java -version
  ```
  in Command Line or Terminal. It will return which version is installed or an
  error if there is no java available. In this case you have to download this
  by yourself.

  Any recent OpenJDK has a Runtime included, you can go for Eclipse Adoptium or
  the Microsoft Build of OpenJDK or any other of those. Just do not use the 
  Oracle/Sun Java JRE as this is the old Version 8. If you're a Linux user, you 
  should not have any issues getting a java runtime for your distribution.
</details>

To run the Simulator, extract the file from Package.zip and run it in terminal 
with

    java -jar RbmkSimulator.jar

**Why is it so complicated? Can't you just provide a link to an exe file?**
Yes and no, I would have to provide a link each time I do an update and you 
would download some executable file from a random dude on the web. This is 
something no sane person should do and most modern web browsers do not allow 
this without lots of warnings anyways. A .exe file would also be designed 
to run on Windows. What you're getting here instead is the output of an 
automated system that generates a new file each time I do any code changes. 
The process with all source code and all scripts is transparent so you can 
see exactly what steps are done and which source code is used to make the 
file you just downloaded.

You can find a short [Operating Manual](docs/OPERATING_MANUAL.md) here.

## Focus
Operating a power plant requires the operators to consider a large amount of 
data describing the actual plant stage and operate the correct valves, pumps 
or other elements. This simulator focuses on control loops, alarms and mainly 
the thermal layout of the plant to make you as an operator control all those
systems. To keep things simple, many systems are either simplified or they are 
not present at all.

## Features (and simplifications)
* Two steam drum separators (instead of 4)
* Simplified reactor with 5 automatic, 28 manual and 4 short control rods.
* Automatic reactor power regulator that requires manual overrides from time to
time.
* Reactor with nasty features, has some surprises on low power levels and the
accident can be triggered.
* Mnemonic displays with additional measurement data on them.
* Some line plots to monitor measurement time series

Still, there is lots of work to do, so currently there is **no turbine** and 
most coolant loops are connected to fixed cold water flow sources instead of 
having a secondary and technical coolant water circuit. It is not possible to 
operate the plant on full power as there is no turbine and no bypass at this
stage.

## Build from Source
To build the simulator from source, you need the source code from this repo
and some additional code that is released on different repositories:
* [PhxNetMod](https://github.com/hartrusion/PhxNetMod) Simulation Engine
* [PhxNetModExt](https://github.com/hartrusion/PhxNetModExt) Extensions for 
control loops
* [Utils](https://github.com/hartrusion/utils) Some commonly used classes
* [JMPLot](https://github.com/hartrusion/jmplot) A Matlab-like line plot library
* [AbsoluteLayout](https://mvnrepository.com/artifact/org.netbeans.external/AbsoluteLayout)
GUI Layout extension for Swing (this comes packed with NetBeans)

The GUI is designed using NetBeans GUI builder, those classes can be used 
with different IDEs but once edited outside of NetBeans, they propably won't
work anymore.

## Getting involved
As this project is in a very early stage, lots of architecture and design 
decisions have to be made and major refactorings do occur. I do believe that 
contributions might not be that useful in this early stage.

Please make yourself familiar with the rather unusual
[Code of Conduct](./CODE_OF_CONDUCT.md) first and read
[Contributing](./CONTRIBUTING.md) guidelines.

Discussions about this particular project shall be discussed here on GitHub 
only. No Z supporters wanted.

## Licencing and usage
I decided to publish this simulator with GPLv3 so this thing is free software. 
As I've also put in lots of thoughts on how to represent the plant with the 
GUI, I do want to keep the authorship and control over it. To make it possible 
to share the developed technology, large parts of the code in other repositories 
are published with MIT licence so you're free to reuse those parts for whatever 
you like.

## Modeling engine
The simulation is based on the PhxNetMod project which implements the theory of 
having similar linear ordinary differential equations in electronics, mechanics, 
hydraulics and thermal systems. The methods of network analysis can therefore 
be applied to other domains. The PhxNetMod core, along with other dependencies,
are available as separate projects.
