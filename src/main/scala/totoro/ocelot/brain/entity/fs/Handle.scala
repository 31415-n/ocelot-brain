package totoro.ocelot.brain.entity.fs

import java.io.IOException

/**
  * Represents a handle to a file opened from a [[FileSystemTrait]].
  */
trait Handle {
  /**
    * The current position in the file.
    */
  def position: Long

  /**
    * The total length of the file.
    */
  def length: Long

  /**
    * Closes the handle.
    *
    * For example, if there is an underlying stream, this should close that
    * stream. Any future calls to `read` or `write` should throw
    * an `IOException` after this function was called.
    */
  def close(): Unit

  /**
    * Tries to read as much data from the file as fits into the specified
    * array.
    *
    * For files opened in write or append mode this should always throw an
    * exception.
    *
    * @param into the buffer to read the data into.
    * @return the number of bytes read; -1 if there are no more bytes (EOF).
    * @throws IOException if the file was opened in writing mode or an
    *                     I/O error occurred or the file was already
    *                     closed.
    */
  @throws[IOException]
  def read(into: Array[Byte]): Int

  /**
    * Jump to the specified position in the file, if possible.
    *
    * For files opened in write or append mode this should always throw an
    * exception.
    *
    * @param to the position in the file to jump to.
    * @return the resulting position in the file.
    * @throws IOException if the file was opened in write mode.
    */
  @throws[IOException]
  def seek(to: Long): Long

  /**
    * Tries to write all the data from the specified array into the file.
    *
    * For files opened in read mode this should always throw an exception.
    *
    * @param value the data to write into the file.
    * @throws IOException if the file was opened in read-only mode, or
    *                     another I/O error occurred (no more space,
    *                     for example), or the file was already closed.
    */
  @throws[IOException]
  def write(value: Array[Byte]): Unit
}
