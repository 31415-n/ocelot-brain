package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.machine.Architecture
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

/**
  * Use this interface to implement item drivers extending the number of
  * components a server can control.
  *
  * Note that the item must be installed in the actual server's inventory to
  * work. If it is installed in an external inventory the server will not
  * recognize the memory.
  */
trait Processor extends CallBudget with Persistable {
  /**
    * The additional number of components supported if this processor is
    * installed in the server.
    *
    * @return the number of additionally supported components.
    */
  def supportedComponents: Int

  protected var _architecture: Class[_ <: Architecture] = _

  /**
    * The architecture of this CPU.
    *
    * This usually controls which architecture is created for a machine the
    * CPU is installed in (this is true for all computers built into OC, such
    * as computer cases, server racks and robots, it my not be true for third-
    * party computers).
    *
    * @return the type of this CPU's architecture.
    */
  def architecture: Class[_ <: Architecture] = _architecture

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    if (nbt.hasKey(Processor.ArchTag)) {
      val archClass = nbt.getString(Processor.ArchTag)
      if (!archClass.isEmpty) try
        _architecture = Class.forName(archClass).asSubclass(classOf[Architecture])
      catch {
        case t: Throwable =>
          Ocelot.log.warn("Failed getting class for CPU architecture. Resetting CPU to use the default.", t)
      }
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setString(Processor.ArchTag, architecture.getName)
  }
}

object Processor {
  val ArchTag = "archClass"
}
