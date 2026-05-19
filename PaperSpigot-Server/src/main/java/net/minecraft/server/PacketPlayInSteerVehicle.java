package net.minecraft.server;

public class PacketPlayInSteerVehicle extends Packet {

    private float a;
    private float b;
    private byte cd = 0;

    private void setC(boolean value) {
        if (value) this.cd |= 0x1;
        else this.cd &= ~0x1;
    }
    private boolean getC() {
        return (this.cd & 0x1) != 0;
    }
    private void setD(boolean value) {
        if (value) this.cd |= 0x2;
        else this.cd &= ~0x2;
    }
    private boolean getD() {
        return (this.cd & 0x2) != 0;
    }

    public PacketPlayInSteerVehicle() {}

    public void a(PacketDataSerializer packetdataserializer) {
        this.a = packetdataserializer.readFloat();
        this.b = packetdataserializer.readFloat();
        // Spigot start - protocol patch
        if ( packetdataserializer.version < 16 )
        {
            setC(packetdataserializer.readBoolean());
            setD(packetdataserializer.readBoolean());
        } else {
            int flags = packetdataserializer.readUnsignedByte();
            setC((flags & 0x1) != 0);
            setD((flags & 0x2) != 0);
        }
        // Spigot end
    }

    public void b(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeFloat(this.a);
        packetdataserializer.writeFloat(this.b);
        packetdataserializer.writeBoolean(getC());
        packetdataserializer.writeBoolean(getD());
    }

    public void a(PacketPlayInListener packetplayinlistener) {
        packetplayinlistener.a(this);
    }

    public float c() {
        return this.a;
    }

    public float d() {
        return this.b;
    }

    public boolean e() {
        return getC();
    }

    public boolean f() {
        return getD();
    }

    public void handle(PacketListener packetlistener) {
        this.a((PacketPlayInListener) packetlistener);
    }
}
