package totoro.ocelot.brain.entity.sound_card

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.result
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment}
import totoro.ocelot.brain.network._

class SoundCard extends Entity with Environment with DeviceInfo {
  override val node: Component = Network.newNode(this, Visibility.Neighbors).
    withComponent("sound").
    create()

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Multimedia,
    DeviceAttribute.Description -> "Audio interface",
    DeviceAttribute.Vendor -> "Yanaki Sound Systems",
    DeviceAttribute.Product -> "MinoSound 244-X",
  )

  val board = new SoundBoard

  override def getDeviceInfo: Map[String, String] = deviceInfo

  override def update(): Unit = {
    super.update()
    board.update(node.address)
  }

  @Callback(doc = "This is a bidirectional table of all valid modes.", direct = true, getter = true)
  def modes(context: Context, args: Arguments): Array[AnyRef] = {
    result(SignalGenerator.modes)
  }

  @Callback(doc = "This is the number of channels this card provides.", direct = true, getter = true)
  def channel_count(context: Context, args: Arguments): Array[AnyRef] = {
    result(board.process.channels.length)
  }

  @Callback(doc = "function(volume:number); Sets the general volume of the entire sound card to a value between 0 and 1. Not an instruction, this affects all channels directly.", direct = true)
  def setTotalVolume(context: Context, args: Arguments): Array[AnyRef] = {
    board.setTotalVolume(args.checkDouble(0))
    result()
  }

  @Callback(doc = "function(); Clears the instruction queue.", direct = true)
  def clear(context: Context, args: Arguments): Array[AnyRef] = {
    board.clear()
    result()
  }

  private def checkChannel(arguments: Arguments, index: Int = 0): Int = {
    board.checkChannel(arguments.checkInteger(index))
  }

  @Callback(doc = "function(channel:number); Instruction; Opens the specified channel, allowing sound to be generated.", direct = true)
  def open(context: Context, args: Arguments): Array[AnyRef] = {
    board.tryAdd(new Instruction.Open(checkChannel(args)))
  }

  @Callback(doc = "function(channel:number); Instruction; Closes the specified channel, stopping sound from being generated.", direct = true)
  def close(context: Context, args: Arguments): Array[AnyRef] = {
    board.tryAdd(new Instruction.Close(checkChannel(args)))
  }

  @Callback(doc = "function(channel:number, type:number); Instruction; Sets the wave type on the specified channel.", direct = true)
  def setWave(context: Context, args: Arguments): Array[AnyRef] = {
    board.setWave(args.checkInteger(0), args.checkInteger(1))
  }

  @Callback(doc = "function(channel:number, frequency:number); Instruction; Sets the frequency on the specified channel.", direct = true)
  def setFrequency(context: Context, args: Arguments): Array[AnyRef] = {
    board.tryAdd(new Instruction.SetFrequency(checkChannel(args), args.checkDouble(1).toFloat))
  }

  @Callback(doc = "function(channel:number, initial:number, mask:number); Instruction; Makes the specified channel generate LFSR noise. Functions like a wave type.", direct = true)
  def setLFSR(context: Context, args: Arguments): Array[AnyRef] = {
    val lfsr = new SignalGenerator.LFSR(args.checkInteger(1), args.checkInteger(2))
    board.tryAdd(new Instruction.SetGenerator(checkChannel(args), lfsr))
  }

  @Callback(doc = "function(duration:number); Instruction; Adds a delay of the specified duration in milliseconds, allowing sound to generate.", direct = true)
  def delay(context: Context, args: Arguments): Array[AnyRef] = {
    board.delay(args.checkInteger(0))
  }

  @Callback(doc = "function(channel:number, modIndex:number, intensity:number); Instruction; Assigns a frequency modulator channel to the specified channel with the specified intensity.", direct = true)
  def setFM(context: Context, args: Arguments): Array[AnyRef] = {
    val fm = new FrequencyModulator(checkChannel(args, 1), args.checkDouble(2).toFloat)
    board.tryAdd(new Instruction.SetFM(checkChannel(args), fm))
  }

  @Callback(doc = "function(channel:number); Instruction; Removes the specified channel's frequency modulator.", direct = true)
  def resetFM(context: Context, args: Arguments): Array[AnyRef] = {
    board.tryAdd(new Instruction.ResetFM(checkChannel(args)))
  }

  @Callback(doc = "function(channel:number, modIndex:number); Instruction; Assigns an amplitude modulator channel to the specified channel.", direct = true)
  def setAM(context: Context, args: Arguments): Array[AnyRef] = {
    val am = new AmplitudeModulator(checkChannel(args, 1))
    board.tryAdd(new Instruction.SetAM(checkChannel(args), am))
  }

  @Callback(doc = "function(channel:number); Instruction; Removes the specified channel's amplitude modulator.", direct = true)
  def resetAM(context: Context, args: Arguments): Array[AnyRef] = {
    board.tryAdd(new Instruction.ResetAM(checkChannel(args)))
  }

  @Callback(doc = "function(channel:number, attack:number, decay:number, attenuation:number, release:number); Instruction; Assigns ADSR to the specified channel with the specified phase durations in milliseconds and attenuation between 0 and 1.", direct = true)
  def setADSR(context: Context, args: Arguments): Array[AnyRef] = {
    val envelope = new ADSREnvelope(args.checkInteger(1), args.checkInteger(2),
      args.checkDouble(3).toFloat, args.checkInteger(4))
    board.tryAdd(new Instruction.SetEnvelope(checkChannel(args), envelope))
  }

  @Callback(doc = "function(channel:number); Instruction; Removes ADSR from the specified channel.", direct = true)
  def resetEnvelope(context: Context, args: Arguments): Array[AnyRef] = {
    board.tryAdd(new Instruction.ResetEnvelope(checkChannel(args)))
  }

  @Callback(doc = "function(channel:number, volume:number); Instruction; Sets the volume of the channel between 0 and 1.", direct = true)
  def setVolume(context: Context, args: Arguments): Array[AnyRef] = {
    board.tryAdd(new Instruction.SetVolume(checkChannel(args), args.checkDouble(1).toFloat))
  }

  @Callback(doc = "function(); Starts processing the queue; Returns true is processing began, false if there is still a queue being processed.", direct = true)
  def process(context: Context, args: Arguments): Array[AnyRef] = {
    board.startProcess()
  }
}
