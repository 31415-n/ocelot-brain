package totoro.ocelot.brain.entity.machine

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Node

import java.util

abstract class ArchitectureAPI(val machine: Machine) {
  protected def node: Node = machine.node

  protected def components: util.Map[String, String] = machine.components

  def initialize(): Unit

  def load(nbt: NBTTagCompound): Unit = {}

  def save(nbt: NBTTagCompound): Unit = {}
}
