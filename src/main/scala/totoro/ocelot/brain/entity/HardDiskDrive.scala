package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{Label, ReadWriteLabel}
import totoro.ocelot.brain.entity.traits.{FileSystemLike, ManagedDisk, Tiered}

class HardDiskDrive(override var tier: Int, name: String) extends ManagedDisk with FileSystemLike with Tiered {
  override val kiloBytes: Int = Settings.get.hddSizes(tier)
  override val label: Label = new ReadWriteLabel(name)
  override val capacity: Int = kiloBytes * 1024
  override val platterCount: Int = Settings.get.hddPlatterCounts(tier)
  override val speed: Int = tier + 2

  setManaged(true)
}
