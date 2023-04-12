package totoro.ocelot.brain.util

import com.github.sarxos.webcam.Webcam

import java.awt.Color
import java.awt.image.BufferedImage

object WebcamCapture {
  private val frameTimeout = 1000
  private val maxDistance = 32f
}

class WebcamCapture extends Runnable {
  private var _webcam: Webcam = Webcam.getDefault()
  private var frame: Option[BufferedImage] = None

  override def run(): Unit = {
    while(true) {
      if (!webcam.isOpen)
          webcam.open()

      while(available) {
        this.synchronized {
          frame = Option(webcam.getImage)
        }

        Thread.sleep(WebcamCapture.frameTimeout)
      }
    }
  }

  def webcam: Webcam = _webcam

  def webcam_=(newWebcam: Webcam): Unit = {
    _webcam = newWebcam
  }

  def available: Boolean = webcam.isOpen

  override def toString: String = webcam.getName

  def ray(x: Float, y: Float): Float = {
    if (!available || frame.isEmpty)
      return 0

    val grayscale = this.synchronized {
      val frameX = (1f - (0f max x min 1f)) * (frame.get.getWidth - 1).toFloat
      val frameY = (0f max y min 1f) * (frame.get.getHeight - 1).toFloat
      val color = new Color(frame.get.getRGB(frameX.toInt, frameY.toInt))

      (color.getRed + color.getGreen + color.getBlue).toFloat / (3f * 255f)
    }

    WebcamCapture.maxDistance * (1f - grayscale)
  }
}
