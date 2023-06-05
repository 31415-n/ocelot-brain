package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{Computer, DeviceInfo, Entity, TieredPersistable}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.workspace.Workspace

class Case(override var tier: Tier) extends Computer with Entity with DeviceInfo with TieredPersistable {

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.System,
    DeviceAttribute.Description -> "Computer",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "WonderCase Pro Edition",
    DeviceAttribute.Capacity -> Int.MaxValue.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  def isCreative: Boolean = tier.isCreative

  def turnOn(): Unit = {
    machine.start()
  }

  def turnOff(): Unit = {
    machine.stop()
  }

  override def save(nbt: NBTTagCompound): Unit = super.save(nbt)
  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = super.load(nbt, workspace)
}
