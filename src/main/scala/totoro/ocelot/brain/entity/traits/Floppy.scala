package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{Label, ReadWriteLabel}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.DyeColor

abstract class Floppy(private var name: String, var color: DyeColor) extends Entity with Disk {
  val label: Label = new ReadWriteLabel(name)
  val capacity: Int = Settings.get.floppySize * 1024
  val speed: Int = 1

  def getName: String = name

  def setName(value: String): Unit = {
    this.name = value
    label.setLabel(value)
  }

  private val NameTag = "name"
  private val ColorTag = "color"

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setString(NameTag, name)
    nbt.setInteger(ColorTag, color.code)
  }

  override def load(nbt: NBTTagCompound): Unit = {
    super.load(nbt)
    setName(nbt.getString(NameTag))
    color = DyeColor.byCode(nbt.getInteger(ColorTag))
  }
}
