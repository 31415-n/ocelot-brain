package li.cil.oc.common.tileentity.traits

import li.cil.oc.Settings
import li.cil.oc.common.EventHandler
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

case class RedstoneChangedEventArgs (side: EnumFacing, oldValue: Int, newValue: Int, color: Int = -1)

trait RedstoneAware extends TileEntity {
  protected[tileentity] val _input: Array[Int] = Array.fill(6)(-1)

  protected[tileentity] val _output: Array[Int] = Array.fill(6)(0)

  protected var _isOutputEnabled = false

  protected var shouldUpdateInput = true

  def isOutputEnabled: Boolean = _isOutputEnabled

  def isOutputEnabled_=(value: Boolean): RedstoneAware = {
    if (value != isOutputEnabled) {
      _isOutputEnabled = value
      if (!value) {
        for (i <- _output.indices) {
          _output(i) = 0
        }
      }
      onRedstoneOutputEnabledChanged()
    }
    this
  }

  def input(side: EnumFacing): Int = _input(side.ordinal()) max 0

  def input(side: EnumFacing, newInput: Int): Unit = {
    val oldInput = _input(side.ordinal())
    _input(side.ordinal()) = newInput
    if (oldInput >= 0 && newInput != oldInput) {
      onRedstoneInputChanged(RedstoneChangedEventArgs(side, oldInput, newInput))
    }
  }

  def maxInput: Int = EnumFacing.values.map(input).max

  def output(side: EnumFacing): Int = _output(side.ordinal())

  def output(side: EnumFacing, value: Int): Unit = if (value != output(side)) {
    _output(side.ordinal()) = value

    onRedstoneOutputChanged(side)
  }

  def checkRedstoneInputChanged() {
    EnumFacing.values().foreach(updateRedstoneInput)
  }

  // ----------------------------------------------------------------------- //

  override def updateEntity() {
    super.updateEntity()
    if (shouldUpdateInput) {
      shouldUpdateInput = false
      EnumFacing.values().foreach(updateRedstoneInput)
    }
  }

  override def initialize(): Unit = {
    super.initialize()
    EventHandler.scheduleServer(() => EnumFacing.values().foreach(updateRedstoneInput))
  }

  def updateRedstoneInput(side: EnumFacing) {
  }

  // ----------------------------------------------------------------------- //

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)

    val input = nbt.getIntArray(Settings.namespace + "rs.input")
    input.copyToArray(_input, 0, input.length min _input.length)
    val output = nbt.getIntArray(Settings.namespace + "rs.output")
    output.copyToArray(_output, 0, output.length min _output.length)
  }

  override def writeToNBT(nbt: NBTTagCompound): Unit = {
    super.writeToNBT(nbt)

    nbt.setIntArray(Settings.namespace + "rs.input", _input)
    nbt.setIntArray(Settings.namespace + "rs.output", _output)
  }

  // ----------------------------------------------------------------------- //

  protected def onRedstoneInputChanged(args: RedstoneChangedEventArgs) {}

  protected def onRedstoneOutputEnabledChanged() {}

  protected def onRedstoneOutputChanged(side: EnumFacing) {}
}
