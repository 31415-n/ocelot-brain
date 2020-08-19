package totoro.ocelot.brain.entity.fs

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.workspace.Workspace

class ReadWriteLabel(private var label: String) extends Label {

  override def getLabel: String = label

  override def setLabel(value: String) {
    label = value
  }

  private final val LabelTag = "fs.label"

  override def load(nbt: NBTTagCompound, workspace: Workspace) {
    super.load(nbt, workspace)
    if (nbt.hasKey(LabelTag)) {
      label = nbt.getString(LabelTag)
    }
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    nbt.setString(LabelTag, label)
  }
}
