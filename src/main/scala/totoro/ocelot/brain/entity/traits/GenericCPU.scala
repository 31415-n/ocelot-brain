package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.Environment
import totoro.ocelot.brain.machine.{Architecture, MachineAPI}
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.Tier

trait GenericCPU extends Environment with MutableProcessor with Tiered {

  override val node: Node = Network.newNode(this, Visibility.Neighbors).create()

  def cpuTier: Int = tier

  override def supportedComponents: Int = Settings.get.cpuComponentSupport(cpuTier)

  override def allArchitectures: Iterable[Class[_ <: Architecture]] = MachineAPI.architectures

  override def architecture: Class[_ <: Architecture] = {
    if (_architecture != null) _architecture
    else MachineAPI.architectures.headOption.orNull
  }

  override def callBudget: Double = Settings.get.callBudgets(cpuTier max Tier.One min Tier.Three)
}
