package net.minecraft.server;

import java.io.IOException;

public class PacketPlayOutAttachEntity extends Packet {
	public int leash;
	public int entityId;
	public int vehicleEntityId;

	public PacketPlayOutAttachEntity() {
	}

	public PacketPlayOutAttachEntity(int i, Entity entity, Entity entity1) {
		this.leash = i;
		this.entityId = entity.getId();
		this.vehicleEntityId = (entity1 != null) ? entity1.getId() : -1;
	}

	public void a(PacketDataSerializer packetdataserializer) {
		this.entityId = packetdataserializer.readInt();
		this.vehicleEntityId = packetdataserializer.readInt();
		this.leash = packetdataserializer.readUnsignedByte();
	}

	public void b(PacketDataSerializer packetdataserializer) {
		packetdataserializer.writeInt(this.entityId);
		packetdataserializer.writeInt(this.vehicleEntityId);
		packetdataserializer.writeByte(this.leash);
	}
	
	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writeInt(this.entityId);
		packetdataserializer.writeInt(this.vehicleEntityId);
	}

	public void a(PacketPlayOutListener packetplayoutlistener) {
		packetplayoutlistener.a(this);
	}

	public void handle(PacketListener packetlistener) {
		a((PacketPlayOutListener) packetlistener);
	}
}
