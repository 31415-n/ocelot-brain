package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.Label
import totoro.ocelot.brain.entity.traits.FileSystemLike

class FloppyDisk(label: Label) extends FileSystemLike {
  override def kiloBytes: Int = Settings.get.floppySize
}
