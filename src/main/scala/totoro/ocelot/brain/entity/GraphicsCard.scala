package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.traits.DeviceInfo
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}

class GraphicsCard(override var tier: Int) extends traits.GraphicsCard with DeviceInfo {
  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Display,
    DeviceAttribute.Description -> "Graphics controller",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> ("GirlForce" + ((tier + 1) * 1000).toString + " X"),
    DeviceAttribute.Capacity -> capacityInfo,
    DeviceAttribute.Width -> widthInfo,
    DeviceAttribute.Clock -> clockInfo
  )

  def capacityInfo: String = (maxResolution._1 * maxResolution._2).toString

  def widthInfo = Array("1", "4", "8").apply(maxDepth.id)

  def clockInfo: String =
      ((2000 / setBackgroundCosts(tier)).toInt / 100).toString + "/" +
      ((2000 / setForegroundCosts(tier)).toInt / 100).toString + "/" +
      ((2000 / setPaletteColorCosts(tier)).toInt / 100).toString + "/" +
      ((2000 / setCosts(tier)).toInt / 100).toString + "/" +
      ((2000 / copyCosts(tier)).toInt / 100).toString + "/" +
      ((2000 / fillCosts(tier)).toInt / 100).toString

  override def getDeviceInfo: Map[String, String] = deviceInfo
}
