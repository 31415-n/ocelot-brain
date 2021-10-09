package totoro.ocelot.brain.event

import totoro.ocelot.brain.entity.GpuTextBuffer

case class TextBufferRamInitEvent(address: String, ram: GpuTextBuffer) extends Event
