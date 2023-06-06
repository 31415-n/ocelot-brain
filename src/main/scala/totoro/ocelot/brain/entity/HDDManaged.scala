package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.traits.{DiskManaged, Entity, TieredPersistable}
import totoro.ocelot.brain.event.FileSystemActivityType.{ActivityType, HDD}
import totoro.ocelot.brain.util.Tier.Tier

class HDDManaged(override var tier: Tier) extends Entity with DiskManaged with TieredPersistable {
  def capacity: Int = Settings.get.hddSizes(tier.id) * 1024
  def speed: Int = tier.num + 1

  def this(tier: Tier, address: String) = {
    this(tier)

    this.address = Option(address)
  }

  override val activityType: Option[ActivityType] = Some(HDD)
}
