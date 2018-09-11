package li.cil.oc.common.tileentity.traits

import li.cil.oc.{Settings, api}
import li.cil.oc.api.internal
import li.cil.oc.api.network.Node
import li.cil.oc.integration.opencomputers.DriverScreen
import net.minecraft.nbt.NBTTagCompound

trait TextBuffer extends Environment {
  lazy val buffer: internal.TextBuffer = {
    val buffer = DriverScreen.createEnvironment(null, this).asInstanceOf[api.internal.TextBuffer]
    val (maxWidth, maxHeight) = Settings.screenResolutionsByTier(tier)
    buffer.setMaximumResolution(maxWidth, maxHeight)
    buffer.setMaximumColorDepth(Settings.screenDepthsByTier(tier))
    buffer
  }

  override def node: Node = buffer.node

  def tier: Int

  override def updateEntity() {
    super.updateEntity()
    if (isConnected) {
      buffer.update()
    }
  }

  // ----------------------------------------------------------------------- //

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    buffer.load(nbt)
  }

  override def writeToNBT(nbt: NBTTagCompound): Unit = {
    super.writeToNBT(nbt)
    buffer.save(nbt)
  }
}
