package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{Label, ReadWriteLabel}
import totoro.ocelot.brain.event.FileSystemActivityType.{ActivityType, Floppy}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.DyeColor
import totoro.ocelot.brain.workspace.Workspace

abstract class Floppy(private var _name: Option[String], var color: DyeColor) extends Entity with Disk {
  lazy val label: Label = new ReadWriteLabel(_name)
  val capacity: Int = Settings.get.floppySize * 1024
  val speed: Int = 1

  override val activityType: Option[ActivityType] = Some(Floppy)

  def name: Option[String] = _name

  def name_=(value: Option[String]): Unit = {
    this._name = value

    try {
      label.setLabel(value.orNull)
    } catch {
      case _: IllegalArgumentException =>
        // this ain't no python but it works
        // (rationale: we want to update the label unless we can't because it's read-only)
    }
  }

  private val NameTag = "name"
  private val ColorTag = "color"

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    for (name <- _name) {
      nbt.setString(NameTag, name)
    }

    nbt.setInteger(ColorTag, color.code)
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    _name = Option.when(nbt.hasKey(NameTag))(nbt.getString(NameTag))
    color = DyeColor.byCode(nbt.getInteger(ColorTag))
  }
}
