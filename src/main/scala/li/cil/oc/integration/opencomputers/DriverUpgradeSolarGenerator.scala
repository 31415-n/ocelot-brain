package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.server.component
import li.cil.oc.server.component.UpgradeSolarGenerator
import net.minecraft.item.ItemStack

object DriverUpgradeSolarGenerator extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.SolarGeneratorUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): UpgradeSolarGenerator =
    new component.UpgradeSolarGenerator(host)

  override def slot(stack: ItemStack): String = Slot.Upgrade

  override def tier(stack: ItemStack): Int = Tier.Two
}
