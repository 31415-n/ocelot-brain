package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.{Entity, Environment}
import totoro.ocelot.brain.event.{EventBus, NoteBlockTriggerEvent}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.workspace.Workspace

class NoteBlock extends Entity with Environment {
  override val node: Node = Network.newNode(this, Visibility.Network).
    withComponent("note_block").
    create()

  var pitch = 1
  var instrument = "harp"

  @Callback(direct = true, doc = "function():number -- Get the currently set pitch on this note block.")
  def getPitch(context: Context, args: Arguments): Array[AnyRef] = {
    result(pitch)
  }

  @Callback(doc = "function(value:number) -- Set the pitch for this note block. Must be in the interval [1, 25].")
  def setPitch(context: Context, args: Arguments): Array[AnyRef] = {
    setPitch(args.checkInteger(0))
    result(true)
  }


  @Callback(doc = "function([pitch:number]):boolean -- Triggers the note block if possible. Allows setting the pitch for to save a tick.")
  def trigger(context: Context, args: Arguments): Array[AnyRef] = {
    if (args.count > 0 && args.checkAny(0) != null) {
      setPitch(args.checkInteger(0))
    }
    EventBus.send(NoteBlockTriggerEvent(node.address, instrument, pitch - 1))
    result(true)
  }

  private def setPitch(value: Int): Unit = {
    if (value < 1 || value > 25) {
      throw new IllegalArgumentException("invalid pitch")
    }
    pitch = value
  }

  private final val PitchTag = "Pitch"
  private final val InstrumentTag = "Instrument"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    pitch = nbt.getByte(PitchTag)
    instrument = nbt.getString(InstrumentTag)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setByte(PitchTag, pitch.toByte)
    nbt.setString(InstrumentTag, instrument)
  }
}
