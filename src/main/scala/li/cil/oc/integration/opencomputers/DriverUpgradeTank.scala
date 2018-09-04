package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.Slot
import li.cil.oc.server.component
import li.cil.oc.server.component.UpgradeTank
import net.minecraft.item.ItemStack

object DriverUpgradeTank extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.TankUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): UpgradeTank =
    new component.UpgradeTank(host, 16000)

  override def slot(stack: ItemStack): String = Slot.Upgrade
}
