package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.{Entity, Environment}
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.util.WebcamCapture

class Camera() extends Entity with Environment {
  var webcamCapture: WebcamCapture = new WebcamCapture
  private val cameraThread = new Thread(webcamCapture)
  //cameraThread.setPriority(Thread.MIN_PRIORITY)
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
}