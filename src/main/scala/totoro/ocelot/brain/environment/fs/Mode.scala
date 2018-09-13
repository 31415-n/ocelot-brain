package totoro.ocelot.brain.environment.fs

/**
  * Possible file modes.
  *
  * This is used when opening files from a [[FileSystemTrait]].
  */
object Mode extends Enumeration {
  type Mode = Value
  val

  /**
    * Open a file in reading mode.
    */
  Read,

  /**
    * Open a file in writing mode, overwriting existing contents.
    */
  Write,

  /**
    * Open a file in append mode, writing new data after existing contents.
    */
  Append = Value
}
