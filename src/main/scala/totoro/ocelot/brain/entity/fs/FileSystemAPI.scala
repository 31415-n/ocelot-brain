package totoro.ocelot.brain.entity.fs

import totoro.ocelot.brain.event.FileSystemActivityType.ActivityType
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Ocelot, Settings}

import java.io
import java.io.File
import java.net.{MalformedURLException, URISyntaxException, URL}
import java.nio.file.Path
import java.util.UUID
import scala.util.Try

object FileSystemAPI extends {
  def isCaseInsensitive(path: io.File): Boolean = Settings.get.forceCaseInsensitive || (try {
    val uuid = UUID.randomUUID().toString
    val lowerCase = new io.File(path, uuid + "oc_rox")
    val upperCase = new io.File(path, uuid + "OC_ROX")
    // This should NEVER happen but could also lead to VERY weird bugs, so we
    // make sure the files don't exist.
    lowerCase.exists() && lowerCase.delete()
    upperCase.exists() && upperCase.delete()
    lowerCase.createNewFile()
    val insensitive = upperCase.exists()
    lowerCase.delete()
    insensitive
  }
  catch {
    case t: Throwable =>
      // Among the security errors, createNewFile can throw an IOException.
      // We just fall back to assuming case insensitive, since that's always
      // safe in those cases.
      Ocelot.log.warn("Couldn't determine if file system is case sensitive, falling back to insensitive.", t)
      true
  })

  // Worst-case: we're on Windows or using a FAT32 partition mounted in *nix.
  // Note: we allow / as the path separator and expect all \s to be converted
  // accordingly before the path is passed to the file system.
  private val invalidChars = """\:*?"<>|""".toSet

  def isValidFilename(name: String): Boolean = !name.exists(invalidChars.contains)

  def validatePath(path: String): String = {
    if (!isValidFilename(path)) {
      throw new java.io.IOException("path contains invalid characters")
    }
    path
  }

  /**
    * Creates a new file system based on the location of a class.
    *
    * This can be used to wrap a folder in the assets folder of your mod's JAR.
    * The actual path is built like this:
    * `"/assets/" + domain + "/" + root`
    *
    * If the class is located in a JAR file, this will create a read-only file
    * system based on that JAR file. If the class file is located in the native
    * file system, this will create a read-only file system first trying from
    * the actual location of the class file, and failing that by searching the
    * class path (i.e. it'll look for a path constructed as described above).
    *
    * If the specified path cannot be located, the creation fails and this
    * returns `null`.
    *
    * @param clazz  the class whose containing JAR to wrap.
    * @param domain the domain, usually your mod's ID.
    * @param root   an optional subdirectory.
    * @return a file system wrapping the specified folder.
    */
  def fromClass(clazz: Class[_], domain: String, root: String): FileSystemTrait = {
    val innerPath = ("/assets/" + domain + "/" + (root.trim + "/")).replace("//", "/")

    val codeSource = clazz.getProtectionDomain.getCodeSource.getLocation.getPath
    val (codeUrl, isArchive) =
      if (codeSource.contains(".zip!") || codeSource.contains(".jar!"))
        (codeSource.substring(0, codeSource.lastIndexOf('!')), true)
      else if (codeSource.contains(".zip") || codeSource.contains(".jar"))
        (codeSource, true)
      else
        (codeSource, false)

    val url = Try {
      new URL(codeUrl)
    }.recoverWith {
      case _: MalformedURLException => Try {
        new URL("file://" + codeUrl)
      }
    }
    val file = url.map(url => new io.File(url.toURI)).recoverWith {
      case _: URISyntaxException => url.map(url => new io.File(url.getPath))
    }.getOrElse(new io.File(codeSource))

    if (isArchive) ZipFileInputStreamFileSystem.fromFile(file, innerPath.substring(1))
    else {
      if (!file.exists) return null
      new io.File(new io.File(file.getParent), innerPath) match {
        case fsp if fsp.exists() && fsp.isDirectory =>
          new ReadOnlyFileSystem(fsp)
        case _ =>
          System.getProperty("java.class.path").split(System.getProperty("path.separator")).
            find(cp => {
              val fsp = new io.File(new io.File(cp), innerPath)
              fsp.exists() && fsp.isDirectory
            }) match {
            case None => null
            case Some(dir) => new ReadOnlyFileSystem(new io.File(new io.File(dir), innerPath))
          }
      }
    }
  }

  /**
    * Creates a new ''writable'' file system in the save folder.
    *
    * This will create a folder, if necessary, and create a writable virtual
    * file system based in that folder. The actual path is based in a sub-
    * folder of the save folder. The actual path is built like this:
    * `"saves/" + WORLD_NAME + "/opencomputers/" + root`
    * The first part may differ, in particular for servers.
    *
    * Usually the name will be the address of the node used to represent the
    * file system.
    *
    * Note that by default file systems are "buffered", meaning that any
    * changes made to them are only saved to disk when the world is saved. This
    * ensured that the file system contents do not go "out of sync" when the
    * game crashes, but introduces additional memory overhead, since all files
    * in the file system have to be kept in memory.
    *
    * @param file     the file of the filesystem
    * @param capacity the amount of space in bytes to allow being used.
    * @param buffered whether data should only be written to disk when saving.
    * @return a file system wrapping the specified folder.
    */
  def fromDirectory(file: File, capacity: Long, buffered: Boolean): Capacity = {
    if (!file.isDirectory) file.delete()
      file.mkdirs()

    if (file.exists() && file.isDirectory)
      if (buffered)
        new BufferedFileSystem(file, capacity)
      else
        new ReadWriteFileSystem(file, capacity)
    else
      null
  }

