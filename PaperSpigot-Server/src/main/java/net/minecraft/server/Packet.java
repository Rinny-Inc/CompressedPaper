package net.minecraft.server;

import java.io.IOException;

import net.minecraft.util.com.google.common.collect.BiMap;
import net.minecraft.util.io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Packet {
	private static final Logger a = LogManager.getLogger();
	public final long timestamp = System.currentTimeMillis(); // CraftBukkit

	public Packet() {
	}

	public static Packet a(BiMap bimap, int i) {
		try {
			final Class oclass = (Class) bimap.get(Integer.valueOf(i));
			return oclass == null ? null : (Packet) oclass.newInstance();
		} catch (Exception exception) {
			a.error("Couldn\'t create packet " + i, exception);
			return null;
		}
	}

	public static void a(ByteBuf bytebuf, byte[] abyte) {
		// Spigot start - protocol patch
		if (bytebuf instanceof PacketDataSerializer packetDataSerializer) { // Rinny
			if (packetDataSerializer.version >= 20) {
				packetDataSerializer.b(abyte.length);
			} else {
				bytebuf.writeShort(abyte.length);
			}
		} else {
			bytebuf.writeShort(abyte.length);
		}
		// Spigot end
		bytebuf.writeBytes(abyte);
	}

	public static byte[] a(ByteBuf bytebuf) throws IOException {
		return readByteArray(bytebuf, 32767);
	}

	public static byte[] readByteArray(ByteBuf bytebuf, int limit) throws IOException {
		short short1 = 0;
		if (bytebuf instanceof PacketDataSerializer packetDataSerializer) { // Rinny
			if (packetDataSerializer.version >= 20) {
				short1 = (short) packetDataSerializer.a();
			} else {
				short1 = bytebuf.readShort();
			}
		} else {
			short1 = bytebuf.readShort();
		}
		if (short1 > limit)
			throw new IOException("The received a byte array longer than allowed " + short1 + " > " + limit);
		if (short1 < 0)
			throw new IOException("Key was smaller than nothing!  Weird key!");
		byte[] abyte = new byte[short1];
		bytebuf.readBytes(abyte);
		return abyte;
	}

	public abstract void a(PacketDataSerializer packetdataserializer) throws IOException; // CraftBukkit - added throws

	public abstract void b(PacketDataSerializer packetdataserializer) throws IOException; // CraftBukkit - added throws

	public abstract void handle(PacketListener packetlistener);

	public boolean a() {
		return false;
	}

	public String toString() {
		return this.getClass().getSimpleName();
	}

	public String b() {
		return "";
	}

	public void read47(PacketDataSerializer packetdataserializer) throws IOException {
		a(packetdataserializer);
	}

	public void write47(PacketDataSerializer packetdataserializer) throws IOException {
		b(packetdataserializer);
	}
	
	public void read107(PacketDataSerializer packetdataserializer) throws IOException {
		read47(packetdataserializer);
	}
	  
	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
		write47(packetdataserializer);
	}
	  
	public void write108(PacketDataSerializer packetdataserializer) throws IOException {
		write107(packetdataserializer);
	}
	
	public void write110(PacketDataSerializer packetdataserializer) throws IOException {
		write108(packetdataserializer);
	}
	  
	public void write210(PacketDataSerializer packetdataserializer) throws IOException {
		write110(packetdataserializer);
	}
}
