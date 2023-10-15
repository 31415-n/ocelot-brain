package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.TapeDriveState.{FastForwardBytesPerTick, PlayUpdateIntervalNs, State}
import totoro.ocelot.brain.entity.tape.traits.TapeStorage

import scala.collection.mutable.ArrayBuffer

class TapeDriveState {
  var state: State = State.Stopped
  var packetSize: Int = 1500
  private var lastCodecTimeNs: Long = 0
  private var _soundVolume: Int = 127
  var storage: Option[TapeStorage] = None

  def setSpeed(speed: Float): Boolean =
    if (speed < 0.25f || speed > 2f) false
    else {
      packetSize = Math.round(1500 * speed)

      true
    }

  def volume: Byte = _soundVolume.toByte

  def volume_=(volume: Byte): Unit = {
    _soundVolume = volume max 0 min 127
  }

  def setVolume(volume: Float): Unit = {
    val clampedVolume = volume max 0 min 1
    _soundVolume = Math.floor(clampedVolume * 127f).toInt
  }

  def switchState(requestedState: State): Unit = {
    val newState = if (storage.isEmpty) State.Stopped else requestedState

    if (newState == State.Playing) {
      lastCodecTimeNs = System.nanoTime()
    }

    state = newState
  }

  private def advancePosition(): Unit = {
    for (storage <- storage) {
      val amount = storage.read(Array.ofDim[Byte](packetSize), simulate = false)

      if (amount < packetSize) {
        switchState(State.Stopped)
      }
    }
  }

  def update(): Unit = (state, storage) match {
    case (State.Stopped, _) =>
    case (_, None) =>

    case (State.Playing, Some(storage)) =>
      if (storage.position >= storage.size || storage.position < 0) {
        storage.setPosition(storage.position)
      }

      val time = System.nanoTime()

      if (time - PlayUpdateIntervalNs > lastCodecTimeNs) {
        lastCodecTimeNs += PlayUpdateIntervalNs

        advancePosition()
      }

    case (State.Rewinding, Some(storage)) =>
      val sought = storage.seek(-FastForwardBytesPerTick)

      if (sought > -FastForwardBytesPerTick) {
        switchState(State.Stopped)
      }

    case (State.Forwarding, Some(storage)) =>
      val sought = storage.seek(FastForwardBytesPerTick)

      if (sought < FastForwardBytesPerTick) {
        switchState(State.Stopped)
      }
  }
}

object TapeDriveState {
  private val PlayUpdateIntervalNs = 250 * 1_000_000
  private val FastForwardBytesPerTick = 2048

  sealed abstract class State(val name: String, val id: Int = State.values.length) {
    State.values += this
  }

  object State {
    private val values = ArrayBuffer.empty[State]

    final case object Stopped extends State("STOPPED")
    final case object Playing extends State("PLAYING")
    final case object Rewinding extends State("REWINDING")
    final case object Forwarding extends State("FORWARDING")

    def apply(id: Int): State = values(id)
  }
}
