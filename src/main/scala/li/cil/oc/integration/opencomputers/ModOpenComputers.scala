package li.cil.oc.integration.opencomputers

import li.cil.oc.api
import li.cil.oc.integration.{ModProxy, Mods}

object ModOpenComputers extends ModProxy {
  override def getMod: Mods.SimpleMod = Mods.OpenComputers

  override def initialize() {
//    api.IMC.registerProgramDiskLabel("build", "builder", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("dig", "dig", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("base64", "data", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("deflate", "data", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("gpg", "data", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("inflate", "data", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("md5sum", "data", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("sha256sum", "data", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("refuel", "generator", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("irc", "irc", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("maze", "maze", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("arp", "network", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("ifconfig", "network", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("ping", "network", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("route", "network", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("opl-flash", "openloader", "Lua 5.2", "Lua 5.3", "LuaJ")
//    api.IMC.registerProgramDiskLabel("oppm", "oppm", "Lua 5.2", "Lua 5.3", "LuaJ")

//    MinecraftForge.EVENT_BUS.register(EventHandler)
//    MinecraftForge.EVENT_BUS.register(SimpleComponentTickHandler.Instance)
//    MinecraftForge.EVENT_BUS.register(EventHandler)
//    MinecraftForge.EVENT_BUS.register(FileSystemAccessHandler)
//    MinecraftForge.EVENT_BUS.register(Loot)
//    MinecraftForge.EVENT_BUS.register(NetworkActivityHandler)
//    MinecraftForge.EVENT_BUS.register(SaveHandler)
//    MinecraftForge.EVENT_BUS.register(Waypoints)
//    MinecraftForge.EVENT_BUS.register(WirelessNetwork)
//    MinecraftForge.EVENT_BUS.register(WirelessNetworkCardHandler)
//    MinecraftForge.EVENT_BUS.register(li.cil.oc.server.ComponentTracker)

    api.Driver.add(DriverAPU)
    api.Driver.add(DriverCPU)
    api.Driver.add(DriverDataCard)
    api.Driver.add(DriverEEPROM)
    api.Driver.add(DriverFileSystem)
    api.Driver.add(DriverGraphicsCard)
    api.Driver.add(DriverInternetCard)
    api.Driver.add(DriverLinkedCard)
    api.Driver.add(DriverMemory)
    api.Driver.add(DriverNetworkCard)
    api.Driver.add(DriverKeyboard)
    api.Driver.add(DriverRedstoneCard)
    api.Driver.add(DriverWirelessNetworkCard)

    api.Driver.add(DriverContainerFloppy)

    api.Driver.add(DriverGeolyzer)
    api.Driver.add(DriverMotionSensor)
    api.Driver.add(DriverScreen)

    api.Driver.add(DriverAPU.Provider)
    api.Driver.add(DriverDataCard.Provider)
    api.Driver.add(DriverEEPROM.Provider)
    api.Driver.add(DriverGraphicsCard.Provider)
    api.Driver.add(DriverInternetCard.Provider)
    api.Driver.add(DriverLinkedCard.Provider)
    api.Driver.add(DriverNetworkCard.Provider)
    api.Driver.add(DriverRedstoneCard.Provider)
    api.Driver.add(DriverWirelessNetworkCard.Provider)

    api.Driver.add(DriverGeolyzer.Provider)
    api.Driver.add(DriverMotionSensor.Provider)
    api.Driver.add(DriverScreen.Provider)
  }
}
