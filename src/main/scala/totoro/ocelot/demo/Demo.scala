package totoro.ocelot.demo

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.{CPU, Cable, Case, EEPROM, Memory}
import totoro.ocelot.brain.network.Network
import totoro.ocelot.brain.util.Tier

object Demo extends App {
  println("Hi! We are testing Ocelot brains here. Join in!")

  Ocelot.initialize()
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
      |while (true) do end
    """.stripMargin.getBytes("UTF-8")
  eeprom.label = "Test BIOS"
  computer.add(eeprom)

  computer.turnOn()

  while (computer.machine.isRunning) {
    computer.update()
    Thread.sleep(50)
  }

  computer.turnOff()
}
