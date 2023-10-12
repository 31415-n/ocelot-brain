package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

/**
 * Just keeps track of the last time the disk was accessed.
 */
trait DiskActivityAware extends Persistable {
  var lastDiskAccess: Long = -1L

  def resetLastDiskAccess(): Unit =
    lastDiskAccess = System.currentTimeMillis()

  def shouldVisualizeDiskActivity: Boolean =
    System.currentTimeMillis() - lastDiskAccess < 400 && Math.random() > 0.1

  // ---------------------------- Persistable ----------------------------

  private final val lastDiskAccessTag: String = "lastDiskAccess"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    nbt.setLong(lastDiskAccessTag, lastDiskAccess)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    if (nbt.hasKey(lastDiskAccessTag))
      lastDiskAccess = nbt.getLong(lastDiskAccessTag)
  }
}
