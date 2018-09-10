package li.cil.oc.server.network

import li.cil.oc.api.network.{Node => ImmutableNode}

// We have to mixin the vararg methods individually in the actual
// implementations of the different node variants (see Network class) because
// for some reason it fails compiling on Linux otherwise (no clue why).
trait NodeVarargPart extends ImmutableNode {
  def sendToAddress(target: String, name: String, data: AnyRef*): Unit =
    if (network != null) network.sendToAddress(this, target, name, data: _*)

  def sendToNeighbors(name: String, data: AnyRef*): Unit =
    if (network != null) network.sendToNeighbors(this, name, data: _*)

  def sendToReachable(name: String, data: AnyRef*): Unit =
    if (network != null) network.sendToReachable(this, name, data: _*)

  def sendToVisible(name: String, data: AnyRef*): Unit =
    if (network != null) network.sendToVisible(this, name, data: _*)
}
