# Things you need to know when working with ocelot-brain
Ocelot-brain is a Scala library that handles the actual emulation of OpenComputers machines, their components and peripherals, and deals with persistence.
Here you'll find classes that in the original mod would belong to its (Java) API or `common` and `server` files of the implementation.
The code is very similar to OpenComputers to ensure emulation correctness.
In fact, the overwhelming majority of all files were basically copy-pasted from the mod sources and then adapted to fit the ocelot-brain architecture.

The reason we have to change things should be quite obvious: OpenComputers is a Minecraft mod, and it uses Minecraft (and Forge) classes to interact with and integrate into the game.
Thankfully, its code puts many abstractions between its core classes and the game, which makes it possible to reimplement these abstractions to completely do away with Minecraft.
To that end, ocelot-brain introduces **workspaces** and **entities**.

## Workspaces
A workspace models a Minecraft save and essentially acts as a container of entities: computer cases, server racks, etc.
Additionally, it defines a root save path for things that cannot be saved to NBT, such as disks and tapes, and keeps track of the "in-game" time.

Its `update` method should be called every simulation tick (20 times per second).
The method in turn calls `update` on the entities it contains and increments the in-game time counter.

The `save` and `load` methods are the two entry points for workspace persistence.
They serialize (or deserialize) entities and connections between them.

Next, we'll talk about **entities** that we keep mentioning.

## Entities
OpenComputers has two classes of things it deals with: tile entities and items.
Tile entities store their NBT data and directly implement the required traits (most importantly, `Environment`).
Items store the NBT but, because of the way Minecraft works, do not implement those traits — each item kind instead registers a driver that constructs the environment for an item stack when requested.

Ocelot, luckily, can avoid this headache head on, and it does so by unifying all these things under the `Entity` trait.
An entity is a persistable object that can be added to an entity inventory or a workspace, such as a redstone card, a screen, a cable, a floppy, or a component bus.

## Environments
An entity on its own doesn't do much work.
For this reason most entities in Ocelot are also **environments**.
Each environment defines a **node**.
By joining nodes together, they form a **network**.
The network defines which nodes are reachable, advertises provided components, notifies its participants of topology changes, and performs message passing.

For example, consider a computer case that contains a CPU, a memory stick, an OpenOS floppy, a GPU, and an EEPROM.
Every single of these things is an entity, and also an environment.
The computer case is connected to the internal entities, forming a network.
Moreover, the case is connected to its peripherals, such as a screen.
Such connection can be indirect: if a RAID is connected to the screen and the latter is connected to the case, it will still be part of the network.
Or, alternatively, you could put a floppy into a disk drive and connect the disk drive to the case: the floppy will still be available via the network.

Each node is assigned an address (a random UUID).
If the node is associated with a component (this includes weirder things like floppies and modems), this is the address it will have.

## Inventories
Ocelot provides two inventory traits.

The `Inventory` trait is a general inventory implementation that manages entities.
An inventory maps slot indices to entities and back (an entity cannot be inserted into multiple slots).
Note that it has no notion of size nor of supported items.
This means you can technically put a computer case into a server rack or insert a network card into a disk drive.
(We, of course, advise you not to do this.)

The contents are accessed via an `inventory` field.
The iterators go over occupied slots or contained entities.
Alternatively, you can access a specific slot: `inventory(4)` selects the fourth slot, for instance.
The indices are zero-based, of course.
Any access to a specific slot is done via an inner `Slot` class for convenience and safety.

The `Inventory` also defines two methods, `onEntityAdded` and `onEntityRemoved`, that handle inventory changes.
Note that `onEntityAdded` is called even during loading.

A `ComponentInventory` is a beef up version of `Inventory` for environments.
It automatically connects entities to itself when they are added to the inventory and disconnects them when they are removed.
It also updates its entities in the `update` method.

All items in a `ComponentInventory` **must** implement `Environment`.
You cannot store a plain entity in such an inventory.
If you do that anyway, expect crashes.

## Disks and file systems
Tread carefully or, rather, not at all.
Even we don't quite understand how it all works.

## Persistence
All objects that can be persisted extend from the `Persistable` trait.

The `save` method should store all significant state in the given `nbt` object.
The `load` method then should be able to restore the state from the given `nbt`.

You should be careful with using NBT accessors if you aren't sure if the key exists, as they will return a default value: `getString` returns an empty string, `getInteger` a zero, and so on.
Use `hasKey` to test for existence.

Top-level entities (i.e. added to the workspace directly) and entities inside of inventories rely on reflection for deserialization.
All of this happens in the `NBTPersistence` class.
During saving, in addition to the actual object data, the object's class name is also written to NBT.
To create an object from NBT, the class is looked up via this name, instantiated using the default constructor, and then the `load` method is called on the created object.

To ensure your object can be deserialized, it **must** provide a default constructor — one that takes no arguments.
You **cannot** use optional Scala parameters for that, since Scala compiles them away and the constructor will end up having parameters.
In other words, this class can **not** be deserialized:

```scala
class MyPersistable(var x: Int = 42) extends Persistable
```

However, this one can:

```scala
class MyPersistable(var x: Int) extends Persistable {
  def this() = this(42)
}
```

Some objects cannot have a default constructor because they require some parameters for instantiation.
This is true for many tiered objects, such as graphics cards or HDDs, which need to know their tier during initialization.
For this reason the `NBTPersistence` class allows registering custom constructors for a class.
These constructors are registered in the `Ocelot` class.

For tiered objects specifically there's also a helper trait `TieredPersistable`.
Note, however: if each tier of an object is managed by a different class (ex.: data cards, wireless network cards), you don't need to extend from this trait nor register a custom constructor — things will just work™.

## Events
Whenever ocelot-brain needs to notify the frontend (ocelot.online or Ocelot Desktop) of some changes, it does so via an event bus.
Use `EventBus.send` to push an event to subscribers.

All events are defined in the `totoro.ocelot.brain.event` package and derive from the `Event` trait.
More useful, however, is the `NodeEvent` trait which requires you to provide an address of the node that originates the event; and so most events are `NodeEvent`s.

Note that events are dispatched immediately, in the thread that originates them.
