package totoro.ocelot.brain.entity.traits

import java.util.UUID

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{FileSystemAPI, Label}
import totoro.ocelot.brain.entity.{Drive, Environment, FileSystem}
import totoro.ocelot.brain.loot.Loot.FileSystemFactory
import totoro.ocelot.brain.network.Node

/**
  * Environment which allows to switch between [[Drive]] and [[FileSystem]].
  */
trait ManagedDisk extends Environment {
  private var _environment: Environment = _

  override def node: Node =
    if (_environment != null) _environment.node
    else null

  def address: String =
    if (node != null) node.address
    else null

  /**
    * Disk label.
    */
  def label: Label

  /**
    * Disk capacity.
    */
  def capacity: Int

  /**
    * Disk platter count.
    */
  def platterCount: Int

  /**
    * Disk speed.
    */
  def speed: Int

  /**
    * Creates a filesystem from loot directories.
    */
  def lootFactory: FileSystemFactory = null

  /**
    * Determines the exact type of environment that will be used for this
    * disk. In the managed mode this is an instance of [[FileSystem]]. In unmanaged
    * mode this is an instance of [[Drive]]. Filesystem is a little bit more advanced
    * and abstract way to communicate with your data storage (files, folders, etc.).
    * Raw Drive is on the other hand quite low level (bytes, sectors, head position, etc.).
    */
  private var managed = true

  def setManaged(value: Boolean): Unit = {
    managed = value
    _environment = generateEnvironment()
  }

  def isManaged: Boolean = managed

  /**
    * Generates new environment with it's type depending on the `managed` field
    */
  def generateEnvironment(): Environment = {
    val oldAddress = address
    val environment = if (lootFactory != null) {
      lootFactory.createFileSystem()
    } else if (managed) {
      val fs = FileSystemAPI.fromSaveDirectory(address, capacity max 0, Settings.get.bufferChanges)
      FileSystemAPI.asManagedEnvironment(fs, label, speed)
    } else {
      new Drive(capacity max 0, platterCount, label, speed)
    }
    if (environment != null && node != null) {
      val newAddress = if (oldAddress == null || oldAddress.isEmpty)
        UUID.randomUUID().toString
      else oldAddress
      node.address = newAddress
    }
    environment
  }
}
