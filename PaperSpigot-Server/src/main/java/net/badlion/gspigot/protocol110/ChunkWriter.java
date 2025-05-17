package net.badlion.gspigot.protocol110;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.PacketDataSerializer;
import net.minecraft.server.PacketPlayOutMapChunk;

public class ChunkWriter {
	public static void write(PacketDataSerializer serializer, PacketPlayOutMapChunk packet) {
		io.noks.protocol107.ChunkWriter.write(serializer, packet);
		serializer.b(packet.tileEntityData.size());
		for (NBTTagCompound data : packet.tileEntityData)
			serializer.a(data);
	}
}
