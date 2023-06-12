package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.entity.Redstone.RedstoneChangedEventArgs
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.RedstoneSignaller.WakeThresholdTag
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Node
import totoro.ocelot.brain.workspace.Workspace

import scala.collection.mutable.ArrayBuffer

trait RedstoneSignaller extends Entity with Environment {
  val node: Node

  var wakeThreshold = 0
  var wakeNeighborsOnly = true

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = """function():number -- Get the current wake-up threshold.""")
  def getWakeThreshold(context: Context, args: Arguments): Array[AnyRef] = result(wakeThreshold)

  @Callback(doc = """function(threshold:number):number -- Set the wake-up threshold.""")
  def setWakeThreshold(context: Context, args: Arguments): Array[AnyRef] = {
    val oldThreshold = wakeThreshold
    wakeThreshold = args.checkInteger(0)
    result(oldThreshold)
  }

  // ----------------------------------------------------------------------- //

  def onRedstoneChanged(args: RedstoneChangedEventArgs): Unit = {
    val side: AnyRef = Int.box(args.side.id)
    val flatArgs = ArrayBuffer[Object]("redstone_changed", side, Int.box(args.oldValue), Int.box(args.newValue))
    if (args.color >= 0) {
      flatArgs += Int.box(args.color)
    }
    node.sendToReachable("computer.signal", flatArgs.toSeq: _*)

    if (args.oldValue < wakeThreshold && args.newValue >= wakeThreshold) {
      if (wakeNeighborsOnly) {
        node.sendToNeighbors("computer.start")
      } else {
        node.sendToReachable("computer.start")
      }
    }
  }

  // ----------------------------------------------------------------------- //

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    wakeThreshold = nbt.getInteger(WakeThresholdTag)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setInteger(WakeThresholdTag, wakeThreshold)
  }
}

object RedstoneSignaller {
  private val WakeThresholdTag = "wakeThreshold"
}
