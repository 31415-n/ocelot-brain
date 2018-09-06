package li.cil.oc.common.item

import li.cil.oc.common.Tier
import net.minecraft.item.Item

class APU(val tier: Int) extends Item with traits.CPULike with traits.GPULike {
  override def cpuTier: Int = math.min(Tier.Three, tier + 1)
  override def gpuTier: Int = tier
}
