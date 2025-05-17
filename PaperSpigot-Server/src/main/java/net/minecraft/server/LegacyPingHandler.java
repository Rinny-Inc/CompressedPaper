package net.minecraft.server;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.buffer.Unpooled;
import net.minecraft.util.io.netty.channel.ChannelFutureListener;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelInboundHandlerAdapter;

public class LegacyPingHandler extends ChannelInboundHandlerAdapter {
    private static final Logger a = LogManager.getLogger();
    private final ServerConnection b;
    private ByteBuf buf; // Paper

    public LegacyPingHandler(ServerConnection var1) {
        this.b = var1;
    }

    public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception {
        final ByteBuf var3 = (ByteBuf)var2;
        // Paper start - Make legacy ping handler more reliable
        if (this.buf != null) {
        	try {
        		readLegacy1_6(var1, var3);
        	} finally {
        		var3.release();
        	}
        	return;
        }
        // Paper end
        var3.markReaderIndex();
        boolean var4 = true;
        try {
            if (var3.readUnsignedByte() == 254) {
                final InetSocketAddress var5 = (InetSocketAddress)var1.channel().remoteAddress();
                final MinecraftServer var6 = this.b.d();
                final int var7 = var3.readableBytes();
                String var8;
                switch(var7) {
                case 0:
                    a.debug("Ping: (<1.3.x) from {}:{}", new Object[]{var5.getAddress(), var5.getPort()});
                    var8 = String.format("%s§%d§%d", var6.getMotd(), var6.C(), var6.D());
                    this.a(var1, this.a(var8));
                    break;
                case 1:
                    if (var3.readUnsignedByte() != 1) {
                        return;
                    }
                    a.debug("Ping: (1.4-1.5.x) from {}:{}", new Object[]{var5.getAddress(), var5.getPort()});
                    var8 = String.format("§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, var6.getVersion(), var6.getMotd(), var6.C(), var6.D());
                    this.a(var1, this.a(var8));
                    break;
                default:
                	// Paper start - Replace with improved version below
                	if (var3.readUnsignedByte() != 0x01 || var3.readUnsignedByte() != 0xFA) return;
                	readLegacy1_6(var1, var3);
                	/*
                    boolean var23 = var3.readUnsignedByte() == 1;
                    var23 &= var3.readUnsignedByte() == 250;
                    var23 &= "MC|PingHost".equals(new String(var3.readBytes(var3.readShort() * 2).array(), Charsets.UTF_16BE));
                    int var9 = var3.readUnsignedShort();
                    var23 &= var3.readUnsignedByte() >= 73;
                    var23 &= 3 + var3.readBytes(var3.readShort() * 2).array().length + 4 == var9;
                    var23 &= var3.readInt() <= 65535;
                    var23 &= var3.readableBytes() == 0;
                    if (!var23) {
                        return;
                    }

                    a.debug("Ping: (1.6) from {}:{}", new Object[]{var5.getAddress(), var5.getPort()});
                    String var10 = String.format("§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, var6.getVersion(), var6.getMotd(), var6.C(), var6.D());
                    ByteBuf var11 = this.a(var10);

                    try {
                        this.a(var1, var11);
                    } finally {
                        var11.release();
                    }
                    */
                	// Paper end
                }

                var3.release();
                var4 = false;
                return;
            }
        } catch (RuntimeException var21) {
            return;
        } finally {
            if (var4) {
                var3.resetReaderIndex();
                var1.channel().pipeline().remove("legacy_query");
                var1.fireChannelRead(var2);
            }

        }

    }
    
	// Paper start
	private static String readLegacyString(ByteBuf buf) {
		final int size = buf.readShort() * Character.BYTES;
		if (!buf.isReadable(size)) {
			return null;
		}
		final String result = buf.toString(buf.readerIndex(), size, StandardCharsets.UTF_16BE);
		buf.skipBytes(size); // toString doesn't increase readerIndex automatically
		return result;
	}
	
	private void readLegacy1_6(ChannelHandlerContext ctx, ByteBuf part) {
		ByteBuf buf = this.buf;
		if (buf == null) {
			this.buf = buf = ctx.alloc().buffer();
			buf.markReaderIndex();
		} else {
			buf.resetReaderIndex();
		}
		buf.writeBytes(part);
		if (!buf.isReadable(Short.BYTES + Short.BYTES + Byte.BYTES + Short.BYTES + Integer.BYTES)) {
			return;
		}
		final String s = readLegacyString(buf);
		if (s == null) {
			return;
		}
		if (!s.equals("MC|PingHost")) {
			removeHandler(ctx);
			return;
		}
		if (!buf.isReadable(Short.BYTES) || !buf.isReadable(buf.readShort())) {
			return;
		}
		final MinecraftServer server = this.b.d();
		//int protocolVersion = buf.readByte();
		final String host = readLegacyString(buf);
		if (host == null) {
			removeHandler(ctx);
			return;
		}
		//int port = buf.readInt();
		if (buf.isReadable()) {
			removeHandler(ctx);
			return;
		}
		buf.release();
		this.buf = null;
		a.debug("Ping: (1.6) from {}", ctx.channel().remoteAddress());
		final String response = String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", Byte.MAX_VALUE, server.getVersion(), server.getMotd(), server.C(), server.D());
		this.a(ctx, this.a(response));
	}
	private void removeHandler(ChannelHandlerContext ctx) {
		final ByteBuf buf = this.buf;
		this.buf = null;
		buf.resetReaderIndex();
		ctx.pipeline().remove(this);
		ctx.fireChannelRead(buf);
	}
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) {
		if (this.buf != null) {
			this.buf.release();
			this.buf = null;
		}
	}
	// Paper end

    private void a(ChannelHandlerContext var1, ByteBuf var2) {
        var1.pipeline().firstContext().writeAndFlush(var2).addListener(ChannelFutureListener.CLOSE);
    }

    private ByteBuf a(String var1) {
    	final ByteBuf var2 = Unpooled.buffer();
        var2.writeByte(255);
        final char[] var3 = var1.toCharArray();
        var2.writeShort(var3.length);
        final char[] var4 = var3;
        final int var5 = var3.length;

        for(int var6 = 0; var6 < var5; ++var6) {
        	final char var7 = var4[var6];
            var2.writeChar(var7);
        }
        return var2;
    }
}