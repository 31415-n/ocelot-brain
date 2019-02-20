package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{Label, ReadWriteLabel}
import totoro.ocelot.brain.entity.traits.DiskManaged

class FloppyManaged(var address: String, name: String) extends DiskManaged {
  override val label: Label = new ReadWriteLabel(name)
  override val capacity: Int = Settings.get.floppySize * 1024
  override val speed: Int = 1
}
