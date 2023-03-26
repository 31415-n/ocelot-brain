package totoro.ocelot.brain.event

import totoro.ocelot.brain.event.FileSystemActivityType.ActivityType

case class FileSystemActivityEvent(address: String, activityType: ActivityType) extends NodeEvent
