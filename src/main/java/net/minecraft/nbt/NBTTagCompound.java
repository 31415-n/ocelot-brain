package net.minecraft.nbt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NBTTagCompound extends NBTBase {

    private static final Logger b = LogManager.getLogger();
    private Map map = new HashMap();

    public NBTTagCompound() {}

    void write(DataOutput dataoutput) throws IOException {

        for (Object o : this.map.keySet()) {
            String s = (String) o;
            NBTBase nbtbase = (NBTBase) this.map.get(s);

            a(s, nbtbase, dataoutput);
        }

        dataoutput.writeByte(0);
    }

    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        if (i > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        } else {
            this.map.clear();

            byte b0;

            while ((b0 = a(datainput, nbtreadlimiter)) != 0) {
                String s = getTagType(datainput, nbtreadlimiter);

                nbtreadlimiter.a((long) (16 * s.length()));
                NBTBase nbtbase = a(b0, s, datainput, i + 1, nbtreadlimiter);

                this.map.put(s, nbtbase);
            }
        }
    }

    public Set getKeySet() {
        return this.map.keySet();
    }

    public byte getId() {
        return (byte) 10;
    }

    public void setTag(String s, NBTBase nbtbase) {
        this.map.put(s, nbtbase);
    }

    public void setByte(String s, byte b0) {
        this.map.put(s, new NBTTagByte(b0));
    }

    public void setShort(String s, short short1) {
        this.map.put(s, new NBTTagShort(short1));
    }

    public void setInteger(String s, int i) {
        this.map.put(s, new NBTTagInt(i));
    }

    public void setLong(String s, long i) {
        this.map.put(s, new NBTTagLong(i));
    }

    public void setFloat(String s, float f) {
        this.map.put(s, new NBTTagFloat(f));
    }

    public void setDouble(String s, double d0) {
        this.map.put(s, new NBTTagDouble(d0));
    }

    public void setString(String s, String s1) {
        this.map.put(s, new NBTTagString(s1));
    }

    public void setByteArray(String s, byte[] abyte) {
        this.map.put(s, new NBTTagByteArray(abyte));
    }

    public void setIntArray(String s, int[] aint) {
        this.map.put(s, new NBTTagIntArray(aint));
    }

    public void setBoolean(String s, boolean flag) {
        this.setByte(s, (byte) (flag ? 1 : 0));
    }

    public NBTBase getTag(String s) {
        return (NBTBase) this.map.get(s);
    }

    public byte getTagType(String s) {
        NBTBase nbtbase = (NBTBase) this.map.get(s);

        return nbtbase != null ? nbtbase.getId() : 0;
    }

    public boolean hasKey(String s) {
        return this.map.containsKey(s);
    }

    public boolean hasKeyOfType(String s, int i) {
        byte b0 = this.getTagType(s);

        return b0 == i || (i == 99 && (b0 == 1 || b0 == 2 || b0 == 3 || b0 == 4 || b0 == 5 || b0 == 6));
    }

    public byte getByte(String s) {
        try {
            return !this.map.containsKey(s) ? 0 : ((NBTNumber) this.map.get(s)).getByte();
        } catch (ClassCastException classcastexception) {
            return (byte) 0;
        }
    }

    public short getShort(String s) {
        try {
            return !this.map.containsKey(s) ? 0 : ((NBTNumber) this.map.get(s)).getShort();
        } catch (ClassCastException classcastexception) {
            return (short) 0;
        }
    }

    public int getInteger(String s) {
        try {
            return !this.map.containsKey(s) ? 0 : ((NBTNumber) this.map.get(s)).getInt();
        } catch (ClassCastException classcastexception) {
            return 0;
        }
    }

    public long getLong(String s) {
        try {
            return !this.map.containsKey(s) ? 0L : ((NBTNumber) this.map.get(s)).getLong();
        } catch (ClassCastException classcastexception) {
            return 0L;
        }
    }

    public float getFloat(String s) {
        try {
            return !this.map.containsKey(s) ? 0.0F : ((NBTNumber) this.map.get(s)).getFloat();
        } catch (ClassCastException classcastexception) {
            return 0.0F;
        }
    }

    public double getDouble(String s) {
        try {
            return !this.map.containsKey(s) ? 0.0D : ((NBTNumber) this.map.get(s)).getDouble();
        } catch (ClassCastException classcastexception) {
            return 0.0D;
        }
    }

    public String getString(String s) {
        try {
            return !this.map.containsKey(s) ? "" : ((NBTBase) this.map.get(s)).getString();
        } catch (ClassCastException classcastexception) {
            return "";
        }
    }

    public byte[] getByteArray(String s) {
        return !this.map.containsKey(s) ? new byte[0] : ((NBTTagByteArray) this.map.get(s)).getByteArray();
    }

    public int[] getIntArray(String s) {
        return !this.map.containsKey(s) ? new int[0] : ((NBTTagIntArray) this.map.get(s)).getIntArray();
    }

    public NBTTagCompound getCompoundTag(String s) {
        return !this.map.containsKey(s) ? new NBTTagCompound() : (NBTTagCompound) this.map.get(s);
    }

    public NBTTagList getTagList(String s, int i) {
        if (this.getTagType(s) != 9) {
            return new NBTTagList();
        } else {
            NBTTagList nbttaglist = (NBTTagList) this.map.get(s);

            return nbttaglist.tagCount() > 0 && nbttaglist.d() != i ? new NBTTagList() : nbttaglist;
        }
    }

    public boolean getBoolean(String s) {
        return this.getByte(s) != 0;
    }

    public void removeTag(String s) {
        this.map.remove(s);
    }

    public String toString() {
        String s = "{";

        String s1;

        for (Iterator iterator = this.map.keySet().iterator(); iterator.hasNext(); s = s + s1 + ':' + this.map.get(s1) + ',') {
            s1 = (String) iterator.next();
        }

        return s + "}";
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public boolean hasNoTags() {
        return isEmpty();
    }

    public NBTBase clone() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        for (Object o : this.map.keySet()) {
            String s = (String) o;

            nbttagcompound.setTag(s, ((NBTBase) this.map.get(s)).clone());
        }

        return nbttagcompound;
    }

    public NBTTagCompound copy() {
        return (NBTTagCompound) clone();
    }

    public boolean equals(Object object) {
        if (super.equals(object)) {
            NBTTagCompound nbttagcompound = (NBTTagCompound) object;

            return this.map.entrySet().equals(nbttagcompound.map.entrySet());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ this.map.hashCode();
    }

    private static void a(String s, NBTBase nbtbase, DataOutput dataoutput) throws IOException {
        dataoutput.writeByte(nbtbase.getId());
        if (nbtbase.getId() != 0) {
            dataoutput.writeUTF(s);
            nbtbase.write(dataoutput);
        }
    }

    private static byte a(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
        return datainput.readByte();
    }

    private static String getTagType(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
        return datainput.readUTF();
    }

    static NBTBase a(byte b0, String s, DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        NBTBase nbtbase = NBTBase.createTag(b0);
        if (nbtbase != null) nbtbase.load(datainput, i, nbtreadlimiter);
        return nbtbase;
    }

    static Map a(NBTTagCompound nbttagcompound) {
        return nbttagcompound.map;
    }
}
