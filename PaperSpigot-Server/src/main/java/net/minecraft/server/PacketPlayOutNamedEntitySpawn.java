package net.minecraft.server;

import java.io.IOException; // CraftBukkit
import java.util.Iterator;
import java.util.UUID;

import org.spigotmc.SpigotDebreakifier;

import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;

public class PacketPlayOutNamedEntitySpawn extends Packet {

    private int a;
    private GameProfile b;
    private int c;
    private int d;
    private int e;
    private byte f;
    private byte g;
    private int h;
    public Object[] metadata;

    public PacketPlayOutNamedEntitySpawn() {}

    public PacketPlayOutNamedEntitySpawn(EntityHuman entityhuman) {
        this.a = entityhuman.getId();
        this.b = entityhuman.getProfile();
        this.c = MathHelper.floor(entityhuman.locX * 32.0D);
        this.d = MathHelper.floor(entityhuman.locY * 32.0D);
        this.e = MathHelper.floor(entityhuman.locZ * 32.0D);
        this.f = (byte) ((int) (entityhuman.yaw * 256.0F / 360.0F));
        this.g = (byte) ((int) (entityhuman.pitch * 256.0F / 360.0F));
        ItemStack itemstack = entityhuman.inventory.getItemInHand();

        this.h = itemstack == null ? 0 : Item.getId(itemstack.getItem());
        this.metadata = new Object[32];
        System.arraycopy((entityhuman.getDataWatcher()).data, 0, this.metadata, 0, 32);
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException { }

    public void b(PacketDataSerializer packetdataserializer) throws IOException { // CraftBukkit - added throws
    	packetdataserializer.b(this.a);
        UUID uuid = this.b.getId();
        packetdataserializer.a((uuid == null) ? "" : ((packetdataserializer.version >= 5) ? uuid.toString() : uuid.toString().replaceAll("-", "")));
        packetdataserializer.a((this.b.getName().length() > 16) ? this.b.getName().substring(0, 16) : this.b.getName());
        if (packetdataserializer.version >= 5) {
          packetdataserializer.b(this.b.getProperties().size());
          Iterator<Property> iterator = this.b.getProperties().values().iterator();
          while (iterator.hasNext()) {
            Property property = iterator.next();
            packetdataserializer.a(property.getName());
            packetdataserializer.a(property.getValue());
            packetdataserializer.a(property.getSignature());
          } 
        } 
        packetdataserializer.writeInt(this.c);
        packetdataserializer.writeInt(this.d);
        packetdataserializer.writeInt(this.e);
        packetdataserializer.writeByte(this.f);
        packetdataserializer.writeByte(this.g);
        packetdataserializer.writeShort(this.h);
        DataWatcher.write(packetdataserializer, this.metadata, (Class)EntityHuman.class);
    }
    
    public void write47(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.b(this.a);
        packetdataserializer.writeUUID(this.b.getId());
        packetdataserializer.writeInt(this.c);
        packetdataserializer.writeInt(this.d);
        packetdataserializer.writeInt(this.e);
        packetdataserializer.writeByte(this.f);
        packetdataserializer.writeByte(this.g);
        packetdataserializer.writeShort(SpigotDebreakifier.getItemId(this.h));
        DataWatcher.write(packetdataserializer, this.metadata, (Class)EntityHuman.class);
	}
    
	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.b(this.a);
        packetdataserializer.writeUUID(this.b.getId());
        packetdataserializer.writeDouble(this.c / 32.0D);
        packetdataserializer.writeDouble(this.d / 32.0D);
        packetdataserializer.writeDouble(this.e / 32.0D);
        packetdataserializer.writeByte(this.f);
        packetdataserializer.writeByte(this.g);
        DataWatcher.write(packetdataserializer, this.metadata, (Class)EntityHuman.class);
	}

    public void a(PacketPlayOutListener packetplayoutlistener) {
        packetplayoutlistener.a(this);
    }

    public String b() {
        return String.format("id=%d, gameProfile=\'%s\', x=%.2f, y=%.2f, z=%.2f, carried=%d", new Object[] { Integer.valueOf(this.a), this.b, Float.valueOf((float) this.c / 32.0F), Float.valueOf((float) this.d / 32.0F), Float.valueOf((float) this.e / 32.0F), Integer.valueOf(this.h)});
    }

    public void handle(PacketListener packetlistener) {
        this.a((PacketPlayOutListener) packetlistener);
    }
}
