package li.cil.oc.util

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import li.cil.oc.{Constants, api}
import li.cil.oc.common.Tier
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{CompressedStreamTools, NBTTagCompound}

object ItemUtils {
  def caseTier(stack: ItemStack): Int = {
    val descriptor = api.Items.get(stack)
    if (descriptor == api.Items.get(Constants.BlockName.CaseTier1)) Tier.One
    else if (descriptor == api.Items.get(Constants.BlockName.CaseTier2)) Tier.Two
    else if (descriptor == api.Items.get(Constants.BlockName.CaseTier3)) Tier.Three
    else if (descriptor == api.Items.get(Constants.BlockName.CaseCreative)) Tier.Four
    else if (descriptor == api.Items.get(Constants.ItemName.MicrocontrollerCaseTier1)) Tier.One
    else if (descriptor == api.Items.get(Constants.ItemName.MicrocontrollerCaseTier2)) Tier.Two
    else if (descriptor == api.Items.get(Constants.ItemName.MicrocontrollerCaseCreative)) Tier.Four
    else if (descriptor == api.Items.get(Constants.ItemName.DroneCaseTier1)) Tier.One
    else if (descriptor == api.Items.get(Constants.ItemName.DroneCaseTier2)) Tier.Two
    else if (descriptor == api.Items.get(Constants.ItemName.DroneCaseCreative)) Tier.Four
    else if (descriptor == api.Items.get(Constants.ItemName.ServerTier1)) Tier.One
    else if (descriptor == api.Items.get(Constants.ItemName.ServerTier2)) Tier.Two
    else if (descriptor == api.Items.get(Constants.ItemName.ServerTier3)) Tier.Three
    else if (descriptor == api.Items.get(Constants.ItemName.ServerCreative)) Tier.Four
    else if (descriptor == api.Items.get(Constants.ItemName.TabletCaseTier1)) Tier.One
    else if (descriptor == api.Items.get(Constants.ItemName.TabletCaseTier2)) Tier.Two
    else if (descriptor == api.Items.get(Constants.ItemName.TabletCaseCreative)) Tier.Four
    else Tier.None
  }

  def caseNameWithTierSuffix(name: String, tier: Int): String = name + (if (tier == Tier.Four) "creative" else (tier + 1).toString)

  def loadTag(data: Array[Byte]): NBTTagCompound = {
    val bais = new ByteArrayInputStream(data)
    CompressedStreamTools.readCompressed(bais)
  }

  def saveStack(stack: ItemStack): Array[Byte] = {
    val tag = new NBTTagCompound()
    stack.writeToNBT(tag)
    saveTag(tag)
  }

  def saveTag(tag: NBTTagCompound): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    CompressedStreamTools.writeCompressed(tag, baos)
    baos.toByteArray
  }
}
