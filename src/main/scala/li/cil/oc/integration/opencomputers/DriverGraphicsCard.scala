package li.cil.oc.integration.opencomputers

import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.{Slot, Tier}
import li.cil.oc.server.component
import li.cil.oc.server.component.GraphicsCard
import li.cil.oc.{Constants, api, common}
import net.minecraft.item.ItemStack

object DriverGraphicsCard extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.GraphicsCardTier1),
    api.Items.get(Constants.ItemName.GraphicsCardTier2),
    api.Items.get(Constants.ItemName.GraphicsCardTier3))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): GraphicsCard =
    tier(stack) match {
      case Tier.One => new component.GraphicsCard(Tier.One)
      case Tier.Two => new component.GraphicsCard(Tier.Two)
      case Tier.Three => new component.GraphicsCard(Tier.Three)
      case _ => null
    }

  override def slot(stack: ItemStack): String = Slot.Card

  override def tier(stack: ItemStack): Int =
    stack.getItem match {
      case gpu: common.item.GraphicsCard => gpu.gpuTier
      case _ => Tier.One
    }

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack))
        classOf[component.GraphicsCard]
      else null
  }
}
