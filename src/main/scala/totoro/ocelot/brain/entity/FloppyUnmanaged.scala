package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{Label, ReadWriteLabel}
import totoro.ocelot.brain.entity.traits.DiskUnmanaged

class FloppyUnmanaged(name: String) extends DiskUnmanaged {
  val label: Label = new ReadWriteLabel(name)
  val capacity: Int = Settings.get.floppySize * 1024
  val platterCount: Int = 1
  val speed: Int = 1
}
