package totoro.ocelot.brain.entity.machine.luac

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, DataInputStream, DataOutputStream}

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.machine.{Arguments, Registry, Value}
import totoro.ocelot.brain.nbt.{CompressedStreamTools, NBTTagCompound}
import totoro.ocelot.brain.entity.machine.ExtendedLuaState.extendLuaState
import totoro.ocelot.brain.util.Persistable

import scala.collection.convert.WrapAsScala._

class UserdataAPI(owner: NativeLuaArchitecture) extends NativeLuaAPI(owner) {
  def initialize() {
    lua.newTable()

    lua.pushScalaFunction(lua => {
      val nbt = new NBTTagCompound()
      val persistable = lua.toJavaObjectRaw(1).asInstanceOf[Persistable]
      lua.pushString(persistable.getClass.getName)
      persistable.save(nbt)
      val baos = new ByteArrayOutputStream()
      val dos = new DataOutputStream(baos)
      CompressedStreamTools.write(nbt, dos)
      lua.pushByteArray(baos.toByteArray)
      2
    })
    lua.setField(-2, "save")

    lua.pushScalaFunction(lua => {
      try {
        val className = lua.toString(1)
        val clazz = Class.forName(className)
        val persistable = clazz.newInstance.asInstanceOf[Persistable]
        val data = lua.toByteArray(2)
        val bais = new ByteArrayInputStream(data)
        val dis = new DataInputStream(bais)
        val nbt = CompressedStreamTools.read(dis)
        persistable.load(nbt)
        lua.pushJavaObjectRaw(persistable)
        1
      }
      catch {
        case t: Throwable =>
          Ocelot.log.warn("Error in userdata load function.", t)
          throw t
      }
    })
    lua.setField(-2, "load")

    lua.pushScalaFunction(lua => {
      val value = lua.toJavaObjectRaw(1).asInstanceOf[Value]
      val args = lua.toSimpleJavaObjects(2)
      owner.invoke(() => Registry.convert(Array(value.apply(machine, new Arguments(args)))))
    })
    lua.setField(-2, "apply")

    lua.pushScalaFunction(lua => {
      val value = lua.toJavaObjectRaw(1).asInstanceOf[Value]
      val args = lua.toSimpleJavaObjects(2)
      owner.invoke(() => {
        value.unapply(machine, new Arguments(args))
        null
      })
    })
    lua.setField(-2, "unapply")

    lua.pushScalaFunction(lua => {
      val value = lua.toJavaObjectRaw(1).asInstanceOf[Value]
      val args = lua.toSimpleJavaObjects(2)
      owner.invoke(() => Registry.convert(value.call(machine, new Arguments(args))))
    })
    lua.setField(-2, "call")

    lua.pushScalaFunction(lua => {
      val value = lua.toJavaObjectRaw(1).asInstanceOf[Value]
      try value.dispose(machine) catch {
        case t: Throwable => Ocelot.log.warn("Error in dispose method of userdata of type " + value.getClass.getName, t)
      }
      0
    })
    lua.setField(-2, "dispose")

    lua.pushScalaFunction(lua => {
      val value = lua.toJavaObjectRaw(1).asInstanceOf[Value]
      lua.pushValue(machine.methods(value).map(entry => {
        val (name, annotation) = entry
        name -> annotation.direct
      }))
      1
    })
    lua.setField(-2, "methods")

    lua.pushScalaFunction(lua => {
      val value = lua.toJavaObjectRaw(1).asInstanceOf[Value]
      val method = lua.checkString(2)
      val args = lua.toSimpleJavaObjects(3)
      owner.invoke(() => machine.invoke(value, method, args.toArray))
    })
    lua.setField(-2, "invoke")

    lua.pushScalaFunction(lua => {
      val value = lua.toJavaObjectRaw(1).asInstanceOf[Value]
      val method = lua.checkString(2)
      owner.documentation(() => machine.methods(value)(method).doc)
    })
    lua.setField(-2, "doc")

    lua.setGlobal("userdata")
  }
}
