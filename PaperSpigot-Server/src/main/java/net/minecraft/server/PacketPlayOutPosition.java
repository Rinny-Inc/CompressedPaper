package net.minecraft.server;

import java.io.IOException;

public class PacketPlayOutPosition extends Packet {

	private double a;
	private double b;
	private double c;
	private float d;
	private float e;
	private boolean f;

	public PacketPlayOutPosition() {
	}

	public PacketPlayOutPosition(double d0, double d1, double d2, float f, float f1, boolean flag) {
		this.a = d0;
		this.b = d1;
		this.c = d2;
		this.d = f;
		this.e = f1;
		this.f = flag;
		// Spigot end
	}

	public void a(PacketDataSerializer packetdataserializer) {
		this.a = packetdataserializer.readDouble();
		this.b = packetdataserializer.readDouble();
		this.c = packetdataserializer.readDouble();
		this.d = packetdataserializer.readFloat();
		this.e = packetdataserializer.readFloat();
		this.f = packetdataserializer.readBoolean();
	}

	public void b(PacketDataSerializer packetdataserializer) {
		packetdataserializer.writeDouble(this.a);
		packetdataserializer.writeDouble(this.b);
		packetdataserializer.writeDouble(this.c);
		packetdataserializer.writeFloat(this.d);
		packetdataserializer.writeFloat(this.e);
		packetdataserializer.writeBoolean(this.f);
	}

	public void write47(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writeDouble(this.a);
		packetdataserializer.writeDouble(this.b - 1.62D);
		packetdataserializer.writeDouble(this.c);
		packetdataserializer.writeFloat(this.d);
		packetdataserializer.writeFloat(this.e);
		packetdataserializer.writeByte(0);
	}
	
	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
		write47(packetdataserializer);
		packetdataserializer.b(0);
	}

	public void a(PacketPlayOutListener packetplayoutlistener) {
		packetplayoutlistener.a(this);
	}

	public void handle(PacketListener packetlistener) {
		this.a((PacketPlayOutListener) packetlistener);
	}
}
