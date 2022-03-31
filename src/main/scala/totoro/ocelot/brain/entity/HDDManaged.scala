package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.traits.{DiskManaged, Entity, Tiered}

class HDDManaged(override var tier: Int) extends Entity with DiskManaged with Tiered {
  def capacity: Int = Settings.get.hddSizes(tier) * 1024
  def speed: Int = tier + 2
}
