package li.cil.oc.common.tileentity

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.network.{ManagedEnvironment, Node}
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.nbt.NBTTagCompound

class Keyboard extends traits.Environment {

  val keyboard: ManagedEnvironment = {
    val keyboardItem = api.Items.get(Constants.BlockName.Keyboard).createItemStack(1)
    api.Driver.driverFor(keyboardItem, getClass).createEnvironment(keyboardItem, this)
  }

  override def node: Node = keyboard.node

  // ----------------------------------------------------------------------- //

  private final val KeyboardTag = Settings.namespace + "keyboard"

  override def readFromNBT(nbt: NBTTagCompound) {
    keyboard.load(nbt.getCompoundTag(KeyboardTag))
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    nbt.setNewCompoundTag(KeyboardTag, keyboard.save)
  }
}
