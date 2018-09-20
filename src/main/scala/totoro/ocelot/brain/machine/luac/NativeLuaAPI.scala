package totoro.ocelot.brain.machine.luac

import li.cil.repack.com.naef.jnlua.LuaState
import totoro.ocelot.brain.machine.ArchitectureAPI

abstract class NativeLuaAPI(val owner: NativeLuaArchitecture) extends ArchitectureAPI(owner.machine) {
  protected def lua: LuaState = owner.lua
}
