package net.minecraft.server;

import java.io.IOException;

public class PacketPlayInEntityAction extends Packet {

    private int a;
    private int animation;
    private int c;

    public PacketPlayInEntityAction() {}

    public void a(PacketDataSerializer packetdataserializer) {
        this.a = packetdataserializer.readInt();
        this.animation = packetdataserializer.readByte();
        this.c = packetdataserializer.readInt();
    }
    
    @Override
    public void read47(PacketDataSerializer packetdataserializer) throws IOException {
    	a = packetdataserializer.a();
        animation = packetdataserializer.readUnsignedByte() + 1;
        c = packetdataserializer.a();
    }

    public void b(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeInt(this.a);
        packetdataserializer.writeByte(this.animation);
        packetdataserializer.writeInt(this.c);
    }

    public void a(PacketPlayInListener packetplayinlistener) {
        packetplayinlistener.a(this);
    }

    public int d() {
        return this.animation;
    }

    public int e() {
        return this.c;
    }

    public void handle(PacketListener packetlistener) {
        this.a((PacketPlayInListener) packetlistener);
    }
}
