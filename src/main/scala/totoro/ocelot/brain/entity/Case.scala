package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{Computer, DeviceInfo, Entity, Tiered}
import totoro.ocelot.brain.util.Tier

class Case(override var tier: Int) extends Entity with Computer with DeviceInfo with Tiered {

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.System,
    DeviceAttribute.Description -> "Computer",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "WonderCase Pro Edition",
    DeviceAttribute.Capacity -> Int.MaxValue.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  def isCreative: Boolean = tier == Tier.Four

  def turnOn(): Unit = {
    machine.start()
  }

  def turnOff(): Unit = {
    machine.stop()
  }
}
