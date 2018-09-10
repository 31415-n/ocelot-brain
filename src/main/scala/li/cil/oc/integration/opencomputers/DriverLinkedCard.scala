package li.cil.oc.integration.opencomputers

import li.cil.oc.{Constants, api}
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.{Slot, Tier}
import li.cil.oc.server.component
import net.minecraft.item.ItemStack

object DriverLinkedCard extends Item {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.LinkedCard))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    new component.LinkedCard()

  override def slot(stack: ItemStack): String = Slot.Card

  override def tier(stack: ItemStack): Int = Tier.Three

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack))
        classOf[component.LinkedCard]
      else null
  }
}
