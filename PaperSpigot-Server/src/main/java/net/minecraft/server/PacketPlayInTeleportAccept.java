package net.minecraft.server;

import java.io.IOException;

import net.minecraft.server.PacketListener;

public class PacketPlayInTeleportAccept extends Packet {
	public int teleportId;

	public void a(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void read107(PacketDataSerializer packetdataserializer) throws IOException {
		this.teleportId = packetdataserializer.a();
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void handle(PacketListener packetlistener) {
	}
}
