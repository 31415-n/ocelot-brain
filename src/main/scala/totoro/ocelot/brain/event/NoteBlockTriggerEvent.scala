package totoro.ocelot.brain.event

case class NoteBlockTriggerEvent(address: String, instrument: String, pitch: Int, volume: Double = 1) extends Event
