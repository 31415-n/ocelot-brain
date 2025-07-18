package totoro.ocelot.brain.entity.machine

import scala.collection.mutable

object ProgramLocations {
  final val architectureLocations = mutable.Map.empty[String, mutable.Map[String, String]]
  final val globalLocations = mutable.Map.empty[String, String]

  def addMapping(program: String, label: String, architectures: String*): Unit = {
    if (architectures == null || architectures.isEmpty) {
      globalLocations += (program -> label)
    }
    else {
      architectures.foreach(architectureLocations.getOrElseUpdate(_, mutable.Map.empty[String, String]) += (program -> label))
    }
  }

  def getMappings(architecture: String): Iterable[(String, String)] = architectureLocations.getOrElse(architecture, Iterable.empty) ++ globalLocations
}
