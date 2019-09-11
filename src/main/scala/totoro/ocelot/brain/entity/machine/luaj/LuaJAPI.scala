package totoro.ocelot.brain.entity.machine.luaj

import li.cil.repack.org.luaj.vm2.Globals
import totoro.ocelot.brain.entity.machine.ArchitectureAPI

abstract class LuaJAPI(val owner: LuaJLuaArchitecture) extends ArchitectureAPI(owner.machine) {
  protected def lua: Globals = owner.lua
}
