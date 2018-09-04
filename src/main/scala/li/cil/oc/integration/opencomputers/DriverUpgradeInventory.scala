package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.driver.item.Inventory
import li.cil.oc.common.Slot
import net.minecraft.item.ItemStack

object DriverUpgradeInventory extends Item with Inventory with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.InventoryUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): Null = null

  override def slot(stack: ItemStack): String = Slot.Upgrade

  override def inventoryCapacity(stack: ItemStack) = 16
}
