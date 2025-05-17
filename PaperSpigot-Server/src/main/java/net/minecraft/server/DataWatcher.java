package net.minecraft.server;

import java.io.IOException;
import java.util.Arrays;

import net.minecraft.util.org.apache.commons.lang3.ObjectUtils;

public class DataWatcher {
	private final Entity entity;
	public final Object[] data = new Object[32];
	public final boolean[] dirtyMap = new boolean[32];
	private boolean empty = true;
	private boolean dirty = false;
	public static final Object[] NULL_ITEM_STACK = new Object[0];

	public DataWatcher(Entity entity) {
		this.entity = entity;
	}

	public void a(int index, Object object) {
		if (object instanceof ItemStack)
			object = ((ItemStack) object).cloneItemStack();
		this.data[index] = object;
		this.empty = false;
	}

	public void add(int index, int type) {
		if (type != 5)
			throw new IllegalArgumentException("Can't add null for this.data type " + type);
		this.data[index] = NULL_ITEM_STACK;
		this.empty = false;
	}

	public byte getByte(int i) {
		return ((Byte) this.data[i]).byteValue();
	}

	public short getShort(int i) {
		return ((Short) this.data[i]).shortValue();
	}

	public int getInt(int i) {
		return ((Integer) this.data[i]).intValue();
	}

	public float getFloat(int i) {
		return ((Float) this.data[i]).floatValue();
	}

	public String getString(int i) {
		return (String) this.data[i];
	}

	public ItemStack getItemStack(int i) {
		return (this.data[i] == NULL_ITEM_STACK) ? null : (ItemStack) this.data[i];
	}

	public void watch(int i, Object object) {
		if (object == null) {
			if (this.data[i] instanceof ItemStack)
				object = NULL_ITEM_STACK;
			if (this.data[i] == NULL_ITEM_STACK)
				return;
		}
		if (ObjectUtils.notEqual(object, this.data[i])) {
			if (object instanceof ItemStack)
				object = ((ItemStack) object).cloneItemStack();
			this.data[i] = object;
			this.entity.i(i);
			this.dirtyMap[i] = true;
			this.dirty = true;
		}
	}

	public void update(int i) {
		this.dirtyMap[i] = true;
		this.dirty = true;
	}

	public boolean a() {
		return this.dirty;
	}

	public boolean d() {
		return this.empty;
	}

	public void e() {
		Arrays.fill(this.dirtyMap, false);
		this.dirty = false;
	}

	public static void write(PacketDataSerializer packetdataserializer, Object[] metadata, Class<? extends Entity> clss) throws IOException {
		switch (packetdataserializer.version) {
		case 47:
			net.badlion.gspigot.protocol47.MetadataWriter.write(packetdataserializer, metadata, clss);
			return;
		case 107, 108, 109, 110:
			net.badlion.gspigot.protocol107.MetadataWriter.write(packetdataserializer, metadata, clss);
			return;
		case 210:
			net.badlion.gspigot.protocol210.MetadataWriter.write(packetdataserializer, metadata, clss);
			return;
		}
		net.badlion.gspigot.protocol5.MetadataWriter.write(packetdataserializer, metadata, clss);
	}

	/*
	 * static { // Spigot Start - remove valueOf classToId.put(Byte.class, 0);
	 * classToId.put(Short.class, 1); classToId.put(Integer.class, 2);
	 * classToId.put(Float.class, 3); classToId.put(String.class, 4);
	 * classToId.put(ItemStack.class, 5); classToId.put(ChunkCoordinates.class, 6);
	 * // Spigot End }
	 */
}
