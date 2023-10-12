package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

/**
 * Just keeps track of the last time the network messages was received.
 */
trait NetworkActivityAware extends Persistable {
  var lastNetworkAccess: Long = -1L

  def resetLastNetworkAccess(): Unit =
    lastNetworkAccess = System.currentTimeMillis()

  def shouldVisualizeNetworkActivity: Boolean =
    System.currentTimeMillis() - lastNetworkAccess < 300 && System.currentTimeMillis() % 200 > 100

  // ---------------------------- Persistable ----------------------------

  private final val lastNetworkAccessTag: String = "lastNetworkAccess"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    nbt.setLong(lastNetworkAccessTag, lastNetworkAccess)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    if (nbt.hasKey(lastNetworkAccessTag))
      lastNetworkAccess = nbt.getLong(lastNetworkAccessTag)
  }
}
