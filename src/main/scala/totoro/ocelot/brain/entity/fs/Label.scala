package totoro.ocelot.brain.entity.fs

import totoro.ocelot.brain.entity.traits.Persistable

/**
  * Used by file system components to get and set the file system's label.
  *
  * @see [[FileSystem]].asManagedEnvironment
  */
trait Label extends Persistable {
  /**
    * Get the current value of this label.
    *
    * May be `null` if no label is set.
    *
    * @return the current label.
    */
  def getLabel: String

  /**
    * Set the new value of this label.
    *
    * May be set to `null` to clear the label.
    *
    * May throw an exception if the label is read-only.
    *
    * @param value the new label.
    * @throws IllegalArgumentException if the label is read-only.
    */
  def setLabel(value: String): Unit
}
