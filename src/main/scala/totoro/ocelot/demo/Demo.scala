package totoro.ocelot.demo

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.{CPU, Cable, Case, GraphicsCard, HDDManaged, Memory, Redstone, Screen}
import totoro.ocelot.brain.event._
import totoro.ocelot.brain.loot.Loot
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.workspace.Workspace

object Demo extends App {
  println("Hi! We are testing Ocelot brains here. Join in!")

  /**
    * We can pass here a custom logger: `Ocelot.initialize(logger)`
    */
  Ocelot.initialize()

  /**
    * All things inside of Ocelot usually are grouped by workspaces.
    * Workspace is like 'world' in Minecraft. It has it's own timeflow,
    * it's own name, random numbers generator and a list of networks with entities.
    * Workspace can be serialized or deserialized from NBT tags.
    */
  val workspace = new Workspace()

  /**
    * Network connects things.
    * Without network - all `a.connect(b)` calls will fail.
    * Without network the components cannot "see" each other.
    * Also network transmits modem messages and OC-signals.
    * We can either create our own network, or use the workspace Default network.
    */
  val network = workspace.DefaultNetwork

  /**
    * We choose the cable to be the base of our demo network.
    * (But we can use any other component actually.)
    */
  val cable = new Cable()

  /**
    * We need to connect one of entites to the network explicitly.
    * All subsequent connection of other entities to this one will pass the network reference implicitly.
    */
  network.connect(cable)

  /**
    * Then we create a new entity - computer case.
    */
  val computer = new Case(Tier.Four)

  /**
    * Here on the left is an already connected to the network entity, on the right - the new one.
    */
  cable.connect(computer)

  /**
    * Computer components need to be added inside of the computers case.
    * They form there their own isolated network. This prevents components from leaking into the global network
    * and cause processor limits overflow and component clashes.
    */
  val cpu = new CPU(Tier.Three)
  computer.add(cpu)

  val gpu = new GraphicsCard(Tier.Three)
  computer.add(gpu)

  val memory = new Memory(Tier.Six)
  computer.add(memory)

  val redstone = new Redstone.Tier1()
  computer.add(redstone)

  /**
    * When creating a new hard drive, you can specify it's address.
    * If you will leave it `null`, then new random UUID will be used.
    */

  val hdd = new HDDManaged("59aef805-4085-485f-b92c-163b3f0426da", Tier.One, "volume 1")
  computer.add(hdd)

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

  /**
    * The computer can be turned on or off. By defaults it is turned off.
    */
  computer.turnOn()


  /**
    * The `computer.machine.isRunning` flag will tell you, if the computer is still operational,
    * or had it crashed or stopped the execution otherwise.
    */
  while (workspace.getIngameTime < 10) {
    /**
      * The `update()` method of workspace will update all components in each registered network,
      * that need to be updated.
      * These are usually computers or network cards.
      */
    workspace.update()
    /**
      * 50 milliseconds is the duration of standard Minecraft tick.
      * You can speed the simulation up or slow it down by changing this value.
      */
    Thread.sleep(50)
  }

  /**
    * Make a snapshot ot the system.
    * You need to use an existing NBT compound tag (or create a new one).
    */
  val nbt = new NBTTagCompound()
  workspace.save(nbt)
  println(nbt)

  computer.turnOff()
}
