package totoro.ocelot.brain.entity.machine

import java.lang.reflect.{Method, Modifier}

import totoro.ocelot.brain.Ocelot

import scala.collection.{immutable, mutable}

object Callbacks {
  private val cache = mutable.Map.empty[Class[_], immutable.Map[String, InnerCallback]]

  def apply(host: Any): Map[String, InnerCallback] =
    cache.getOrElseUpdate(host.getClass, dynamicAnalyze(host))

  // Clear the cache; used when world is unloaded, mostly to allow reacting to
  // stuff (aka configs) that may influence which @Callbacks are enabled.
  def clear(): Unit = {
    cache.clear()
  }

  def fromClass(environment: Class[_]): mutable.Map[String, InnerCallback] = staticAnalyze(environment)

  private def dynamicAnalyze(host: Any) = {
    val whitelists = mutable.Buffer.empty[Set[String]]
    val callbacks = mutable.Map.empty[String, InnerCallback]

    // Lazy val to allow referencing it in closures before it's actually
    // initialized after the base whitelist has been compiled.
    lazy val whitelist = whitelists.reduceOption(_.intersect(_)).getOrElse(Set.empty)

    def shouldAdd(name: String) = !callbacks.contains(name) && (whitelist.isEmpty || whitelist.contains(name))

    def process(environment: Any) = {
      val priority = 0

      val filter = shouldAdd _

      (priority, () => staticAnalyze(environment.getClass, Option(filter), Option(callbacks)))
    }

    // First collect whitelist and priority information, then sort and
    // fetch callbacks.
    Seq(process(host)).sortBy(-_._1).map(_._2).foreach(_ ())

    callbacks.toMap
  }

  private def staticAnalyze(seed: Class[_], shouldAdd: Option[String => Boolean] = None, optCallbacks: Option[mutable.Map[String, InnerCallback]] = None) = {
    val callbacks = optCallbacks.getOrElse(mutable.Map.empty[String, InnerCallback])
    var c: Class[_] = seed
    while (c != null && c != classOf[Object]) {
      val ms = c.getDeclaredMethods

      ms.filter(_.isAnnotationPresent(classOf[Callback])).foreach(m =>
        if (m.getParameterTypes.length != 2 ||
          m.getParameterTypes()(0) != classOf[Context] ||
          m.getParameterTypes()(1) != classOf[Arguments]) {
          Ocelot.log.error(s"Invalid use of Callback annotation on ${m.getDeclaringClass.getName}.${m.getName}: invalid argument types or count.")
        }
        else if (m.getReturnType != classOf[Array[AnyRef]]) {
          Ocelot.log.error(s"Invalid use of Callback annotation on ${m.getDeclaringClass.getName}.${m.getName}: invalid return type.")
        }
        else if (!Modifier.isPublic(m.getModifiers)) {
          Ocelot.log.error(s"Invalid use of Callback annotation on ${m.getDeclaringClass.getName}.${m.getName}: method must be public.")
        }
        else {
          val a = m.getAnnotation[Callback](classOf[Callback])
          val name = if (a.value != null && a.value.trim != "") a.value else m.getName
          if (shouldAdd.fold(true)(_ (name))) {
            callbacks += name -> new ComponentCallback(m, a)
          }
        }
      )

      c = c.getSuperclass
    }
    callbacks
  }

  // ----------------------------------------------------------------------- //

  abstract class InnerCallback(val annotation: Callback) {
    def apply(instance: AnyRef, context: Context, args: Arguments): Array[AnyRef]
  }

  class ComponentCallback(val method: Method, annotation: Callback) extends InnerCallback(annotation) {
    final val callWrapper = CallbackWrapper.createCallbackWrapper(method)

    override def apply(instance: AnyRef, context: Context, args: Arguments): Array[AnyRef] = callWrapper.call(instance, context, args)
  }

}
