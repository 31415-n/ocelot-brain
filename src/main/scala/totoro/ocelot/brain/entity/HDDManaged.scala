package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.traits.{DiskManaged, Entity, Tiered}
import totoro.ocelot.brain.event.FileSystemActivityType.{ActivityType, HDD}

class HDDManaged(override var tier: Int, address: String = null) extends Entity with DiskManaged with Tiered {
  def capacity: Int = Settings.get.hddSizes(tier) * 1024
  def speed: Int = tier + 2

  _address = Option(address)

  override val activityType: Option[ActivityType] = Some(HDD)
}
