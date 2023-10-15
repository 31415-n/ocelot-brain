package totoro.ocelot.brain.entity.tape.traits

trait TapeStorage {
  /**
    * The unique identifier of the [[TapeStorage]].
    */
  def uniqueId: String

  /**
    * The name (note: not label, think of it more as a type) of the [[TapeStorage]].
    */
  def name: String

  /**
    * The position the [[TapeStorage]] is currently in.
    */
  def position: Int

  /**
    * The size of the [[TapeStorage]], in bytes.
    */
  def size: Int

  /**
    * Sets the position of the tape.
    *
    * @return the position the tape is now set to
    */
  def setPosition(newPosition: Int): Int

  /**
    * Seek `amount` bytes in the tape. Negative values indicate rewinding.
    *
    * @return the amount of bytes sought
    */
  def seek(amount: Int): Int

  /**
    * Read a single byte.
    *
    * @param simulate if `true`, do not automatically seek the tape
    * @return the byte read, automatically converted to an unsigned form (0-255)
    */
  def read(simulate: Boolean): Int

  /**
    * Read `intoArray.length` bytes into `intoArray`.
    *
    * @param intoArray the array into which the data should be read
    * @param simulate if `true`, do not automatically seek the tape
    * @return the amount of bytes read
    */
  def read(intoArray: Array[Byte], simulate: Boolean): Int

  /**
    * Write a byte on the tape.
    */
  def write(b: Byte): Unit

  /**
    * Write the array into the tape.
    *
    * @return the amount of bytes written
    */
  def write(array: Array[Byte]): Int

  /**
    * Called when the storage is about to be unloaded - use this entrypoint to save the (modified) data.
    */
  def onStorageUnload(): Unit

  def save(): Unit
}
