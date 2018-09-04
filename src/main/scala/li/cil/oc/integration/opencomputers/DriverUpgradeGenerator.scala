package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.internal
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.server.component
import li.cil.oc.server.component.UpgradeGenerator
import net.minecraft.item.ItemStack

object DriverUpgradeGenerator extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.GeneratorUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): UpgradeGenerator =
    host match {
      case host: internal.Agent => new component.UpgradeGenerator(host)
      case _ => null
    }

  override def slot(stack: ItemStack): String = Slot.Upgrade

  override def tier(stack: ItemStack): Int = Tier.Two

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack))
        classOf[component.UpgradeGenerator]
      else null
  }
}
