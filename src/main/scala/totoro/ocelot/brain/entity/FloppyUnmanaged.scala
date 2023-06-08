package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.{DiskUnmanaged, Floppy}
import totoro.ocelot.brain.util.DyeColor

class FloppyUnmanaged(name: Option[String], color: DyeColor) extends Floppy(name, color) with DiskUnmanaged {
  def this() = this(None, DyeColor.Gray)

  val platterCount: Int = 1
}
