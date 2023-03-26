package totoro.ocelot.brain.event

case class MachineCrashEvent(address: String, message: String) extends NodeEvent
