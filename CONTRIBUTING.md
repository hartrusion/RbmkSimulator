# Contributing
Thank you for going that far reading this and your will to assist in the 
project. I try to use the [Issue](https://github.com/hartrusion/RbmkSimulator/issues) 
section as a main development roadmap and overview. Everything you find there is 
likely to be implemented sooner or later.

## Non-coding contributions
There are things you can contribute to the project:

* Testing: Besides known bugs listed in the issues section, the simulation 
  should be stable in all conditions. Reports on how to crash the simulation 
  with a safe file attached are very welcome
* Operators feedback: If you encounter things that never ever under no 
  circumstances would be operated like this, please also report them
* Suggestions and ideas: If you have some ideas on how to use the project but 
  you find some things missing, feel free to open an issue with a feature 
  request.

## Community management 
To make a clear statement, I do not want to have a community build around this 
project (like Discord, forums and so on) as I do not have time to manage all 
this.

## Documentation
Documenting things is also not recommmended at the moment as there are still too 
much changes and every document would be outdated soon. There will be a time 
when this line here will be removed. It is in your own interest to not publish 
information that might be outdated soon, so please, be patient. 

## Coding contributions
As this is a full scale dynamic simulation, certain things in code might not be
the way you would expect them to be. For most things, there are reasons or some 
things might be the way they are cause I do not know any better.

1. There are no low-hanging fruits. A lot of things are not implemented as 
   there is no proper scalable architecture.
2. No external dependencies. This is a personal thing, I do not like this 
   project to become a blackbox mess of libraries. Yes, there are great charts 
   that are way better than my used JMPlot package.
3. Code must be easy to read and understand. A lot of class fields might be 
   unnecessary and can be considered obsolete, however, sometimes they also 
   serve as a description of what is actually going on or JavaDoc is used on 
   class fields. It also helps during debugging. The main thermal layout serves 
   not only as a java class but also as a documentation on how the plant is 
   modeled.
4. Please try to keep the short line length limit. It allows to have two pages 
   of code next to each other on a 1080p screen.
5. A lot of modeling is done that way for a reason (keeping it possible to 
   calculate even in extreme conditions is one such thing).

If you find obvious wrong things or you can fix an issue or bug yourself, you 
can try to create a pull request directly but I do recommend to create an issue 
first so it can be discussed.

## Source code and build
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