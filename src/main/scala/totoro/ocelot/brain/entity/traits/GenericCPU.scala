package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.machine.{Architecture, MachineAPI}
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.util.Tier.Tier

import scala.math.Ordering.Implicits.infixOrderingOps

trait GenericCPU extends Environment with MutableProcessor with TieredPersistable {

  override val node: Node = Network.newNode(this, Visibility.Neighbors).create()

  def cpuTier: Tier = tier

  override def supportedComponents: Int = Settings.get.cpuComponentSupport(cpuTier.id)

  override def allArchitectures: Iterable[Class[_ <: Architecture]] = MachineAPI.architectures

  override def architecture: Class[_ <: Architecture] = {
    if (_architecture != null) _architecture
    else MachineAPI.defaultArchitecture
  }

  override def callBudget: Double = Settings.get.callBudgets((cpuTier max Tier.One min Tier.Three).id)
}
