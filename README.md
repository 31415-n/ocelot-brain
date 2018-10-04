## ocelot-brain

The core of Ocelot project.  
This is a library that contains the internal code of OpenComputers mod
separated from it's Minecraft origin. 

This means that you can use it to build your own OpenComputers emulator
for any platform (Linux, Windows, Mac OS and BSD - using JNLua, any other
platform that supports Java - using LuaJ fallback).  
For example the `ocelot-online` project uses `ocelot-brain` under the hood
to do all the emulation work on backend.

This repository contains some examples of the library usage in the
`totoro.ocelot.demo` package.

### build instructions

* Import the project into your favorite IDE with the last Scala and SBT installed
* Run the `sbt assemly` task to build JAR file with dependencies
* Take the JAR from `target/scala-x.xx/` folder

At the moment of development I've used Scala 2.12.

**P.S.** This project is a *work-in-progress*, breaking changes and bugs will break
things from time to time. Beware.
