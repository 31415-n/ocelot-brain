package totoro.ocelot.brain.entity.traits

trait CPULike {
  /**
    * There may be some special cases like APU, when the overall item tier does not exaclty correspond the
    * tier of CPU component.
    * @return the tier of CPU
    */
  def cpuTier: Int
}
