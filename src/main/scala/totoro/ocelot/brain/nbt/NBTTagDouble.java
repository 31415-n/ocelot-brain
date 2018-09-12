package totoro.ocelot.brain.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagDouble extends NBTNumber {

    private double data;

    NBTTagDouble() {}

    public NBTTagDouble(double d0) {
        this.data = d0;
    }

    void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeDouble(this.data);
    }

    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        nbtreadlimiter.a(64L);
        this.data = datainput.readDouble();
    }

    public byte getId() {
        return (byte) 6;
    }

    public String toString() {
        return "" + this.data + "d";
    }

    public NBTBase clone() {
        return new NBTTagDouble(this.data);
    }

    public boolean equals(Object object) {
        if (super.equals(object)) {
            NBTTagDouble nbttagdouble = (NBTTagDouble) object;

            return this.data == nbttagdouble.data;
        } else {
            return false;
        }
    }

    public int hashCode() {
        long i = Double.doubleToLongBits(this.data);

        return super.hashCode() ^ (int) (i ^ i >>> 32);
    }

    public long getLong() {
        return (long) Math.floor(this.data);
    }

    public int getInt() {
        return (int) Math.floor(this.data);
    }

    public short getShort() {
        return (short) (getInt() & '\uffff');
    }

    public byte getByte() {
        return (byte) (getInt() & 255);
    }

    public double getDouble() {
        return this.data;
    }

    public float getFloat() {
        return (float) this.data;
    }
}
