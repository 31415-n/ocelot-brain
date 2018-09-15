package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.machine.Architecture

/**
  * Use this interface to implement item drivers extending the number of
  * components a server can control.
  *
  * Note that the item must be installed in the actual server's inventory to
  * work. If it is installed in an external inventory the server will not
  * recognize the memory.
  */
trait Processor extends CallBudget {
  /**
    * The additional number of components supported if this processor is
    * installed in the server.
    *
    * @return the number of additionally supported components.
    */
  def supportedComponents: Int

  /**
    * The architecture of this CPU.
    *
    * This usually controls which architecture is created for a machine the
    * CPU is installed in (this is true for all computers built into OC, such
    * as computer cases, server racks and robots, it my not be true for third-
    * party computers).
    *
    * @return the type of this CPU's architecture.
    */
  def architecture: Class[_ <: Architecture]
}
