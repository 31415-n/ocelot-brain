package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.entity.fs.Label
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable

/**
  * Basic trait for all data disks
  */
trait Disk extends Environment with Persistable {
  def label: Label
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
  private val LockTag = "lock"
  private val LabelTag = "label"

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)
    lockInfo = if (nbt.hasKey(LockTag)) {
      nbt.getString(LockTag)
    } else ""
    label.setLabel(nbt.getString(LabelTag))
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    nbt.setString(LockTag, lockInfo)
    nbt.setString(LabelTag, label.getLabel)
  }
}
