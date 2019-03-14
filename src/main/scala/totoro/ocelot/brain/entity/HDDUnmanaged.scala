package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.fs.{Label, ReadWriteLabel}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, DiskUnmanaged, Entity, Tiered}
import totoro.ocelot.brain.{Constants, Settings}

class HDDUnmanaged(override var tier: Int, name: String)
  extends Entity with DiskUnmanaged with Tiered with DeviceInfo {

  val label: Label = new ReadWriteLabel(name)
  val capacity: Int = Settings.get.hddSizes(tier) * 1024
  val platterCount: Int = Settings.get.hddPlatterCounts(tier)
  val speed: Int = tier + 2

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Disk,
    DeviceAttribute.Description -> "Hard disk drive",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> ("Catfish " + (capacity / 1024).toString + "L" + platterCount.toString),
    DeviceAttribute.Capacity -> (capacity * 1.024).toInt.toString,
    DeviceAttribute.Size -> capacity.toString,
    DeviceAttribute.Clock ->
      (((2000 / readSectorCosts(speed)).toInt / 100).toString + "/" +
        ((2000 / writeSectorCosts(speed)).toInt / 100).toString + "/" +
        ((2000 / readByteCosts(speed)).toInt / 100).toString + "/" +
        ((2000 / writeByteCosts(speed)).toInt / 100).toString)
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo
}
