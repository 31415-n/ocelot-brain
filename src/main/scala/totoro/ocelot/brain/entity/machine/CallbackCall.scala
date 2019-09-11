package totoro.ocelot.brain.entity.machine

trait CallbackCall {
  def call(instance: AnyRef, context: Context, args: Arguments): Array[AnyRef]
}
