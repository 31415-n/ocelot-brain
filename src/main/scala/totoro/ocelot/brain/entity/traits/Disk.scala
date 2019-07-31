package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.entity.fs.Label
import totoro.ocelot.brain.entity.Environment
import totoro.ocelot.brain.nbt.NBTTagCompound

/**
  * Basic trait for all data disks
  */
trait Disk extends Environment {
  def label: Label
  def capacity: Int
  def speed: Int

  // ----------------------------------------------------------------------- //

  protected var lockInfo: String = ""

  def isLocked: Boolean = lockInfo != null && !lockInfo.isEmpty

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

  private val LockKey = "lock"

  override def load(nbt: NBTTagCompound) {
    lockInfo = if (nbt.hasKey(LockKey)) {
      nbt.getString(LockKey)
    } else ""
  }

  override def save(nbt: NBTTagCompound) {
    nbt.setString(LockKey, lockInfo)
  }
}
