package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.traits.{DiskManaged, Entity, Tiered}

class HDDManaged(override var tier: Int) extends Entity with DiskManaged with Tiered {
  val capacity: Int = Settings.get.hddSizes(tier) * 1024
  val speed: Int = tier + 2
}
