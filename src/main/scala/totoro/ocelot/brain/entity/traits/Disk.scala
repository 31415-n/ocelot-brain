package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.entity.Environment
import totoro.ocelot.brain.entity.fs.Label

/**
  * Basic trait for all data disks
  */
trait Disk extends Environment {
  def label: Label
  def capacity: Int
  def speed: Int
}
