package net.minecraft.server;

import java.io.IOException;

import net.minecraft.server.PacketListener;

public class PacketPlayOutUnloadChunk extends Packet {
	public final int x;

	public final int z;

	public PacketPlayOutUnloadChunk(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public void a(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writeInt(this.x);
		packetdataserializer.writeInt(this.z);
	}

	public void handle(PacketListener packetlistener) {
	}
}
