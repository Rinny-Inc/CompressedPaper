package net.badlion.gspigot;

import java.io.IOException;
import java.util.Collection;

import net.minecraft.server.Packet;
import net.minecraft.server.PacketDataSerializer;
import net.minecraft.server.PacketListener;

public class MultiPacket extends Packet {
	private final Packet[] packets;

	public MultiPacket(Packet... packets) {
		this.packets = packets;
	}

	public MultiPacket(Collection<Packet> packets) {
		this.packets = packets.<Packet>toArray(new Packet[packets.size()]);
	}

	public Packet[] getPackets() {
		return this.packets;
	}

	public void a(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void handle(PacketListener packetlistener) {
	}
}
