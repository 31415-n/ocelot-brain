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
import li.cil.oc.server.component.UpgradeExperience
import net.minecraft.item.ItemStack

object DriverUpgradeExperience extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.ExperienceUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): UpgradeExperience =
    host match {
      case host: internal.Agent => new component.UpgradeExperience(host)
      case _ => null
    }

  override def slot(stack: ItemStack): String = Slot.Upgrade

  override def tier(stack: ItemStack): Int = Tier.Three

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack))
        classOf[component.UpgradeExperience]
      else null
  }
}
