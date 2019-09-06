package totoro.ocelot.brain

import java.io.File

import org.apache.logging.log4j.{LogManager, Logger}
import totoro.ocelot.brain.entity._
import totoro.ocelot.brain.entity.machine.{MachineAPI, Registry}
import totoro.ocelot.brain.entity.machine.luac.{LuaStateFactory, NativeLua52Architecture, NativeLua53Architecture}
import totoro.ocelot.brain.entity.machine.luaj.LuaJLuaArchitecture
import totoro.ocelot.brain.loot.Loot
import totoro.ocelot.brain.nbt.NBTPersistence
import totoro.ocelot.brain.nbt.NBTPersistence.TieredConstructor
import totoro.ocelot.brain.util.{FontUtils, ThreadPoolFactory}

object Ocelot {
  final val Name = "Ocelot"
  // do not forget to change the version in `build.sbt`
  final val Version = "0.3.2"

  def log: Logger = logger.getOrElse(LogManager.getLogger(Name))
  var logger: Option[Logger] = None

  /**
    * This `preInit`, `init`, `postInit` thing is a legacy from Minecraft/Forge life cycle.
    * It can be replaced with simple `initialization` procedure in the future.
    */

  private def preInit(): Unit = {
    log.info("Loading configuration...")
    Settings.load(new File("brain.conf"))
  }

  private def init(): Unit = {
    log.info("Registering available machine architectures...")
    if (LuaStateFactory.include52) {
      MachineAPI.add(classOf[NativeLua52Architecture], "Lua 5.2")
    }
    if (LuaStateFactory.include53) {
      MachineAPI.add(classOf[NativeLua53Architecture], "Lua 5.3")
    }
    if (MachineAPI.architectures.isEmpty) {
      MachineAPI.add(classOf[LuaJLuaArchitecture], "LuaJ")
    }

    log.info("Registering loot (floppies and EEPROMs with standard OpenComputers software)...")
    Loot.init()

    log.info("Registering entity constructors (for persistence purposes)...")
    val tieredConstructor = new TieredConstructor()
    NBTPersistence.registerConstructor(classOf[Case].getName, tieredConstructor)
    NBTPersistence.registerConstructor(classOf[CPU].getName, tieredConstructor)
    NBTPersistence.registerConstructor(classOf[Memory].getName, tieredConstructor)
    NBTPersistence.registerConstructor(classOf[GraphicsCard].getName, tieredConstructor)

    FontUtils.init()

    ThreadPoolFactory.safePools.foreach(_.newThreadPool())
  }

  private def postInit(): Unit = {
    // Don't allow registration after this point, to avoid issues.
    Registry.locked = true
  }

  def initialize(logger: Logger): Unit = {
    this.logger = Some(logger)
    initialize()
  }

  def initialize(): Unit = {
    log.info("Brain initialization...")
    log.info("Version: " + Ocelot.Version)
    preInit()
    init()
    postInit()
    log.info("Initialization finished.")
  }

  def shutdown(): Unit = {
    log.info("Preparing for Ocelot shutdown...")
    ThreadPoolFactory.safePools.foreach(_.waitForCompletion())
    log.info("Ocelot is shut down.")
  }
}
