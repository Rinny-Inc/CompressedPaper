package net.minecraft.server;

import net.minecraft.util.org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class PacketPlayInTabComplete extends Packet {

	private String a;

	public PacketPlayInTabComplete() {
	}

	public PacketPlayInTabComplete(String s) {
		this.a = s;
	}

	public void a(PacketDataSerializer packetdataserializer) throws IOException {
		this.a = packetdataserializer.c(32767);
	}

	public void read47(PacketDataSerializer packetdataserializer) throws IOException {
		this.a = packetdataserializer.c(32767);
		if (packetdataserializer.readBoolean()) {
			packetdataserializer.readLong(); 
		}
    }
	
	public void read107(PacketDataSerializer packetdataserializer) throws IOException {
	    this.a = packetdataserializer.c(32767);
	    packetdataserializer.readBoolean();
	    if (packetdataserializer.readBoolean())
	      packetdataserializer.readLong(); 
	}

	public void b(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.a(StringUtils.substring(this.a, 0, 32767));
	}

	private static final java.util.concurrent.ExecutorService executors = java.util.concurrent.Executors
			.newCachedThreadPool(new com.google.common.util.concurrent.ThreadFactoryBuilder().setDaemon(true)
					.setNameFormat("Async TabComplete Thread - #%d").build());

	public void a(PacketPlayInListener packetplayinlistener) {
		executors.submit(new Runnable() {

			@Override
			public void run() {
				packetplayinlistener.a(PacketPlayInTabComplete.this);
			}
		});
	}
	/*
	 * public void a(PacketPlayInListener packetplayinlistener) {
	 * packetplayinlistener.a(this); }
	 */

	public String c() {
		return this.a;
	}

	public String b() {
		return String.format("message=\'%s\'", new Object[] { this.a });
	}

	public void handle(PacketListener packetlistener) {
		this.a((PacketPlayInListener) packetlistener);
	}
}
