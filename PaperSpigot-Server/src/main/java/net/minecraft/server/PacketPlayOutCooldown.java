package net.minecraft.server;

import java.io.IOException;

public class PacketPlayOutCooldown extends Packet {
	public int itemId;
	public int cooldownLength;

	public PacketPlayOutCooldown() {

	}

	public PacketPlayOutCooldown(int itemId, int cooldownLength) {
		this.itemId = itemId;
		this.cooldownLength = cooldownLength;
	}

	public void a(PacketDataSerializer packetdataserializer) throws IOException {

	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.b(this.itemId);
		packetdataserializer.b(this.cooldownLength);
	}

	public void handle(PacketListener packetlistener) {

	}
}
