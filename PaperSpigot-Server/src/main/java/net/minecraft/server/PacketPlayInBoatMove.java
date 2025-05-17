package net.minecraft.server;

import java.io.IOException;

import net.minecraft.server.PacketListener;

public class PacketPlayInBoatMove extends Packet {
	public void a(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void read107(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.readBoolean();
		packetdataserializer.readBoolean();
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void handle(PacketListener packetlistener) {
	}
}
