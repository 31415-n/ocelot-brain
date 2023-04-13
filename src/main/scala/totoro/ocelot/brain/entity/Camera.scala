package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment}
import totoro.ocelot.brain.nbt._
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.util.WebcamCapture
import totoro.ocelot.brain.workspace.Workspace
import com.github.sarxos.webcam.Webcam

class Camera() extends Entity with Environment with DeviceInfo {
  var webcamCapture: WebcamCapture = new WebcamCapture
  private val cameraThread = new Thread(webcamCapture)
  cameraThread.setPriority(Thread.MIN_PRIORITY)
  cameraThread.start()

  override val node: Component = Network.newNode(this, Visibility.Network).
    withComponent("camera", Visibility.Network).
    create()

  @Callback(direct = true, doc = "function([x:number, y:number]):number; " +
    "Returns the distance to the block the ray is shot at with the specified x-y offset, " +
    "or if the block directly in front")
  def distance(context: Context, args: Arguments): Array[AnyRef] = {
    var x: Float = 0f
    var y: Float = 0f
    if (args.count() == 2) {
      // [-1; 1] => [0; 1]
      x = 1f - (args.checkDouble(0).toFloat + 1f) / 2f
      y = 1f - (args.checkDouble(1).toFloat + 1f) / 2f
    }

    result(webcamCapture.ray(x, y))
  }

  override def getDeviceInfo: Map[String, String] = Map(
    DeviceAttribute.Class -> DeviceClass.Multimedia,
    DeviceAttribute.Description -> "Dungeon Scanner 3D",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> webcamCapture.toString
  )

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    if (nbt.hasKey("device"))
      try webcamCapture.webcam = Webcam.getWebcamByName(nbt.getString("device"))

    if (nbt.hasKey("flipHorizontally"))
      webcamCapture.flipHorizontally = nbt.getBoolean("flipHorizontally")

    if (nbt.hasKey("flipVertically"))
      webcamCapture.flipVertically = nbt.getBoolean("flipVertically")

    if (nbt.hasKey("invertColor"))
      webcamCapture.invertColor = nbt.getBoolean("invertColor")
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    nbt.setString("device", webcamCapture.webcam.getName)
    nbt.setBoolean("flipHorizontally", webcamCapture.flipHorizontally)
    nbt.setBoolean("flipVertically", webcamCapture.flipVertically)
    nbt.setBoolean("invertColor", webcamCapture.invertColor)
  }
}