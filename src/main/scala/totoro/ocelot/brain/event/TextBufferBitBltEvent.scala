package totoro.ocelot.brain.event

import totoro.ocelot.brain.entity.GpuTextBuffer

case class TextBufferBitBltEvent(address: String, x: Int, y: Int, width: Int, height: Int, ram: GpuTextBuffer, fromX: Int, fromY: Int) extends Event
