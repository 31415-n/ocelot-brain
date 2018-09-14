package li.cil.oc.integration.opencomputers

import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.{Slot, Tier}
import li.cil.oc.server.component
import li.cil.oc.{Constants, api}
import net.minecraft.item.ItemStack

object DriverEEPROM extends Item {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.EEPROM))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    new component.EEPROM()

  override def slot(stack: ItemStack): String = Slot.EEPROM

  override def tier(stack: ItemStack): Int = Tier.One

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack))
        classOf[component.EEPROM]
      else null
  }
}
