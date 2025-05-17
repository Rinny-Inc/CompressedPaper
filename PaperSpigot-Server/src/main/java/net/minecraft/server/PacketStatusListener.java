package net.minecraft.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
// CraftBukkit start
import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;

import net.minecraft.util.com.mojang.authlib.GameProfile;
// CraftBukkit end
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;

public class PacketStatusListener implements PacketStatusInListener {

    private final MinecraftServer minecraftServer;
    private final NetworkManager networkManager;

    public PacketStatusListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.minecraftServer = minecraftserver;
        this.networkManager = networkmanager;
    }

    public void a(IChatBaseComponent ichatbasecomponent) {}

    public void a(EnumProtocol enumprotocol, EnumProtocol enumprotocol1) {
    	if (enumprotocol1 == EnumProtocol.STATUS /*|| enumprotocol1 == EnumProtocol.HANDSHAKING*/) {
            return;
        }
        throw new UnsupportedOperationException("Unexpected protocol change from " + enumprotocol + " to " + enumprotocol);
    }

    public void a() {}
    
    // PaperSpigot start - Readability for ping MOTD fix backport // Rinny put it as byte
    private static final byte WAITING = 0;
    private static final byte PING = 1;
    private static final byte DONE = 2;
    private byte state = WAITING;
    // PaperSpigot end

    /*public void a(PacketStatusInStart packetstatusinstart) {
        // PaperSpigot start - Backport ping MOTD fix
        if (this.state != WAITING) {
            this.networkManager.close(null);
            return;
        }
        this.state = PING;
        // PaperSpigot end
        // CraftBukkit start - fire ping event
        final Object[] players = minecraftServer.getPlayerList().players.toArray();
        class ServerListPingEvent extends org.bukkit.event.server.ServerListPingEvent {

            ServerListPingEvent() {
                super(((InetSocketAddress) networkManager.getSocketAddress()).getAddress(), minecraftServer.getMotd(), minecraftServer.getPlayerList().getMaxPlayers());
            }

            @Override
            public Iterator<Player> iterator() throws UnsupportedOperationException {
                return new Iterator<Player>() {
                    int i;
                    int ret = Integer.MIN_VALUE;
                    EntityPlayer player;

                    @Override
                    public boolean hasNext() {
                        if (player != null) {
                            return true;
                        }
                        final Object[] currentPlayers = players;
                        for (int length = currentPlayers.length, i = this.i; i < length; i++) {
                            final EntityPlayer player = (EntityPlayer) currentPlayers[i];
                            if (player != null) {
                                this.i = i + 1;
                                this.player = player;
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public Player next() {
                        if (!hasNext()) {
                            throw new java.util.NoSuchElementException();
                        }
                        final EntityPlayer player = this.player;
                        this.player = null;
                        this.ret = this.i - 1;
                        return player.getBukkitEntity();
                    }

                    @Override
                    public void remove() {
                        final Object[] currentPlayers = players;
                        final int i = this.ret;
                        if (i < 0 || currentPlayers[i] == null) {
                            throw new IllegalStateException();
                        }
                        currentPlayers[i] = null;
                    }
                };
            }
        }

        ServerListPingEvent event = new ServerListPingEvent();
        this.minecraftServer.server.getPluginManager().callEvent(event);

        java.util.List<GameProfile> profiles = new java.util.ArrayList<GameProfile>(players.length);
        for (Object player : players) {
            if (player != null) {
                profiles.add(((EntityPlayer) player).getProfile());
            }
        }

        ServerPingPlayerSample playerSample = new ServerPingPlayerSample(event.getMaxPlayers(), profiles.size());
        // Spigot Start
        if ( !profiles.isEmpty() )
        {
            java.util.Collections.shuffle( profiles ); // This sucks, its inefficient but we have no simple way of doing it differently
            profiles = profiles.subList( 0, Math.min( profiles.size(), org.spigotmc.SpigotConfig.playerSample ) ); // Cap the sample to n (or less) displayed players, ie: Vanilla behaviour
        }
        // Spigot End
        playerSample.a(profiles.toArray(new GameProfile[profiles.size()]));

        ServerPing ping = minecraftServer.ay();
        ping.setMOTD(new ChatComponentText(event.getMotd()));
        ping.setPlayerSample(playerSample);
        ping.setServerInfo(new ServerPingServerData(minecraftServer.getServerModName() + " " + minecraftServer.getVersion(), networkManager.getVersion())); // TODO: Update when protocol changes

        this.networkManager.handle(new PacketStatusOutServerInfo(ping), new GenericFutureListener[0]);
        // CraftBukkit end
    }*/
    
    // Rinny start
    @Override
    public void a(PacketStatusInStart packetstatusinstart) {
        if (this.state != WAITING) {
            this.networkManager.close(null);
            return;
        }
        this.state = PING;

        final List<EntityPlayer> players = new ArrayList<>(minecraftServer.getPlayerList().players);

        ServerListPingEvent event = new ServerListPingEvent(
            ((InetSocketAddress) networkManager.getSocketAddress()).getAddress(),
            minecraftServer.getMotd(),
            minecraftServer.getPlayerList().getMaxPlayers()
        ) {
            @Override
            public Iterator<Player> iterator() {
                return players.stream()
                    .filter(player -> player != null)
                    .map(player -> (Player) player.getBukkitEntity())
                    .iterator();
            }
        };

        this.minecraftServer.server.getPluginManager().callEvent(event);

        List<GameProfile> profiles = new ArrayList<>(players.size());
        for (EntityPlayer player : players) {
            if (player != null) {
                profiles.add(player.getProfile());
            }
        }

        ServerPingPlayerSample playerSample = new ServerPingPlayerSample(event.getMaxPlayers(), profiles.size());
        if (!profiles.isEmpty()) {
            profiles = profiles.subList(0, Math.min(profiles.size(), org.spigotmc.SpigotConfig.playerSample));
        }
        playerSample.a(profiles.toArray(new GameProfile[0]));

        ServerPing ping = minecraftServer.ay();
        ping.setMOTD(new ChatComponentText(event.getMotd()));
        ping.setPlayerSample(playerSample);
        ping.setServerInfo(new ServerPingServerData(
            minecraftServer.getServerModName() + " " + minecraftServer.getVersion(),
            networkManager.getVersion()
        ));

        this.networkManager.handle(new PacketStatusOutServerInfo(ping), new GenericFutureListener[0]);
    }
    // Rinny end

    public void a(PacketStatusInPing packetstatusinping) {
        // PaperSpigot start - Backport ping MOTD fix
        if (this.state != PING) {
            this.networkManager.close(null);
            return;
        }
        this.state = DONE;
        // PaperSpigot end
        this.networkManager.handle(new PacketStatusOutPong(packetstatusinping.c()), new GenericFutureListener[0]);
    }
}
