package totoro.ocelot.brain.nbt;

import java.io.DataInput;
import java.io.DataOutput;

public class NBTTagEnd extends NBTBase {

    NBTTagEnd() {}

    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) {}

    void write(DataOutput dataoutput) {}

    public byte getId() {
        return (byte) 0;
    }

    public String toString() {
        return "END";
    }

    public NBTBase clone() {
        return new NBTTagEnd();
    }
}
