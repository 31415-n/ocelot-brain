package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.driver.item.Container
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import net.minecraft.item.ItemStack

object DriverContainerFloppy extends Item with Container {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.BlockName.DiskDrive))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): Null = null

  override def slot(stack: ItemStack): String = Slot.Container

  override def providedSlot(stack: ItemStack): String = Slot.Floppy

  override def providedTier(stack: ItemStack): Int = Tier.Any
}
