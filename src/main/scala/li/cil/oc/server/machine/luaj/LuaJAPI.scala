package li.cil.oc.server.machine.luaj

import li.cil.oc.server.machine.ArchitectureAPI
import li.cil.repack.org.luaj.vm2.Globals

abstract class LuaJAPI(val owner: LuaJLuaArchitecture) extends ArchitectureAPI(owner.machine) {
  protected def lua: Globals = owner.lua
}
