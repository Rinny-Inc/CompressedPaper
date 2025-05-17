package net.minecraft.server;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.google.common.base.Charsets;

import net.badlion.gspigot.MultiPacket;
import net.minecraft.util.com.google.common.collect.BiMap;
import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.buffer.Unpooled;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.handler.codec.EncoderException;
import net.minecraft.util.io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder {

	private static final Logger a = LogManager.getLogger();
	private static final Marker b = MarkerManager.getMarker("PACKET_SENT", NetworkManager.b);
	private final NetworkStatistics c;
	private boolean play = false;

	public PacketEncoder(NetworkStatistics networkstatistics) {
		this.c = networkstatistics;
	}

	protected void a(ChannelHandlerContext channelhandlercontext, Packet packet, ByteBuf bytebuf) throws IOException, EncoderException {
		Integer integer;
		if (!this.play) {
			this.play = (channelhandlercontext.channel().attr(NetworkManager.d).get() == EnumProtocol.PLAY);
		}
		int version = NetworkManager.getVersion(channelhandlercontext.channel());
		if (this.play) {
			integer = switch (version) {
				case 107, 108, 109 -> net.badlion.gspigot.protocol107.Packets.OUTBOUND.get(packet.getClass());
				case 110, 210 -> net.badlion.gspigot.protocol110.Packets.OUTBOUND.get(packet.getClass());
				default -> (Integer)((BiMap)channelhandlercontext.channel().attr(NetworkManager.f).get()).inverse().get(packet.getClass());
			};
		} else {
			integer = (Integer)((BiMap)channelhandlercontext.channel().attr(NetworkManager.f).get()).inverse().get(packet.getClass());
		} 
		if (a.isDebugEnabled())
			a.debug(b, "OUT: [{}:{}] {}[{}]",
					new Object[] { channelhandlercontext.channel().attr(NetworkManager.d).get(), integer,
							packet.getClass().getName(), packet.b() });
		if (integer == null)
			throw new IOException("Can't serialize unregistered packet");
		PacketDataSerializer packetdataserializer = new PacketDataSerializer(bytebuf, version);
		packetdataserializer.b(integer.intValue());
		if (this.play) {
			switch (version) {
			case 47:
				packet.write47(packetdataserializer);
				break;
			case 107:
				packet.write107(packetdataserializer);
				break;
			case 108, 109:
				packet.write108(packetdataserializer);
				break;
			case 110:
				packet.write110(packetdataserializer);
				break;
			case 210:
				packet.write210(packetdataserializer);
				break;
			default:
				packet.b(packetdataserializer);
				break;
			}
		} else {
			packet.b(packetdataserializer);
		}
		this.c.b(integer.intValue(), packetdataserializer.readableBytes());
	}

	private static final byte[] BUNGEECORD_SEND_RAW_SUBCHANNEL = "SendRaw".getBytes(Charsets.UTF_8);

	protected void encode(ChannelHandlerContext channelhandlercontext, Object object, ByteBuf bytebuf) throws IOException {
		if (object instanceof MultiPacket) {
			PacketDataSerializer payload = new PacketDataSerializer(Unpooled.buffer());
			payload.writeShort(BUNGEECORD_SEND_RAW_SUBCHANNEL.length);
			payload.writeBytes(BUNGEECORD_SEND_RAW_SUBCHANNEL);
			int count = 0;
			for (Packet packet : ((MultiPacket) object).getPackets()) {
				if (packet != null) {
					PacketDataSerializer packetBytes = new PacketDataSerializer(Unpooled.buffer());
					a(channelhandlercontext, packet, packetBytes);
					payload.b(packetBytes.readableBytes());
					payload.writeBytes(packetBytes);
					packetBytes.release();
					count++;
				}
			}
			if (count > 0) {
				byte[] payloadArray = new byte[payload.readableBytes()];
				payload.readBytes(payloadArray);
				PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("BungeeCord", payloadArray);
				a(channelhandlercontext, packet, bytebuf);
			}
			payload.release();
		} else {
			a(channelhandlercontext, (Packet) object, bytebuf);
		}
	}
}
