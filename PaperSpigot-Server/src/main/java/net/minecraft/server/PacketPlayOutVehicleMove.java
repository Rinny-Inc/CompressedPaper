package net.minecraft.server;

import java.io.IOException;

import net.minecraft.server.PacketListener;

public class PacketPlayOutVehicleMove extends Packet {
	public double x;
	public double y;
	public double z;
	public float yaw;
	public float pitch;

	public PacketPlayOutVehicleMove() {
	}

	public PacketPlayOutVehicleMove(double x, double y, double z, float yaw, float pitch) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public PacketPlayOutVehicleMove(Entity entity) {
		this(entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
	}

	public void a(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writeDouble(this.x);
		packetdataserializer.writeDouble(this.y);
		packetdataserializer.writeDouble(this.z);
		packetdataserializer.writeFloat(this.yaw);
		packetdataserializer.writeFloat(this.pitch);
	}

	public void handle(PacketListener packetlistener) {
	}
}
