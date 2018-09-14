package totoro.ocelot.brain.machine

import java.util

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Node

abstract class ArchitectureAPI(val machine: Machine) {
  protected def node: Node = machine.node

  protected def components: util.Map[String, String] = machine.components

  def initialize(): Unit

  def load(nbt: NBTTagCompound) {}

  def save(nbt: NBTTagCompound) {}
}
