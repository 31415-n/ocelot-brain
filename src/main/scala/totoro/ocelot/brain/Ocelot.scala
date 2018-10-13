package totoro.ocelot.brain

import java.io.File

import org.apache.logging.log4j.{LogManager, Logger}
import totoro.ocelot.brain.entity.{CPU, Cable, Case, EEPROM, EntityFactory, Memory}
import totoro.ocelot.brain.loot.Loot
import totoro.ocelot.brain.machine.luac.{LuaStateFactory, NativeLua52Architecture, NativeLua53Architecture}
import totoro.ocelot.brain.machine.luaj.LuaJLuaArchitecture
import totoro.ocelot.brain.machine.{MachineAPI, Registry}

object Ocelot {
  final val Name = "Ocelot"
  // do not forget to change the version in `build.sbt`
  final val Version = "0.2.2"

  def log: Logger = logger.getOrElse(LogManager.getLogger(Name))
  var logger: Option[Logger] = None

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
    EntityFactory.add(classOf[Cable])
    EntityFactory.add(classOf[Case])
    EntityFactory.add(classOf[CPU])
    EntityFactory.add(classOf[Memory])
    EntityFactory.add(classOf[EEPROM])

    log.info("Registering loot (floppies and EEPROMs with standart OpenComputers software)...")
    Loot.init()
  }

  private def init(): Unit = {}

  private def postInit(): Unit = {
    // Don't allow registration after this point, to avoid issues.
    Registry.locked = true
  }

  def initialize(): Unit = {
    log.info("Brain initialization...")
    preInit()
    init()
    postInit()
    log.info("Initialization finished.")
  }
}
