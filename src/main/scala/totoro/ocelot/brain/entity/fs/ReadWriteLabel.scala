package totoro.ocelot.brain.entity.fs

import totoro.ocelot.brain.nbt.NBTTagCompound

private class ReadWriteLabel(private var label: String) extends Label {

  override def getLabel: String = label

  override def setLabel(value: String) {
    label = value
  }

  private final val LabelTag = "fs.label"

  override def load(nbt: NBTTagCompound) {
    if (nbt.hasKey(LabelTag)) {
      label = nbt.getString(LabelTag)
    }
  }

  override def save(nbt: NBTTagCompound) {
    nbt.setString(LabelTag, label)
  }
}
