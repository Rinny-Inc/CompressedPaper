package net.minecraft.server;

import java.io.IOException;

public class PacketPlayOutEntityMetadata extends Packet {
	public int a;
	public Class<? extends Entity> clss;
	public Object[] metadata;
	  
	public PacketPlayOutEntityMetadata() {}
	  
	public PacketPlayOutEntityMetadata(Entity entity, boolean flag) {
		this.a = entity.getId();
		this.clss = (Class)entity.getClass();
		this.metadata = new Object[32];
		for (int i = 0; i < 32; i++) {
			if (flag || (entity.getDataWatcher()).dirtyMap[i])
				this.metadata[i] = (entity.getDataWatcher()).data[i]; 
		} 
	}
	  
	public void obfuscateHealth() {
		if (this.metadata[6] instanceof Float && ((Float)this.metadata[6]).floatValue() > 0.0F)
			this.metadata[6] = Float.valueOf(1.0F); 
	}
	  
	public void a(PacketDataSerializer packetdataserializer) {}
	  
	public void b(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writeInt(this.a);
		DataWatcher.write(packetdataserializer, this.metadata, this.clss);
	}
	  
	public void write47(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.b(this.a);
		DataWatcher.write(packetdataserializer, this.metadata, this.clss);
	}
	  
	public void a(PacketPlayOutListener packetplayoutlistener) {
		packetplayoutlistener.a(this);
	}
	  
	public void handle(PacketListener packetlistener) {
		a((PacketPlayOutListener)packetlistener);
	}
}
