package totoro.ocelot.brain.entity.machine.luac

import li.cil.repack.com.naef.jnlua.LuaState
import totoro.ocelot.brain.entity.machine.ArchitectureAPI

abstract class NativeLuaAPI(val owner: NativeLuaArchitecture) extends ArchitectureAPI(owner.machine) {
  protected def lua: LuaState = owner.lua
}
