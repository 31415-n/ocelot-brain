package totoro.ocelot.brain.loot

import totoro.ocelot.brain.entity.fs.{FileSystem, FileSystemAPI}
import totoro.ocelot.brain.entity.traits.Entity
import totoro.ocelot.brain.entity.{EEPROM, FloppyManaged}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.DyeColor
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Ocelot, Settings}

object Loot {
  var Eeproms: IndexedSeq[EEPROMFactory] = _
  var Floppies: IndexedSeq[FloppyFactory] = _

  var LuaBiosEEPROM: EEPROMFactory = _
  var AdvLoaderEEPROM: EEPROMFactory = _
  var CyanBIOSEEPROM: EEPROMFactory = _
  var MineOSEFIEEPROM: EEPROMFactory = _

  var OpenOsFloppy: FloppyFactory = _
  var Plan9kFloppy: FloppyFactory = _
  var OPPMFloppy: FloppyFactory = _
  var OpenLoaderFloppy: FloppyFactory = _
  var NetworkFloppy: FloppyFactory = _
  var IrcFloppy: FloppyFactory = _
  var DataFloppy: FloppyFactory = _
  var HpmFloppy: FloppyFactory = _

  def init(): Unit = {
    // EEPROM
    val eeproms = IndexedSeq.newBuilder[EEPROMFactory]
    def registerEeprom(eeprom: EEPROMFactory): EEPROMFactory = {
      eeproms += eeprom
      eeprom
    }

    LuaBiosEEPROM = registerEeprom(new EEPROMFactory("Lua BIOS", "bios.lua"))
    AdvLoaderEEPROM = registerEeprom(new EEPROMFactory("advancedLoader", "advLoader.lua"))
    CyanBIOSEEPROM = registerEeprom(new EEPROMFactory("Cyan BIOS", "cyan.lua"))
    MineOSEFIEEPROM = new EEPROMFactory("MineOS EFI", "mineosEFI.lua")

    // Floppies
    val floppies = IndexedSeq.newBuilder[FloppyFactory]
    def registerFloppy(floppy: FloppyFactory): FloppyFactory = {
      floppies += floppy
      floppy
    }

    NetworkFloppy = registerFloppy(new FloppyFactory("Network (Network Stack)", DyeColor.Lime, "network"))
    Plan9kFloppy = registerFloppy(new FloppyFactory("Plan9k (Operating System)", DyeColor.Red, "plan9k"))
    IrcFloppy = registerFloppy(new FloppyFactory("OpenIRC (IRC Client)", DyeColor.LightBlue, "irc"))
    OpenLoaderFloppy = registerFloppy(new FloppyFactory("OpenLoader (Boot Loader)", DyeColor.Magenta, "openloader"))
    OpenOsFloppy = registerFloppy(new FloppyFactory("OpenOS (Operating System)", DyeColor.Green, "openos"))
    OPPMFloppy = registerFloppy(new FloppyFactory("OPPM (Package Manager)", DyeColor.Cyan, "oppm"))
    DataFloppy = registerFloppy(new FloppyFactory("Data Card Software", DyeColor.Pink, "data"))
    HpmFloppy = registerFloppy(new FloppyFactory("hpm (Package manager)", DyeColor.Red, "hpm"))

    Eeproms = eeproms.result()
    Floppies = floppies.result()
  }

  // ----------------------------------------------------------------------- //

  abstract class LootFactory[T <: Entity] {
    def create(): T
  }

  class LootFloppy(name: String, private var _label: String, color: DyeColor, var path: String)
    extends FloppyManaged(Option(name), color) {

    def this() = this(null, null, DyeColor.Gray, null)

    override protected def generateEnvironment(): FileSystem = {
      FileSystemAPI.asManagedEnvironment(
        FileSystemAPI.fromClass(Ocelot.getClass, Settings.resourceDomain, "loot/" + path),
        _label, activityType.orNull
      )
    }

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)

      nbt.setString(LootFloppy.PathTag, path)
      nbt.setString(LootFloppy.LabelTag, _label)
    }

    override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
      // important: we need to load these things first to be able to use it later
      //            to initialize the filesystem (see generateEnvironment)

      // this tag didn't exist previously. a ludicrous omission, I agree.
      if (nbt.hasKey(LootFloppy.LabelTag)) {
        _label = nbt.getString(LootFloppy.LabelTag)
      }

      path = nbt.getString(LootFloppy.PathTag)

      super.load(nbt, workspace)
    }
  }

  object LootFloppy {
    private val PathTag = "path"
    private val LabelTag = "label"
  }

  class FloppyFactory(val name: String, val color: DyeColor, path: String) extends LootFactory[LootFloppy] {
    val label: String = path

    override def create() = new LootFloppy(name, label, color, path)
  }

  class EEPROMFactory(val label: String, file: String, readonly: Boolean = false) extends LootFactory[EEPROM] {
    private val code = new Array[Byte](4 * 1024)
    private val count = Ocelot.getClass.getResourceAsStream(Settings.scriptPath + file).read(code)
    private val codeData = code.take(count)

    override def create(): EEPROM = {
      val eeprom = new EEPROM()
      eeprom.label = label
      eeprom.readonly = readonly
      eeprom.codeBytes = Some(codeData)
      eeprom
    }
  }
}
