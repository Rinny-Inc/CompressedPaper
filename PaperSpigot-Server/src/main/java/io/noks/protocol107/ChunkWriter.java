package io.noks.protocol107;

import org.spigotmc.SpigotDebreakifier;

import net.minecraft.server.PacketDataSerializer;
import net.minecraft.server.PacketPlayOutMapChunk;

public class ChunkWriter {
	public static void write(PacketDataSerializer serializer, PacketPlayOutMapChunk packet) {
	    int index = 0;
	    int sectionCount = 0;

	    for (int j = 0; j < 16; j++) {
	        if ((packet.c & (1 << j)) != 0) {
	            sectionCount++;
	        }
	    }

	    int dataIndex = sectionCount * 4096;
	    int lightIndex = dataIndex + sectionCount * 2048;
	    int skylightIndex = lightIndex + sectionCount * 2048;
	    int dataLen = sectionCount * 4096 * 13 / 8;
	    dataLen += sectionCount * (packet.world.worldProvider.g ? 2048 : 4096);
	    dataLen += sectionCount * 4;

	    if (packet.g) {
	        dataLen += 256;
	    }

	    serializer.b(dataLen);

	    for (int section = 0; section < 16; section++) {
	        if ((packet.c & (1 << section)) != 0) {
	            serializer.writeByte(13);
	            serializer.b(0);
	            serializer.b(832);

	            long buf = 0L;
	            int bufIndex = 0;
	            int end = index + 4096;

	            for (; index < end; index++) {
	                int id = packet.f[index] & 0xFF;

	                int data = packet.f[dataIndex + (index >> 1)] & 0xFF;
	                data = ((index & 0x1) == 0) ? (data & 0xF) : (data >> 4 & 0xF);
	                data = SpigotDebreakifier.getCorrectedData(id, data);

	                id = id << 4 | data;
	                buf |= (long) id << bufIndex; 
	                bufIndex += 13;

	                if (bufIndex > 63) {
	                    serializer.writeLong(buf);
	                    bufIndex -= 64;
	                    buf = id >> (13 - bufIndex);
	                }
	            }

	            serializer.writeBytes(packet.f, lightIndex, 2048);
	            lightIndex += 2048;

	            if (!packet.world.worldProvider.g) {
	                serializer.writeBytes(packet.f, skylightIndex, 2048);
	                skylightIndex += 2048;
	            }
	        }
	    }

	    if (packet.g) {
	        serializer.writeBytes(packet.f, packet.f.length - 256, 256);
	    }
	}
}
