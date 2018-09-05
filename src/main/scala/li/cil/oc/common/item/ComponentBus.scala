package li.cil.oc.common.item

class ComponentBus(val parent: Delegator, val tier: Int) extends traits.Delegate {
  override val unlocalizedName: String = super.unlocalizedName + tier
}
