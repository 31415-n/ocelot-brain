package totoro.ocelot.demo

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.traits.Environment
import totoro.ocelot.brain.entity.{CPU, Cable, Case, EEPROM, GraphicsCard, HDDManaged, Memory, Redstone, Screen}
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
    * it's own name, random numbers generator and a list of entities ('blocks' and 'items' in Minecraft).
    * Workspace can be serialized to an NBT tag, and then restored back from it.
    * Workspace is also responsible for the lifecycle of all its entities.
    *
    * For things to work correctly, you will usually add new entities to some workspace.
    * Entities then will be managed by this workspace.
    * Entities still can form connections between workspaces, and exchange data.
    */
  val workspace = new Workspace()

  /**
    * We choose the cable to be the base of our demo setup.
    * (But we can use any other component actually.)
    */
  val cable = workspace.add(new Cable())

  /**
    * Then we create a new entity - computer case.
    */
  val computer = workspace.add(new Case(Tier.Four))

  /**
    * The cable and the computer case still exist separately. They are in the same workspace,
    * but not connected.
    */
  cable.connect(computer)

  /**
    * Computer components do not need to be added to the workspace explicitly,
    * because they are a part of Case entity.
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

  val eeprom = new EEPROM()
  eeprom.codeData =
    """
      |computer.beep(1000, 1)
      |local gpu = component.proxy(component.list("gpu")())
      |local screen = component.list("screen")()
      |gpu.bind(screen)
    """.stripMargin.getBytes("UTF-8")
  eeprom.label = "Test BIOS"
  computer.add(eeprom)

//  computer.add(Loot.OpenOsEEPROM.create())
//  computer.add(Loot.OpenOsFloppy.create())

  val screen = workspace.add(new Screen(Tier.One))
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
  while (workspace.getIngameTime < 20) {
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
  println("... Creating snapshot ...")

  val nbt = new NBTTagCompound()
  workspace.save(nbt)
  println(nbt)

  println("... Loading snapshot ...")

  /**
    * Load workspace from snapshot
    */
  val loadedWorkspace = new Workspace()
  loadedWorkspace.load(nbt)

  println("Loaded.Name: " + loadedWorkspace.name);
  loadedWorkspace.getEntitiesIter.foreach { case environment: Environment =>
      println("Loaded.Entity.Address: " + environment.node.address)
  }

  while (loadedWorkspace.getIngameTime < 100) {
    loadedWorkspace.update()
    Thread.sleep(50)
  }

  computer.turnOff()

  /**
    * Necessary to successfully complete multi-threaded saving operation, and finalize other Ocelot tasks.
    */
  Ocelot.shutdown()
}
