package li.cil.oc.integration.opencomputers

import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.{EnvironmentHost, ManagedEnvironment}
import li.cil.oc.common.{Slot, Tier}
import li.cil.oc.server.component
import li.cil.oc.util.BlockPosition
import li.cil.oc.{Constants, api}
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing

/**
  * @author Vexatos (modified by Totoro)
  */
object DriverUpgradeMF extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.MFU))

  override def worksWith(stack: ItemStack, host: Class[_ <: EnvironmentHost]): Boolean =
    worksWith(stack) && isAdapter(host)

  override def slot(stack: ItemStack): String = Slot.Upgrade

  override def tier(stack: ItemStack): Int = Tier.Three

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): ManagedEnvironment =
    new component.UpgradeMF(host, BlockPosition(0, 0, 0, host.world()), EnumFacing.NORTH)

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack))
        classOf[component.UpgradeMF]
      else null
  }
}
