package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{RackBusConnectable, RackMountable, StateAware}

import scala.collection.immutable.HashSet

class DiskDriveMountable extends FloppyDiskDrive with RackMountable {
  // ----------------------------------------------------------------------- //
  // DeviceInfo

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Disk,
    DeviceAttribute.Description -> "Floppy disk drive",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "RackDrive 100 Rev. 2"
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //
  // RackMountable

  override def getConnectableCount: Int = 0

  override def getConnectableAt(index: Int): RackBusConnectable = null

  // ----------------------------------------------------------------------- //
  // StateAware

  override def getCurrentState: Set[StateAware.State.Value] = HashSet[StateAware.State.Value]()
}
