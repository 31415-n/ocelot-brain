package li.cil.oc.integration.opencomputers

import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.{Slot, Tier}
import li.cil.oc.server.component
import li.cil.oc.server.component.WirelessNetworkCard
import li.cil.oc.{Constants, api, common}
import net.minecraft.item.ItemStack

object DriverWirelessNetworkCard extends Item {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.WirelessNetworkCardTier1),
    api.Items.get(Constants.ItemName.WirelessNetworkCardTier2))
    
  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): WirelessNetworkCard.Tier1 =
    tier(stack) match {
      case Tier.One => new component.WirelessNetworkCard.Tier1(host)
      case Tier.Two => new component.WirelessNetworkCard.Tier2(host)
      case _ => null
    }

  override def slot(stack: ItemStack): String = Slot.Card

  override def tier(stack: ItemStack): Int =
    stack.getItem match {
      case card: common.item.WirelessNetworkCard => card.tier
      case _ => Tier.One
    }

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack)) tier(stack) match {
        case Tier.One => classOf[component.WirelessNetworkCard.Tier1]
        case Tier.Two => classOf[component.WirelessNetworkCard.Tier2]
        case _ => null
      }
      else null
  }
}
