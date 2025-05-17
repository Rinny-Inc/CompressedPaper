package net.minecraft.server;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.craftbukkit.util.CraftChatMessage;

import net.minecraft.server.ChatSerializer;
import net.minecraft.server.PacketListener;

public class PacketPlayOutBossBar extends Packet {
	public UUID uuid;
	public int action;
	public String title;
	public float health;
	public int color;
	public int division;
	public byte flags;
	
	public void a(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writeUUID(this.uuid);
		packetdataserializer.b(this.action);
		switch (this.action) {
		case 0:
			packetdataserializer.a(ChatSerializer.a(CraftChatMessage.fromString(this.title)[0]));
			packetdataserializer.writeFloat(this.health);
			packetdataserializer.b(this.color);
			packetdataserializer.b(this.division);
			packetdataserializer.writeByte(this.flags);
			break;
		case 2:
			packetdataserializer.writeFloat(this.health);
			break;
		case 3:
			packetdataserializer.a(ChatSerializer.a(CraftChatMessage.fromString(this.title)[0]));
			break;
		case 4:
			packetdataserializer.b(this.color);
			packetdataserializer.b(this.division);
			break;
		case 5:
			packetdataserializer.writeByte(this.flags);
			break;
		}
	}

	public void handle(PacketListener packetlistener) {
	}
	
	public enum State {
		ADD, // 0...
		REMOVE,
		SET_HEALTH,
		SET_TITLE,
		SET_STYLE,
		SET_FLAGS;
	}
}