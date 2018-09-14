package totoro.ocelot.brain.environment.traits

import totoro.ocelot.brain.environment.Environment

/**
  * Implement this on [[Environment]]s if you wish to
  * expose some (typically static) information about the device represented by
  * that environment to a [[totoro.ocelot.brain.machine.Machine]] connected to it.
  *
  * You may also implement this on a [[totoro.ocelot.brain.entity.MachineHost]]
  * in which case the `Machine` will forward that information as
  * its own (since `MachineHost`s usually use the machine's node as
  * their own, this avoids a dummy environment used solely for device info).
  *
  * This is intended to permit programs to reflect on the hardware they are
  * running on, typically for purely informational purposes, but possibly to
  * toggle certain hardware specific features.
  *
  * For example, graphics cards may expose their timings via this interface, so
  * that programs may determine at what speed they can redraw, and optimize
  * execution order.
  *
  * While the format of the returned table of information is entirely up to you,
  * it is recommended to orient yourself on the key values and names that
  * `lshw` uses (),
  * where applicable.
  */
object DeviceInfo {

  /**
    * Recommended list of key values for the device info table.
    *
    * You are strongly encouraged to at least define `class`, `description`,
    * `vendor` and `product`, to allow a more homogenous experience for the
    * end-user reading this information via a script.
    *
    * Feel free to be somewhat... flexible with the designated uses of these fields. For example,
    * the capacity and size fields have differing meaning depending on the device in OpenComputers
    * itself (e.g. they're used for maximum number of characters for graphics cards, width is
    * used for bit depth on graphics cards, etc.), just try to stick with what's somewhat logical.
    */
  object DeviceAttribute {
    val Class = "class" // device's class (see below), e.g. "processor"
    val Description = "description" // human-readable description of the hardware node, e.g. "Ethernet interface"
    val Vendor = "vendor" // vendor/manufacturer of the device, e.g. "Minecorp Inc."
    val Product = "product" // product name of the device, e.g. "ATY Raderps 4200X"
    val Version = "version" // version/release of the device, e.g. "2.1.0"
    val Serial = "serial" // serial number of the device
    val Capacity = "capacity" // maximum capacity reported by the device, e.g. unformatted size of a disk
    val Size = "size" // actual size of the device, e.g. actual usable space on a disk
    val Clock = "clock" // bus clock (in Hz) of the device, e.g. call speed(s) of a component
    val Width = "width" // address width of the device, in the broadest sense
  }

  /**
    * Recommended list of values for the `class` attribute (see above).
    *
    * Again, feel free to be somewhat creative with those. When in doubt, use `generic`.
    */
  object DeviceClass {
    val System = "system" // used to refer to the whole machine, e.g. "Computer", "Server", "Robot"
    val Bridge = "bridge" // internal bus converter, maybe useful for some low-level archs?
    val Memory = "memory" // memory bank that can contain data, executable code, e.g. RAM, EEPROM
    val Processor = "processor" // execution processor, e.g. CPU, cryptography support
    val Address = "address" // memory address range, e.g. video memory (again, low-level archs maybe?)
    val Storage = "storage" // storage controller, e.g. IDE controller (low-level...)
    val Disk = "disk" // random-access storage device, e.g. floppies
    val Tape = "tape" // sequential-access storage device, e.g. cassette tapes
    val Bus = "bus" // device-connecting bus, e.g. USB
    val Network = "network" // network interface, e.g. ethernet, wlan
    val Display = "display" // display adapter, e.g. graphics cards
    val Input = "input" // user input device, e.g. keyboard, mouse
    val Printer = "printer" // printing device, e.g. printer, 3D-printer
    val Multimedia = "multimedia" // audio/video device, e.g. sound cards
    val Communication = "communication" // line communication device, e.g. modem, serial ports
    val Power = "power" // energy source, e.g. battery, power supply
    val Volume = "volume" // disk volume, e.g. file system
    val Generic = "generic" // generic device (used when no other class is suitable)
  }

}

trait DeviceInfo {
  /**
    * Compile a list of device information strings as key-value pairs.
    *
    * For example, this may list the type of the device, a vendor (for example
    * your mod name, or something more creative if you like), specifications
    * of the device (speeds, capacities).
    *
    * For example, OC's tier one memory module returns the following:
    *
    * - class:       memory
    * - description: Memory bank
    * - vendor:      MightyPirates GmbH & Co. KG
    * - product:     Multipurpose RAM Type
    * - clock:       500
    *
    * @return the table of information on this device, or `null`.
    */
  def getDeviceInfo: Map[String, String]
}
