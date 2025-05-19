package net.minecraft.server;

// CraftBukkit start
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.spigotmc.SpigotConfig;

import io.noks.utils.PaperNetworkClient;
import net.minecraft.util.com.mojang.authlib.properties.Property; // Spigot
import net.minecraft.util.com.mojang.util.UUIDTypeAdapter;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;
// CraftBukkit end

public class HandshakeListener implements PacketHandshakingInListener {
    private static final com.google.gson.Gson gson = new com.google.gson.Gson(); // Spigot
    // CraftBukkit start - add fields
    private static final Map<InetAddress, Long> throttleTracker = Collections.synchronizedMap(new HashMap<>()); // Rinny
    private static int throttleCounter = 0;
    // CraftBukkit end

    private final MinecraftServer a;
    private final NetworkManager networkmanager;

    public HandshakeListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.a = minecraftserver;
        this.networkmanager = networkmanager;
    }

    public void a(PacketHandshakingInSetProtocol packethandshakinginsetprotocol) {
        // Spigot start
        if ( NetworkManager.SUPPORTED_VERSIONS.contains( packethandshakinginsetprotocol.d() ) )
        {
            NetworkManager.a( this.networkmanager ).attr( NetworkManager.protocolVersion ).set( packethandshakinginsetprotocol.d() );
        }
        // Spigot end
        switch (packethandshakinginsetprotocol.c()) {
        case LOGIN:
            this.networkmanager.a(packethandshakinginsetprotocol.c());

            // CraftBukkit start - Connection throttle
            try {
                final long currentTime = System.currentTimeMillis();
                final long connectionThrottle = MinecraftServer.getServer().server.getConnectionThrottle();
                final InetAddress address = ((java.net.InetSocketAddress) this.networkmanager.getSocketAddress()).getAddress();

                throttleTracker.computeIfPresent(address, (_, lastConnection) -> {
                    if (!"127.0.0.1".equals(address.getHostAddress()) && (currentTime - lastConnection) < connectionThrottle) {
                    	ChatComponentText chatcomponenttext = new ChatComponentText("Connection throttled! Please wait before reconnecting.");
                        this.networkmanager.handle(new PacketLoginOutDisconnect(chatcomponenttext), new GenericFutureListener[0]);
                        this.networkmanager.close(chatcomponenttext);
                        return lastConnection;
                    }
                    return currentTime;
                });
                /*if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - throttleTracker.get(address) < connectionThrottle) {
                    throttleTracker.put(address, currentTime);
                    chatcomponenttext = new ChatComponentText("Connection throttled! Please wait before reconnecting.");
                    this.networkmanager.handle(new PacketLoginOutDisconnect(chatcomponenttext), new GenericFutureListener[0]);
                    this.networkmanager.close(chatcomponenttext);
                    return;
                }*/
                
                if (throttleTracker.containsKey(address)) {
                	throttleTracker.put(address, currentTime);
                }
                //throttleCounter++;
                if (++throttleCounter > 200) {
                    throttleCounter = 0;

                    // Cleanup stale entries
                    // MOJANG
                    /*final java.util.Iterator<Map.Entry<InetAddress, Long>> iter = throttleTracker.entrySet().iterator();
                    while (iter.hasNext()) {
                        java.util.Map.Entry<InetAddress, Long> entry = iter.next();
                        if (entry.getValue() > connectionThrottle) {
                            iter.remove();
                        }
                    }*/
                    // Rinny
                    throttleTracker.values().removeIf(value -> (currentTime - value) > connectionThrottle);
                }
            } catch (Throwable t) {
                org.apache.logging.log4j.LogManager.getLogger().debug("Failed to check connection throttle", t);
            }
            // CraftBukkit end

            if (packethandshakinginsetprotocol.d() > 110) {
                ChatComponentText chatcomponenttext = new ChatComponentText(SpigotConfig.outdatedServerMessage);
                this.networkmanager.handle(new PacketLoginOutDisconnect(chatcomponenttext), new net.minecraft.util.io.netty.util.concurrent.GenericFutureListener[0]);
                this.networkmanager.close(chatcomponenttext);
            } else if (packethandshakinginsetprotocol.d() < 4) {
                ChatComponentText chatcomponenttext = new ChatComponentText(SpigotConfig.outdatedClientMessage);
                this.networkmanager.handle(new PacketLoginOutDisconnect(chatcomponenttext), new net.minecraft.util.io.netty.util.concurrent.GenericFutureListener[0]);
                this.networkmanager.close(chatcomponenttext);
            } else if (!NetworkManager.SUPPORTED_VERSIONS.contains(Integer.valueOf(packethandshakinginsetprotocol.d()))) {
                ChatComponentText chatcomponenttext = new ChatComponentText("Please connect with minecraft version 1.7, 1.8 or 1.9");
                this.networkmanager.handle(new PacketLoginOutDisconnect(chatcomponenttext), new net.minecraft.util.io.netty.util.concurrent.GenericFutureListener[0]);
                this.networkmanager.close(chatcomponenttext);
            } else {
                this.networkmanager.a((PacketListener) (new LoginListener(this.a, this.networkmanager)));
                final String ip = ((InetSocketAddress)this.networkmanager.getSocketAddress()).getAddress().getHostAddress();
                final boolean proxyLogicEnabled = (SpigotConfig.bungee && SpigotConfig.bungeeAddresses.contains(ip));
                
                if (proxyLogicEnabled) {
                	this.networkmanager.isProxied = true;
                	String[] split = packethandshakinginsetprotocol.b.split("\00"); // \000h
                	if (split.length == 6 && "FML".equals(split[1])) {
                		split = new String[] { split[0], split[3], split[4], split[5] };
                	}
                	if (split.length == 3 || split.length == 4) {
                		packethandshakinginsetprotocol.b = split[0];
                		this.networkmanager.n = new InetSocketAddress(split[1], ((InetSocketAddress)this.networkmanager.getSocketAddress()).getPort());
                		this.networkmanager.spoofedUUID = UUIDTypeAdapter.fromString(split[2]);
                	} 
                	if (split.length == 4) {
                		networkmanager.spoofedProfile = gson.fromJson(split[3], Property[].class);
                	}
                }
                // Spigot End
                ((LoginListener) this.networkmanager.getPacketListener()).hostname = packethandshakinginsetprotocol.b + ":" + packethandshakinginsetprotocol.c; // CraftBukkit - set hostname
            }
            break;

        case STATUS:
            this.networkmanager.a(packethandshakinginsetprotocol.c());
            this.networkmanager.a(new PacketStatusListener(this.a, this.networkmanager));
            break;

        default:
            throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.c());
        }
        this.networkmanager.protocolVersions = packethandshakinginsetprotocol.getProtocolVersion();
        this.networkmanager.virtualHost = PaperNetworkClient.prepareVirtualHost(packethandshakinginsetprotocol.b, packethandshakinginsetprotocol.c);
    }

    public void a(IChatBaseComponent ichatbasecomponent) {}

    public void a(EnumProtocol enumprotocol, EnumProtocol enumprotocol1) {
        if (enumprotocol1 != EnumProtocol.LOGIN && enumprotocol1 != EnumProtocol.STATUS) {
            throw new UnsupportedOperationException("Invalid state " + enumprotocol1);
        }
    }

    public void a() {}
}
