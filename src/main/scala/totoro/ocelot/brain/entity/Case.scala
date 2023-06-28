package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{Computer, DeviceInfo, Entity, TieredPersistable}
import totoro.ocelot.brain.util.Tier.Tier

class Case(override var tier: Tier) extends Computer with Entity with DeviceInfo with TieredPersistable {
  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.System,
    DeviceAttribute.Description -> "Computer",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "WonderCase Pro Edition",
    DeviceAttribute.Capacity -> Int.MaxValue.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo
}
