package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.traits.{DiskManaged, Entity, MultiTiered}
import totoro.ocelot.brain.event.FileSystemActivityType.{ActivityType, HDD}

class HDDManaged(override var tier: Int) extends Entity with DiskManaged with MultiTiered {
  def capacity: Int = Settings.get.hddSizes(tier) * 1024
  def speed: Int = tier + 2

  def this(tier: Int, address: String) {
    this(tier)

    this.address = Option(address)
  }

  override val activityType: Option[ActivityType] = Some(HDD)
}
