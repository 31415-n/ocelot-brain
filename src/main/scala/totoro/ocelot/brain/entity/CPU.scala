package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.{CPULike, MutableProcessor, Tiered}
import totoro.ocelot.brain.environment.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.environment.traits.{DeviceInfo, Environment}
import totoro.ocelot.brain.machine.{Architecture, MachineAPI}
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.{Constants, Settings}

class CPU(override var tier: Int) extends Environment with MutableProcessor with Tiered with CPULike with DeviceInfo {

  override val node: Node = Network.newNode(this, Visibility.Neighbors).create()

  override def supportedComponents = Settings.get.cpuComponentSupport(cpuTier)

  override def allArchitectures: Iterable[Class[_ <: Architecture]] = MachineAPI.architectures

  override def architecture: Class[_ <: Architecture] = {
    if (_architecture != null) _architecture
    else MachineAPI.architectures.headOption.orNull
  }

  override def cpuTier: Int = tier

  override def callBudget: Double = Settings.get.callBudgets(cpuTier max Tier.One min Tier.Three)

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Processor,
    DeviceAttribute.Description -> "CPU",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> ("RipPU X" + (tier + 1).toString),
    DeviceAttribute.Clock -> (Settings.get.callBudgets(tier) * 1000).toInt.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo
}
