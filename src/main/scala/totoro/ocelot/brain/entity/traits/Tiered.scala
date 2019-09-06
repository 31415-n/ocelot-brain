package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable

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

  private final val TierTag = "tier"

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)
    tier = nbt.getByte(TierTag) max 0 min 3
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    nbt.setByte(TierTag, tier.toByte)
  }
}
