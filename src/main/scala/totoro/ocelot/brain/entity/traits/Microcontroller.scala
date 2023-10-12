package totoro.ocelot.brain.entity.traits

/**
 * This interface is implemented as a marker by microcontrollers.
 * <br>
 * This is implemented by microcontroller tile entities. That means you can
 * use this to check for microcontrollers by using:
 * <pre>
 * if (tileEntity instanceof Microcontroller) {
 * </pre>
 * <br>
 * The only purpose is to allow identifying tile entities as microcontrollers
 * via the API, i.e. without having to link against internal classes. This
 * also means that <em>you should not implement this</em>.
 */
trait Microcontroller extends Environment with MachineHost with Tiered {

}
