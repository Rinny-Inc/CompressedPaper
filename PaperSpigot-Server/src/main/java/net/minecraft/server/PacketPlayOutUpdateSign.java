package net.minecraft.server;

import java.io.IOException;

import org.bukkit.craftbukkit.util.CraftChatMessage; // Spigot - protocol patch

public class PacketPlayOutUpdateSign extends Packet {

	private int x;
	private int y;
	private int z;
	private String[] lines;

	public PacketPlayOutUpdateSign() {
	}

	public PacketPlayOutUpdateSign(int i, int j, int k, String[] astring) {
		this.x = i;
		this.y = j;
		this.z = k;
		this.lines = new String[] { astring[0], astring[1], astring[2], astring[3] };
	}

	public void a(PacketDataSerializer packetdataserializer) throws IOException {
		this.x = packetdataserializer.readInt();
		this.y = packetdataserializer.readShort();
		this.z = packetdataserializer.readInt();
		this.lines = new String[4];

		for (int i = 0; i < 4; ++i) {
			this.lines[i] = packetdataserializer.c(15);
		}
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writeInt(this.x);
		packetdataserializer.writeShort(this.y);
		packetdataserializer.writeInt(this.z);
		for (int i = 0; i < 4; i++)
			packetdataserializer.a(this.lines[i]);
	}

	public void write47(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writePosition(this.x, this.y, this.z);
		for (int i = 0; i < 4; i++) {
			String line = ChatSerializer.a(CraftChatMessage.fromString(this.lines[i])[0]);
			packetdataserializer.a(line);
		}
	}
	
	public void write108(PacketDataSerializer packetdataserializer) throws IOException {
	    packetdataserializer.writePosition(this.x, this.y, this.z);
	    packetdataserializer.writeByte(9);
	    NBTTagCompound nbt = new NBTTagCompound();
	    nbt.setString("id", "Sign");
	    nbt.setInt("x", this.x);
	    nbt.setInt("y", this.y);
	    nbt.setInt("z", this.z);
	    nbt.setString("Text1", ChatSerializer.a(CraftChatMessage.fromString(this.lines[0])[0]));
	    nbt.setString("Text2", ChatSerializer.a(CraftChatMessage.fromString(this.lines[1])[0]));
	    nbt.setString("Text3", ChatSerializer.a(CraftChatMessage.fromString(this.lines[2])[0]));
	    nbt.setString("Text4", ChatSerializer.a(CraftChatMessage.fromString(this.lines[3])[0]));
	    packetdataserializer.a(nbt);
	}

	public void a(PacketPlayOutListener packetplayoutlistener) {
		packetplayoutlistener.a(this);
	}

	public void handle(PacketListener packetlistener) {
		this.a((PacketPlayOutListener) packetlistener);
	}
}
