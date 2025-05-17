package net.minecraft.server;

import java.io.IOException;

public class PacketPlayOutSetSlot extends Packet {

    public int a; // Spigot
    public int b; // Spigot
    private ItemStack c;

    public PacketPlayOutSetSlot() {}

    public PacketPlayOutSetSlot(int i, int j, ItemStack itemstack) {
        this.a = i;
        this.b = j;
        this.c = itemstack == null ? null : itemstack.cloneItemStack();
    }

    public void a(PacketPlayOutListener packetplayoutlistener) {
        packetplayoutlistener.a(this);
    }

    public void a(PacketDataSerializer packetdataserializer) {
        this.a = packetdataserializer.readByte();
        this.b = packetdataserializer.readShort();
        this.c = packetdataserializer.c();
    }

    public void b(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.a);
        packetdataserializer.writeShort(this.b);
        packetdataserializer.a(this.c);
    }
    
	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.writeByte(this.a);
        packetdataserializer.writeShort(this.b);
        if (this.a == 0 && this.b == 45 && this.c != null) {
          packetdataserializer.writeShort(442);
          packetdataserializer.writeByte(1);
          packetdataserializer.writeShort(0);
          packetdataserializer.writeByte(0);
        } else {
          packetdataserializer.a(this.c);
        } 
	}

    public void handle(PacketListener packetlistener) {
        this.a((PacketPlayOutListener) packetlistener);
    }
}
