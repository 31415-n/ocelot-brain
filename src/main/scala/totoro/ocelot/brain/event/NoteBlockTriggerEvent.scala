package totoro.ocelot.brain.event

case class NoteBlockTriggerEvent(address: String, instrument: String, pitch: Int) extends Event
