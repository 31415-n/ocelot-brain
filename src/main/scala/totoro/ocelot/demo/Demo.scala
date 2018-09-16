package totoro.ocelot.demo

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.Cable
import totoro.ocelot.brain.network.Network

object Demo extends App {
  println("Hi! We are testing Ocelot brains here. Join in!")

  Ocelot.initialize()
  val cable = new Cable()
  val network = new Network(cable.node)
  cable.node.connect(new Cable().node)
  cable.node.connect(new Cable().node)
}
