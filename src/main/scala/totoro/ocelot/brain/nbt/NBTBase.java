package totoro.ocelot.brain.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class NBTBase {

    abstract void write(DataOutput dataoutput) throws IOException;

    abstract void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException;

    public abstract String toString();

    public abstract byte getId();

    protected NBTBase() {
    }

    protected static NBTBase createTag(byte b0) {
        switch (b0) {
            case 0:
                return new NBTTagEnd();
            case 1:
                return new NBTTagByte();
            case 2:
                return new NBTTagShort();
            case 3:
                return new NBTTagInt();
            case 4:
                return new NBTTagLong();
            case 5:
                return new NBTTagFloat();
            case 6:
                return new NBTTagDouble();
            case 7:
                return new NBTTagByteArray();
            case 8:
                return new NBTTagString();
            case 9:
                return new NBTTagList();
            case 10:
                return new NBTTagCompound();
            case 11:
                return new NBTTagIntArray();
            default:
                return null;
        }
    }

    public abstract NBTBase clone();

    public boolean equals(Object object) {
        if (!(object instanceof NBTBase)) {
            return false;
        } else {
            NBTBase nbtbase = (NBTBase) object;

            return this.getId() == nbtbase.getId();
        }
    }

    public int hashCode() {
        return this.getId();
    }

    protected String getString() {
        return this.toString();
    }
}
