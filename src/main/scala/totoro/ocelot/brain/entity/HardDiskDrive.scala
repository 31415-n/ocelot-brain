package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.traits.{FileSystemLike, Tiered}

class HardDiskDrive(override var tier: Int) extends FileSystemLike with Tiered {
  val kiloBytes: Int = Settings.get.hddSizes(tier)
  val platterCount: Int = Settings.get.hddPlatterCounts(tier)
}
