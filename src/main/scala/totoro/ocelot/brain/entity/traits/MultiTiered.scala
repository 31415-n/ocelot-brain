package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

/**
  * This thing is inherited by everything that has several tiers and
  * somehow changes its behavior depending on the tier
  *
  * For example, by GPUs, CPUs, Network Cards, etc.
  */
trait MultiTiered extends Tiered with Persistable {
  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    tier = nbt.getByte(MultiTiered.TierTag)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    nbt.setByte(MultiTiered.TierTag, tier.toByte)
  }
}

object MultiTiered {
  final val TierTag = "tier"
}
