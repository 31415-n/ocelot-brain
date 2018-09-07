package li.cil.oc

import li.cil.oc.common.Proxy
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object OpenComputers {
  final val ID = "opencomputers"
  final val Name = "OpenComputers"
  final val Version = "@VERSION@"

  def log: Logger = logger.getOrElse(LogManager.getLogger(Name))
  var logger: Option[Logger] = None

  var proxy: Proxy = _

  def init(): Unit = {
    proxy.preInit()
    OpenComputers.log.info("Done with pre init phase.")
    proxy.init()
    OpenComputers.log.info("Done with init phase.")
    proxy.postInit()
    OpenComputers.log.info("Done with post init phase.")
  }
}
