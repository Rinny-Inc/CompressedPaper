package net.minecraft.server;

import java.io.IOException;

public class PacketPlayInBlockPlace extends Packet {

	public int a;
	public int b;
	public int c;
	public int d;
	private ItemStack e;
	private float f;
	private float g;
	private float h;
	public int hand;

	public PacketPlayInBlockPlace() {
	}

	public void a(PacketDataSerializer packetdataserializer) {
		this.a = packetdataserializer.readInt();
		this.b = packetdataserializer.readUnsignedByte();
		this.c = packetdataserializer.readInt();
		this.d = packetdataserializer.readUnsignedByte();
		this.e = packetdataserializer.c();
		this.f = packetdataserializer.readUnsignedByte() / 16.0F;
		this.g = packetdataserializer.readUnsignedByte() / 16.0F;
		this.h = packetdataserializer.readUnsignedByte() / 16.0F;
	}

	public void read47(PacketDataSerializer packetdataserializer) throws IOException {
		long position = packetdataserializer.readLong();
		this.a = packetdataserializer.readPositionX(position);
		this.b = packetdataserializer.readPositionY(position);
		this.c = packetdataserializer.readPositionZ(position);
		this.d = packetdataserializer.readUnsignedByte();
		this.e = packetdataserializer.c();
		this.f = packetdataserializer.readUnsignedByte() / 16.0F;
		this.g = packetdataserializer.readUnsignedByte() / 16.0F;
		this.h = packetdataserializer.readUnsignedByte() / 16.0F;
	}

	public void b(PacketDataSerializer packetdataserializer) {
		packetdataserializer.writeInt(this.a);
		packetdataserializer.writeByte(this.b);
		packetdataserializer.writeInt(this.c);
		packetdataserializer.writeByte(this.d);
		packetdataserializer.a(this.e);
		packetdataserializer.writeByte((int) (this.f * 16.0F));
		packetdataserializer.writeByte((int) (this.g * 16.0F));
		packetdataserializer.writeByte((int) (this.h * 16.0F));
	}
	
	public void read107(PacketDataSerializer packetdataserializer) throws IOException {
	    long position = packetdataserializer.readLong();
	    this.a = packetdataserializer.readPositionX(position);
	    this.b = packetdataserializer.readPositionY(position);
	    this.c = packetdataserializer.readPositionZ(position);
	    this.d = packetdataserializer.a();
	    this.hand = packetdataserializer.a();
	    this.f = packetdataserializer.readUnsignedByte() / 16.0F;
	    this.g = packetdataserializer.readUnsignedByte() / 16.0F;
	    this.h = packetdataserializer.readUnsignedByte() / 16.0F;
	}

	public void a(PacketPlayInListener packetplayinlistener) {
		packetplayinlistener.a(this);
	}

	public int c() {
		return this.a;
	}

	public int d() {
		return this.b;
	}

	public int e() {
		return this.c;
	}

	public int getFace() {
		return this.d;
	}

	public ItemStack getItemStack() {
		return this.e;
	}

	public float h() {
		return this.f;
	}

	public float i() {
		return this.g;
	}

	public float j() {
		return this.h;
	}

	public void handle(PacketListener packetlistener) {
		this.a((PacketPlayInListener) packetlistener);
	}
}
