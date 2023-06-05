package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.{Entity, Environment}
import totoro.ocelot.brain.event.{EventBus, NoteBlockTriggerEvent}
import totoro.ocelot.brain.network.{Network, Node, Visibility}

class IronNoteBlock extends Entity with Environment {
  override val node: Node = Network.newNode(this, Visibility.Neighbors).
    withComponent("iron_noteblock", Visibility.Neighbors).
    create()

  @Callback(direct = true, limit = 10, doc = "function([instrument:number or string,] note:number [, volume:number]); " +
    "Plays the specified note with the specified instrument or the default one; volume may be a number between 0 and 1")
  def playNote(context: Context, args: Arguments): Array[AnyRef] = {
    if (args.count >= 1) {
      if (args.count >= 2 && args.isInteger(1)) {
        val instrument = if (args.isInteger(0)) {
          instruments(args.checkInteger(0) % instruments.length)
        } else if (args.isString(0)) {
          val str = args.checkString(0)
          val idx = instruments.indexOf(str)
          if (idx == -1) {
            throw new IllegalArgumentException(s"invalid instrument: $str")
          }
          str
        } else if (args.checkAny(0) == null) {
          "harp"
        } else {
          return null
        }

        EventBus.send(NoteBlockTriggerEvent(node.address, instrument, args.checkInteger(1), args.optDouble(2, 1)))
      } else if (args.isInteger(0)) {
        EventBus.send(NoteBlockTriggerEvent(node.address, "harp", args.checkInteger(0), args.optDouble(1, 1)))
      }
    }

    null
  }

  private val instruments = Seq("harp", "basedrum", "snare", "hat", "bass", "flute", "bell", "guitar", "chime", "xylophone", "pling")
}
