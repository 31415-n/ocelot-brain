package li.cil.oc.common.tileentity

import li.cil.oc.Settings
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound

import scala.language.postfixOps

class Screen(var tier: Int) extends traits.TextBuffer {
  def this() = this(0)

  var width, height = 1

  var invertTouchMode = false

  // ----------------------------------------------------------------------- //

  def click(x: Double, y: Double): Boolean = {
    buffer.mouseDown(x, y, 0, null)
    true
  }

  def walk(entity: Entity, x: Double, y: Double) {
    entity match {
      case player: EntityPlayer if Settings.get.inputUsername =>
        node.sendToReachable("computer.signal", "walk", x: java.lang.Double, y: java.lang.Double, player.getName)
      case _ =>
        node.sendToReachable("computer.signal", "walk", x: java.lang.Double, y: java.lang.Double)
    }
  }

  // ----------------------------------------------------------------------- //

  private final val TierTag = Settings.namespace + "tier"
  private final val InvertTouchModeTag = Settings.namespace + "invertTouchMode"

  override def readFromNBT(nbt: NBTTagCompound) {
    tier = nbt.getByte(TierTag) max 0 min 2
    super.readFromNBT(nbt)
    invertTouchMode = nbt.getBoolean(InvertTouchModeTag)
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    nbt.setByte(TierTag, tier.toByte)
    super.writeToNBT(nbt)
    nbt.setBoolean(InvertTouchModeTag, invertTouchMode)
  }
}
