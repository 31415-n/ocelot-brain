package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.util.ExtendedTier

class MagicalMemory extends Memory(ExtendedTier.One) {
  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Memory,
    DeviceAttribute.Description -> "Memory vortex",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Mnemomagic 47"
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  override def amount: Double = Double.PositiveInfinity
}
