package net.minecraft.server;

import java.io.IOException;

public class PacketPlayOutSpawnEntityWeather extends Packet {
	private int a;

	private int b;

	private int c;

	private int d;

	private int e;

	public PacketPlayOutSpawnEntityWeather() {
	}

	public PacketPlayOutSpawnEntityWeather(Entity entity) {
		this.a = entity.getId();
		this.b = MathHelper.floor(entity.locX * 32.0D);
		this.c = MathHelper.floor(entity.locY * 32.0D);
		this.d = MathHelper.floor(entity.locZ * 32.0D);
		if (entity instanceof EntityLightning)
			this.e = 1;
	}

	public void a(PacketDataSerializer packetdataserializer) {
		this.a = packetdataserializer.a();
		this.e = packetdataserializer.readByte();
		this.b = packetdataserializer.readInt();
		this.c = packetdataserializer.readInt();
		this.d = packetdataserializer.readInt();
	}

	public void b(PacketDataSerializer packetdataserializer) {
		packetdataserializer.b(this.a);
		packetdataserializer.writeByte(this.e);
		packetdataserializer.writeInt(this.b);
		packetdataserializer.writeInt(this.c);
		packetdataserializer.writeInt(this.d);
	}

	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.b(this.a);
		packetdataserializer.writeByte(this.e);
		packetdataserializer.writeDouble(this.b / 32.0D);
		packetdataserializer.writeDouble(this.c / 32.0D);
		packetdataserializer.writeDouble(this.d / 32.0D);
	}

	public void a(PacketPlayOutListener packetplayoutlistener) {
		packetplayoutlistener.a(this);
	}

	public String b() {
		return String.format("id=%d, type=%d, x=%.2f, y=%.2f, z=%.2f",
				new Object[] { Integer.valueOf(this.a), Integer.valueOf(this.e), Float.valueOf(this.b / 32.0F),
						Float.valueOf(this.c / 32.0F), Float.valueOf(this.d / 32.0F) });
	}

	public void handle(PacketListener packetlistener) {
		a((PacketPlayOutListener) packetlistener);
	}
}