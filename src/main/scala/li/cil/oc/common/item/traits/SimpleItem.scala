package li.cil.oc.common.item.traits

import java.util

import li.cil.oc.Settings
import li.cil.oc.common.tileentity
import li.cil.oc.util.Tooltip
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

trait SimpleItem extends Item {
  def createItemStack(amount: Int = 1) = new ItemStack(this, amount)

  @SideOnly(Side.CLIENT)
  override def addInformation(stack: ItemStack, world: World, tooltip: util.List[String], flag: ITooltipFlag) {
    tooltip.addAll(Tooltip.get(getClass.getSimpleName.toLowerCase))

    if (stack.hasTagCompound && stack.getTagCompound.hasKey(Settings.namespace + "data")) {
      val data = stack.getTagCompound.getCompoundTag(Settings.namespace + "data")
      if (data.hasKey("node") && data.getCompoundTag("node").hasKey("address")) {
        tooltip.add("§8" + data.getCompoundTag("node").getString("address").substring(0, 13) + "...§7")
      }
    }
  }
}
