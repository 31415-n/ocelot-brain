package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.fs.{Label, ReadWriteLabel}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, DiskUnmanaged, Entity, TieredPersistable}
import totoro.ocelot.brain.event.FileSystemActivityType.{ActivityType, HDD}
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.{Constants, Settings}

class HDDUnmanaged private(override var tier: Tier, name: Option[String])
  extends Entity
    with DiskUnmanaged
    with TieredPersistable
    with DeviceInfo {

  // ugly shenanigans to allow creating an unnamed unmanaged hdd
  def this(tier: Tier) = {
    this(tier, None)
  }

  def this(tier: Tier, name: String) = {
    this(tier, Some(name))
  }

  val label: Label = new ReadWriteLabel(name)

  def capacity: Int = Settings.get.hddSizes(tier.id) * 1024

  def platterCount: Int = Settings.get.hddPlatterCounts(tier.id)

  def speed: Int = tier.num + 1

  override val activityType: Option[ActivityType] = Some(HDD)

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
