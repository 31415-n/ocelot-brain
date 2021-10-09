package totoro.ocelot.brain.event

case class TextBufferRamDestroyEvent(address: String, ids: Array[Int]) extends Event
