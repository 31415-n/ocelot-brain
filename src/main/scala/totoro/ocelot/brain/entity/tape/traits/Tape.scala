package totoro.ocelot.brain.entity.tape.traits

import totoro.ocelot.brain.entity.traits.Entity

trait Tape extends Entity {
  def storage: TapeStorage

  def label: String
  def label_=(value: String): Unit
}
