package net.minecraft.server;

import java.io.IOException;

public class PacketPlayInClientCommand extends Packet {
	private EnumClientCommand a;

	public PacketPlayInClientCommand() {
	}

	public PacketPlayInClientCommand(EnumClientCommand enumclientcommand) {
		this.a = enumclientcommand;
	}

	public void a(PacketDataSerializer packetdataserializer) {
		this.a = EnumClientCommand.a()[packetdataserializer.readByte() % (EnumClientCommand.a()).length];
	}

	public void read107(PacketDataSerializer packetdataserializer) throws IOException {
		this.a = EnumClientCommand.a()[packetdataserializer.a() % (EnumClientCommand.a()).length];
	}

	public void b(PacketDataSerializer packetdataserializer) {
		packetdataserializer.writeByte(EnumClientCommand.a(this.a));
	}

	public void a(PacketPlayInListener packetplayinlistener) {
		packetplayinlistener.a(this);
	}

	public EnumClientCommand c() {
		return this.a;
	}

	public void handle(PacketListener packetlistener) {
		a((PacketPlayInListener) packetlistener);
	}
}
