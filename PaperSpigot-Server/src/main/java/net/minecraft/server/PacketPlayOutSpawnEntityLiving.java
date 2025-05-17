package net.minecraft.server;

import java.io.IOException;
import java.util.UUID;

public class PacketPlayOutSpawnEntityLiving extends Packet {

    public int a; //entityID
    public int b; //entityTYPE
    public int c; //x
    public int d; //y
    public int e; //z
    public int f; //yaw
    public int g; //pitch
    public int h; //headPitch
    public byte i; //velocityX
    public byte j; //velocityY
    public byte k; //velocityZ
    public Class<? extends Entity> clss;
    public Object[] metadata;
    public UUID uuid;

    public PacketPlayOutSpawnEntityLiving() {}

    public PacketPlayOutSpawnEntityLiving(EntityLiving entityliving) {
        this.a = entityliving.getId();
        this.b = (byte) EntityTypes.a(entityliving);
        this.c = MathHelper.floor(entityliving.locX * 32.0D);
        this.d = MathHelper.floor(entityliving.locY * 32.0D);
        this.e = MathHelper.floor(entityliving.locZ * 32.0D);
        this.i = (byte) ((int) (entityliving.yaw * 256.0F / 360.0F));
        this.j = (byte) ((int) (entityliving.pitch * 256.0F / 360.0F));
        this.k = (byte) ((int) (entityliving.aO * 256.0F / 360.0F));
        double d3 = 3.9D;
        double d0 = entityliving.motX;
        double d1 = entityliving.motY;
        double d2 = entityliving.motZ;
        
        this.f = (int) (MathHelper.a(d0, -d3, d3) * 8000.0D);
        this.g = (int) (MathHelper.a(d1, -d3, d3) * 8000.0D);
        this.h = (int) (MathHelper.a(d2, -d3, d3) * 8000.0D);
        this.clss = (Class)entityliving.getClass();
        this.metadata = new Object[32];
        System.arraycopy((entityliving.getDataWatcher()).data, 0, this.metadata, 0, 32);
        this.uuid = entityliving.getUniqueID();
    }

    public void a(PacketDataSerializer packetdataserializer) {}

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.b(this.a);
        packetdataserializer.writeByte(this.b & 255);
        packetdataserializer.writeInt(this.c);
        packetdataserializer.writeInt(this.d);
        packetdataserializer.writeInt(this.e);
        packetdataserializer.writeByte(this.i);
        packetdataserializer.writeByte(this.j);
        packetdataserializer.writeByte(this.k);
        packetdataserializer.writeShort(this.f);
        packetdataserializer.writeShort(this.g);
        packetdataserializer.writeShort(this.h);
        DataWatcher.write(packetdataserializer, this.metadata, this.clss);
    }
    
    public void write107(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.b(this.a);
        packetdataserializer.writeUUID(this.uuid);
        packetdataserializer.writeByte(this.b & 0xFF);
        packetdataserializer.writeDouble(this.c / 32.0D);
        packetdataserializer.writeDouble(this.d / 32.0D);
        packetdataserializer.writeDouble(this.e / 32.0D);
        packetdataserializer.writeByte(this.i);
        packetdataserializer.writeByte(this.j);
        packetdataserializer.writeByte(this.k);
        packetdataserializer.writeShort(this.f);
        packetdataserializer.writeShort(this.g);
        packetdataserializer.writeShort(this.h);
        DataWatcher.write(packetdataserializer, this.metadata, this.clss);
	}

    public void a(PacketPlayOutListener packetplayoutlistener) {
        packetplayoutlistener.a(this);
    }

    public String b() {
        return String.format("id=%d, type=%d, x=%.2f, y=%.2f, z=%.2f, xd=%.2f, yd=%.2f, zd=%.2f", new Object[] { Integer.valueOf(this.a), Integer.valueOf(this.b), Float.valueOf((float) this.c / 32.0F), Float.valueOf((float) this.d / 32.0F), Float.valueOf((float) this.e / 32.0F), Float.valueOf((float) this.f / 8000.0F), Float.valueOf((float) this.g / 8000.0F), Float.valueOf((float) this.h / 8000.0F)});
    }

    public void handle(PacketListener packetlistener) {
        this.a((PacketPlayOutListener) packetlistener);
    }
}
