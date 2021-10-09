package totoro.ocelot.brain.entity.machine.luaj

import li.cil.repack.org.luaj.vm2.{LuaValue, Varargs}
import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.machine.ScalaClosure._

class SystemAPI(owner: LuaJLuaArchitecture) extends LuaJAPI(owner) {
  override def initialize(): Unit = {
    val system = LuaValue.tableOf()

    // Whether bytecode may be loaded directly.
    system.set("allowBytecode", (_: Varargs) => LuaValue.valueOf(Settings.get.allowBytecode))

    // Whether custom __gc callbacks are allowed.
    system.set("allowGC", (_: Varargs) => LuaValue.valueOf(Settings.get.allowGC))

    // How long programs may run without yielding before we stop them.
    system.set("timeout", (_: Varargs) => LuaValue.valueOf(Settings.get.timeout))

    lua.set("system", system)
  }
}
