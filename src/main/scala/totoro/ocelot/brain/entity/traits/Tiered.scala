package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

/**
  * This is implemented by most things that are tiered in some way.
  *
  * For example, this is implemented by screens, computer cases, robots and
  * drones as well as microcontrollers. If you want you can add tier specific
  * behavior this way.
  */
trait Tiered extends Persistable {
  /**
    * The zero-based tier of this... thing.
    *
    * For example, a tier one screen will return 0 here, a tier three screen
    * will return 2.
    */
  var tier: Int

  // ----------------------------------------------------------------------- //

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    tier = nbt.getByte(Tiered.TierTag)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setByte(Tiered.TierTag, tier.toByte)
  }
}

object Tiered {
  final val TierTag = "tier"
}