  /**
    * Creates a new ''writable'' file system that resides in memory.
    *
    * Any contents created and written on this file system will be lost when
    * the node is removed from the network.
    *
    * This is used for computers' `/tmp` mount, for example.
    *
    * @param capacity the capacity of the file system.
    * @return a file system residing in memory.
    */
  def fromMemory(capacity: Long): FileSystemTrait = new RamFileSystem(capacity)

  /**
    * Wrap a file system retrieved via one of the `from???` methods to
    * make it read-only.
    *
    * @param fileSystem the file system to wrap.
    * @return the specified file system wrapped to be read-only.
    */
  def asReadOnly(fileSystem: FileSystemTrait): FileSystemTrait =
    if (fileSystem.isReadOnly) fileSystem
    else new ReadOnlyWrapper(fileSystem)

  /**
    * Creates a network node that makes the specified file system available via
    * the common file system driver.
    *
    * This can be useful for providing some data if you don't wish to implement
    * your own driver. Which will probably be most of the time. If you need
    * more control over the node, implement your own, and connect this one to
    * it. In that case you will have to forward any disk driver messages to the
    * node, though.
    *
    * The container parameter is used to give the file system some physical
    * relation to the world, for example this is used by hard drives to send
    * the disk event notifications to the client that are used to play disk
    * access sounds.
    *
    * The container may be `null`, if no such context can be provided.
    *
    * The access sound is the name of the sound effect to play when the file
    * system is accessed, for example by listing a directory or reading from
    * a file. It may be `null` to create a silent file system.
    *
    * The speed multiplier controls how fast read and write operations on the
    * file system are. It must be a value in [1,6], and controls the access
    * speed, with the default being one.
    * For reference, floppies are using the default, hard drives scale with
    * their tiers, i.e. a tier one hard drive uses speed two, tier three uses
    * speed four.
    *
    * @param fileSystem  the file system to wrap.
    * @param label       the label of the file system.
    * @param speed       the speed multiplier for this file system.
    * @return the network node wrapping the file system.
    */
  def asManagedEnvironment(fileSystem: FileSystemTrait, label: Label, speed: Int, activityType: ActivityType): FileSystem =
    Option(fileSystem).flatMap(fs => Some(new FileSystem(fs, label, (speed - 1) max 0 min 5, Option(activityType)))).orNull

  def asManagedEnvironment(address: String, fileSystem: FileSystemTrait, label: Label, speed: Int, activityType: ActivityType): FileSystem =
    Option(fileSystem).flatMap(fs => Some(new FileSystem(address, fs, label, (speed - 1) max 0 min 5, Option(activityType)))).orNull

  /**
    * Creates a network node that makes the specified file system available via
    * the common file system driver.
    *
    * Creates a file system with the a read-only label and the specified
    * access sound and file system speed.
    *
    * @param fileSystem  the file system to wrap.
    * @param label       the read-only label of the file system.
    * @return the network node wrapping the file system.
    */
  def asManagedEnvironment(fileSystem: FileSystemTrait, label: String, speed: Int, activityType: ActivityType): FileSystem =
    asManagedEnvironment(fileSystem, new ReadOnlyLabel(label), speed, activityType)

  def asManagedEnvironment(fileSystem: FileSystemTrait, label: Label, activityType: ActivityType): FileSystem =
    asManagedEnvironment(fileSystem, label, 1, activityType)

  def asManagedEnvironment(fileSystem: FileSystemTrait, label: String, activityType: ActivityType): FileSystem =
    asManagedEnvironment(fileSystem, new ReadOnlyLabel(label), 1, activityType)

  def asManagedEnvironment(fileSystem: FileSystemTrait, activityType: ActivityType): FileSystem =
    asManagedEnvironment(fileSystem, null: Label, 1, activityType)

  private class ReadOnlyLabel(val label: String) extends Label {
    def setLabel(value: String): Unit = throw new IllegalArgumentException("label is read only")

    def getLabel: String = label

    private final val LabelTag = "fs.label"

    override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = { super.load(nbt, workspace) }

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      if (label != null) {
        nbt.setString(LabelTag, label)
      }
    }
  }

  private class ReadOnlyFileSystem(protected val root: io.File)
    extends InputStreamFileSystem
      with FileInputStreamFileSystem

  private class ReadWriteFileSystem(protected val root: io.File, protected val capacity: Long)
    extends OutputStreamFileSystem
      with FileOutputStreamFileSystem
      with Capacity

  private class RamFileSystem(protected val capacity: Long)
    extends VirtualFileSystem
      with Volatile
      with Capacity

  private class BufferedFileSystem(protected val fileRoot: io.File, protected val capacity: Long)
    extends VirtualFileSystem
      with Buffered
      with Capacity {
    protected override def segments(path: String): Array[String] = {
      val parts = super.segments(path)
      if (isCaseInsensitive(fileRoot)) toCaseInsensitive(parts) else parts
    }

    private def toCaseInsensitive(path: Array[String]): Array[String] = {
      var node = root
      path.map(segment => {
        assert(node != null, "corrupted virtual file system")
        node.children.find(entry => entry._1.toLowerCase == segment.toLowerCase) match {
          case Some((name, child: VirtualDirectory)) =>
            node = child
            name
          case Some((name, _: VirtualFile)) =>
            node = null
            name
          case _ => segment
        }
      })
    }
  }
}
