package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.entity.fs.Label
import totoro.ocelot.brain.entity.Environment

/**
  * Basic trait for all data disks
  */
trait Disk extends Environment {
  def label: Label
  def capacity: Int
  def speed: Int
}
