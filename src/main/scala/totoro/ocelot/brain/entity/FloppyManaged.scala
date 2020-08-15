package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.{DiskManaged, Floppy}
import totoro.ocelot.brain.util.DyeColor

class FloppyManaged(address: String, name: String, color: DyeColor) extends Floppy(address, name, color) with DiskManaged
