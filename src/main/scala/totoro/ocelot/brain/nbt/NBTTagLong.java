package totoro.ocelot.brain.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagLong extends NBTNumber {

    private long data;

    NBTTagLong() {}

    public NBTTagLong(long i) {
        this.data = i;
    }

    void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeLong(this.data);
    }

    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        nbtreadlimiter.a(64L);
        this.data = datainput.readLong();
    }

    public byte getId() {
        return (byte) 4;
    }

    public String toString() {
        return "" + this.data + "L";
    }

    public NBTBase clone() {
        return new NBTTagLong(this.data);
    }

    public boolean equals(Object object) {
        if (super.equals(object)) {
            NBTTagLong nbttaglong = (NBTTagLong) object;

            return this.data == nbttaglong.data;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ (int) (this.data ^ this.data >>> 32);
    }

    public long getLong() {
        return this.data;
    }

    public int getInt() {
        return (int) this.data;
    }

    public short getShort() {
        return (short) ((int) (this.data & 65535L));
    }

    public byte getByte() {
        return (byte) ((int) (this.data & 255L));
    }

    public double getDouble() {
        return (double) this.data;
    }

    public float getFloat() {
        return (float) this.data;
    }
}
