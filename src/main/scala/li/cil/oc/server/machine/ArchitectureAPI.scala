package li.cil.oc.server.machine

import java.util

import li.cil.oc.api
import li.cil.oc.api.network.Node
import net.minecraft.nbt.NBTTagCompound

abstract class ArchitectureAPI(val machine: api.machine.Machine) {
  protected def node: Node = machine.node

  protected def components: util.Map[String, String] = machine.components

  def initialize(): Unit

  def load(nbt: NBTTagCompound) {}

  def save(nbt: NBTTagCompound) {}
}
