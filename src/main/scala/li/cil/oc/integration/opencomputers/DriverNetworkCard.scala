package li.cil.oc.integration.opencomputers

import li.cil.oc.{Constants, api}
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.Slot
import li.cil.oc.server.component
import li.cil.oc.server.component.NetworkCard
import net.minecraft.item.ItemStack

object DriverNetworkCard extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.NetworkCard))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): NetworkCard =
    new component.NetworkCard(host)

  override def slot(stack: ItemStack): String = Slot.Card

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack))
        classOf[component.NetworkCard]
      else null
  }
}
