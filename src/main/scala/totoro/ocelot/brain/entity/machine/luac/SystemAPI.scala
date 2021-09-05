package totoro.ocelot.brain.entity.machine.luac

import li.cil.repack.com.naef.jnlua.LuaType
import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.machine.ExtendedLuaState.extendLuaState

class SystemAPI(owner: NativeLuaArchitecture) extends NativeLuaAPI(owner) {
  override def initialize(): Unit = {
    // Until we get to ingame screens we log to Java's stdout.
    lua.pushScalaFunction(lua => {
      println((1 to lua.getTop).map(i => lua.`type`(i) match {
        case LuaType.NIL => "nil"
        case LuaType.BOOLEAN => lua.toBoolean(i)
        case LuaType.NUMBER => lua.toNumber(i)
        case LuaType.STRING => lua.toString(i)
        case LuaType.TABLE => "table"
        case LuaType.FUNCTION => "function"
        case LuaType.THREAD => "thread"
        case LuaType.LIGHTUSERDATA | LuaType.USERDATA => "userdata"
      }).mkString("  "))
      0
    })
    lua.setGlobal("print")

    // Create system table, avoid magic global non-tables.
    lua.newTable()

    // Whether bytecode may be loaded directly.
    lua.pushScalaFunction(lua => {
      lua.pushBoolean(Settings.get.allowBytecode)
      1
    })
    lua.setField(-2, "allowBytecode")

    // Whether custom __gc callbacks are allowed.
    lua.pushScalaFunction(lua => {
      lua.pushBoolean(Settings.get.allowGC)
      1
    })
    lua.setField(-2, "allowGC")

    // How long programs may run without yielding before we stop them.
    lua.pushScalaFunction(lua => {
      lua.pushNumber(Settings.get.timeout)
      1
    })
    lua.setField(-2, "timeout")

    lua.setGlobal("system")
  }
}
