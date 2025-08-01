package totoro.ocelot.brain.entity.machine.luac

import com.google.common.base.Strings
import com.google.common.io.PatternFilenameFilter
import li.cil.repack.com.naef.jnlua
import li.cil.repack.com.naef.jnlua.{LuaState, LuaStateFiveFour, LuaStateFiveThree}
import org.apache.commons.lang3.SystemUtils
import totoro.ocelot.brain.entity.machine.ExtendedLuaState._
import totoro.ocelot.brain.entity.machine.Machine
import totoro.ocelot.brain.{Ocelot, Settings}

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream}
import java.nio.channels.Channels
import java.nio.file.{Files, Path, Paths}
import java.util.regex.Pattern
import scala.util.Random

object LuaStateFactory {
  def isAvailable: Boolean = {
    // Force initialization of all.
    val lua52 = Lua52.isAvailable
    val lua53 = Lua53.isAvailable
    val lua54 = Lua54.isAvailable
    lua52 || lua53 || lua54
  }

  def luajRequested: Boolean = Settings.get.forceLuaJ || Settings.get.registerLuaJArchitecture

  def includeLuaJ: Boolean = !isAvailable || luajRequested

  def include52: Boolean = Lua52.isAvailable && !Settings.get.forceLuaJ

  def include53: Boolean = Lua53.isAvailable && Settings.get.enableLua53 && !Settings.get.forceLuaJ

  def include54: Boolean = Lua54.isAvailable && Settings.get.enableLua54 && !Settings.get.forceLuaJ

  def default53: Boolean = include53 && Settings.get.defaultLua53

  def init(librariesPath: Path): Unit = {
    Files.createDirectories(librariesPath)

    Lua52.init(librariesPath)
    Lua53.init(librariesPath)
    Lua54.init(librariesPath)
  }

  object Lua52 extends LuaStateFactory {
    override def version: String = "52"

    override protected def create(maxMemory: Option[Int]): LuaState = maxMemory.fold(new jnlua.LuaState())(new jnlua.LuaState(_))

    override protected def openLibs(state: jnlua.LuaState): Unit = {
      state.openLib(jnlua.LuaState.Library.BASE)
      state.openLib(jnlua.LuaState.Library.BIT32)
      state.openLib(jnlua.LuaState.Library.COROUTINE)
      state.openLib(jnlua.LuaState.Library.DEBUG)
      state.openLib(jnlua.LuaState.Library.ERIS)
      state.openLib(jnlua.LuaState.Library.MATH)
      state.openLib(jnlua.LuaState.Library.STRING)
      state.openLib(jnlua.LuaState.Library.TABLE)
      state.pop(8)
    }
  }

  object Lua53 extends LuaStateFactory {
    override def version: String = "53"

    override protected def create(maxMemory: Option[Int]): LuaStateFiveThree = maxMemory.fold(new jnlua.LuaStateFiveThree())(new jnlua.LuaStateFiveThree(_))

    override protected def openLibs(state: jnlua.LuaState): Unit = {
      state.openLib(jnlua.LuaState.Library.BASE)
      state.openLib(jnlua.LuaState.Library.COROUTINE)
      state.openLib(jnlua.LuaState.Library.DEBUG)
      state.openLib(jnlua.LuaState.Library.ERIS)
      state.openLib(jnlua.LuaState.Library.MATH)
      state.openLib(jnlua.LuaState.Library.STRING)
      state.openLib(jnlua.LuaState.Library.TABLE)
      state.openLib(jnlua.LuaState.Library.UTF8)
      state.pop(8)
    }
  }

  object Lua54 extends LuaStateFactory {
    override def version: String = "54"

    override protected def create(maxMemory: Option[Int]): LuaStateFiveFour = maxMemory.fold(new jnlua.LuaStateFiveFour())(new jnlua.LuaStateFiveFour(_))

