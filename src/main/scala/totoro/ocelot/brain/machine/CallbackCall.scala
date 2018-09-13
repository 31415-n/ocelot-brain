package totoro.ocelot.brain.machine

trait CallbackCall {
  def call(instance: AnyRef, context: Context, args: Arguments): Array[AnyRef]
}
