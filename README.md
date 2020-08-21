## Ocelot Brain

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

Web client implementation: [Ocelot Online](https://gitlab.com/cc-ru/ocelot/ocelot-online)

Desktop client implementation: [Ocelot Desktop](https://gitlab.com/cc-ru/ocelot/ocelot-desktop)

### Corresponding OpenComputers version

Current `master` branch of Ocelot Brain corresponds to OC 1.7.5 (Nov 17, 2019).

### Build instructions

* Import the project into your favorite IDE with the last Scala and SBT installed
* Run the `sbt assemly` task to build JAR file with dependencies
* Take the JAR from `target/scala-x.xx/` folder

At the moment of development we've used Scala 2.13.

### Usage tips

The core can be configured using a `brain.conf` file.  
This file has the exact format of OpenComputers configuration file.

You can take one from your Minecraft configs (or copy from the
[`resources`](https://gitlab.com/cc-ru/ocelot/ocelot-brain/blob/master/src/main/resources/application.conf)
folder) and modify it as needed.  
All not used fields will be just ignored.

### Feedback

You can leave bug reports and feature requests in our [issue tracker](https://gitlab.com/cc-ru/ocelot/ocelot-brain/-/issues).

Also, most of the time Ocelot developers can be found in the [#CC.RU channel](https://webchat.esper.net/?join=cc.ru) of Esper IRC server.

### Credits

Ocelot Dev Team, the guys who made it possible:
* Totoro
* LeshaInc
* 140bpmdubstep
* Fingercomp
* rasongame
* MeXaN1cK

Thanks to everyone who helped us with feedback, feature requests and bug reports!

And at last but not least, thanks to OpenComputers team, who created this wonderful mod.

### Beware

This project is a *work-in-progress*, expect breaking changes or bugs
from time to time.