    override protected def openLibs(state: jnlua.LuaState): Unit = {
      state.openLib(jnlua.LuaState.Library.BASE)
      state.openLib(jnlua.LuaState.Library.COROUTINE)
      state.openLib(jnlua.LuaState.Library.DEBUG)
      state.openLib(jnlua.LuaState.Library.ERIS)
      state.openLib(jnlua.LuaState.Library.MATH)
      state.openLib(jnlua.LuaState.Library.STRING)
      state.openLib(jnlua.LuaState.Library.TABLE)
      state.openLib(jnlua.LuaState.Library.UTF8)
      state.pop(8)
    }
  }

}

/**
  * Factory singleton used to spawn new LuaState instances.
  *
  * This is realized as a singleton so that we only have to resolve shared
  * library references once during initialization and can then re-use the
  * already loaded ones.
  */
abstract class LuaStateFactory {
  def version: String

  // ----------------------------------------------------------------------- //
  // Initialization
  // ----------------------------------------------------------------------- //

  /** Set to true in initialization code below if available. */
  private var haveNativeLibrary = false

  private var currentLib = ""

  private val libraryName = {
    val libExtension = {
      if (SystemUtils.IS_OS_MAC) ".dylib"
      else if (SystemUtils.IS_OS_WINDOWS) ".dll"
      else ".so"
    }

    val platformName = {
      if (!Strings.isNullOrEmpty(Settings.get.forceNativeLibPlatform)) Settings.get.forceNativeLibPlatform
      else {
        val systemName = {
          if (SystemUtils.IS_OS_FREE_BSD) "freebsd"
          else if (SystemUtils.IS_OS_NET_BSD) "netbsd"
          else if (SystemUtils.IS_OS_OPEN_BSD) "openbsd"
          else if (SystemUtils.IS_OS_SOLARIS) "solaris"
          else if (SystemUtils.IS_OS_LINUX) "linux"
          else if (SystemUtils.IS_OS_MAC) "darwin"
          else if (SystemUtils.IS_OS_WINDOWS) "windows"
          else "unknown"
        }

        val archName = {
          if (Architecture.IS_OS_ARM64) "aarch64"
          else if (Architecture.IS_OS_ARM) "arm"
          else if (Architecture.IS_OS_X64) "x86_64"
          else if (Architecture.IS_OS_X86) "x86"
          else "unknown"
        }

        systemName + "-" + archName
      }
    }

    "libjnlua" + version + "-" + platformName + libExtension
  }

  protected def create(maxMemory: Option[Int] = None): jnlua.LuaState

  protected def openLibs(state: jnlua.LuaState): Unit

  // ----------------------------------------------------------------------- //

  def isAvailable: Boolean = haveNativeLibrary

