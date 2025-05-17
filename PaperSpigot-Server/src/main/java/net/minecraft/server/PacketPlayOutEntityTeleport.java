package net.minecraft.server;

import java.io.IOException;

public class PacketPlayOutEntityTeleport extends Packet {
	public int a;

	public int b;

	public int c;

	public int d;

	public byte e;

	public byte f;

	public boolean onGround;

	public boolean doBlockHeightCorrection;

	public PacketPlayOutEntityTeleport() {
	}

	public PacketPlayOutEntityTeleport(Entity entity) {
		this.a = entity.getId();
		this.b = MathHelper.floor(entity.locX * 32.0D);
		this.c = MathHelper.floor(entity.locY * 32.0D);
		this.d = MathHelper.floor(entity.locZ * 32.0D);
		this.e = (byte) (int) (entity.yaw * 256.0F / 360.0F);
		this.f = (byte) (int) (entity.pitch * 256.0F / 360.0F);
		this.doBlockHeightCorrection = (entity instanceof EntityFallingBlock || entity instanceof EntityTNTPrimed);
	}

	public PacketPlayOutEntityTeleport(int i, int j, int k, int l, byte b0, byte b1, boolean onGround, Entity entity) {
		this.a = i;
		this.b = j;
		this.c = k;
		this.d = l;
		this.e = b0;
		this.f = b1;
		this.onGround = onGround;
		this.doBlockHeightCorrection = (entity instanceof EntityFallingBlock || entity instanceof EntityTNTPrimed);
	}

	public void a(PacketDataSerializer packetdataserializer) {
		this.a = packetdataserializer.readInt();
		this.b = packetdataserializer.readInt();
		this.c = packetdataserializer.readInt();
		this.d = packetdataserializer.readInt();
		this.e = packetdataserializer.readByte();
		this.f = packetdataserializer.readByte();
	}

	public void b(PacketDataSerializer packetdataserializer) {
		packetdataserializer.writeInt(this.a);
		packetdataserializer.writeInt(this.b);
		packetdataserializer.writeInt(this.c);
		packetdataserializer.writeInt(this.d);
		packetdataserializer.writeByte(this.e);
		packetdataserializer.writeByte(this.f);
	}

	public void write47(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.b(this.a);
		packetdataserializer.writeInt(this.b);
		packetdataserializer.writeInt(this.doBlockHeightCorrection ? (this.c - 16) : this.c);
		packetdataserializer.writeInt(this.d);
		packetdataserializer.writeByte(this.e);
		packetdataserializer.writeByte(this.f);
		packetdataserializer.writeBoolean(this.onGround);
	}

	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.b(this.a);
		packetdataserializer.writeDouble(this.b / 32.0D);
		packetdataserializer.writeDouble((this.doBlockHeightCorrection ? (this.c - 16) : this.c) / 32.0D);
		packetdataserializer.writeDouble(this.d / 32.0D);
		packetdataserializer.writeByte(this.e);
		packetdataserializer.writeByte(this.f);
		packetdataserializer.writeBoolean(this.onGround);
	}

	public void a(PacketPlayOutListener packetplayoutlistener) {
		packetplayoutlistener.a(this);
	}

	public void handle(PacketListener packetlistener) {
		a((PacketPlayOutListener) packetlistener);
	}
}
