package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.util.{Persistable, Tier}
import totoro.ocelot.brain.workspace.Workspace

/**
  * Provides a persisted tier value.
  *
  * Differs from [[Tiered]] in that the latter does not necessarily entail tier persistence.
  * You only need this trait if you manage multiple tiers in a single class.
  * Thus [[GenericGPU]] is [[TieredPersistable]] whereas [[totoro.ocelot.brain.entity.DataCard]]s are merely [[Tiered]].
  */
trait TieredPersistable extends Tiered with Persistable {
  var tier: Tier

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    tier = Tier(nbt.getByte(TieredPersistable.TierTag))
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    nbt.setByte(TieredPersistable.TierTag, tier.id.toByte)
  }
}

object TieredPersistable {
  final val TierTag = "tier"
}
