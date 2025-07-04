package totoro.ocelot.brain.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagString extends NBTBase {

    private String data;

    public NBTTagString() {
        this.data = "";
    }

    public NBTTagString(String s) {
        this.data = s;
        if (s == null) {
            throw new IllegalArgumentException("Empty string not allowed");
        }
    }

    void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeUTF(this.data);
    }

    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        this.data = datainput.readUTF();
        nbtreadlimiter.a(16L * this.data.length());
    }

    public byte getId() {
        return (byte) 8;
    }

    public String toString() {
        return "\"" + this.data + "\"";
    }

    public NBTBase clone() {
        return new NBTTagString(this.data);
    }

    public boolean equals(Object object) {
        if (!super.equals(object)) {
            return false;
        } else {
            NBTTagString nbttagstring = (NBTTagString) object;

            return this.data == null && nbttagstring.data == null || this.data != null && this.data.equals(nbttagstring.data);
        }
    }

    public int hashCode() {
        return super.hashCode() ^ this.data.hashCode();
    }

    public String getString() {
        return this.data;
    }
}
