package totoro.ocelot.brain

import org.apache.logging.log4j.{LogManager, Logger}

object Ocelot {
  final val Name = "Ocelot"

  def log: Logger = logger.getOrElse(LogManager.getLogger(Name))
  var logger: Option[Logger] = None
}
