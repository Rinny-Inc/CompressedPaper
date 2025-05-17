package net.minecraft.server;

import java.io.IOException;

import net.minecraft.server.PacketListener;

public class PacketPlayInVehicleMove extends Packet {
	public double x;
	public double y;
	public double z;
	public float yaw;
	public float pitch;

	public void a(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void read107(PacketDataSerializer packetdataserializer) throws IOException {
		this.x = packetdataserializer.readDouble();
		this.y = packetdataserializer.readDouble();
		this.z = packetdataserializer.readDouble();
		this.yaw = packetdataserializer.readFloat();
		this.pitch = packetdataserializer.readFloat();
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void handle(PacketListener packetlistener) {
		((PlayerConnection) packetlistener).handleVehicleMove(this);
	}
}