# Contributing
Thank you for going that far reading this and your will to assist in the 
project. I try to use the [Issue](https://github.com/hartrusion/RbmkSimulator/issues) 
section to track the projects issues in a limited scope, but besides that, 
there's no public development milestone thingy.

## A few words on interaction and working together
Open Source means open source code. While I enjoy the company of nice and 
friendly people and really do like to interact with such, I won't spend my 
limited time with toxic people which I can't stand. This thing is already a few 
hundred hours of unpaid work and I do not own anything to anyone. There's a 
[Code of Conduct](./CODE_OF_CONDUCT.md) that states things that are very 
important to me.

## Non-coding contributions
The far best thing you can contribute to the project is using it and report back
anything that does not work for you or things you do not understand. This helps 
me shape the simulator towards a better end-user experience.

* Testing: Besides known bugs listed in the issues section, the simulation 
  should be stable in all conditions. Reports on how to crash the simulation 
  with a safe file attached are very welcome!
* Operators feedback: If you encounter things that never ever under no 
  circumstances would be operated like this, please report them.
* Suggestions and ideas: If you have some ideas on how to use the project but 
  you find some things missing, feel free to open an issue with a feature 
  request.

I want to personally point out that I welcome all questions and issues as I do 
consider them as a general feedback on how intuitive the app is. You're 
welcome to ask as many questions as you like. You can contact me on any way 
you want.

## Community management 
To make a clear statement, I do not want to have a community build around this 
project (like Discord, forums and so on) as I do not have time to manage all 
this. There are some great communites around chornobyl already.

## Documentation
Documenting things is also not recommmended at the moment as there are still too 
much changes and every document would be outdated soon. There will be a time 
when this line here will be removed. It is in your own interest to not publish 
information that might be outdated soon, so please, be patient. 

## Coding contributions
As this is a full scale dynamic simulation, certain things in code might not be
the way you would expect them to be. For most things, there are reasons or some 
things might be the way they are cause I do not know any better.

The code style is matched to the concept of having a cyclic run full dynamic 
model simulation with a GUI connected, this might be very different from other 
applications out there.
   
I require coding contributions to absolutely match into the project. Full LLM 
generated code usually works as those LLM tools also read the extensive comments 
all around the code but they still fail to scale it in the proper way.

Best thing however is not to contribute code. It is open source, not open for 
all contributions. 

## Source code and build
To build the simulator from source, you need the source code from this repo
and some additional code that is released on different repositories:
* [PhxNetMod](https://github.com/hartrusion/PhxNetMod) Simulation engine with
  control loops and automation classes. This is the simulation core an holds 
  most of the actual source code.
* [Utils](https://github.com/hartrusion/utils) Some commonly used classes
* [JMPLot](https://github.com/hartrusion/jmplot) A Matlab-like line plot library
* [AbsoluteLayout](https://mvnrepository.com/artifact/org.netbeans.external/AbsoluteLayout)
GUI Layout extension for Swing (this comes packed with NetBeans)

Currently, those 3 packages have github actions that gerate unversioned 
snapshot builds and publish those as github maven packages. The release action
here uses maven to resolve those dependencies and an ant script to build the 
project.

There's a setup script available that automatically generates a VS Code multi
project workspace. You can clone this repository in a folder, for example 
SimulatorWorkspace/RbmkSimulator and run the setup_vsc_workspace.ps1/sh script. 
It will set up the SimulatorWorkspace as a workspace with all sources.

The GUI is designed using NetBeans GUI builder, those classes can be used 
with different IDEs but once edited outside of NetBeans, they propably won't
work anymore.
