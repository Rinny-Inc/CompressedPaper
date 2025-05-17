package net.badlion.gspigot.protocol107;

import java.io.IOException;

import org.spigotmc.SpigotDebreakifier;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityAgeable;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityBat;
import net.minecraft.server.EntityBlaze;
import net.minecraft.server.EntityBoat;
import net.minecraft.server.EntityCreeper;
import net.minecraft.server.EntityEnderman;
import net.minecraft.server.EntityFireworks;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.EntityHorse;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityIronGolem;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityItemFrame;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityMinecartAbstract;
import net.minecraft.server.EntityMinecartFurnace;
import net.minecraft.server.EntityOcelot;
import net.minecraft.server.EntityPig;
import net.minecraft.server.EntityPotion;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.EntitySlime;
import net.minecraft.server.EntitySpider;
import net.minecraft.server.EntityTameableAnimal;
import net.minecraft.server.EntityVillager;
import net.minecraft.server.EntityWitch;
import net.minecraft.server.EntityWither;
import net.minecraft.server.EntityWolf;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.ItemStack;
import net.minecraft.server.PacketDataSerializer;

public class MetadataWriter {
	public static void write(PacketDataSerializer serializer, Object[] data, Class<? extends Entity> clss) throws IOException {
		writeByte(serializer, 0, (Byte) data[0]);
		writeInt(serializer, 1, (Short) data[1]);
		if (EntityLiving.class.isAssignableFrom(clss)) {
			writeFloat(serializer, 6, (Float) data[6]);
			writeInt(serializer, 7, (Integer) data[7]);
			writeBooleanFromByte(serializer, 8, (Byte) data[8]);
			writeInt(serializer, 9, (Byte) data[9]);
			writeString(serializer, 2, (String) data[10]);
			writeBooleanFromByte(serializer, 3, (Byte) data[11]);
			if (EntityAgeable.class.isAssignableFrom(clss)) {
				if (data[12] instanceof Integer i)
					writeBoolean(serializer, 11, i < 0);
				if (EntityTameableAnimal.class.isAssignableFrom(clss)) {
					writeByte(serializer, 12, (Byte) data[16]);
					if (EntityOcelot.class.isAssignableFrom(clss)) {
						writeInt(serializer, 14, (Byte) data[18]);
					} else if (EntityWolf.class.isAssignableFrom(clss)) {
						writeFloat(serializer, 14, (Float) data[18]);
						writeBooleanFromByte(serializer, 15, (Byte) data[19]);
						writeInt(serializer, 16, (Byte) data[20]);
					}
				} else if (EntityPig.class.isAssignableFrom(clss)) {
					writeBooleanFromByte(serializer, 12, (Byte) data[16]);
				} else if (EntitySheep.class.isAssignableFrom(clss)) {
					writeByte(serializer, 12, (Byte) data[16]);
				} else if (EntityVillager.class.isAssignableFrom(clss)) {
					writeInt(serializer, 12, (Integer) data[16]);
				} else if (EntityHorse.class.isAssignableFrom(clss)) {
					writeByte(serializer, 12, (Integer) data[16]);
					writeInt(serializer, 13, (Byte) data[19]);
					writeInt(serializer, 14, (Integer) data[20]);
					writeInt(serializer, 16, (Integer) data[22]);
				}
			} else if (EntityHuman.class.isAssignableFrom(clss)) {
				writeByte(serializer, 12, (Byte) data[16]);
				writeFloat(serializer, 10, (Float) data[17]);
				writeInt(serializer, 11, (Integer) data[18]);
			} else if (EntityBat.class.isAssignableFrom(clss)) {
				writeByte(serializer, 11, (Byte) data[16]);
			} else if (EntityEnderman.class.isAssignableFrom(clss)) {
				if (data[16] instanceof Byte || data[17] instanceof Byte) {
					int blockId = (data[16] instanceof Byte) ? (((Byte) data[16]).byteValue() & 0xFF) : 0;
					int blockData = (data[17] instanceof Byte) ? (((Byte) data[17]).byteValue() & 0xF) : 0;
					blockData = SpigotDebreakifier.getCorrectedData(blockId, blockData);
					writeBlockId(serializer, 11, blockId, blockData);
				}
				writeBooleanFromByte(serializer, 12, (Byte) data[18]);
			} else if (EntityZombie.class.isAssignableFrom(clss)) {
				writeBooleanFromByte(serializer, 11, (Byte) data[12]);
				writeInt(serializer, 12, (Byte) data[13]);
				writeBooleanFromByte(serializer, 13, (Byte) data[14]);
			} else if (EntityBlaze.class.isAssignableFrom(clss)) {
				writeByte(serializer, 11, (Byte) data[16]);
			} else if (EntitySpider.class.isAssignableFrom(clss)) {
				writeByte(serializer, 11, (Byte) data[16]);
			} else if (EntityCreeper.class.isAssignableFrom(clss)) {
				writeInt(serializer, 11, (Byte) data[16]);
				writeBooleanFromByte(serializer, 12, (Byte) data[17]);
			} else if (EntityGhast.class.isAssignableFrom(clss)) {
				writeBooleanFromByte(serializer, 11, (Byte) data[16]);
			} else if (EntitySlime.class.isAssignableFrom(clss)) {
				writeInt(serializer, 11, (Byte) data[16]);
			} else if (EntitySkeleton.class.isAssignableFrom(clss)) {
				writeInt(serializer, 11, (Byte) data[13]);
			} else if (EntityWitch.class.isAssignableFrom(clss)) {
				writeBooleanFromByte(serializer, 11, (Byte) data[21]);
			} else if (EntityIronGolem.class.isAssignableFrom(clss)) {
				writeByte(serializer, 11, (Byte) data[16]);
			} else if (EntityWither.class.isAssignableFrom(clss)) {
				writeInt(serializer, 11, (Integer) data[17]);
				writeInt(serializer, 12, (Integer) data[18]);
				writeInt(serializer, 13, (Integer) data[19]);
				writeInt(serializer, 14, (Integer) data[20]);
			}
		} else if (EntityBoat.class.isAssignableFrom(clss)) {
			writeInt(serializer, 5, (Integer) data[17]);
			writeInt(serializer, 6, (Integer) data[18]);
			writeFloat(serializer, 7, (Float) data[19]);
		} else if (EntityMinecartAbstract.class.isAssignableFrom(clss)) {
			writeInt(serializer, 5, (Integer) data[17]);
			writeInt(serializer, 6, (Integer) data[18]);
			writeFloat(serializer, 7, (Float) data[19]);
			if (data[20] instanceof Integer i) {
				int blockId = i & 0xFFFF;
				int blockData = i >> 16;
				writeInt(serializer, 8, Integer.valueOf(blockId | blockData << 12));
			}
			writeInt(serializer, 9, (Integer) data[21]);
			writeBooleanFromByte(serializer, 10, (Byte) data[22]);
			if (EntityMinecartFurnace.class.isAssignableFrom(clss))
				writeBooleanFromByte(serializer, 11, (Byte) data[16]);
		} else if (EntityItem.class.isAssignableFrom(clss)) {
			writeItemStack(serializer, 5, data[10]);
		} else if (EntityItemFrame.class.isAssignableFrom(clss)) {
			writeItemStack(serializer, 5, data[2]);
			if (data[3] instanceof Byte)
				writeInt(serializer, 6, Integer.valueOf(((Byte) data[3]).byteValue() * 2));
		} else if (EntityArrow.class.isAssignableFrom(clss)) {
			writeByte(serializer, 5, (Byte) data[16]);
		} else if (EntityFireworks.class.isAssignableFrom(clss)) {
			writeItemStack(serializer, 5, data[8]);
		} else if (EntityPotion.class.isAssignableFrom(clss)) {
			writeItemStack(serializer, 6, data[6]);
		}
		serializer.writeByte(-1);
	}

