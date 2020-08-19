package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

/**
  * Basic trait for all data disks
  */
trait Disk extends Environment with Persistable {
  def capacity: Int
  def speed: Int

  // ----------------------------------------------------------------------- //

  protected var lockInfo: String = ""

  def isLocked: Boolean = isLocked(lockInfo)

  def isLocked(forLockInfo: String): Boolean = forLockInfo != null && !forLockInfo.isEmpty

  def setLocked(player: String): Unit = {
    val oldInfo = lockInfo
    this.lockInfo = player
    onLockChange(oldInfo)
  }

  /**
    * Will be called right after the old lock info was replaced with a new value.
    * @param oldLockInfo the old value of lock info
    */
  def onLockChange(oldLockInfo: String): Unit = {}

  // ----------------------------------------------------------------------- //

  override def load(nbt: NBTTagCompound, workspace: Workspace) {
    super.load(nbt, workspace)
    lockInfo = if (nbt.hasKey(Disk.LockTag)) {
      nbt.getString(Disk.LockTag)
    } else ""
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    nbt.setString(Disk.LockTag, lockInfo)
  }
}

object Disk {
  final val LockTag = "lock"
}
