package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, GenericCPU}
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.{Constants, Settings}

class CPU(override var tier: Tier) extends Entity with GenericCPU with DeviceInfo {
  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Processor,
    DeviceAttribute.Description -> "CPU",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> ("Ripper X" + tier.toString),
    DeviceAttribute.Clock -> (Settings.get.callBudgets(tier.id) * 1000).toInt.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo
}
