package totoro.ocelot.brain.machine

import totoro.ocelot.brain.nbt.NBTTagCompound

/**
  * Basic implementation for the [[Value]] trait.
  */
class AbstractValue extends Value {
  override def apply(context: Context, arguments: Arguments): AnyRef = null

  override def unapply(context: Context, arguments: Arguments): Unit = {
  }

  override def call(context: Context, arguments: Arguments) = throw new RuntimeException("trying to call a non-callable value")

  override def dispose(context: Context): Unit = {
  }

  override def load(nbt: NBTTagCompound): Unit = {
  }

  override def save(nbt: NBTTagCompound): Unit = {
  }
}
