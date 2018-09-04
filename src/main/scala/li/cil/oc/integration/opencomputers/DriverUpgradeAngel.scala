package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.server.component
import net.minecraft.item.ItemStack

object DriverUpgradeAngel extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.AngelUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    new component.UpgradeAngel()

  override def slot(stack: ItemStack): String = Slot.Upgrade

  override def tier(stack: ItemStack): Int = Tier.Two
}
