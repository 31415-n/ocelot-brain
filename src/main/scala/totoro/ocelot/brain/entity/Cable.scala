package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.EntityEnvironment
import totoro.ocelot.brain.network.{Network, Node, Visibility}

class Cable extends EntityEnvironment {
  override val node: Node = Network.newNode(this, Visibility.None).create()
}
