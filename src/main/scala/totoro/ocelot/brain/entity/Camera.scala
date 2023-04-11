package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.{Entity, Environment}
import totoro.ocelot.brain.network.{Component, Network, Node, Visibility}
import com.github.sarxos.webcam.Webcam

import java.awt.Color
import java.awt.image.BufferedImage

object Camera {
  val frameTimeout: Long = 1000
  val maxDistance: Float = 32f
}

class Camera() extends Entity with Environment {
  private var _webcam: Webcam = Webcam.getDefault()
  webcam.open()

  private var frame: BufferedImage = _
  private var lastUpdateTime: Long = -1

  override val node: Component = Network.newNode(this, Visibility.Network).
    withComponent("camera", Visibility.Network).
    create()

  @Callback(direct = true, doc = "function([x:number, y:number]):number; " +
    "Returns the distance to the block the ray is shot at with the specified x-y offset, " +
    "or if the block directly in front")
  def distance(context: Context, args: Arguments): Array[AnyRef] = {
    if (!available)
      return result(-1)

    var x: Float = 0f
    var y: Float = 0f
    if (args.count() == 2) {
      // [-1; 1] => [0; 1]
      x = 1f - (args.checkDouble(0).toFloat + 1f) / 2f
      y = 1f - (args.checkDouble(1).toFloat + 1f) / 2f
    }

    result(ray(x, y))
  }

  def available: Boolean = webcam.isOpen

  def webcam: Webcam = _webcam

  def webcam_=(newWebcam: Webcam): Unit = {
    if (webcam.isOpen)
      webcam.close()

    if (!newWebcam.isOpen)
      newWebcam.open()

    _webcam = newWebcam
  }

  private def updateFrame(): Unit = {
    if (lastUpdateTime < 0 || System.currentTimeMillis - lastUpdateTime > Camera.frameTimeout) {
      frame = webcam.getImage
      lastUpdateTime = System.currentTimeMillis
    }
  }

  def ray(x: Float, y: Float): Float = {
    if (!available)
      return 0

    updateFrame()

    val frameX = (1f - (0f max x min 1f)) * (frame.getWidth - 1).toFloat
    val frameY = (0f max y min 1f) * (frame.getHeight - 1).toFloat
    val color = new Color(frame.getRGB(frameX.toInt, frameY.toInt))
    val grayscale = (color.getRed + color.getGreen + color.getBlue).toFloat / (3f * 255f)

    Camera.maxDistance * (1f - grayscale)
  }
}