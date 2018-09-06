package li.cil.oc.common.item

import net.minecraft.item.Item

class GraphicsCard(val tier: Int) extends Item with traits.GPULike {
  override def gpuTier: Int = tier
}
