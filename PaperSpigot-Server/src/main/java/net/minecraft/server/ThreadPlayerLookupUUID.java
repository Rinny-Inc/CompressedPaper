package net.minecraft.server;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
// CraftBukkit end

import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.exceptions.AuthenticationUnavailableException;

class ThreadPlayerLookupUUID implements Runnable {
    final LoginListener a;
    
    ThreadPlayerLookupUUID(LoginListener loginlistener) {
        this.a = loginlistener;
    }

    public void run() {
        GameProfile gameprofile = LoginListener.a(this.a);

        try {
            // Spigot Start
            if ( !LoginListener.c( this.a ).getOnlineMode() )
            {
                a.initUUID();
                fireLoginEvent();
                return;
            }
            // Spigot End
            String hash = (new BigInteger(MinecraftEncryption.a(LoginListener.b(this.a), LoginListener.c(this.a).K().getPublic(), LoginListener.d(this.a)))).toString(16);

            LoginListener.a(this.a, LoginListener.c(this.a).av().hasJoinedServer(new GameProfile((UUID) null, gameprofile.getName()), hash));
            if (LoginListener.a(this.a) != null) {
                fireLoginEvent(); // Spigot
            } else if (LoginListener.c(this.a).N()) {
                LoginListener.e().warn("Failed to verify username but will let them in anyway!");
                LoginListener.a(this.a, this.a.a(gameprofile));
                LoginListener.a(this.a, EnumProtocolState.READY_TO_ACCEPT);
            } else {
                this.a.disconnect("Failed to verify username!");
                LoginListener.e().error("Username \'" + gameprofile.getName() + "\' tried to join with an invalid session");
            }
        } catch (AuthenticationUnavailableException authenticationunavailableexception) {
            if (LoginListener.c(this.a).N()) {
                LoginListener.e().warn("Authentication servers are down but will let them in anyway!");
                LoginListener.a(this.a, this.a.a(gameprofile));
                LoginListener.a(this.a, EnumProtocolState.READY_TO_ACCEPT);
            } else {
                this.a.disconnect("Authentication servers are down. Please try again later, sorry!");
                LoginListener.e().error("Couldn\'t verify username because servers are unavailable");
            }
            // CraftBukkit start - catch all exceptions
        } catch (Exception exception) {
            this.a.disconnect("Failed to verify username!");
            LoginListener.c(this.a).server.getLogger().log(java.util.logging.Level.WARNING, "Exception verifying " + gameprofile.getName(), exception);
            // CraftBukkit end
        }
    }

    private void fireLoginEvent() throws Exception {
        // CraftBukkit start - fire PlayerPreLoginEvent
        if (!this.a.networkManager.isConnected()) {
            return;
        }

        final String playerName = LoginListener.a(this.a).getName();
        final InetAddress address = ((InetSocketAddress) a.networkManager.getSocketAddress()).getAddress();
        final UUID uniqueId = LoginListener.a(this.a).getId();
        final org.bukkit.craftbukkit.CraftServer server = LoginListener.c(this.a).server;

        final AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
        server.getPluginManager().callEvent(asyncEvent);

        if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            this.a.disconnect(asyncEvent.getKickMessage());
            return;
        }
        // CraftBukkit end

        LoginListener.e().info("UUID of player " + playerName + " is " + uniqueId);
        LoginListener.a(this.a, EnumProtocolState.READY_TO_ACCEPT);
    }
}
