package totoro.ocelot.brain.util

class MovingAverage(val size: Int) {
  private val data = Array.fill(size)(0)
  private var head = 0
  private var cachedAverage = 0
  private var dirty = true

  def apply(): Int = {
    if (dirty) {
      cachedAverage = data.sum / size
      dirty = false
    }
    cachedAverage
  }

  def +=(value: Int): MovingAverage = {
    data(head) = value
    head = (head + 1) % size
    dirty = true
    this
  }
}
