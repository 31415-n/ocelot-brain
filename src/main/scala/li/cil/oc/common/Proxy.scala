package li.cil.oc.common

import java.io.File

import li.cil.oc._
import li.cil.oc.common.capabilities.Capabilities
import li.cil.oc.common.init.Items
import li.cil.oc.common.item.traits.Delegate
import li.cil.oc.integration.Mods
import li.cil.oc.server._
import li.cil.oc.server.machine.luac.LuaStateFactory
import li.cil.oc.server.machine.luac.NativeLua52Architecture
import li.cil.oc.server.machine.luac.NativeLua53Architecture
import li.cil.oc.server.machine.luaj.LuaJLuaArchitecture
import net.minecraft.item.Item

import scala.collection.convert.WrapAsScala._

class Proxy {
  def preInit(): Unit = {
    checkForBrokenJavaVersion()

    Settings.load(new File("settings.conf"))

    OpenComputers.log.info("Initializing blocks and items.")

    Items.init()

    OpenComputers.log.info("Initializing OpenComputers API.")

    api.API.driver = driver.Registry
    api.API.fileSystem = fs.FileSystem
    api.API.items = Items
    api.API.machine = machine.Machine
    api.API.nanomachines = nanomachines.Nanomachines
    api.API.network = network.Network

    api.API.config = Settings.get.config

    if (LuaStateFactory.include52) {
      api.Machine.add(classOf[NativeLua52Architecture])
    }
    if (LuaStateFactory.include53) {
      api.Machine.add(classOf[NativeLua53Architecture])
    }
    if (api.Machine.architectures.size == 0) {
      api.Machine.add(classOf[LuaJLuaArchitecture])
    }
    api.Machine.LuaArchitecture = api.Machine.architectures.head
  }

  def init() {
    Loot.init()
    Achievement.init()

    OpenComputers.log.info("Initializing mod integration.")
    Mods.init()

    OpenComputers.log.info("Initializing capabilities.")
    Capabilities.init()
  }

  def postInit() {
    // Don't allow driver registration after this point, to avoid issues.
    driver.Registry.locked = true
  }

  def registerModel(instance: Delegate, id: String): Unit = {}

  def registerModel(instance: Item, id: String): Unit = {}

  // OK, seriously now, I've gotten one too many bug reports because of this Java version being broken.

  private final val BrokenJavaVersions = Set("1.6.0_65, Apple Inc.")

  def isBrokenJavaVersion: Boolean = {
    val javaVersion = System.getProperty("java.version") + ", " + System.getProperty("java.vendor")
    BrokenJavaVersions.contains(javaVersion)
  }

  def checkForBrokenJavaVersion(): Unit = if (isBrokenJavaVersion) {
    throw new Exception("You're using a broken Java version! Please update now, or remove Ocelot. DO NOT REPORT THIS! UPDATE YOUR JAVA!")
  }
}
