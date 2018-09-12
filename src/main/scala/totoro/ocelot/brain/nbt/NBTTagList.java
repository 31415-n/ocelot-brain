package totoro.ocelot.brain.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NBTTagList extends NBTBase {

    private List<NBTBase> list = new ArrayList<>();
    private byte type = 0;

    public NBTTagList() {
    }

    void write(DataOutput dataoutput) throws IOException {
        if (!this.list.isEmpty()) {
            this.type = this.list.get(0).getId();
        } else {
            this.type = 0;
        }

        dataoutput.writeByte(this.type);
        dataoutput.writeInt(this.list.size());

        for (NBTBase aList : this.list) {
            aList.write(dataoutput);
        }
    }

    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        if (i > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        } else {
            nbtreadlimiter.a(8L);
            this.type = datainput.readByte();
            int j = datainput.readInt();

            this.list = new ArrayList<>();

            for (int k = 0; k < j; ++k) {
                NBTBase nbtbase = NBTBase.createTag(this.type);
                if (nbtbase != null) nbtbase.load(datainput, i + 1, nbtreadlimiter);
                this.list.add(nbtbase);
            }
        }
    }

    public byte getId() {
        return (byte) 9;
    }

    public String toString() {
        StringBuilder s = new StringBuilder("[");
        int i = 0;

        for (Iterator iterator = this.list.iterator(); iterator.hasNext(); ++i) {
            NBTBase nbtbase = (NBTBase) iterator.next();

            s.append(i).append(':').append(nbtbase).append(',');
        }

        return s + "]";
    }

    public void appendTag(NBTBase nbtbase) {
        if (this.type == 0) {
            this.type = nbtbase.getId();
        } else if (this.type != nbtbase.getId()) {
            System.err.println("WARNING: Adding mismatching tag types to tag list");
            return;
        }
        this.list.add(nbtbase);
    }

    public NBTBase removeTag(final int i) {
        return this.list.remove(i);
    }

    public void set(final int i, final NBTBase nbt) {
        if (i >= 0 && i < this.list.size()) {
            if (this.type == 0) {
                this.type = nbt.getId();
            } else if (this.type != nbt.getId()) {
                return;
            }
            this.list.set(i, nbt);
        }
    }

    public NBTTagCompound getCompoundTagAt(int i) {
        if (i >= 0 && i < this.list.size()) {
            NBTBase nbtbase = this.list.get(i);

            return nbtbase.getId() == 10 ? (NBTTagCompound) nbtbase : new NBTTagCompound();
        } else {
            return new NBTTagCompound();
        }
    }

    public int[] c(int i) {
        if (i >= 0 && i < this.list.size()) {
            NBTBase nbtbase = this.list.get(i);

            return nbtbase.getId() == 11 ? ((NBTTagIntArray) nbtbase).getIntArray() : new int[0];
        } else {
            return new int[0];
        }
    }

    public double d(int i) {
        if (i >= 0 && i < this.list.size()) {
            NBTBase nbtbase = this.list.get(i);

            return nbtbase.getId() == 6 ? ((NBTTagDouble) nbtbase).getDouble() : 0.0D;
        } else {
            return 0.0D;
        }
    }

    public float e(int i) {
        if (i >= 0 && i < this.list.size()) {
            NBTBase nbtbase = this.list.get(i);

            return nbtbase.getId() == 5 ? ((NBTTagFloat) nbtbase).getFloat() : 0.0F;
        } else {
            return 0.0F;
        }
    }

    public String getStringTagAt(int i) {
        if (i >= 0 && i < this.list.size()) {
            NBTBase nbtbase = this.list.get(i);

            return nbtbase.getId() == 8 ? nbtbase.getString() : nbtbase.toString();
        } else {
            return "";
        }
    }

    public int tagCount() {
        return this.list.size();
    }

    public NBTBase clone() {
        NBTTagList nbttaglist = new NBTTagList();

        nbttaglist.type = this.type;

        for (Object aList : this.list) {
            NBTBase nbtbase = (NBTBase) aList;
            NBTBase nbtbase1 = nbtbase.clone();

            nbttaglist.list.add(nbtbase1);
        }

        return nbttaglist;
    }

    public NBTTagList copy() {
        return (NBTTagList) clone();
    }

    public boolean equals(Object object) {
        if (super.equals(object)) {
            NBTTagList nbttaglist = (NBTTagList) object;

            if (this.type == nbttaglist.type) {
                return this.list.equals(nbttaglist.list);
            }
        }

        return false;
    }

    public int hashCode() {
        return super.hashCode() ^ this.list.hashCode();
    }

    public int d() {
        return this.type;
    }
}
