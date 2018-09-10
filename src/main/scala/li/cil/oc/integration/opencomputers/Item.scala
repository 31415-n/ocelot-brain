package li.cil.oc.integration.opencomputers

import li.cil.oc.{Settings, api}
import li.cil.oc.api.driver.DriverItem
import li.cil.oc.api.internal
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.Tier
import li.cil.oc.server.driver.Registry
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

trait Item extends DriverItem {
  def worksWith(stack: ItemStack, host: Class[_ <: EnvironmentHost]): Boolean =
    worksWith(stack) && !Registry.blacklist.exists {
      case (blacklistedStack, blacklistedHost) =>
        stack.isItemEqual(blacklistedStack) &&
          blacklistedHost.exists(_.isAssignableFrom(host))
    }

  override def tier(stack: ItemStack): Int = Tier.One

  override def dataTag(stack: ItemStack): NBTTagCompound = Item.dataTag(stack)

  protected def isOneOf(stack: ItemStack, items: api.detail.ItemInfo*): Boolean = items.filter(_ != null).contains(api.Items.get(stack))

  protected def isComputer(host: Class[_ <: EnvironmentHost]): Boolean = classOf[internal.Case].isAssignableFrom(host)
}

object Item {
  def dataTag(stack: ItemStack): NBTTagCompound = {
    if (!stack.hasTagCompound) {
      stack.setTagCompound(new NBTTagCompound())
    }
    val nbt = stack.getTagCompound
    if (!nbt.hasKey(Settings.namespace + "data")) {
      nbt.setTag(Settings.namespace + "data", new NBTTagCompound())
    }
    nbt.getCompoundTag(Settings.namespace + "data")
  }
}
