package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagByte extends NBTNumber {

    private byte data;

    NBTTagByte() {}

    public NBTTagByte(byte b0) {
        this.data = b0;
    }

    void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeByte(this.data);
    }

    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        nbtreadlimiter.a(8L);
        this.data = datainput.readByte();
    }

    public byte getId() {
        return (byte) 1;
    }

    public String toString() {
        return "" + this.data + "b";
    }

    public NBTBase clone() {
        return new NBTTagByte(this.data);
    }

    public boolean equals(Object object) {
        if (super.equals(object)) {
            NBTTagByte nbttagbyte = (NBTTagByte) object;

            return this.data == nbttagbyte.data;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ this.data;
    }

    public long getLong() {
        return (long) this.data;
    }

    public int getInt() {
        return this.data;
    }

    public short getShort() {
        return (short) this.data;
    }

    public byte getByte() {
        return this.data;
    }

    public double getDouble() {
        return (double) this.data;
    }

    public float getFloat() {
        return (float) this.data;
    }
}
