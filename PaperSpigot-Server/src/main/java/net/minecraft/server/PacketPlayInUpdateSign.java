package net.minecraft.server;

import java.io.IOException;

public class PacketPlayInUpdateSign extends Packet {

	private int a;
	private int b;
	private int c;
	private String[] d;

	public PacketPlayInUpdateSign() {
	}

	public void a(PacketDataSerializer packetdataserializer) throws IOException {
		this.a = packetdataserializer.readInt();
		this.b = packetdataserializer.readShort();
		this.c = packetdataserializer.readInt();
		this.d = new String[4];
		for (int i = 0; i < 4; i++) {
			this.d[i] = packetdataserializer.c(15);
			if (this.d[i].length() > 15)
				this.d[i] = this.d[i].substring(0, 15);
		}
	}

	public void read47(PacketDataSerializer packetdataserializer) throws IOException {
		long position = packetdataserializer.readLong();
		this.a = packetdataserializer.readPositionX(position);
		this.b = packetdataserializer.readPositionY(position);
		this.c = packetdataserializer.readPositionZ(position);
		this.d = new String[4];
		for (int i = 0; i < 4; i++) {
			this.d[i] = ChatSerializer.a(packetdataserializer.c(Short.MAX_VALUE)).c();
			if (this.d[i].length() > 15)
				this.d[i] = this.d[i].substring(0, 15);
		}
	}
	
	public void read107(PacketDataSerializer packetdataserializer) throws IOException {
	    long position = packetdataserializer.readLong();
	    this.a = packetdataserializer.readPositionX(position);
	    this.b = packetdataserializer.readPositionY(position);
	    this.c = packetdataserializer.readPositionZ(position);
	    this.d = new String[4];
	    for (int i = 0; i < 4; i++) {
	      this.d[i] = packetdataserializer.c(384);
	      if (this.d[i].length() > 15)
	        this.d[i] = this.d[i].substring(0, 15); 
	    } 
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writeInt(this.a);
		packetdataserializer.writeShort(this.b);
		packetdataserializer.writeInt(this.c);

		for (int i = 0; i < 4; ++i) {
			packetdataserializer.a(this.d[i]);
		}
	}

	public void a(PacketPlayInListener packetplayinlistener) {
		packetplayinlistener.a(this);
	}

	public int c() {
		return this.a;
	}

	public int d() {
		return this.b;
	}

	public int e() {
		return this.c;
	}

	public String[] f() {
		return this.d;
	}

	public void handle(PacketListener packetlistener) {
		this.a((PacketPlayInListener) packetlistener);
	}
}
