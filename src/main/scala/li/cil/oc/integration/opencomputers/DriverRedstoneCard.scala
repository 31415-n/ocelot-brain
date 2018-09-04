package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.common.item
import li.cil.oc.common.item.Delegator
import li.cil.oc.common.tileentity.traits.BundledRedstoneAware
import li.cil.oc.common.tileentity.traits.RedstoneAware
import li.cil.oc.server.component
import li.cil.oc.server.component.RedstoneVanilla
import net.minecraft.item.ItemStack

object DriverRedstoneCard extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.RedstoneCardTier1),
    api.Items.get(Constants.ItemName.RedstoneCardTier2))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): RedstoneVanilla = {
    val isAdvanced = tier(stack) == Tier.Two
    val hasBundled = isAdvanced
    host match {
      case redstone: BundledRedstoneAware if hasBundled =>
        new component.Redstone.Bundled(redstone)
      case redstone: RedstoneAware =>
        new component.Redstone.Vanilla(redstone)
      case _ => null
    }
  }

  override def slot(stack: ItemStack): String = Slot.Card

  override def tier(stack: ItemStack): Int =
    Delegator.subItem(stack) match {
      case Some(card: item.RedstoneCard) => card.tier
      case _ => Tier.One
    }

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack)) {
        val isAdvanced = tier(stack) == Tier.Two
        val hasBundled = isAdvanced
        if (hasBundled) {
          classOf[component.Redstone.Bundled]
        }
        else {
          classOf[component.Redstone.Vanilla]
        }
      }
      else null
  }
}
