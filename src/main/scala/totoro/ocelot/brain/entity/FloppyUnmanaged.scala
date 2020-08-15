package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.{DiskUnmanaged, Floppy}
import totoro.ocelot.brain.util.DyeColor

class FloppyUnmanaged(address: String, name: String, color: DyeColor) extends Floppy(address, name, color) with DiskUnmanaged {
  val platterCount: Int = 1
}
