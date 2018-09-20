package totoro.ocelot.demo

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.{CPU, Cable, Case, EEPROM, GraphicsCard, Memory, Screen}
import totoro.ocelot.brain.event._
import totoro.ocelot.brain.loot.Loot
import totoro.ocelot.brain.network.Network
import totoro.ocelot.brain.util.Tier

object Demo extends App {
  println("Hi! We are testing Ocelot brains here. Join in!")

  Ocelot.initialize()

  // setup simple network with a computer
  val cable = new Cable()
  val network = new Network(cable.node)

  val computer = new Case(Tier.Four)
  cable.node.connect(computer.node)

  val cpu = new CPU(Tier.Three)
  computer.add(cpu)

  val memory = new Memory(Tier.Six)
  computer.add(memory)

  val eeprom = new EEPROM()
  eeprom.codeData =
    """
      |computer.beep(1000, 1)
      |local gpu = component.proxy(component.list("gpu")())
      |local screen = component.list("screen")()
      |gpu.bind(screen)
      |gpu.set(1, 1, "Hello from Ocelot EEPROM!")
      |while (true) do end
    """.stripMargin.getBytes("UTF-8")
  eeprom.label = "Test BIOS"
  //computer.add(eeprom)

  computer.add(Loot.OpenOsBIOS.create())

  val gpu = new GraphicsCard(Tier.One)
  computer.add(gpu)

  val screen = new Screen(Tier.Three)
  cable.node.connect(screen.node)

  // register some event listeners
  EventBus.listenTo(classOf[BeepEvent], { case event: BeepEvent =>
    println(s"[EVENT] Beep (frequency = ${event.frequency}, duration = ${event.duration})")
  })
  EventBus.listenTo(classOf[BeepPatternEvent], { case event: BeepPatternEvent =>
    println(s"[EVENT] Beep (${event.pattern})")
  })
  EventBus.listenTo(classOf[MachineCrashEvent], { case event: MachineCrashEvent =>
    println(s"[EVENT] Machine crash! (${event.message})")
  })
  EventBus.listenTo(classOf[TextBufferSetEvent], { case event: TextBufferSetEvent =>
    println(s"[EVENT] Text buffer set (${event.x}, ${event.y}, ${event.value}, ${event.vertical})")
  })

  // turn the computer on
  computer.turnOn()

  while (computer.machine.isRunning) {
    computer.update()
    Thread.sleep(50)
  }

  computer.turnOff()
}
