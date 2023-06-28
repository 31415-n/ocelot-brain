package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.entity.machine.{Architecture, MachineAPI}
import totoro.ocelot.brain.entity.traits.GenericCPU.ArchTag
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Ocelot, Settings}

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

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    if (nbt.hasKey(ArchTag)) {
      val archClass = nbt.getString(ArchTag)

      if (archClass.nonEmpty) try {
        _architecture = Class.forName(archClass).asSubclass(classOf[Architecture])
      }
      catch {
        case t: Throwable =>
          Ocelot.log.warn("Failed getting class for CPU architecture. Resetting CPU to use the default.", t)
      }
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    nbt.setString(ArchTag, architecture.getName)
  }
}

object GenericCPU {
  val ArchTag = "archClass"
}
