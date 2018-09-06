package li.cil.oc.common.item

import net.minecraft.item.Item

class CPU(val tier: Int) extends Item with traits.CPULike {
  override def cpuTier: Int = tier
}
