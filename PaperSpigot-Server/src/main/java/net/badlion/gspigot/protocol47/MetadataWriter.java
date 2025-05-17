package net.badlion.gspigot.protocol47;

import java.io.IOException;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityAgeable;
import net.minecraft.server.EntityEnderman;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityInsentient;
import net.minecraft.server.EntityItemFrame;
import net.minecraft.server.EntityMinecartAbstract;
import net.minecraft.server.ItemStack;
import net.minecraft.server.PacketDataSerializer;

public class MetadataWriter {
	public static void write(PacketDataSerializer serializer, Object[] data, Class<? extends Entity> clss) throws IOException {
		for (int i = 0; i < 32; i++) {
			if (data[i] != null)
				if (i == 10 && EntityInsentient.class.isAssignableFrom(clss)) {
					serializer.writeByte(130);
					serializer.a((String) data[i]);
				} else if (i == 11 && EntityInsentient.class.isAssignableFrom(clss)) {
					serializer.writeByte(3);
					serializer.writeByte(((Byte) data[i]).byteValue());
				} else if (i == 12 && EntityAgeable.class.isAssignableFrom(clss)) {
					serializer.writeByte(i);
					int age = ((Integer) data[i]).intValue();
					serializer.writeByte((age < 0) ? -1 : ((age >= 6000) ? age : 0));
				} else if (i == 16 && EntityEnderman.class.isAssignableFrom(clss)) {
					serializer.writeByte(0x20 | i);
					serializer.writeShort(((Byte) data[i]).byteValue() & 0xFF);
				} else if (i == 16 && EntityHuman.class.isAssignableFrom(clss)) {
					serializer.writeByte(10);
					serializer.writeByte(((Byte) data[i]).byteValue());
				} else if (i == 2 && EntityItemFrame.class.isAssignableFrom(clss)) {
					serializer.writeByte(168);
					serializer.a((data[i] == DataWatcher.NULL_ITEM_STACK) ? null : (ItemStack) data[i]);
				} else if (i == 3 && EntityItemFrame.class.isAssignableFrom(clss)) {
					serializer.writeByte(9);
					serializer.writeByte(((Byte) data[i]).byteValue() * 2);
				} else if (i == 20 && EntityMinecartAbstract.class.isAssignableFrom(clss)) {
					serializer.writeByte(0x40 | i);
					int blockData = ((Integer) data[i]).intValue() >> 16;
					serializer.writeInt(((Integer) data[20]).intValue() & 0xFFFF | blockData << 12);
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