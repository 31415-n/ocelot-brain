package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{CallBudget, DeviceInfo, Entity, Environment, Tiered}
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.{Constants, Settings}

class Memory(override var tier: Int) extends Entity with Environment with DeviceInfo with Tiered with traits.Memory with CallBudget {
  override val node: Node = Network.newNode(this, Visibility.Neighbors).
    create()

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Memory,
    DeviceAttribute.Description -> "Memory bank",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> ("MRAM 1x" + tier.toString),
    DeviceAttribute.Clock -> (Settings.get.callBudgets(tier / 2) * 1000).toInt.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  override def amount: Double = {
    val sizes = Settings.get.ramSizes
    sizes(tier max 0 min (sizes.length - 1))
  }

  override def callBudget: Double = Settings.get.callBudgets((tier / 2) max Tier.One min Tier.Three)
}
