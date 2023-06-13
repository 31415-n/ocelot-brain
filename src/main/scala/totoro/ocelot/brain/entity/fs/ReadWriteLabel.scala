package totoro.ocelot.brain.entity.fs

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.workspace.Workspace

class ReadWriteLabel(private var label: Option[String]) extends Label {
  def this() = {
    this(None)
  }

  override def getLabel: String = label.orNull

  override def labelOption: Option[String] = label

  override def setLabel(value: String): Unit = {
    label = Option(value)
  }

  private final val LabelTag = "fs.label"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    label = Option.when(nbt.hasKey(LabelTag))(nbt.getString(LabelTag))
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    for (label <- label) {
      nbt.setString(LabelTag, label)
    }
  }
}
