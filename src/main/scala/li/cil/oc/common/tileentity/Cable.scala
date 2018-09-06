package li.cil.oc.common.tileentity

import li.cil.oc.api
import li.cil.oc.api.network.{Node, Visibility}
import li.cil.oc.Constants
import net.minecraft.item.ItemStack

class Cable extends traits.Environment {
  val node: Node = api.Network.newNode(this, Visibility.None).create()

  def createItemStack(): ItemStack = {
    api.Items.get(Constants.BlockName.Cable).createItemStack(1)
  }
}
