package li.cil.oc.common.tileentity

import li.cil.oc.api.network.Node
import li.cil.oc.server.component
import net.minecraft.nbt.NBTTagCompound

class Transposer extends traits.Environment {
  val transposer = new component.Transposer

  def node: Node = transposer.node

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    transposer.load(nbt)
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    transposer.save(nbt)
  }
}
