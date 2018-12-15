package totoro.ocelot.demo

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.{APU, Cable, Case, EEPROM, Memory, Redstone, Screen}
import totoro.ocelot.brain.event._
import totoro.ocelot.brain.loot.Loot
import totoro.ocelot.brain.network.Network
import totoro.ocelot.brain.util.Tier

object Demo extends App {
  println("Hi! We are testing Ocelot brains here. Join in!")

  /**
    * We can pass here tout custom logger: `Ocelot.initialize(logger)`
    */
  Ocelot.initialize()

  /**
    * Network connects things.
    * Without network - all `a.connect(b)` calls will fail.
    * Without network the components cannot "see" each other.
    * Also network transmits modem messages and OC-signals.
    */
  val network = new Network()

  /**
    * We choose the cable to be the base of our demo network.
    * But it can be any component actually.
    */
  val cable = new Cable()

  /**
    * We need to connect one of entites to the network explicitly.
    * All subsequent connection of other entities to this one will pass the network reference implicitly.
    */
  network.connect(cable)

  val computer = new Case(Tier.Four)

  /**
    * Here, on the left is an already connected to the network entity, on the right - the new one.
    */
  cable.connect(computer)

  /**
    * Computer components need to be added inside of the computers case.
    * They form there their own lisolated network. This prevents components leaking and clashes.
    */
  val cpu = new APU(Tier.Two)
  computer.add(cpu)

  val memory = new Memory(Tier.Six)
  computer.add(memory)

  val redstone = new Redstone.Tier1()
  computer.add(redstone)

  /**
    * Custom EEPROM can be created like this:
    * `
    * val eeprom = new EEPROM()
    * eeprom.codeData =
    *   """
    *     |computer.beep(1000, 1)
    *     |local gpu = component.proxy(component.list("gpu")())
    *     |local screen = component.list("screen")()
    *     |gpu.bind(screen)
    *     |gpu.set(1, 1, "Hello from Ocelot EEPROM!")
    *     |while (true) do end
    *   """.stripMargin.getBytes("UTF-8")
    * eeprom.label = "Test BIOS"
    * computer.add(eeprom)
    * `
    */

  computer.add(Loot.AdvLoaderEEPROM.create())
  computer.add(Loot.OpenOsFloppy.create())

  val screen = new Screen(Tier.Three)
  cable.connect(screen)

  // register some event listeners
  EventBus.listenTo(classOf[BeepEvent], { case event: BeepEvent =>
    println(s"[EVENT] Beep (address = ${event.address}, frequency = ${event.frequency}, duration = ${event.duration})")
  })
  EventBus.listenTo(classOf[BeepPatternEvent], { case event: BeepPatternEvent =>
    println(s"[EVENT] Beep (address = ${event.address}, pattern = ${event.pattern})")
  })
  EventBus.listenTo(classOf[MachineCrashEvent], { case event: MachineCrashEvent =>
    println(s"[EVENT] Machine crash! (address = ${event.address}, ${event.message})")
  })
  EventBus.listenTo(classOf[TextBufferSetEvent], { case event: TextBufferSetEvent =>
    println(s"[EVENT] Text buffer set (address = ${event.address}, ${event.x}, ${event.y}, ${event.value}, ${event.vertical})")
  })
  EventBus.listenTo(classOf[TextBufferSetForegroundColorEvent], { case event: TextBufferSetForegroundColorEvent =>
    println(s"[EVENT] Foreground color changed (address = ${event.address}, ${event.color})")
  })
  EventBus.listenTo(classOf[TextBufferSetBackgroundColorEvent], { case event: TextBufferSetBackgroundColorEvent =>
    println(s"[EVENT] Background color changed (address = ${event.address}, ${event.color})")
  })

  // turn the computer on
  computer.turnOn()

  while (computer.machine.isRunning) {
    computer.update()
    Thread.sleep(50)
  }

  computer.turnOff()
}
