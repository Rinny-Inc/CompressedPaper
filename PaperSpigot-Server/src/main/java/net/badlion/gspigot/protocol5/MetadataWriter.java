package net.badlion.gspigot.protocol5;

import java.io.IOException;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.ItemStack;
import net.minecraft.server.PacketDataSerializer;

public class MetadataWriter {
	public static void write(PacketDataSerializer serializer, Object[] data, Class<? extends Entity> clss) throws IOException {
		for (int i = 0; i < 32; i++) {
			if (data[i] != null)
				if (i == 16 && EntityHuman.class.isAssignableFrom(clss)) {
					serializer.writeByte(i);
					serializer.writeByte(((((Byte) data[i]).byteValue() & 0x1) == 0) ? 2 : 0);
				} else if (data[i] == DataWatcher.NULL_ITEM_STACK) {
					serializer.writeByte(0xA0 | i);
					serializer.a((ItemStack) null);
				} else if (data[i] instanceof Byte b) {
					serializer.writeByte(i);
					serializer.writeByte(b);
				} else if (data[i] instanceof Short s) {
					serializer.writeByte(0x20 | i);
					serializer.writeShort(s);
				} else if (data[i] instanceof Integer in) {
					serializer.writeByte(0x40 | i);
					serializer.writeInt(in);
				} else if (data[i] instanceof Float f) {
					serializer.writeByte(0x60 | i);
					serializer.writeFloat(f);
				} else if (data[i] instanceof String s) {
					serializer.writeByte(0x80 | i);
					serializer.a(s);
				} else if (data[i] instanceof ItemStack is) {
					serializer.writeByte(0xA0 | i);
					serializer.a(is);
				}
		}
		serializer.writeByte(127);
	}
}