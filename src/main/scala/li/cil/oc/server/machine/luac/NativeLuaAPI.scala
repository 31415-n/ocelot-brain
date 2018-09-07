package li.cil.oc.server.machine.luac

import li.cil.oc.server.machine.ArchitectureAPI
import li.cil.repack.com.naef.jnlua.LuaState

abstract class NativeLuaAPI(val owner: NativeLuaArchitecture) extends ArchitectureAPI(owner.machine) {
  protected def lua: LuaState = owner.lua
}