  // Since we use native libraries we have to do some work. This includes
  // figuring out what we're running on, so that we can load the proper shared
  // libraries compiled for that system. It also means we have to unpack the
  // shared libraries somewhere so that we can load them, because we cannot
  // load them directly from a JAR. Lastly, we need to handle library overrides in
  // case the user wants to use custom libraries, or are not on a supported platform.
  def init(librariesPath: Path): Unit = {
    if (libraryName == null) {
      return
    }

    if (SystemUtils.IS_OS_WINDOWS && !Settings.get.alwaysTryNative) {
      if (SystemUtils.IS_OS_WINDOWS_XP) {
        Ocelot.log.warn("Sorry, but Windows XP isn't supported. I'm afraid you'll have to use a newer Windows. " +
          "I very much recommend upgrading your Windows, anyway, since Microsoft has stopped supporting Windows XP in April 2014.")
        return
      }

      if (SystemUtils.IS_OS_WINDOWS_2003) {
        Ocelot.log.warn("Sorry, but Windows Server 2003 isn't supported. I'm afraid you'll have to use a newer Windows.")
        return
      }
    }

    var tmpLibFile: File = null
    if (!Strings.isNullOrEmpty(Settings.get.forceNativeLibPathFirst)) {
      val libraryTest = new File(Settings.get.forceNativeLibPathFirst, libraryName)

      if (libraryTest.canRead) {
        tmpLibFile = libraryTest
        currentLib = libraryTest.getAbsolutePath
        Ocelot.log.info(s"Found forced-path filesystem library $currentLib.")
      }
      else
        Ocelot.log.warn(s"forceNativeLibPathFirst is set, but $currentLib was not found there. Falling back to checking the built-in libraries.")
    }

    if (currentLib.isEmpty) {
      val libraryUrl = classOf[Machine].getResource(s"/assets/${Settings.resourceDomain}/lib/$libraryName")
      if (libraryUrl == null) {
        Ocelot.log.warn(s"Native library with name '$libraryName' not found.")
        return
      }

      val tmpLibName = s"Ocelot-${Ocelot.Version}-$version-$libraryName"

      val tmpBasePath: Path =
        if (Settings.get.nativeInTmpDir) {
          val tmpDirName = System.getProperty("java.io.tmpdir")

          Paths.get(
            if (tmpDirName == null) ""
            else tmpDirName
          )
        }
        else {
          librariesPath
        }

      tmpLibFile = tmpBasePath.resolve(tmpLibName).toFile

      // Clean up old library files when not in tmp dir.
      if (!Settings.get.nativeInTmpDir) {
        val libDir = tmpBasePath.toFile

        if (libDir.isDirectory) {
          for (file <- libDir.listFiles(new PatternFilenameFilter("^" + Pattern.quote("Ocelot-") + ".*" + Pattern.quote("-" + libraryName) + "$"))) {
            if (file.compareTo(tmpLibFile) != 0) {
              file.delete()
            }
          }
        }
      }

      // If the file, already exists, make sure it's the same we need, if it's
      // not disable use of the natives.
      if (tmpLibFile.exists()) {
        var matching = true
        try {
          val inCurrent = new BufferedInputStream(libraryUrl.openStream())
          val inExisting = new BufferedInputStream(new FileInputStream(tmpLibFile))
          var inCurrentByte = 0
          var inExistingByte = 0

          do {
            inCurrentByte = inCurrent.read()
            inExistingByte = inExisting.read()
            if (inCurrentByte != inExistingByte) {
              matching = false
              inCurrentByte = -1
              inExistingByte = -1
            }
          }
          while (inCurrentByte != -1 && inExistingByte != -1)

          inCurrent.close()
          inExisting.close()
        }
        catch {
          case _: Throwable =>
            matching = false
        }
        if (!matching) {
          // Try to delete an old instance of the library, in case we have an update
          // and deleteOnExit fails (which it regularly does on Windows it seems).
          // Note that this should only ever be necessary for dev-builds, where the
          // version number didn't change (since the version number is part of the name).
          try {
            tmpLibFile.delete()
          }
          catch {
            case _: Throwable => // Ignore.
          }
          if (tmpLibFile.exists()) {
            Ocelot.log.warn(s"Could not update native library '${tmpLibFile.getName}'!")
          }
        }
      }

      // Copy the file contents to the temporary file.
      try {
        val in = Channels.newChannel(libraryUrl.openStream())
        try {
          val out = new FileOutputStream(tmpLibFile).getChannel
          try {
            out.transferFrom(in, 0, Long.MaxValue)
            tmpLibFile.deleteOnExit()
            // Set file permissions more liberally for multi-user+instance servers.
            tmpLibFile.setReadable(true, false)
            tmpLibFile.setWritable(true, false)
            tmpLibFile.setExecutable(true, false)
          }
          finally {
            out.close()
          }
        }
        finally {
          in.close()
        }
      }
      catch {
        // Java (or Windows?) locks the library file when opening it, so any
        // further tries to update it while another instance is still running
        // will fail. We still want to try each time, since the files may have
        // been updated.
        // Alternatively, the file could not be opened for reading/writing.
        case _: Throwable => // Nothing.
      }
      // Try to load the lib.
      currentLib = tmpLibFile.getAbsolutePath
    }

    try {
      LuaStateFactory.synchronized {
        System.load(currentLib)
        try {
          create().close()
        } catch {
          case t: Throwable => Ocelot.log.trace("Something went wrong!", t)
        }
      }
      Ocelot.log.info(s"Found a compatible native library: '${tmpLibFile.getName}'.")
      haveNativeLibrary = true
    }
    catch {
      case t: Throwable =>
        if (Settings.get.logFullLibLoadErrors) {
          Ocelot.log.warn(s"Could not load native library '${tmpLibFile.getName}'.", t)
        }
        else {
          Ocelot.log.trace(s"Could not load native library '${tmpLibFile.getName}'.")
        }
        tmpLibFile.delete()
    }

    if (!haveNativeLibrary) {
      Ocelot.log.warn("Unsupported platform, you won't be able to host games with persistent computers.")
    }
  }

