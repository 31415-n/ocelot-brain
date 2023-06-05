## Ocelot Brain

The core of Ocelot project.  
This is a library that contains the internal code of OpenComputers mod
separated from its Minecraft origin. 

This means that you can use it to build your own OpenComputers emulator
for any platform (Linux, Windows, macOS and BSD when using JNLua, any other
platform that supports Java - using LuaJ fallback).  
For example the `ocelot-online` and `ocelot-desktop` projects use `ocelot-brain`
under the hood to do all the emulation work.

This repository contains some examples of the library usage in the
`totoro.ocelot.demo` package.

Web client implementation: [Ocelot Online](https://gitlab.com/cc-ru/ocelot/ocelot-online)

Desktop client implementation: [Ocelot Desktop](https://gitlab.com/cc-ru/ocelot/ocelot-desktop)

### Corresponding OpenComputers version

Current `master` branch of Ocelot Brain corresponds to OC 1.8.2 (June 4, 2023, 94f6405).

### Build instructions

* Import the project into your favorite IDE with fresh Scala and SBT installed
* Run `sbt assemly` task to build JAR file
* Take JAR file from the `target/scala-x.xx/` folder

At the moment we are using Scala 2.13.x.

### Usage tips

Ocelot Brain can be configured using a `brain.conf` file.

This file has the exact format of OpenComputers configuration file.  
You can take one from your Minecraft installation (or copy from the
[`resources`](https://gitlab.com/cc-ru/ocelot/ocelot-brain/blob/master/src/main/resources/application.conf)
folder) and rename it.

All fields that are applicable to Ocelot will be recognized and used.  
(All the rest, like Minecraft-specific stuff, will be ignored.)

### Feedback

You can leave bug reports and feature requests in our [issue tracker](https://gitlab.com/cc-ru/ocelot/ocelot-brain/-/issues).

Also, most of the time Ocelot developers can be found in the [#CC.RU channel](https://webchat.esper.net/?join=cc.ru) of Esper IRC server.

For those more comfortable with Discord there is [our Discord server](https://discord.com/invite/FM9qWGm).

(IRC and Discord are mostly in Russian, but we speak English too, as well as Ukrainian, Japanese and probably a couple of other languages.)

### Credits

Ocelot Dev Team, the guys who made it possible:
* Totoro
* LeshaInc
* 140bpmdubstep
* Fingercomp
* rasongame
* MeXaN1cK
* BrightYC
* ECS
* Smok1e

Thanks to everyone who helped us with feedback, feature requests and bug reports!

And at last but not least, thanks to OpenComputers team, who created this wonderful mod.

### Beware

This project is a *work-in-progress*, breaking changes and bugs are expected.  
