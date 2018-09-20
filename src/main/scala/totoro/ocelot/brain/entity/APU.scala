package totoro.ocelot.brain.entity

import li.cil.oc.common.Tier
import totoro.ocelot.brain.entity.traits.DeviceInfo
import totoro.ocelot.brain.{Constants, Settings}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}

class APU(override var tier: Int) extends traits.CPU with traits.GraphicsCard with DeviceInfo {
  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Processor,
    DeviceAttribute.Description -> "APU",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> ("Ripper FX " + (tier + 1).toString + " (Builtin Graphics)"),
    DeviceAttribute.Capacity -> capacityInfo,
    DeviceAttribute.Width -> widthInfo,
    DeviceAttribute.Clock -> ((Settings.get.callBudgets(tier) * 1000).toInt.toString + "+" + clockInfo)
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

  override def cpuTier: Int = math.min(Tier.Three, tier + 1)
}
