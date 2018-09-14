package li.cil.oc.integration.opencomputers

import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.Slot
import li.cil.oc.server.component
import li.cil.oc.{Constants, api}
import net.minecraft.item.ItemStack

object DriverKeyboard extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.BlockName.Keyboard))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) = new component.Keyboard(host)

  override def slot(stack: ItemStack): String = Slot.Upgrade
}
