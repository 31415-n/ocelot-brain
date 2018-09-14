package totoro.ocelot.brain

import java.io.File

import org.apache.logging.log4j.{LogManager, Logger}
import totoro.ocelot.brain.machine.{MachineAPI, Registry}
import totoro.ocelot.brain.machine.luac.{LuaStateFactory, NativeLua52Architecture, NativeLua53Architecture}
import totoro.ocelot.brain.machine.luaj.LuaJLuaArchitecture

object Ocelot {
  final val Name = "Ocelot"
  final val Version = "0.1.0"

  def log: Logger = logger.getOrElse(LogManager.getLogger(Name))
  var logger: Option[Logger] = None

  private def preInit(): Unit = {
    log.info("Loading configuration...")
    Settings.load(new File("settings.conf"))

    log.info("Registering available machine architectures...")
    if (LuaStateFactory.include53) {
      MachineAPI.add(classOf[NativeLua53Architecture])
    }
    if (LuaStateFactory.include52) {
      MachineAPI.add(classOf[NativeLua52Architecture])
    }
    if (MachineAPI.architectures.size == 0) {
      MachineAPI.add(classOf[LuaJLuaArchitecture])
    }
  }

  private def init(): Unit = {}

  private def postInit(): Unit = {
    // Don't allow registration after this point, to avoid issues.
    Registry.locked = true
  }

  def initialize(): Unit = {
    preInit()
    init()
    postInit()
  }
}
