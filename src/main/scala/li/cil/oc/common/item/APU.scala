package li.cil.oc.common.item

import li.cil.oc.common.Tier

class APU(val parent: Delegator, val tier: Int) extends traits.Delegate with traits.CPULike with traits.GPULike {
  override val unlocalizedName: String = super[Delegate].unlocalizedName + tier
  override def cpuTier: Int = math.min(Tier.Three, tier + 1)
  override def gpuTier: Int = tier
}
