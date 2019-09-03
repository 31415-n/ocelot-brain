package totoro.ocelot.brain.loot

import totoro.ocelot.brain.entity.fs.{FileSystemAPI, ReadWriteLabel}
import totoro.ocelot.brain.entity.traits.{Entity, Environment}
import totoro.ocelot.brain.entity.{EEPROM, FileSystem, FloppyManaged}
import totoro.ocelot.brain.util.DyeColor
import totoro.ocelot.brain.{Ocelot, Settings}

object Loot {
  var OpenOsEEPROM: LootFactory = _
  var AdvLoaderEEPROM: LootFactory = _
  var OpenOsFloppy: LootFactory = _

  def init(): Unit = {
    // EEPROM
    OpenOsEEPROM = new EEPROMFactory("OpenOS BIOS", "bios.lua")
    AdvLoaderEEPROM = new EEPROMFactory("advancedLoader", "advLoader.lua")

    // Floppies
    OpenOsFloppy = new FloppyFactory("OpenOS (Installation Floppy)", "openos", DyeColor.GREEN)
  }

  // ----------------------------------------------------------------------- //

  abstract class LootFactory {
    def create(): Entity
  }

  class LootFloppy(name: String, path: String, external: Boolean) extends FloppyManaged(null, name) {
    override protected def generateEnvironment(): FileSystem = {
      FileSystemAPI.asManagedEnvironment(
        if (external) FileSystemAPI.asReadOnly(FileSystemAPI.fromSaveDirectory("loot/" + path, 0, buffered = false))
        else FileSystemAPI.fromClass(Ocelot.getClass, Settings.resourceDomain, "loot/" + path),
        new ReadWriteLabel(name)
      )
    }
  }

  class FloppyFactory(name: String, path: String, color: DyeColor, external: Boolean = false) extends LootFactory {
    override def create(): Entity = {
      new LootFloppy(name, path, external)
    }
  }

  class EEPROMFactory(label: String, file: String, readonly: Boolean = true) extends LootFactory {
    private val code = new Array[Byte](4 * 1024)
    private val count = Ocelot.getClass.getResourceAsStream(Settings.scriptPath + file).read(code)
    private val codeData = code.take(count)

    override def create(): Entity = {
      val eeprom = new EEPROM()
      eeprom.label = label
      eeprom.codeData = codeData
      eeprom.readonly = readonly
      eeprom
    }
  }
}
