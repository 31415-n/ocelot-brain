package totoro.ocelot.brain.util

import com.github.sarxos.webcam.{Webcam, WebcamLockException}

import java.awt.Color
import java.awt.image.BufferedImage

object WebcamCapture {
  private final val frameTimeout: Long = 1000
  private final val deviceTimeout: Long = 10000
  private final val maxDistance: Float = 32f
}

class WebcamCapture extends Runnable {

  private var _webcam: Webcam = Webcam.getDefault
  private var lastUsageTime: Long = -1
  private var frame: Option[BufferedImage] = None
  private var deviceOpen: Boolean = false
  var flipHorizontally: Boolean = false
  var flipVertically: Boolean = false
  var invertColor: Boolean = false

  override def run(): Unit = {
    while (true) {
      if (lastUsageTime < 0 || System.currentTimeMillis - lastUsageTime >= WebcamCapture.deviceTimeout) {
        close()
      }

      else {
        open()

        synchronized {
          frame = Option(webcam.getImage)
        }
      }

      Thread.sleep(WebcamCapture.frameTimeout)
    }
  }

  private def open(): Unit = {
    if (!webcam.isOpen)
      webcam.open()

    deviceOpen = true
  }

  private def close(): Unit = {
    if (webcam.isOpen && deviceOpen) {
      webcam.close()
      deviceOpen = false
    }
  }

  private def requestFrameUpdate(): Boolean = {
    lastUsageTime = System.currentTimeMillis
    frame.isDefined
  }

  override def toString: String = webcam.getName

  def webcam: Webcam = _webcam
  def webcam_=(newWebcam: Webcam): Unit = {
    close()

    synchronized {
      _webcam = newWebcam
      lastUsageTime = -1
    }
  }

  def ray(x: Float, y: Float): Float = {
    if (!requestFrameUpdate)
      return 0

    val normalizedDistance = synchronized {
      val clampedX = 0f max x min 1f
      val clampedY = 0f max y min 1f
      val frameX = (if (flipHorizontally) 1f - clampedX else clampedX) * (frame.get.getWidth - 1).toFloat
      val frameY = (if (flipVertically) 1f - clampedY else clampedY) * (frame.get.getHeight - 1).toFloat
      val color = new Color(frame.get.getRGB(frameX.toInt, frameY.toInt))
      (color.getRed + color.getGreen + color.getBlue).toFloat / (3f * 255f)
    }

    WebcamCapture.maxDistance * (if (invertColor) normalizedDistance else 1f - normalizedDistance)
  }
}
