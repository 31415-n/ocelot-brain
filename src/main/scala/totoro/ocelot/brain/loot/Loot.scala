package totoro.ocelot.brain.loot

import totoro.ocelot.brain.entity.EEPROM
import totoro.ocelot.brain.{Ocelot, Settings}

object Loot {
  val OpenOsBIOS: EEPROM = new EEPROM()

  def init(): Unit = {
    // EEPROM (Lua BIOS)
    val code = new Array[Byte](4 * 1024)
    val count = Ocelot.getClass.getResourceAsStream(Settings.scriptPath + "bios.lua").read(code)
    OpenOsBIOS.label = "OpenOS BIOS"
    OpenOsBIOS.codeData = code.take(count)
    OpenOsBIOS.readonly = true
  }
}
