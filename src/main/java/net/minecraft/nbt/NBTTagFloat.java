package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagFloat extends NBTNumber {

    private float data;

    NBTTagFloat() {}

    public NBTTagFloat(float f) {
        this.data = f;
    }

    void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeFloat(this.data);
    }

    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        nbtreadlimiter.a(32L);
        this.data = datainput.readFloat();
    }

    public byte getId() {
        return (byte) 5;
    }

    public String toString() {
        return "" + this.data + "f";
    }

    public NBTBase clone() {
        return new NBTTagFloat(this.data);
    }

    public boolean equals(Object object) {
        if (super.equals(object)) {
            NBTTagFloat nbttagfloat = (NBTTagFloat) object;

            return this.data == nbttagfloat.data;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ Float.floatToIntBits(this.data);
    }

    public long getLong() {
        return (long) this.data;
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
        return (double) this.data;
    }

    public float getFloat() {
        return this.data;
    }
}
