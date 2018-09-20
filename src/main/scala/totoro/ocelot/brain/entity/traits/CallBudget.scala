package totoro.ocelot.brain.entity.traits

/**
  * Common functionality provided by parts that influence a machine's speed.
  *
  * By default, this is implemented by OpenComputers' [[Processor]]s and
  * [[Memory]].
  *
  * The actual call budget of a machine is set to the average of
  * the specified call budget of all present components.
  *
  * A processor and memory implementation may choose not to implement this
  * interface. If no component providing a call budget it present in a machine,
  * a value of `1.0` will be used, i.e. the "default" speed modifier.
  */
trait CallBudget {
  /**
    * The budget for direct calls provided by the specified component.
    *
    * For reference, the default budgets for OpenComputers' processors are
    * 0.5, 1.0 and 1.5 for tier one, two and three, respectively. This means
    * you can consider it a multiplier for the machine's operation speed.
    *
    * @return the budget for direct calls per tick provided.
    */
  def callBudget: Double = 1.0
}
