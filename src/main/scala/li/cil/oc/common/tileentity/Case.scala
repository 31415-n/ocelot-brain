package li.cil.oc.common.tileentity

import java.util

import li.cil.oc.{Constants, Settings}
import li.cil.oc.api.driver.DeviceInfo
import li.cil.oc.api.driver.DeviceInfo.{DeviceAttribute, DeviceClass}
import li.cil.oc.api.{Driver, internal}
import li.cil.oc.common.{InventorySlots, Slot, Tier}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

import scala.collection.convert.WrapAsJava._

class Case(var tier: Int) extends traits.Computer with internal.Case with DeviceInfo {
  def this() = {
    this(0)
    // If no tier was defined when constructing this case, then we don't yet know the inventory size
    // this is set back to true when the nbt data is loaded
    isSizeInventoryReady = false
  }

  // Used on client side to check whether to render disk activity/network indicators.
  var lastFileSystemAccess = 0L
  var lastNetworkActivity = 0L

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.System,
    DeviceAttribute.Description -> "Computer",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Blocker",
    DeviceAttribute.Capacity -> getSizeInventory.toString
  )

  override def getDeviceInfo: util.Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //

  def isCreative: Boolean = tier == Tier.Four

  // ----------------------------------------------------------------------- //

  override def componentSlot(address: String): Int = components.indexWhere(_.exists(env => env.node != null && env.node.address == address))

  // ----------------------------------------------------------------------- //

  private final val TierTag = Settings.namespace + "tier"

  override def readFromNBT(nbt: NBTTagCompound) {
    tier = nbt.getByte(TierTag) max 0 min 3
    super.readFromNBT(nbt)
    isSizeInventoryReady = true
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    nbt.setByte(TierTag, tier.toByte)
    super.writeToNBT(nbt)
  }

  // ----------------------------------------------------------------------- //

  override protected def onItemRemoved(slot: Int, stack: ItemStack) {
    super.onItemRemoved(slot, stack)
      val slotType = InventorySlots.computer(tier)(slot).slot
    if (slotType == Slot.CPU) {
      machine.stop()
    }
  }

  override def getSizeInventory: Int =
    if (tier < 0 || tier >= InventorySlots.computer.length) 0 else InventorySlots.computer(tier).length

  override def isItemValidForSlot(slot: Int, stack: ItemStack): Boolean =
    Option(Driver.driverFor(stack, getClass)).fold(false)(driver => {
      val provided = InventorySlots.computer(tier)(slot)
      driver.slot(stack) == provided.slot && driver.tier(stack) <= provided.tier
    })
}
