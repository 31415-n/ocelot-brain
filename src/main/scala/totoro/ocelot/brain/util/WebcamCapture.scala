package totoro.ocelot.brain.util

import com.github.sarxos.webcam.Webcam

import java.awt.Color
import java.awt.image.BufferedImage
import scala.collection.mutable

object WebcamCapture {
  private final val frameTimeout: Long = 1000
  private final val deviceTimeout: Long = 10000
  private final val maxDistance: Float = 32f

  private val instances = new mutable.HashMap[String, WebcamCapture]

  def getInstance(name: String): WebcamCapture = instances.getOrElseUpdate(name, new WebcamCapture(name))
  def getInstance(webcam: Webcam): WebcamCapture = getInstance(webcam.getName)
  def getDefault: WebcamCapture = getInstance(Webcam.getDefault)
}

class WebcamCapture(webcamName: String) extends Thread {
  private val webcam: Webcam = Webcam.getWebcamByName(webcamName)
  private var frame: Option[BufferedImage] = None
  private var lastUsageTime: Long = -1
  start()

  override def run(): Unit = {
    while (true) {
      if (System.currentTimeMillis() - lastUsageTime >= WebcamCapture.deviceTimeout) {
        webcam.close()
        synchronized {
          println("Waiting")
          wait()
        }
      }
      else {
        println("Updating")
        webcam.open()
        frame = Option(webcam.getImage)
        Thread.sleep(WebcamCapture.frameTimeout)
      }
    }
  }

  def ray(x: Float, y: Float): Float = {
    lastUsageTime = System.currentTimeMillis()
    synchronized {
      notify()
    }

    if (frame.isEmpty)
      return 0

    val clampedX = 0f max x min 1f
    val clampedY = 0f max y min 1f
    val frameX = clampedX * (frame.get.getWidth - 1).toFloat
    val frameY = clampedY * (frame.get.getHeight - 1).toFloat
    val color = new Color(frame.get.getRGB(frameX.toInt, frameY.toInt))

    val normalizedDistance = (color.getRed + color.getGreen + color.getBlue).toFloat / (3f * 255f)
    WebcamCapture.maxDistance * (1f - normalizedDistance)
  }

  def name: String = webcam.getName
  override def toString: String = name
}