  // ----------------------------------------------------------------------- //
  // Factory
  // ----------------------------------------------------------------------- //

  def createState(): Option[jnlua.LuaState] = {
    if (!haveNativeLibrary) return None

    try {
      val state = LuaStateFactory.synchronized {
        System.load(currentLib)
        if (Settings.get.limitMemory) create(Some(Int.MaxValue))
        else create()
      }
      try {
        // Load all libraries.
        openLibs(state)

        if (!Settings.get.disableLocaleChanging) {
          state.openLib(jnlua.LuaState.Library.OS)
          state.getField(-1, "setlocale")
          state.pushString("C")
          state.call(1, 0)
          state.pop(1)
        }

        // Prepare table for os stuff.
        state.newTable()
        state.setGlobal("os")

        // Kill compat entries.
        state.pushNil()
        state.setGlobal("unpack")
        state.pushNil()
        state.setGlobal("loadstring")
        state.getGlobal("math")
        state.pushNil()
        state.setField(-2, "log10")
        state.pop(1)
        state.getGlobal("table")
        state.pushNil()
        state.setField(-2, "maxn")
        state.pop(1)

        // Remove some other functions we don't need and are dangerous.
        state.pushNil()
        state.setGlobal("dofile")
        state.pushNil()
        state.setGlobal("loadfile")

        state.getGlobal("math")

        // We give each Lua state it's own randomizer, since otherwise they'd
        // use the good old rand() from C. Which can be terrible, and isn't
        // necessarily thread-safe.
        val random = new Random
        state.pushScalaFunction(lua => {
          val r = random.nextDouble()
          lua.getTop match {
            case 0 => lua.pushNumber(r)
            case 1 =>
              val u = lua.checkNumber(1)
              lua.checkArg(1, 1 <= u, "interval is empty")
              lua.pushNumber(math.floor(r * u) + 1)
            case 2 =>
              val l = lua.checkNumber(1)
              val u = lua.checkNumber(2)
              lua.checkArg(2, l <= u, "interval is empty")
              lua.pushNumber(math.floor(r * (u - l + 1)) + l)
            case _ => throw new IllegalArgumentException("wrong number of arguments")
          }
          1
        })
        state.setField(-2, "random")

        state.pushScalaFunction(lua => {
          random.setSeed(lua.checkInteger(1))
          0
        })
        state.setField(-2, "randomseed")

        // Pop the math table.
        state.pop(1)

        return Some(state)
      }
      catch {
        case t: Throwable =>
          Ocelot.log.warn("Failed creating Lua state.", t)
          state.close()
      }
    }
    catch {
      case t: UnsatisfiedLinkError =>
        Ocelot.log.error("Failed loading the native libraries.", t)
      case t: Throwable =>
        Ocelot.log.warn("Failed creating Lua state.", t)
    }
    None
  }

  // Inspired by org.apache.commons.lang3.SystemUtils
  object Architecture {
    val OS_ARCH: String = try System.getProperty("os.arch") catch {
      case _: SecurityException => null
    }

    val IS_OS_ARM: Boolean = isOSArchMatch("arm")

    val IS_OS_ARM64: Boolean = isOSArchMatch("aarch64")

    val IS_OS_X86: Boolean = isOSArchMatch("x86") || isOSArchMatch("i386")

    val IS_OS_X64: Boolean = isOSArchMatch("x86_64") || isOSArchMatch("amd64")

    private def isOSArchMatch(archPrefix: String): Boolean = OS_ARCH != null && OS_ARCH.startsWith(archPrefix)
  }
}
