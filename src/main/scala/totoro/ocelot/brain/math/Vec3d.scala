package totoro.ocelot.brain.math

case class Vec3d(var x: Double = .0, var y: Double = .0, var z: Double = .0) {

  def length: Double = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z)

  def normalize(): Vec3d = {
    val multiplier = 1.0D / this.length
    this.x *= multiplier
    this.y *= multiplier
    this.z *= multiplier
    this
  }
}
