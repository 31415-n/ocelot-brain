package li.cil.oc.common.item

class WirelessNetworkCard(val parent: Delegator, var tier: Int) extends traits.Delegate {
  override val unlocalizedName: String = super.unlocalizedName + tier
}
