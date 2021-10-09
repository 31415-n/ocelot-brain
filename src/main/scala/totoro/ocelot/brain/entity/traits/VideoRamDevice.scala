package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.entity.GpuTextBuffer
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.{GenericTextBuffer, PackedColor}
import totoro.ocelot.brain.workspace.Workspace

import scala.collection.mutable

trait VideoRamDevice {
  private val internalBuffers = new mutable.HashMap[Int, GpuTextBuffer]
  val RESERVED_SCREEN_INDEX: Int = 0

  def isEmpty: Boolean = internalBuffers.isEmpty

  def onBufferRamDestroy(id: Int): Unit = {}

  def bufferIndexes(): Array[Int] = internalBuffers.collect {
    case (index: Int, _: Any) => index
  }.toArray

  def addBuffer(ram: GpuTextBuffer): Boolean = {
    val preexists = internalBuffers.contains(ram.id)
    internalBuffers += ram.id -> ram
    preexists
  }

  def removeBuffers(ids: Array[Int]): Int = {
    var count = 0
    if (ids.nonEmpty) {
      for (id <- ids) {
        if (internalBuffers.remove(id).nonEmpty) {
          onBufferRamDestroy(id)
          count += 1
        }
      }
    }
    count
  }

  def removeAllBuffers(): Int = removeBuffers(bufferIndexes())

  def loadBuffer(address: String, id: Int, nbt: NBTTagCompound, workspace: Workspace): Unit = {
    val src = new GenericTextBuffer(width = 1, height = 1, PackedColor.SingleBitFormat)
    src.load(nbt, workspace)
    addBuffer(GpuTextBuffer.wrap(address, id, src))
  }

  def getBuffer(id: Int): Option[GpuTextBuffer] = {
    if (internalBuffers.contains(id))
      Option(internalBuffers(id))
    else
      None
  }

  def nextAvailableBufferIndex: Int = {
    var index = RESERVED_SCREEN_INDEX + 1
    while (internalBuffers.contains(index)) {
      index += 1
    }
    index
  }

  def calculateUsedMemory(): Int = {
    var sum: Int = 0
    for ((_, buffer: GpuTextBuffer) <- internalBuffers) {
      sum += buffer.data.width * buffer.data.height
    }
    sum
  }
}
