package net.minecraft.server;

import java.io.IOException;

public class PacketPlayInArmAnimation extends Packet {

	private int a;
	private int b;

	public PacketPlayInArmAnimation() {
	}

	public void a(PacketDataSerializer packetdataserializer) {
		this.a = packetdataserializer.readInt();
		this.b = packetdataserializer.readByte();
	}

	public void read47(PacketDataSerializer packetdataserializer) throws IOException {
		this.b = 1;
	}

	public void b(PacketDataSerializer packetdataserializer) {
		packetdataserializer.writeInt(this.a);
		packetdataserializer.writeByte(this.b);
	}

	public void a(PacketPlayInListener packetplayinlistener) {
		packetplayinlistener.a(this);
	}
	
	public void read107(PacketDataSerializer packetdataserializer) throws IOException {
	    packetdataserializer.a();
	    this.b = 1;
	}

	public String b() {
		return String.format("id=%d, type=%d", new Object[] { Integer.valueOf(this.a), Integer.valueOf(this.b) });
	}

	public int d() {
		return this.b;
	}

	public void handle(PacketListener packetlistener) {
		this.a((PacketPlayInListener) packetlistener);
	}
}
