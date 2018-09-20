package totoro.ocelot.brain.loot

import totoro.ocelot.brain.entity.fs.{FileSystemAPI, ReadWriteLabel}
import totoro.ocelot.brain.entity.{EEPROM, Environment, FileSystem, FloppyDisk}
import totoro.ocelot.brain.util.DyeColor
import totoro.ocelot.brain.{Ocelot, Settings}

object Loot {
  var OpenOsBIOS: LootFactory = _
  var OpenOsFloppy: LootFactory = _

  def init(): Unit = {
    // EEPROM
    OpenOsBIOS = new LootFactory {
      private val code = new Array[Byte](4 * 1024)
      private val count = Ocelot.getClass.getResourceAsStream(Settings.scriptPath + "bios.lua").read(code)
      private val codeData = code.take(count)

      override def create(): Environment = {
        val eeprom = new EEPROM()
        eeprom.label = "OpenOS BIOS"
        eeprom.codeData = codeData
        eeprom.readonly = true
        eeprom
      }
    }

    // Floppies
    OpenOsFloppy = new FloppyFactory("OpenOS (Operating System)", "openos", DyeColor.GREEN)
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
}
