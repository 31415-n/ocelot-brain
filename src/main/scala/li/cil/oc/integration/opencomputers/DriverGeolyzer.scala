package li.cil.oc.integration.opencomputers

import li.cil.oc.{Constants, api}
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.Slot
import li.cil.oc.server.component
import li.cil.oc.server.component.Geolyzer
import net.minecraft.item.ItemStack

object DriverGeolyzer extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.BlockName.Geolyzer))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): Geolyzer =
    new component.Geolyzer(host)

  override def slot(stack: ItemStack): String = Slot.Upgrade

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack))
        classOf[component.Geolyzer]
      else null
  }
}
