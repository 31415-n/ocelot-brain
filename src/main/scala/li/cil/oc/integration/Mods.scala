package li.cil.oc.integration

import li.cil.oc.integration

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Mods {
  private val handlers = mutable.Set.empty[ModProxy]

  private val knownMods = mutable.ArrayBuffer.empty[ModBase]

  // ----------------------------------------------------------------------- //

  def All: ArrayBuffer[ModBase] = knownMods.clone()
  val OpenComputers = new SimpleMod(IDs.OpenComputers)

  // ----------------------------------------------------------------------- //

  val Proxies = Array(
    // We go late to ensure all other mod integration is done, e.g. to
    // allow properly checking if wireless redstone is present.
    integration.opencomputers.ModOpenComputers
  )

  def init(): Unit = {
    for (proxy <- Proxies) {
      tryInit(proxy)
    }
  }

  private def tryInit(mod: ModProxy) {
    if ((mod.getMod == null || mod.getMod.isModAvailable) && handlers.add(mod)) {
      li.cil.oc.OpenComputers.log.debug(s"Initializing mod integration for '${mod.getMod.id}'.")
      try mod.initialize() catch {
        case e: Throwable =>
          li.cil.oc.OpenComputers.log.warn(s"Error initializing integration for '${mod.getMod.id}'", e)
      }
    }
  }

  // ----------------------------------------------------------------------- //

  object IDs {
    final val OpenComputers = "opencomputers"
  }

  // ----------------------------------------------------------------------- //

  trait ModBase extends Mod {
    knownMods += this

    def isModAvailable: Boolean

    def id: String
  }

  class SimpleMod(val id: String, version: String = "") extends ModBase {
    def isModAvailable: Boolean = true
  }

  class ClassBasedMod(val id: String, val classNames: String*) extends ModBase {
    private lazy val isModAvailable_ = classNames.forall(className => try Class.forName(className) != null catch {
      case _: Throwable => false
    })
    def isModAvailable: Boolean = isModAvailable_
  }
}
