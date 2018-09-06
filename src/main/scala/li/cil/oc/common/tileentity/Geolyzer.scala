package li.cil.oc.common.tileentity

import li.cil.oc.api.network.Node
import li.cil.oc.server.component
import net.minecraft.nbt.NBTTagCompound

class Geolyzer extends traits.Environment {
  val geolyzer = new component.Geolyzer(this)

  def node: Node = geolyzer.node

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    geolyzer.load(nbt)
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    geolyzer.save(nbt)
  }
}
