package totoro.ocelot.brain.network

import java.util

import li.cil.oc.api.machine.{Callback, Context}
import li.cil.oc.common.item.data.NodeData
import li.cil.oc.server.driver.{CompoundBlockEnvironment, Registry}
import li.cil.oc.server.machine.Callbacks.{ComponentCallback, PeripheralCallback}
import li.cil.oc.server.machine.{ArgumentsImpl, Callbacks, Machine}
import net.minecraft.nbt.NBTTagCompound

trait Component extends Node {
  def visibility: Visibility.Value = _visibility

  private lazy val callbacks = Callbacks(host)

  private lazy val hosts = host match {
    case multi: CompoundBlockEnvironment =>
      callbacks.map {
        case (method, callback) => callback match {
          case component: ComponentCallback =>
            multi.environments.find {
              case (_, environment) => environment.getClass == component.method.getDeclaringClass
            } match {
              case Some((_, environment)) => method -> Some(environment)
              case _ => method -> None
            }
          case peripheral: PeripheralCallback =>
            multi.environments.find {
              case (_, environment: ManagedPeripheral) => environment.methods.contains(peripheral.annotation.value)
              case _ => false
            } match {
              case Some((_, environment)) => method -> Some(environment)
              case _ => method -> None
            }
        }
      }
    case _ => callbacks.map {
      case (method, _) => method -> Some(host)
    }
  }

  private var _visibility = Visibility.None

  def setVisibility(value: Visibility): Unit = {
    if (value.ordinal() > reachability.ordinal()) {
      throw new IllegalArgumentException("Trying to set computer visibility to '" + value + "' on a '" + name +
        "' node with reachability '" + reachability + "'. It will be limited to the node's reachability.")
    }
    if (network != null) _visibility match {
      case Visibility.Neighbors => value match {
        case Visibility.Network => addTo(reachableNodes)
        case Visibility.None => removeFrom(neighbors)
        case _ =>
      }
      case Visibility.Network => value match {
        case Visibility.Neighbors =>
          val neighborSet = neighbors.toSet
          removeFrom(reachableNodes.filterNot(neighborSet.contains))
        case Visibility.None => removeFrom(reachableNodes)
        case _ =>
      }
      case Visibility.None => value match {
        case Visibility.Neighbors => addTo(neighbors)
        case Visibility.Network => addTo(reachableNodes)
        case _ =>
      }
    }
    _visibility = value
  }

  def canBeSeenFrom(other: Node): Boolean = visibility match {
    case Visibility.None => false
    case Visibility.Network => canBeReachedFrom(other)
    case Visibility.Neighbors => isNeighborOf(other)
  }

  private def addTo(nodes: Iterable[ImmutableNode]): Unit = nodes.foreach(_.host match {
    case machine: Machine => machine.addComponent(this)
    case _ =>
  })

  private def removeFrom(nodes: Iterable[ImmutableNode]): Unit = nodes.foreach(_.host match {
    case machine: Machine => machine.removeComponent(this)
    case _ =>
  })

  // ----------------------------------------------------------------------- //

  override def methods: util.Set[String] = callbacks.keySet

  override def annotation(method: String): Callback =
    callbacks.get(method) match {
      case Some(_) => callbacks(method).annotation
      case _ => throw new NoSuchMethodException()
    }

  override def invoke(method: String, context: Context, arguments: AnyRef*): Array[AnyRef] = {
    callbacks.get(method) match {
      case Some(callback) => hosts(method) match {
        case Some(environment) => Registry.convert(callback(environment, context, new ArgumentsImpl(Seq(arguments: _*))))
        case _ => throw new NoSuchMethodException()
      }
      case _ => throw new NoSuchMethodException()
    }
  }

  // ----------------------------------------------------------------------- //

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)
    if (nbt.hasKey(NodeData.VisibilityTag)) {
      _visibility = Visibility.values()(nbt.getInteger(NodeData.VisibilityTag))
    }
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    nbt.setInteger(NodeData.VisibilityTag, _visibility.ordinal())
  }

  override def toString: String = super.toString + s"@$name"
}
