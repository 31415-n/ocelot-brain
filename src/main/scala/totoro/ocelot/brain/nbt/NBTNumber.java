package totoro.ocelot.brain.nbt;

public abstract class NBTNumber extends NBTBase {

    protected NBTNumber() {}

    public abstract long getLong();

    public abstract int getInt();

    public abstract short getShort();

    public abstract byte getByte();

    public abstract double getDouble();

    public abstract float getFloat();
}
