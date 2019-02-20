package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{Label, ReadWriteLabel}
import totoro.ocelot.brain.entity.traits.{DiskManaged, Tiered}

class HDDManaged(var address: String, override var tier: Int, name: String) extends DiskManaged with Tiered {
  val label: Label = new ReadWriteLabel(name)
  val capacity: Int = Settings.get.hddSizes(tier) * 1024
  val speed: Int = tier + 2
}
