package net.minecraft.server;

import java.io.IOException;

import net.minecraft.server.PacketListener;

public class PacketPlayInUseItem extends Packet {
	public int hand;

	public void a(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void read107(PacketDataSerializer packetdataserializer) throws IOException {
		this.hand = packetdataserializer.a();
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void handle(PacketListener packetlistener) {
		((PlayerConnection) packetlistener).handleUseItem(this);
	}
}
