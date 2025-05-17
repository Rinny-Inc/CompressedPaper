package net.minecraft.server;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import net.badlion.gspigot.protocol107.Packets;
import net.minecraft.util.com.google.common.collect.BiMap;
import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.handler.codec.ByteToMessageDecoder;
import net.minecraft.util.io.netty.handler.codec.DecoderException;

public class PacketDecoder extends ByteToMessageDecoder {

	private static final Logger a = LogManager.getLogger();
	private static final Marker b = MarkerManager.getMarker("PACKET_RECEIVED", NetworkManager.b);
	private final NetworkStatistics c;
	private boolean play = false;

	public PacketDecoder(NetworkStatistics networkstatistics) {
		this.c = networkstatistics;
	}

	protected void decode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, List list) throws IOException, DecoderException {
		int i = bytebuf.readableBytes();

		if (i != 0) {
			if (!this.play) {
				this.play = (channelhandlercontext.channel().attr(NetworkManager.d).get() == EnumProtocol.PLAY);
			}
			int version = NetworkManager.getVersion(channelhandlercontext.channel());
			PacketDataSerializer packetdataserializer = new PacketDataSerializer(bytebuf, version);
			final int j = packetdataserializer.a();
			Packet packet = null;
			Class<Packet> clss = null;
			
			if (this.play) {
		        switch (version) {
		          case 107, 108, 109, 110, 210:
		            if (j < Packets.INBOUND.length)
		              clss = Packets.INBOUND[j]; 
		            break;
		          default:
		            packet = Packet.a((BiMap)channelhandlercontext.channel().attr(NetworkManager.e).get(), j);
		            break;
		        } 
			} else {
		        packet = Packet.a((BiMap)channelhandlercontext.channel().attr(NetworkManager.e).get(), j);
			} 
			
			if (clss != null) {
				try {
					packet = clss.newInstance();
				} catch (Exception exception) {
					a.error("Couldn't create packet " + i, exception);
				}  
			}

			if (packet == null)
				throw new IOException("Bad packet id " + j);
			if (this.play) {
				switch (version) {
				case 47:
					packet.read47(packetdataserializer);
					break;
		          case 107, 108, 109, 110, 210:
		        	packet.read107(packetdataserializer);
		            break;
		          default:
		            packet.a(packetdataserializer);
		            break;
		        } 
			} else {
				packet.a(packetdataserializer);
			}
			if (packetdataserializer.readableBytes() > 0) {
				int extraBytes = packetdataserializer.readableBytes();
			    packetdataserializer.skipBytes(extraBytes);
			    throw new IOException("Packet " + packet.toString() +  " was larger than I expected, found " + extraBytes + " bytes extra whilst reading packet " + j);
			}
			list.add(packet);
			this.c.a(j, i);
			if (a.isDebugEnabled())
				a.debug(b, " IN: [{}:{}] {}[{}]", new Object[] { channelhandlercontext.channel().attr(NetworkManager.d).get(), Integer.valueOf(j), packet.getClass().getName(), packet.b() });
		}
	}
}
