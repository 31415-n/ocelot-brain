package totoro.ocelot.brain.entity

import totoro.ocelot.brain.network.{Network, Node, Visibility}

class Cable extends Environment {
  override val node: Node = Network.newNode(this, Visibility.None).create()
}
