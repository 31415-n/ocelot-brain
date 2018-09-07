package net.minecraft.nbt;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressedStreamTools {

    public static NBTTagCompound readCompressed(InputStream inputstream) throws IOException {
        NBTTagCompound nbttagcompound;
        try (DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(inputstream)))) {
            nbttagcompound = read(datainputstream, NBTReadLimiter.INFINITE);
        }
        return nbttagcompound;
    }

    public static void writeCompressed(NBTTagCompound nbttagcompound, OutputStream outputstream) throws IOException {
        try (DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputstream)))) {
            write(nbttagcompound, dataoutputstream);
        }
    }

    public static NBTTagCompound read(byte[] abyte, NBTReadLimiter nbtreadlimiter) throws IOException {
        NBTTagCompound nbttagcompound;
        try (DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte))))) {
            nbttagcompound = read(datainputstream, nbtreadlimiter);
        }
        return nbttagcompound;
    }

    public static byte[] write(NBTTagCompound nbttagcompound) throws IOException {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        try (DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream))) {
            write(nbttagcompound, dataoutputstream);
        }
        return bytearrayoutputstream.toByteArray();
    }

    public static NBTTagCompound read(DataInputStream datainputstream) throws IOException {
        return read(datainputstream, NBTReadLimiter.INFINITE);
    }

    public static NBTTagCompound read(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
        NBTBase nbtbase = read(datainput, 0, nbtreadlimiter);
        if (nbtbase instanceof NBTTagCompound) {
            return (NBTTagCompound) nbtbase;
        } else {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    public static void write(NBTTagCompound nbttagcompound, DataOutput dataoutput) throws IOException {
        writeTag(nbttagcompound, dataoutput);
    }

    private static void writeTag(NBTBase nbtbase, DataOutput dataoutput) throws IOException {
        dataoutput.writeByte(nbtbase.getId());
        if (nbtbase.getId() != 0) {
            dataoutput.writeUTF("");
            nbtbase.write(dataoutput);
        }
    }

    private static NBTBase read(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        byte b0 = datainput.readByte();
        if (b0 == 0) {
            return new NBTTagEnd();
        } else {
            datainput.readUTF();
            NBTBase nbtbase = NBTBase.createTag(b0);
            if (nbtbase != null) nbtbase.load(datainput, i, nbtreadlimiter);
            return nbtbase;
        }
    }
}