	private static void writeByte(PacketDataSerializer serializer, int index, Number value) {
		if (value != null) {
			serializer.writeByte(index);
			serializer.writeByte(0);
			serializer.writeByte(value.byteValue());
		}
	}

	private static void writeInt(PacketDataSerializer serializer, int index, Number value) {
		if (value != null) {
			serializer.writeByte(index);
			serializer.writeByte(1);
			serializer.b(value.intValue());
		}
	}

	private static void writeFloat(PacketDataSerializer serializer, int index, Number value) {
		if (value != null) {
			serializer.writeByte(index);
			serializer.writeByte(2);
			serializer.writeFloat(value.floatValue());
		}
	}

	private static void writeString(PacketDataSerializer serializer, int index, String value) throws IOException {
		if (value != null) {
			serializer.writeByte(index);
			serializer.writeByte(3);
			serializer.a(value);
		}
	}

	private static void writeItemStack(PacketDataSerializer serializer, int index, Object itemStack) {
		if (itemStack instanceof ItemStack i) {
			serializer.writeByte(index);
			serializer.writeByte(5);
			serializer.a(i);
		} else if (itemStack == DataWatcher.NULL_ITEM_STACK) {
			serializer.writeByte(index);
			serializer.writeByte(5);
			serializer.a((ItemStack) null);
		}
	}

	private static void writeBoolean(PacketDataSerializer serializer, int index, boolean value) {
		serializer.writeByte(index);
		serializer.writeByte(6);
		serializer.writeByte(value ? 1 : 0);
	}

	private static void writeBooleanFromByte(PacketDataSerializer serializer, int index, Byte value) {
		if (value != null) {
			serializer.writeByte(index);
			serializer.writeByte(6);
			serializer.writeByte((value.byteValue() == 0) ? 0 : 1);
		}
	}

	private static void writeBlockId(PacketDataSerializer serializer, int index, int type, int data) {
		serializer.writeByte(index);
		serializer.writeByte(12);
		serializer.b(type << 4 | data);
	}
}
