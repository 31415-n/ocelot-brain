package totoro.ocelot.brain.event

import totoro.ocelot.brain.entity.GpuTextBuffer

case class TextBufferRamDestroyEvent(address: String, ram: GpuTextBuffer) extends NodeEvent
