package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{Label, ReadWriteLabel}
import totoro.ocelot.brain.entity.traits.FileSystemLike
import totoro.ocelot.brain.loot.Loot.FileSystemFactory

class FloppyDisk(name: String, override val lootFactory: FileSystemFactory = null) extends ManagedDisk with FileSystemLike {
  override val kiloBytes: Int = Settings.get.floppySize
  override val label: Label = new ReadWriteLabel(name)
  override val capacity: Int = kiloBytes * 1024
  override val platterCount: Int = 1
  override val speed: Int = 1
}
