package li.cil.oc.common.item.traits

import li.cil.oc.api
import li.cil.oc.api.driver.DriverItem
import li.cil.oc.common.item.Delegator
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

trait Delegate {
  def parent: Delegator

  def unlocalizedName: String = getClass.getSimpleName.toLowerCase

  val itemId: Int = parent.add(this)

  def maxStackSize = 64

  def createItemStack(amount: Int = 1) = new ItemStack(parent, amount, itemId)

  // ----------------------------------------------------------------------- //

  def update(stack: ItemStack, world: World, player: Entity, slot: Int, selected: Boolean) {}

  // ----------------------------------------------------------------------- //

  protected def tierFromDriver(stack: ItemStack): Int =
    api.Driver.driverFor(stack) match {
      case driver: DriverItem => driver.tier(stack)
      case _ => 0
    }

  def color(stack: ItemStack, pass: Int) = 0xFFFFFF

  def getContainerItem(stack: ItemStack): ItemStack = ItemStack.EMPTY

  def hasContainerItem(stack: ItemStack): Boolean = false

  def displayName(stack: ItemStack): Option[String] = None

  def showDurabilityBar(stack: ItemStack) = false

  def durability(stack: ItemStack) = 0.0
}
