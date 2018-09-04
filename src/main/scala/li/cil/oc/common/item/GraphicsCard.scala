package li.cil.oc.common.item

class GraphicsCard(val parent: Delegator, val tier: Int) extends traits.Delegate with traits.ItemTier with traits.GPULike {
  override val unlocalizedName: String = super.unlocalizedName + tier

  override def gpuTier: Int = tier

  override protected def tooltipName = Option(super.unlocalizedName)
}
