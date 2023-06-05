package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, GenericCPU, GenericGPU}
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.{Constants, Settings}

import scala.math.Ordering.Implicits.infixOrderingOps

// FIXME: have `tier` be the CPU tier (instead of GPU)
class APU(override var tier: Tier) extends Entity with GenericCPU with GenericGPU with DeviceInfo {
  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Processor,
    DeviceAttribute.Description -> "APU",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> s"Ripper FX ${tier.num} (Builtin Graphics)",
    DeviceAttribute.Capacity -> capacityInfo,
    DeviceAttribute.Width -> widthInfo,
    DeviceAttribute.Clock -> ((Settings.get.callBudgets(tier.id) * 1000).toInt.toString + "+" + clockInfo)
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

  override def cpuTier: Tier = tier.saturatingAdd(1) min Tier.Three
}
