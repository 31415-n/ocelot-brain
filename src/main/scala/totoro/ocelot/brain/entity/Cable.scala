package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.{Entity, Environment}
import totoro.ocelot.brain.network.{Network, Node, Visibility}

class Cable extends Entity with Environment {
  override val node: Node = Network.newNode(this, Visibility.None).create()
}
