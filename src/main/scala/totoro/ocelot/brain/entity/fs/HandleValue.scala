package totoro.ocelot.brain.entity.fs

import totoro.ocelot.brain.entity.FileSystem
import totoro.ocelot.brain.machine.{AbstractValue, Context}
import totoro.ocelot.brain.nbt.NBTTagCompound

final class HandleValue extends AbstractValue {
  def this(owner: String, handle: Int) = {
    this()
    this.owner = owner
    this.handle = handle
  }

  var owner = ""
  var handle = 0

  override def dispose(context: Context): Unit = {
    super.dispose(context)
    if (context.node != null && context.node.network != null) {
      val node = context.node.network.node(owner)
      if (node != null) {
        node.host match {
          case fs: FileSystem => try fs.close(context, handle) catch {
            case _: Throwable => // Ignore, already closed.
          }
        }
      }
    }
  }

  private val OwnerTag = "owner"
  private val HandleTag = "handle"

  override def load(nbt: NBTTagCompound): Unit = {
    super.load(nbt)
    owner = nbt.getString(OwnerTag)
    handle = nbt.getInteger(HandleTag)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setString(OwnerTag, owner)
    nbt.setInteger(HandleTag, handle)
  }

  override def toString: String = handle.toString
}
