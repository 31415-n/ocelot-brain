package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.DeviceInfo
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.{Constants, Settings}

class CPU(override var tier: Int) extends traits.GenericCPU with DeviceInfo {
  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Processor,
    DeviceAttribute.Description -> "CPU",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> ("Ripper X" + (tier + 1).toString),
    DeviceAttribute.Clock -> (Settings.get.callBudgets(tier) * 1000).toInt.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo
}
