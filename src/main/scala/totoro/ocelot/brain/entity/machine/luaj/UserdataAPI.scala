package totoro.ocelot.brain.entity.machine.luaj

import li.cil.repack.org.luaj.vm2.{LuaValue, Varargs}
import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.machine.ScalaClosure._
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Registry, Value}

import scala.jdk.CollectionConverters._

class UserdataAPI(owner: LuaJLuaArchitecture) extends LuaJAPI(owner) {
  override def initialize(): Unit = {
    val userdata = LuaValue.tableOf()

    userdata.set("apply", (args: Varargs) => {
      val value = args.checkuserdata(1, classOf[Value]).asInstanceOf[Value]
      val params = toSimpleJavaObjects(args, 2)
      owner.invoke(() => Registry.convert(Array(value.apply(machine, new Arguments(params)))))
    })

    userdata.set("unapply", (args: Varargs) => {
      val value = args.checkuserdata(1, classOf[Value]).asInstanceOf[Value]
      val params = toSimpleJavaObjects(args, 2)
      owner.invoke(() => {
        value.unapply(machine, new Arguments(params))
        null
      })
    })

    userdata.set("call", (args: Varargs) => {
      val value = args.checkuserdata(1, classOf[Value]).asInstanceOf[Value]
      val params = toSimpleJavaObjects(args, 2)
      owner.invoke(() => Registry.convert(value.call(machine, new Arguments(params))))
    })

    userdata.set("dispose", (args: Varargs) => {
      val value = args.checkuserdata(1, classOf[Value]).asInstanceOf[Value]
      try value.dispose(machine) catch {
        case t: Throwable => Ocelot.log.warn("Error in dispose method of userdata of type " + value.getClass.getName, t)
      }
      LuaValue.NIL
    })

    userdata.set("methods", (args: Varargs) => {
      val value = args.checkuserdata(1, classOf[Value])
      LuaValue.tableOf(machine.methods(value).asScala.flatMap((entry: (String, Callback)) => {
        val (name, annotation) = entry
        Seq(LuaValue.valueOf(name), LuaValue.valueOf(annotation.direct))
      }).toArray)
    })

    userdata.set("invoke", (args: Varargs) => {
      val value = args.checkuserdata(1, classOf[Value]).asInstanceOf[Value]
      val method = args.checkjstring(2)
      val params = toSimpleJavaObjects(args, 3)
      owner.invoke(() => machine.invoke(value, method, params.toArray))
    })

    userdata.set("doc", (args: Varargs) => {
      val value = args.checkuserdata(1, classOf[Value]).asInstanceOf[Value]
      val method = args.checkjstring(2)
      owner.documentation(() => machine.methods(value).get(method).doc)
    })

    lua.set("userdata", userdata)
  }
}
