package totoro.ocelot.brain.loot

import totoro.ocelot.brain.entity.fs.{FileSystemAPI, ReadWriteLabel}
import totoro.ocelot.brain.entity.{EEPROM, Environment, FileSystem, FloppyDisk}
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

  abstract class LootFactory {
    def create(): Environment
  }

  abstract class FileSystemFactory extends LootFactory {
    def createFileSystem(): FileSystem
  }

  class FloppyFactory(name: String, path: String, color: DyeColor, external: Boolean = false) extends FileSystemFactory {
    override def createFileSystem(): FileSystem = {
      FileSystemAPI.asManagedEnvironment(
        if (external) FileSystemAPI.asReadOnly(FileSystemAPI.fromSaveDirectory("loot/" + path, 0, buffered = false))
        else FileSystemAPI.fromClass(Ocelot.getClass, Settings.resourceDomain, "loot/" + path),
        new ReadWriteLabel(name)
      )
    }

    override def create(): Environment = {
      new FloppyDisk(name, this)
    }
  }

  class EEPROMFactory(label: String, file: String, readonly: Boolean = true) extends LootFactory {
    private val code = new Array[Byte](4 * 1024)
    private val count = Ocelot.getClass.getResourceAsStream(Settings.scriptPath + file).read(code)
    private val codeData = code.take(count)

    override def create(): Environment = {
      val eeprom = new EEPROM()
      eeprom.label = label
      eeprom.codeData = codeData
      eeprom.readonly = readonly
      eeprom
    }
  }
}
