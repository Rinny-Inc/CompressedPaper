package net.minecraft.server;

public class PacketPlayInPositionLook extends PacketPlayInFlying {

    public PacketPlayInPositionLook() {
        this.hasPos = true;
        this.hasLook = true;
    }

    public void a(PacketDataSerializer packetdataserializer) {
        this.x = packetdataserializer.readDouble();
        this.y = packetdataserializer.readDouble();
        
        this.stance = (packetdataserializer.version < 16 ? this.stance = packetdataserializer.readDouble() : y + 1.62);
        // Spigot start - protocol patch
        /*if (packetdataserializer.version < 16)
        {
            this.stance = packetdataserializer.readDouble();
        } else
        {
            this.stance = y + 1.62;
        }*/
        // Spigot end
        this.z = packetdataserializer.readDouble();
        this.yaw = packetdataserializer.readFloat();
        this.pitch = packetdataserializer.readFloat();
        super.a(packetdataserializer);
    }

    public void b(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeDouble(this.x);
        packetdataserializer.writeDouble(this.y);
        packetdataserializer.writeDouble(this.stance);
        packetdataserializer.writeDouble(this.z);
        packetdataserializer.writeFloat(this.yaw);
        packetdataserializer.writeFloat(this.pitch);
        super.b(packetdataserializer);
    }

    public void handle(PacketListener packetlistener) {
        super.a((PacketPlayInListener) packetlistener);
    }
}
