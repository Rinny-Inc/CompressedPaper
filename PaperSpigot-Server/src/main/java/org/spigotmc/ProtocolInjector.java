package org.spigotmc;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.ChatSerializer;
import net.minecraft.server.EnumProtocol;
import net.minecraft.server.IChatBaseComponent;
import net.minecraft.server.Packet;
import net.minecraft.server.PacketDataSerializer;
import net.minecraft.server.PacketListener;
import net.minecraft.server.PacketPlayOutListener;
import net.minecraft.util.com.google.common.collect.BiMap;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public class ProtocolInjector
{
    public static void inject()
    {
        try
        {
            addPacket( EnumProtocol.LOGIN, true, 0x3, PacketLoginCompression.class );

            addPacket( EnumProtocol.PLAY, true, 0x45, PacketTitle.class );
            addPacket( EnumProtocol.PLAY, true, 0x47, PacketTabHeaderFooter.class );
            addPacket( EnumProtocol.PLAY, true, 0x48, PacketPlayResourcePackSend.class );
            addPacket( EnumProtocol.PLAY, false, 0x19, PacketPlayResourcePackStatus.class );
        } catch ( NoSuchFieldException e )
        {
            e.printStackTrace();
        } catch ( IllegalAccessException e )
        {
            e.printStackTrace();
        }
    }

    private static void addPacket(EnumProtocol protocol, boolean clientbound, int id, Class<? extends Packet> packet) throws NoSuchFieldException, IllegalAccessException
    {
        Field packets = EnumProtocol.class.getDeclaredField( !clientbound ? "h" : "i" );
        packets.setAccessible( true );
        final BiMap<Integer, Class<? extends Packet>> pMap = (BiMap<Integer, Class<? extends Packet>>) packets.get( protocol );
        pMap.put( id, packet );
        final Field map = EnumProtocol.class.getDeclaredField( "f" );
        map.setAccessible( true );
        final Map<Class<? extends Packet>, EnumProtocol> protocolMap = (Map<Class<? extends Packet>, EnumProtocol>) map.get( null );
        protocolMap.put( packet, protocol );
    }

    public static class PacketPlayResourcePackStatus extends Packet { // TODO: WHAT IS THIS FOR???

        @Override
        public void a(PacketDataSerializer packetdataserializer) throws IOException
        {
            packetdataserializer.c( 255 ); // Hash
            packetdataserializer.a(); // Result
        }

        @Override
        public void b(PacketDataSerializer packetdataserializer) throws IOException
        {

        }

        @Override
        public void handle(PacketListener packetlistener)
        {

        }
    }

    public static class PacketPlayResourcePackSend extends Packet { // TODO: HANDLE THE PACKET ALREADY BEEN DID LIKE 1.8

        private String url;
        private String hash;

        public PacketPlayResourcePackSend(String url, String hash)
        {
            this.url = url;
            this.hash = hash;
            if (hash.length() > 40) {
            	throw new IllegalArgumentException("Hash is too long (max 40, was " + hash.length() + ")");
            }
        }

        @Override
        public void a(PacketDataSerializer packetdataserializer) throws IOException
        {
        	this.url = packetdataserializer.c( 32767 );
        	this.hash = packetdataserializer.c( 40 );
        }

        @Override
        public void b(PacketDataSerializer packetdataserializer) throws IOException
        {
            packetdataserializer.a( url );
            packetdataserializer.a( hash );
        }

        @Override
        public void handle(PacketListener packetlistener)
        {

        }
    }

    public static class PacketLoginCompression extends Packet { // TODO: WHAT IS THIS FOR???
 
        private int threshold;

        public PacketLoginCompression(int threshold)
        {
            this.threshold = threshold;
        }

        @Override
        public void a(PacketDataSerializer packetdataserializer) throws IOException
        {

        }

        @Override
        public void b(PacketDataSerializer packetdataserializer) throws IOException
        {
            packetdataserializer.b( threshold );
        }

        @Override
        public void handle(PacketListener packetlistener)
        {

        }
    }

    public static class PacketTabHeaderFooter extends Packet
    {
    	public BaseComponent[] header;
    	public BaseComponent[] footer;
    	private IChatBaseComponent a;
    	private IChatBaseComponent b;

        public PacketTabHeaderFooter()
        {
        }

        public PacketTabHeaderFooter(IChatBaseComponent header, IChatBaseComponent footer)
        {
            this.a = header;
            this.b = footer;
        }

        @Override
        public void a(PacketDataSerializer packetdataserializer) throws IOException
        {
            this.a = ChatSerializer.a( packetdataserializer.c( 32767 ) );
            this.b = ChatSerializer.a( packetdataserializer.c( 32767 ) );
        }

        @Override
        public void b(PacketDataSerializer packetdataserializer) throws IOException
        {
        	if (header != null) {
        		packetdataserializer.a(ComponentSerializer.toString(this.header));
        	} else {
        		packetdataserializer.a(ChatSerializer.a(this.a));
        	}
        	if (footer != null) {
        		packetdataserializer.a(ComponentSerializer.toString(this.footer));
        	} else {
        		packetdataserializer.a(ChatSerializer.a(this.b));
        	}
        }	
        
        public void a(PacketPlayOutListener packetplayoutlistener) {
            packetplayoutlistener.a(this);
        }

        @Override
        public void handle(PacketListener packetlistener)
        {
        	this.a((PacketPlayOutListener) packetlistener);
        }
    }

    public static class PacketTitle extends Packet
    {
        private EnumTitleAction action;

        // TITLE & SUBTITLE
        private IChatBaseComponent text;

        // TIMES
        private int fadeIn = -1;
        private int stay = -1;
        private int fadeOut = -1;
        
        public BaseComponent[] components;

        public PacketTitle() {}
        
        public PacketTitle(EnumTitleAction action, BaseComponent[] components, int fadeIn, int stay, int fadeOut) {
            this.action = action;
            this.components = components;
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }

        public PacketTitle(EnumTitleAction action, IChatBaseComponent text)
        {
            this(action, text, -1, -1, -1);
        }
        
        public PacketTitle(int i, int j, int k) {
        	this(EnumTitleAction.TIMES, (IChatBaseComponent)null, i, j, k);
        }
        
        public PacketTitle(EnumTitleAction action, IChatBaseComponent ichatbasecomponent, int i, int j, int k) {
        	this.action = action;
            this.text = ichatbasecomponent;
            this.fadeIn = i;
            this.stay = j;
            this.fadeOut = k;
        }

        @Override
        public void a(PacketDataSerializer packetdataserializer) throws IOException
        {
            this.action = EnumTitleAction.values()[packetdataserializer.a()];
            if (this.action == EnumTitleAction.TITLE || this.action == EnumTitleAction.SUBTITLE) {
            	this.text = ChatSerializer.a( packetdataserializer.c(32767) );
            }
            if (this.action == EnumTitleAction.TIMES) {
            	this.fadeIn = packetdataserializer.readInt();
                this.stay = packetdataserializer.readInt();
                this.fadeOut = packetdataserializer.readInt();
            }
        }

        @Override
        public void b(PacketDataSerializer packetdataserializer) throws IOException
        {
            packetdataserializer.b( action.ordinal() );
            if (this.action == EnumTitleAction.TITLE || this.action == EnumTitleAction.SUBTITLE) {
            	if (this.components != null) {
            		packetdataserializer.a(ComponentSerializer.toString(this.components));
            	} else {
            		packetdataserializer.a(ChatSerializer.a(this.text));
            	}
            }
            if (this.action == EnumTitleAction.TIMES) {
            	packetdataserializer.writeInt(this.fadeIn);
            	packetdataserializer.writeInt(this.stay);
            	packetdataserializer.writeInt(this.fadeOut);
            }
        }

        public void a(PacketPlayOutListener packetplayoutlistener) {
            packetplayoutlistener.a(this);
        }

        @Override
        public void handle(PacketListener packetlistener)
        {
        	this.a((PacketPlayOutListener) packetlistener);
        }

        public static enum EnumTitleAction {
            TITLE,
            SUBTITLE,
            TIMES,
            CLEAR,
            RESET;
        }
    }
}
