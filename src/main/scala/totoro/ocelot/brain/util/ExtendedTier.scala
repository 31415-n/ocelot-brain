package totoro.ocelot.brain.util

import totoro.ocelot.brain.util.Tier.Tier

object ExtendedTier extends Enumeration {
  type ExtendedTier = ExtendedTierVal

  val One: ExtendedTier = ExtendedTierVal("1", "Tier 1")
  val OneHalf: ExtendedTier = ExtendedTierVal("1.5", "Tier 1.5")
  val Two: ExtendedTier = ExtendedTierVal("2", "Tier 2")
  val TwoHalf: ExtendedTier = ExtendedTierVal("2.5", "Tier 2.5")
  val Three: ExtendedTier = ExtendedTierVal("3", "Tier 3")
  val ThreeHalf: ExtendedTier = ExtendedTierVal("3.5", "Tier 3.5")
  val Creative: ExtendedTier = ExtendedTierVal("Creative")

  protected class ExtendedTierVal(name: String, val label: String) extends super.Val(name) {
    def toTier: Tier = this match {
      case One | OneHalf => Tier.One
      case Two | TwoHalf => Tier.Two
      case Three | ThreeHalf => Tier.Three
      case Creative => Tier.Creative
    }

    def to(end: ExtendedTier): IndexedSeq[ExtendedTier] =
      (id to end.id).map(ExtendedTier(_))
  }

  private object ExtendedTierVal {
    def apply(name: String, label: String): ExtendedTierVal = new ExtendedTierVal(name, label)
    def apply(name: String): ExtendedTierVal = new ExtendedTierVal(name, name)
  }

  import scala.language.implicitConversions
  implicit def valueToExtendedTierVal(x: Value): ExtendedTierVal = x.asInstanceOf[ExtendedTierVal]
}
