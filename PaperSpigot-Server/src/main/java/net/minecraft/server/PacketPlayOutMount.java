package net.minecraft.server;

import java.io.IOException;

import net.minecraft.server.PacketListener;

public class PacketPlayOutMount extends Packet {
	public int entityId;
	public int passengerId;

	public PacketPlayOutMount() {
	}

	public PacketPlayOutMount(Entity entity, Entity passenger) {
		this.entityId = entity.getId();
		this.passengerId = (passenger != null) ? passenger.getId() : -1;
	}

	public void a(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.b(this.entityId);
		if (this.passengerId != -1) {
			packetdataserializer.b(1);
			packetdataserializer.b(this.passengerId);
		} else {
			packetdataserializer.b(0);
		}
	}

	public void handle(PacketListener packetlistener) {
	}
}
