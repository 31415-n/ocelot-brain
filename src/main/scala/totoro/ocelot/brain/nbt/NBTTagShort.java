package totoro.ocelot.brain.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagShort extends NBTNumber {

    private short data;

    public NBTTagShort() {}

    public NBTTagShort(short short1) {
        this.data = short1;
    }

    void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeShort(this.data);
    }

    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        nbtreadlimiter.a(16L);
        this.data = datainput.readShort();
    }

    public byte getId() {
        return (byte) 2;
    }

    public String toString() {
        return "" + this.data + "s";
    }

    public NBTBase clone() {
        return new NBTTagShort(this.data);
    }

    public boolean equals(Object object) {
        if (super.equals(object)) {
            NBTTagShort nbttagshort = (NBTTagShort) object;

            return this.data == nbttagshort.data;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ this.data;
    }

    public long getLong() {
        return this.data;
    }

    public int getInt() {
        return this.data;
    }

    public short getShort() {
        return this.data;
    }

    public byte getByte() {
        return (byte) (this.data & 255);
    }

    public double getDouble() {
        return this.data;
    }

    public float getFloat() {
        return this.data;
    }
}
