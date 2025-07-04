package totoro.ocelot.brain.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class NBTTagIntArray extends NBTBase {

    private int[] data;

    NBTTagIntArray() {}

    public NBTTagIntArray(int[] aint) {
        this.data = aint;
    }

    void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeInt(this.data.length);

        for (int datum : this.data) {
            dataoutput.writeInt(datum);
        }
    }

    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        int j = datainput.readInt();

        nbtreadlimiter.a(32L * j);
        this.data = new int[j];

        for (int k = 0; k < j; ++k) {
            this.data[k] = datainput.readInt();
        }
    }

    public byte getId() {
        return (byte) 11;
    }

    public String toString() {
        StringBuilder s = new StringBuilder("[");
        for (int k : this.data) {
            s.append(k).append(",");
        }
        return s + "]";
    }

    public NBTBase clone() {
        int[] aint = new int[this.data.length];

        System.arraycopy(this.data, 0, aint, 0, this.data.length);
        return new NBTTagIntArray(aint);
    }

    public boolean equals(Object object) {
        return super.equals(object) && Arrays.equals(this.data, ((NBTTagIntArray) object).data);
    }

    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(this.data);
    }

    public int[] getIntArray() {
        return this.data;
    }
}
