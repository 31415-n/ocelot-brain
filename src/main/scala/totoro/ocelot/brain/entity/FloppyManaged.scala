package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.{DiskManaged, Floppy}
import totoro.ocelot.brain.util.DyeColor

class FloppyManaged(name: String, color: DyeColor) extends Floppy(name, color) with DiskManaged {
  def this() = this("noname", DyeColor.Gray)
}
