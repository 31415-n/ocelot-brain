package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.entity.machine.Architecture

/**
  * May be implemented in processor drivers of processors that can be reconfigured.
  *
  * This is the case for OC's built-in CPUs, for example, which can be reconfigured
  * to any registered architecture. It a CPU has such a driver, it may also be
  * reconfigured by the machine it is running in (e.g. in the Lua case via
  * `computer.setArchitecture`).
  */
trait MutableProcessor extends Processor {
  protected var _architecture: Class[_ <: Architecture] = _

  /**
    * Get a list of all architectures supported by this processor.
    */
  def allArchitectures: Iterable[Class[_ <: Architecture]]

  /**
    * Set the architecture to use for the specified processor.
    *
    * @param architecture the architecture to use on the processor.
    */
  def setArchitecture(architecture: Class[_ <: Architecture]): Unit = {
    if (allArchitectures.exists(_ == architecture))
      _architecture = architecture
    else throw new IllegalArgumentException("Unsupported processor type.")
  }
}
