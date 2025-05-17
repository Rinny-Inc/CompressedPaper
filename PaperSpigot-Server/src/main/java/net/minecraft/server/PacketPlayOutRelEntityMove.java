package net.minecraft.server;

import java.io.IOException;

public class PacketPlayOutRelEntityMove extends PacketPlayOutEntity {

	private boolean onGround; // Spigot - protocol patch

	public PacketPlayOutRelEntityMove() {
	}

	public PacketPlayOutRelEntityMove(int i, byte b0, byte b1, byte b2, boolean onGround) { // Spigot - protocol patch
		super(i);
		this.b = b0;
		this.c = b1;
		this.d = b2;
		this.onGround = onGround; // Spigot - protocol patch
	}

	public void a(PacketDataSerializer packetdataserializer) {
		super.a(packetdataserializer);
		this.b = packetdataserializer.readByte();
		this.c = packetdataserializer.readByte();
		this.d = packetdataserializer.readByte();
	}

	public void b(PacketDataSerializer packetdataserializer) {
		packetdataserializer.writeInt(this.a);
		packetdataserializer.writeByte(this.b);
		packetdataserializer.writeByte(this.c);
		packetdataserializer.writeByte(this.d);
	}

	public void write47(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.b(this.a);
		packetdataserializer.writeByte(this.b);
		packetdataserializer.writeByte(this.c);
		packetdataserializer.writeByte(this.d);
		packetdataserializer.writeBoolean(this.onGround);
	}
	
	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
	    packetdataserializer.b(this.a);
	    packetdataserializer.writeShort(this.b * 128);
	    packetdataserializer.writeShort(this.c * 128);
	    packetdataserializer.writeShort(this.d * 128);
	    packetdataserializer.writeBoolean(this.onGround);
	}

	public String b() {
		return super.b() + String.format(", xa=%d, ya=%d, za=%d",
				new Object[] { Byte.valueOf(this.b), Byte.valueOf(this.c), Byte.valueOf(this.d) });
	}

	public void handle(PacketListener packetlistener) {
		super.a((PacketPlayOutListener) packetlistener);
	}
}
