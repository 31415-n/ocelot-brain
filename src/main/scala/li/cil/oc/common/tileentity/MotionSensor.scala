package li.cil.oc.common.tileentity

import li.cil.oc.api.network.Node
import li.cil.oc.server.component
import net.minecraft.nbt.NBTTagCompound

class MotionSensor extends traits.Environment {
  val motionSensor = new component.MotionSensor(this)

  def node: Node = motionSensor.node

  override def updateEntity() {
    super.updateEntity()
    motionSensor.update()
  }

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    motionSensor.load(nbt)
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    motionSensor.save(nbt)
  }
}
