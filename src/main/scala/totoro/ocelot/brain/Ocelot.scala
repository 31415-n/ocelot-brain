package totoro.ocelot.brain

import org.apache.logging.log4j.{LogManager, Logger}
import totoro.ocelot.brain.machine.Registry

object Ocelot {
  final val Name = "Ocelot"

  def log: Logger = logger.getOrElse(LogManager.getLogger(Name))
  var logger: Option[Logger] = None

  def preInit(): Unit = {}

  def init(): Unit = {}

  def postInit(): Unit = {
    // Don't allow registration after this point, to avoid issues.
    Registry.locked = true
  }
}
