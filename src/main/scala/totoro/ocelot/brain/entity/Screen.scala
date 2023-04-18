package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.{Entity, MultiTiered}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.user.User
import totoro.ocelot.brain.workspace.Workspace

class Screen(tier: Int) extends TextBuffer(tier) with Entity with MultiTiered {

  var invertTouchMode = false

  def walk(player: Option[User], x: Double, y: Double): Unit = {
    player match {
      case Some(user) if Settings.get.inputUsername =>
        node.sendToReachable("computer.signal", "walk", x, y, user.nickname)
      case _ =>
        node.sendToReachable("computer.signal", "walk", x, y)
    }
  }

  def getPrecisionMode: Boolean = precisionMode

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = """function():boolean -- Whether touch mode is inverted (sneak-activate opens GUI, instead of normal activate).""")
  def isTouchModeInverted(computer: Context, args: Arguments): Array[AnyRef] = result(invertTouchMode)

  @Callback(doc = """function(value:boolean):boolean -- Sets whether to invert touch mode (sneak-activate opens GUI, instead of normal activate).""")
  def setTouchModeInverted(computer: Context, args: Arguments): Array[AnyRef] = {
    val newValue = args.checkBoolean(0)
    val oldValue = invertTouchMode
    if (newValue != oldValue) {
      invertTouchMode = newValue
    }
    result(oldValue)
  }

  // ----------------------------------------------------------------------- //

  private final val InvertTouchModeTag = "invertTouchMode"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    invertTouchMode = nbt.getBoolean(InvertTouchModeTag)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setBoolean(InvertTouchModeTag, invertTouchMode)
  }
}
