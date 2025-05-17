package net.minecraft.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class NBTBase {

    public static final String[] a = new String[] { "END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]"};

    abstract void write(DataOutput dataoutput) throws IOException;

    abstract void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException;

    public abstract String toString();

    public abstract byte getTypeId();

    protected NBTBase() {}

    protected static NBTBase createTag(byte b0) {
        return switch (b0) {
	        case 0 -> new NBTTagEnd();
	        case 1 -> new NBTTagByte();
	        case 2 -> new NBTTagShort();
	        case 3 -> new NBTTagInt();
	        case 4 -> new NBTTagLong();
	        case 5 -> new NBTTagFloat();
	        case 6 -> new NBTTagDouble();
	        case 7 -> new NBTTagByteArray();
	        case 8 -> new NBTTagString();
	        case 9 -> new NBTTagList();
	        case 10 -> new NBTTagCompound();
	        case 11 -> new NBTTagIntArray();
	        default -> null;
        };
    }

    public abstract NBTBase clone();

    public boolean equals(Object object) {
        if (object instanceof NBTBase nbtbase) {
        	return this.getTypeId() == nbtbase.getTypeId();
        }
        return false;
    }

    public int hashCode() {
        return this.getTypeId();
    }

    protected String a_() {
        return this.toString();
    }
}
