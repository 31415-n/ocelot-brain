package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.fs.Label
import totoro.ocelot.brain.entity.traits.{DiskManaged, Floppy}
import totoro.ocelot.brain.util.DyeColor

class FloppyManaged(name: Option[String], color: DyeColor) extends Floppy(name, color) with DiskManaged {
  def this() = this(None, DyeColor.Gray)

  override lazy val label: Label = fileSystem.label
}
