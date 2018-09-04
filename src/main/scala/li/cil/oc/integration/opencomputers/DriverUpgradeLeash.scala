package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.server.component
import li.cil.oc.server.component.UpgradeLeash
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack

object DriverUpgradeLeash extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.LeashUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): UpgradeLeash =
    host match {
      case entity: Entity => new component.UpgradeLeash(entity)
      case _ => null
    }

  override def slot(stack: ItemStack): String = Slot.Upgrade

  override def tier(stack: ItemStack): Int = Tier.One

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack))
        classOf[component.UpgradeLeash]
      else null
  }
}
