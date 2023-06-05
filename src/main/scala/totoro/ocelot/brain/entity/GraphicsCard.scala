package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, GenericGPU}
import totoro.ocelot.brain.util.Tier.Tier

class GraphicsCard(override var tier: Tier) extends Entity with GenericGPU with DeviceInfo {
  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Display,
    DeviceAttribute.Description -> "Graphics controller",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> s"GirlForce ${tier.num * 1000} X",
    DeviceAttribute.Capacity -> capacityInfo,
    DeviceAttribute.Width -> widthInfo,
    DeviceAttribute.Clock -> clockInfo
  )

  def capacityInfo: String = (maxResolution._1 * maxResolution._2).toString

  def widthInfo: String = Array("1", "4", "8").apply(maxDepth.id)

  def clockInfo: String =
      ((2000 / setBackgroundCosts(tier.id)).toInt / 100).toString + "/" +
      ((2000 / setForegroundCosts(tier.id)).toInt / 100).toString + "/" +
      ((2000 / setPaletteColorCosts(tier.id)).toInt / 100).toString + "/" +
      ((2000 / setCosts(tier.id)).toInt / 100).toString + "/" +
      ((2000 / copyCosts(tier.id)).toInt / 100).toString + "/" +
      ((2000 / fillCosts(tier.id)).toInt / 100).toString

  override def getDeviceInfo: Map[String, String] = deviceInfo
}
