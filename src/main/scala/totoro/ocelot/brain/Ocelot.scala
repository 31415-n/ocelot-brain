package totoro.ocelot.brain

import java.io.File

import org.apache.logging.log4j.{LogManager, Logger}
import totoro.ocelot.brain.entity._
import totoro.ocelot.brain.loot.Loot
import totoro.ocelot.brain.machine.luac.{LuaStateFactory, NativeLua52Architecture, NativeLua53Architecture}
import totoro.ocelot.brain.machine.luaj.LuaJLuaArchitecture
import totoro.ocelot.brain.machine.{MachineAPI, Registry}
import totoro.ocelot.brain.util.ThreadPoolFactory

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

    log.info("Registering available entities (to be able to persist them later)...")
    EntityFactory.add(classOf[APU])
    EntityFactory.add(classOf[Cable])
    EntityFactory.add(classOf[Case])
    EntityFactory.add(classOf[CPU])
    EntityFactory.add(classOf[DataCard.Tier1])
    EntityFactory.add(classOf[DataCard.Tier2])
    EntityFactory.add(classOf[DataCard.Tier3])
    EntityFactory.add(classOf[HDDUnmanaged])
    EntityFactory.add(classOf[HDDManaged])
    EntityFactory.add(classOf[EEPROM])
    EntityFactory.add(classOf[FloppyManaged])
    EntityFactory.add(classOf[FloppyUnmanaged])
    EntityFactory.add(classOf[FloppyDiskDrive])
    EntityFactory.add(classOf[GraphicsCard])
    EntityFactory.add(classOf[InternetCard])
    EntityFactory.add(classOf[Keyboard])
    EntityFactory.add(classOf[LinkedCard])
    EntityFactory.add(classOf[Memory])
    EntityFactory.add(classOf[NetworkCard])
    EntityFactory.add(classOf[Redstone.Tier1])
    EntityFactory.add(classOf[Redstone.Tier2])
    EntityFactory.add(classOf[Screen])
    EntityFactory.add(classOf[WirelessNetworkCard])

    log.info("Registering loot (floppies and EEPROMs with standart OpenComputers software)...")
    Loot.init()
  }

  private def init(): Unit = {
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
