package net.minecraft.server;

import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
// CraftBukkit start
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.chunkio.ChunkIOExecutor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import com.avaje.ebean.validation.NotNull;

import net.minecraft.server.PacketPlayOutPlayerInfo.PlayerInfo;
import net.minecraft.util.com.google.common.base.Charsets;
import net.minecraft.util.com.google.common.collect.Maps;
import net.minecraft.util.com.mojang.authlib.GameProfile;

public abstract class PlayerList {
    private final Logger g = LogManager.getLogger(); // Rinny - private static > private
    private final MinecraftServer server;
    public final @NotNull List<EntityPlayer> players = new CopyOnWriteArrayList<>(); // CraftBukkit - ArrayList -> CopyOnWriteArrayList: Iterator safety
    // PaperSpigot start - Player lookup improvements
    public final @NotNull Map<String, EntityPlayer> playerMap;
    public final @NotNull Map<UUID, EntityPlayer> uuidMap;
    private final @NotNull Map<UUID, ServerStatisticManager> statisticMap;
    // PaperSpigot end
    private final GameProfileBanList j;
    private final IpBanList k;
    private final OpList operators;
    private final WhiteList whitelist;
    public IPlayerFileData playerFileData; // CraftBukkit - private -> public
    public boolean hasWhitelist; // CraftBukkit - private -> public
    protected int maxPlayers;
    private int q;
    private EnumGamemode r;
    private boolean s;
    private byte tabIndex = 0; // Rinny - use byte instead of int

    // CraftBukkit start
    private CraftServer cserver;

    public PlayerList(MinecraftServer minecraftserver) {
        minecraftserver.server = new CraftServer(minecraftserver, this);
        minecraftserver.console = org.bukkit.craftbukkit.command.ColouredConsoleSender.getInstance();
        minecraftserver.reader.addCompleter(new org.bukkit.craftbukkit.command.ConsoleCommandCompleter(minecraftserver.server));
        this.cserver = minecraftserver.server;
        // CraftBukkit end

        this.j = new GameProfileBanList(new File("banned-players.json"));
        this.k = new IpBanList(new File("banned-ips.json"));
        this.operators = new OpList(new File("ops.json"));
        this.whitelist = new WhiteList(new File("whitelist.json"));
        this.statisticMap = Maps.newHashMap();
        this.server = minecraftserver;
        this.playerMap = new java.util.HashMap<String, EntityPlayer>(minecraftserver.server.getMaxPlayers() + 10) {
            @Override
            public EntityPlayer put(String key, EntityPlayer value) {
                return super.put(key.toLowerCase(), value);
            }

            @Override
            public EntityPlayer get(Object key) {
                // put the .playerConnection check done in other places here
            	final EntityPlayer player = super.get(key instanceof String ? ((String)key).toLowerCase() : key);
                return (player != null && player.playerConnection != null) ? player : null;
            }

            @Override
            public boolean containsKey(Object key) {
                return get(key) != null;
            }

            @Override
            public EntityPlayer remove(Object key) {
                return super.remove(key instanceof String ? ((String)key).toLowerCase() : key);
            }
        };
        uuidMap = new java.util.HashMap<UUID, EntityPlayer>(minecraftserver.server.getMaxPlayers() + 10) {

    		@Override
            public EntityPlayer get(Object key) {
                // put the .playerConnection check done in other places here
            	final EntityPlayer player = super.get(key instanceof String ? ((String)key).toLowerCase() : key);
                return (player != null && player.playerConnection != null) ? player : null;
            }
        };
        this.j.a(false);
        this.k.a(false);
        this.maxPlayers = 8;
    }

    public void handlePlayerLogin(NetworkManager networkmanager, EntityPlayer entityplayer) {
    	final GameProfile gameprofile = entityplayer.getProfile();
    	final UserCache usercache = this.server.getUserCache();
        //GameProfile gameprofile1 = usercache.a(gameprofile.getId());
        //String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();

        usercache.a(gameprofile);
        final NBTTagCompound nbttagcompound = this.a(entityplayer);

        entityplayer.spawnIn(this.server.getWorldServer(entityplayer.dimension));
        entityplayer.playerInteractManager.a((WorldServer) entityplayer.world);
        String s1 = "local";

        if (networkmanager.getSocketAddress() != null) {
            s1 = networkmanager.getSocketAddress().toString();
        }

        // Spigot start - spawn location event
        final Player bukkitPlayer = entityplayer.getBukkitEntity();
        final Location loc = bukkitPlayer.getLocation();
        final WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();

        entityplayer.spawnIn(world);
        entityplayer.setPosition(loc.getX(), loc.getY(), loc.getZ());
        entityplayer.b(loc.getYaw(), loc.getPitch()); // should be setYawAndPitch
        // Spigot end

        // CraftBukkit - Moved message to after join
        // g.info(entityplayer.getName() + "[" + s1 + "] logged in with entity id " + entityplayer.getId() + " at (" + entityplayer.locX + ", " + entityplayer.locY + ", " + entityplayer.locZ + ")");
        WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);
        final ChunkCoordinates chunkcoordinates = worldserver.getSpawn();

        this.a(entityplayer, (EntityPlayer) null, worldserver);
        final PlayerConnection playerconnection = new PlayerConnection(this.server, networkmanager, entityplayer);

        // CraftBukkit start - Don't send a higher than 60 MaxPlayer size, otherwise the PlayerInfo window won't render correctly.
        final int maxPlayers = Math.min(this.getMaxPlayers(), 60);
        playerconnection.sendPacket(new PacketPlayOutLogin(entityplayer.getId(), entityplayer.playerInteractManager.getGameMode(), worldserver.getWorldData().isHardcore(), worldserver.worldProvider.dimension, worldserver.difficulty, maxPlayers, worldserver.getWorldData().getType()));
        entityplayer.getBukkitEntity().sendSupportedChannels();
        // CraftBukkit end
        playerconnection.sendPacket(new PacketPlayOutCustomPayload("MC|Brand", this.getServer().getServerModName().getBytes(Charsets.UTF_8)));
        playerconnection.sendPacket(new PacketPlayOutSpawnPosition(chunkcoordinates.x, chunkcoordinates.y, chunkcoordinates.z));
        playerconnection.sendPacket(new PacketPlayOutAbilities(entityplayer.abilities));
        playerconnection.sendPacket(new PacketPlayOutHeldItemSlot(entityplayer.inventory.itemInHandIndex));
        playerconnection.sendPacket(new PacketPlayOutEntityStatus(entityplayer, (byte) (worldserver.getGameRules().getBoolean("reducedDebugInfo") ? 22 : 23)));
        entityplayer.getStatisticManager().d();
        entityplayer.getStatisticManager().updateStatistics(entityplayer);
        this.sendScoreboard((ScoreboardServer) worldserver.getScoreboard(), entityplayer);
        this.server.az();
        /* CraftBukkit start - login message is handled in the event
        ChatMessage chatmessage;

        if (!entityplayer.getName().equalsIgnoreCase(s)) {
            chatmessage = new ChatMessage("multiplayer.player.joined.renamed", new Object[] { entityplayer.getScoreboardDisplayName(), s});
        } else {
            chatmessage = new ChatMessage("multiplayer.player.joined", new Object[] { entityplayer.getScoreboardDisplayName()});
        }

        chatmessage.getChatModifier().setColor(EnumChatFormat.YELLOW);
        this.sendMessage(chatmessage);
        // CraftBukkit end */
        this.handlePlayerJoin(entityplayer);
        worldserver = this.server.getWorldServer(entityplayer.dimension); // CraftBukkit - Update in case join event changed it
        playerconnection.a(entityplayer.locX, entityplayer.locY, entityplayer.locZ, entityplayer.yaw, entityplayer.pitch);
        this.b(entityplayer, worldserver);
        if (this.server.getResourcePack().length() > 0) {
            entityplayer.setResourcePack(this.server.getResourcePack());
        }

        final Iterator iterator = entityplayer.getEffects().iterator();

        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            playerconnection.sendPacket(new PacketPlayOutEntityEffect(entityplayer.getId(), mobeffect));
        }

        entityplayer.syncInventory();
        if (nbttagcompound != null && nbttagcompound.hasKeyOfType("Riding", 10)) {
        	final Entity entity = EntityTypes.a(nbttagcompound.getCompound("Riding"), worldserver);

            if (entity != null) {
                entity.attachedToPlayer = true;
                worldserver.addEntity(entity);
                entityplayer.mount(entity);
                entity.attachedToPlayer = false;
            }
        }
        
        ((CraftPlayer) bukkitPlayer).connect(); // IPVP - Update last login time

        // CraftBukkit - Moved from above, added world
        g.info(entityplayer.getName() + "[" + s1 + "] logged in with entity id " + entityplayer.getId() + " at ([" + entityplayer.world.worldData.getName() + "] " + entityplayer.locX + ", " + entityplayer.locY + ", " + entityplayer.locZ + ")");
    }

    public void sendScoreboard(ScoreboardServer scoreboardserver, EntityPlayer entityplayer) { // CraftBukkit - protected -> public
    	final Iterator iterator = scoreboardserver.getTeams().iterator();

        while (iterator.hasNext()) {
            ScoreboardTeam scoreboardteam = (ScoreboardTeam) iterator.next();

            entityplayer.playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(scoreboardteam, 0));
        }
        
        final Set hashset = new HashSet();

        for (int i = 0; i < 3; ++i) {
            ScoreboardObjective scoreboardobjective = scoreboardserver.getObjectiveForSlot(i);

            if (scoreboardobjective != null && !hashset.contains(scoreboardobjective)) {
                List list = scoreboardserver.getScoreboardScorePacketsForObjective(scoreboardobjective);
                Iterator iterator1 = list.iterator();

                while (iterator1.hasNext()) {
                    Packet packet = (Packet) iterator1.next();

                    entityplayer.playerConnection.sendPacket(packet);
                }

                hashset.add(scoreboardobjective);
            }
        }
    }
    
    public EntityPlayer getEntityPlayer(UUID uuid) {
        return uuidMap.get(uuid);
    }

    public void setPlayerFileData(WorldServer[] aworldserver) {
        if (this.playerFileData != null) return; // CraftBukkit
        this.playerFileData = aworldserver[0].getDataManager().getPlayerFileData();
    }

    public void a(EntityPlayer entityplayer, WorldServer worldserver) {
    	final WorldServer worldserver1 = entityplayer.r();

        if (worldserver != null) {
            worldserver.getPlayerChunkMap().removePlayer(entityplayer);
        }

        worldserver1.getPlayerChunkMap().addPlayer(entityplayer);
        worldserver1.chunkProviderServer.getChunkAt(MathHelper.floor(entityplayer.locX) >> 4, MathHelper.floor(entityplayer.locZ) >> 4);
    }

    public int d() {
        return PlayerChunkMap.getFurthestViewableBlock(this.s());
    }

    public NBTTagCompound a(EntityPlayer entityplayer) {
        // CraftBukkit - fix reference to worldserver array
    	final NBTTagCompound nbttagcompound = this.server.worlds.get(0).getWorldData().i();
    	final NBTTagCompound nbttagcompound1;

        if (entityplayer.getName().equals(this.server.M()) && nbttagcompound != null) {
            entityplayer.f(nbttagcompound);
            nbttagcompound1 = nbttagcompound;
            g.debug("loading single player");
        } else {
            nbttagcompound1 = this.playerFileData.load(entityplayer);
        }

        return nbttagcompound1;
    }

    protected void b(EntityPlayer entityplayer) {
        this.playerFileData.save(entityplayer);
        final ServerStatisticManager serverstatisticmanager = this.statisticMap.get(entityplayer.getUniqueID());

        if (serverstatisticmanager != null) {
            serverstatisticmanager.b();
        }
    }

    public void handlePlayerJoin(EntityPlayer entityplayer) {
        cserver.detectListNameConflict(entityplayer); // CraftBukkit
        // this.sendAll(new PacketPlayOutPlayerInfo(entityplayer.getName(), true, 1000)); // CraftBukkit - replaced with loop below
	    this.players.add(entityplayer);
	    this.playerMap.put(entityplayer.getName(), entityplayer); // PaperSpigot
	    this.uuidMap.put(entityplayer.getUniqueID(), entityplayer); // PaperSpigot
        final WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);

        // CraftBukkit start
        final PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(this.cserver.getPlayer(entityplayer), "\u00A7e" + entityplayer.getName() + " joined the game.");
        this.cserver.getPluginManager().callEvent(playerJoinEvent);

        final String joinMessage = playerJoinEvent.getJoinMessage();

        if ((joinMessage != null) && (joinMessage.length() > 0)) {
            for (IChatBaseComponent line : org.bukkit.craftbukkit.util.CraftChatMessage.fromString(joinMessage)) {
                this.server.getPlayerList().sendAll(new PacketPlayOutChat(line));
            }
        }
        ChunkIOExecutor.adjustPoolSize(this.getPlayerCount());
        // CraftBukkit end

        // CraftBukkit start - Only add if the player wasn't moved in the event
        if (entityplayer.world == worldserver && !worldserver.players.contains(entityplayer)) {
            worldserver.addEntity(entityplayer);
            this.a(entityplayer, (WorldServer) null);
        }
        // CraftBukkit end

        // Rinny start - do all in a single for loop
        final PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(entityplayer, PlayerInfo.ADD_PLAYER); // Spigot - protocol patch
        final PacketPlayOutPlayerInfo displayPacket = new PacketPlayOutPlayerInfo(entityplayer, PlayerInfo.UPDATE_DISPLAY_NAME); // Spigot - protocol patch
        for (EntityPlayer entityplayer1 : this.players) {
            // .name -> .listName
        	entityplayer1.playerConnection.sendPacket(packet);
        	if (!entityplayer1.getBukkitEntity().isHiddenFromTablist()) {
        		entityplayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(entityplayer1, PlayerInfo.ADD_PLAYER)); // Spigot - protocol patch
        	}
            // Spigot start - protocol patch
            if (!entityplayer.getName().equals(entityplayer.listName)) {
	            if (!entityplayer1.getBukkitEntity().isHiddenFromTablist() && entityplayer.playerConnection.networkManager.getVersion() > 28) {
	            	entityplayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(entityplayer1, PlayerInfo.UPDATE_DISPLAY_NAME));
	            }
            	if (entityplayer1.playerConnection.networkManager.getVersion() > 28) {
            		entityplayer1.playerConnection.sendPacket(displayPacket);
            	}
            }
            // Spigot end
            // CraftBukkit end
        }
        // Rinny stop 
    }

    public void d(EntityPlayer entityplayer) {
        entityplayer.r().getPlayerChunkMap().movePlayer(entityplayer);
    }
    
    public String disconnect(EntityPlayer player) {
    	return disconnect(player, null);
	}

    public String disconnect(EntityPlayer entityplayer, @Nullable String disconnectReason) { // CraftBukkit - return string
        entityplayer.a(StatisticList.f);

        // CraftBukkit start - Quitting must be before we do final save of data, in case plugins need to modify it
        org.bukkit.craftbukkit.event.CraftEventFactory.handleInventoryCloseEvent(entityplayer);

        final Player player = cserver.getPlayer(entityplayer);
        final PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(this.cserver.getPlayer(entityplayer), "\u00A7e" + entityplayer.getName() + " left the game.", disconnectReason);
        this.cserver.getPluginManager().callEvent(playerQuitEvent);
        entityplayer.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());
        // CraftBukkit end

        this.b(entityplayer);
        final WorldServer worldserver = entityplayer.r();

        if (entityplayer.vehicle != null && !(entityplayer.vehicle instanceof EntityPlayer)) { // CraftBukkit - Don't remove players
            worldserver.removeEntity(entityplayer.vehicle);
            g.debug("removing player mount");
        }

        worldserver.kill(entityplayer);
        worldserver.getPlayerChunkMap().removePlayer(entityplayer);
        this.players.remove(entityplayer);
        this.uuidMap.remove(entityplayer.getUniqueID()); // PaperSpigot
        this.playerMap.remove(entityplayer.getName()); // PaperSpigot
        this.statisticMap.remove(entityplayer.getUniqueID());
        ChunkIOExecutor.adjustPoolSize(this.getPlayerCount()); // CraftBukkit
        // KigPaper start - fix memory leak
        final CraftingManager craftingManager = CraftingManager.getInstance();
        final CraftInventoryView lastView = (CraftInventoryView) craftingManager.lastCraftView;
        if (lastView != null && lastView.getHandle() instanceof ContainerPlayer && lastView.getPlayer() == player) {
        	craftingManager.lastCraftView = null;
        }
        // KigPaper stOP - fix memory leak
        // CraftBukkit start - .name -> .listName, replace sendAll with loop
        // this.sendAll(new PacketPlayOutPlayerInfo(entityplayer.getName(), false, 9999));
        final PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(entityplayer, PlayerInfo.REMOVE_PLAYER); // Spigot - protocol patch
        for (EntityPlayer entityplayer1 : this.players) {
            entityplayer1.playerConnection.sendPacket(packet);
            entityplayer1.getBukkitEntity().removeDisconnectingPlayer(entityplayer.getBukkitEntity());
        }
        // This removes the scoreboard (and player reference) for the specific player in the manager
        this.cserver.getScoreboardManager().removePlayer(entityplayer.getBukkitEntity());
        entityplayer.r().getTracker().untrackPlayer(entityplayer); // Rinny - untrack player
        return playerQuitEvent.getQuitMessage();
        // CraftBukkit end
    }

    // CraftBukkit start - Whole method, SocketAddress to LoginListener, added hostname to signature, return EntityPlayer
    public EntityPlayer attemptLogin(LoginListener loginlistener, GameProfile gameprofile, String hostname) {
        // Instead of kicking then returning, we need to store the kick reason
        // in the event, check with plugins to see if it's ok, and THEN kick
        // depending on the outcome.
        final SocketAddress socketaddress = loginlistener.networkManager.getSocketAddress();

        final EntityPlayer entity = new EntityPlayer(this.server, this.server.getWorldServer(0), gameprofile, new PlayerInteractManager(this.server.getWorldServer(0)));
        final Player player = entity.getBukkitEntity();
        final PlayerLoginEvent event = new PlayerLoginEvent(player, hostname, ((java.net.InetSocketAddress) socketaddress).getAddress(), ((java.net.InetSocketAddress) loginlistener.networkManager.getRawAddress()).getAddress());

        // TODO: check if okay
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd \'at\' HH:mm:ss z");
        // Paper start - Fix MC-158900
        GameProfileBanEntry gameprofilebanentry;
        if (getProfileBans().isBanned(gameprofile) && (gameprofilebanentry = (GameProfileBanEntry) getProfileBans().get(gameprofile)) != null) {
        // Paper end
        /*if (this.j.isBanned(gameprofile) && !this.j.get(gameprofile).hasExpired()) {
            final GameProfileBanEntry gameprofilebanentry = (GameProfileBanEntry) this.j.get(gameprofile);*/

            // return s;
            if (!gameprofilebanentry.hasExpired()) {
            	event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "You are banned from this server!" + 
      			  													"\nSource: " + gameprofilebanentry.getSource() +
      			  													"\nReason: " + gameprofilebanentry.getReason() +
      			  													"\n" + (gameprofilebanentry.getExpires() != null ? "Your ban will be removed on " + sdf.format(gameprofilebanentry.getExpires()) : "You are permanently banned from this server")); // Spigot
            }
        } else if (!this.isWhitelisted(gameprofile) && !player.hasPermission("whitelist.bypass")) {
            // return "You are not white-listed on this server!";
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, org.spigotmc.SpigotConfig.whitelistMessage); // Spigot
        } else if (this.k.isBanned(socketaddress) && !this.k.get(socketaddress).hasExpired()) { // Spigot
            final IpBanEntry ipbanentry = this.k.get(socketaddress);
            // return s;
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "Your IP address is banned from this server!" + 
            													"\nSource: " + ipbanentry.getSource() +
            													"\nReason: " + ipbanentry.getReason() +
            													"\n" + (ipbanentry.getExpires() != null ? "Your ban will be removed on " + sdf.format(ipbanentry.getExpires()) : "You are permanently banned from this server"));
        } else {
            // return this.players.size() >= this.maxPlayers ? "The server is full!" : null;
            if (this.players.size() >= this.maxPlayers && (!player.isOp() && !player.hasPermission("full.bypass"))) { // Rinny allow to join server even if its full
                event.disallow(PlayerLoginEvent.Result.KICK_FULL, org.spigotmc.SpigotConfig.serverFullMessage); // Spigot
            }
        }

        this.cserver.getPluginManager().callEvent(event);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            loginlistener.disconnect(event.getKickMessage());
            return null;
        }

        return entity;
        // CraftBukkit end
    }

    public EntityPlayer processLogin(GameProfile gameprofile, EntityPlayer player) { // CraftBukkit - added EntityPlayer
    	final UUID uuid = EntityHuman.a(gameprofile);
        EntityPlayer entityplayer;

        /* // PaperSpigot start - Use exact lookup below
        for (int i = 0; i < this.players.size(); ++i) {
            entityplayer = (EntityPlayer) this.players.get(i);
            if (entityplayer.getUniqueID().equals(uuid)) {
                arraylist.add(entityplayer);
            }
        }

        Iterator iterator = arraylist.iterator();

        while (iterator.hasNext()) {
            entityplayer = (EntityPlayer) iterator.next();
        */
        if ((entityplayer = uuidMap.get(uuid)) != null) {
            // PaperSpigot end
            entityplayer.playerConnection.disconnect("You are logged in from another location");
        }

        /* CraftBukkit start
        Object object;

        if (this.server.R()) {
            object = new DemoPlayerInteractManager(this.server.getWorldServer(0));
        } else {
            object = new PlayerInteractManager(this.server.getWorldServer(0));
        }

        return new EntityPlayer(this.server, this.server.getWorldServer(0), gameprofile, (PlayerInteractManager) object);
        // */
        return player;
        // CraftBukkit end
    }

    // CraftBukkit start
    public EntityPlayer moveToWorld(EntityPlayer entityplayer, int i, boolean flag) {
        return this.moveToWorld(entityplayer, i, flag, null, true);
    }

    public EntityPlayer moveToWorld(EntityPlayer entityplayer, int i, boolean flag, Location location, boolean avoidSuffocation) {
        // CraftBukkit end
        entityplayer.r().getTracker().untrackPlayer(entityplayer);
        // entityplayer.r().getTracker().untrackEntity(entityplayer); // CraftBukkit
        entityplayer.r().getPlayerChunkMap().removePlayer(entityplayer);
        this.players.remove(entityplayer);
        this.server.getWorldServer(entityplayer.dimension).removeEntity(entityplayer);
        ChunkCoordinates chunkcoordinates = entityplayer.getBed();
        final boolean flag1 = entityplayer.isRespawnForced();

        /* CraftBukkit start
        entityplayer.dimension = i;
        Object object;

        if (this.server.R()) {
            object = new DemoPlayerInteractManager(this.server.getWorldServer(entityplayer.dimension));
        } else {
            object = new PlayerInteractManager(this.server.getWorldServer(entityplayer.dimension));
        }

        EntityPlayer entityplayer1 = new EntityPlayer(this.server, this.server.getWorldServer(entityplayer.dimension), entityplayer.getProfile(), (PlayerInteractManager) object);
        // */
        final org.bukkit.World fromWorld = entityplayer.getBukkitEntity().getWorld();
        entityplayer.viewingCredits = false;
        // CraftBukkit end

        entityplayer.playerConnection = entityplayer.playerConnection;
        entityplayer.copyTo(entityplayer, flag);
        entityplayer.d(entityplayer.getId());
        // WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension); // CraftBukkit - handled later

        // this.a(entityplayer1, entityplayer, worldserver); // CraftBukkit - removed
        ChunkCoordinates chunkcoordinates1;

        // CraftBukkit start - fire PlayerRespawnEvent
        this.players.add(entityplayer);
        if (location == null) {
            boolean isBedSpawn = false;
            CraftWorld cworld = (CraftWorld) this.server.server.getWorld(entityplayer.spawnWorld);
            if (cworld != null && chunkcoordinates != null) {
                chunkcoordinates1 = EntityHuman.getBed(cworld.getHandle(), chunkcoordinates, flag1);
                if (chunkcoordinates1 != null) {
                    isBedSpawn = true;
                    location = new Location(cworld, chunkcoordinates1.x , chunkcoordinates1.y, chunkcoordinates1.z + 0.5);
                } else {
                    entityplayer.setRespawnPosition(null, true);
                    entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(0, 0));
                }
            }

            if (location == null) {
                cworld = (CraftWorld) this.server.server.getWorlds().get(0);
                chunkcoordinates = cworld.getHandle().getSpawn();
                location = new Location(cworld, chunkcoordinates.x + 0.5, chunkcoordinates.y, chunkcoordinates.z + 0.5, cworld.getHandle().getWorldData().spawnYaw(), cworld.getHandle().getWorldData().spawnPitch());
            }

            final Player respawnPlayer = this.cserver.getPlayer(entityplayer);
            final PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn);
            this.cserver.getPluginManager().callEvent(respawnEvent);
            // Spigot Start
            if (entityplayer.playerConnection.isDisconnected()) {
                return entityplayer;
            }
            // Spigot End
            location = respawnEvent.getRespawnLocation();
            entityplayer.reset();
        } else {
            location.setWorld(this.server.getWorldServer(i).getWorld());
        }
        final WorldServer worldserver = ((CraftWorld) location.getWorld()).getHandle();
        entityplayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        // CraftBukkit end

        worldserver.chunkProviderServer.getChunkAt(MathHelper.floor(entityplayer.locX) >> 4, MathHelper.floor(entityplayer.locZ) >> 4);

        while (avoidSuffocation && !worldserver.getCubes(entityplayer, entityplayer.boundingBox).isEmpty()) { // CraftBukkit
            entityplayer.setPosition(entityplayer.locX, entityplayer.locY + 1.0D, entityplayer.locZ);
        }

        // CraftBukkit start
        final byte actualDimension = (byte) (worldserver.getWorld().getEnvironment().getId());
        // Force the client to refresh their chunk cache.
        entityplayer.playerConnection.sendPacket(new PacketPlayOutRespawn((byte) (actualDimension >= 0 ? -1 : 0), worldserver.difficulty, worldserver.getWorldData().getType(), entityplayer.playerInteractManager.getGameMode()));
        entityplayer.playerConnection.sendPacket(new PacketPlayOutRespawn(actualDimension, worldserver.difficulty, worldserver.getWorldData().getType(), entityplayer.playerInteractManager.getGameMode()));
        entityplayer.spawnIn(worldserver);
        entityplayer.dead = false;
        entityplayer.playerConnection.teleport(new Location(worldserver.getWorld(), entityplayer.locX, entityplayer.locY, entityplayer.locZ, entityplayer.yaw, entityplayer.pitch));
        chunkcoordinates1 = worldserver.getSpawn();
        // entityplayer1.playerConnection.a(entityplayer1.locX, entityplayer1.locY, entityplayer1.locZ, entityplayer1.yaw, entityplayer1.pitch);
        // CraftBukkit end
        entityplayer.playerConnection.sendPacket(new PacketPlayOutSpawnPosition(chunkcoordinates1.x, chunkcoordinates1.y, chunkcoordinates1.z));
        entityplayer.playerConnection.sendPacket(new PacketPlayOutExperience(entityplayer.exp, entityplayer.expTotal, entityplayer.expLevel));
        this.b(entityplayer, worldserver);
        // CraftBukkit start
        // Don't re-add player to player list if disconnected
        if (entityplayer.playerConnection.isDisconnected()) {
            this.players.remove(entityplayer);
        } else {
            worldserver.getPlayerChunkMap().addPlayer(entityplayer);
            worldserver.addEntity(entityplayer);
        }
        // Added from changeDimension
        this.updateClient(entityplayer); // Update health, etc...
        entityplayer.updateAbilities();
        Iterator iterator = entityplayer.getEffects().iterator();

        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            entityplayer.playerConnection.sendPacket(new PacketPlayOutEntityEffect(entityplayer.getId(), mobeffect));
        }
        // entityplayer1.syncInventory();
        // CraftBukkit end
        entityplayer.setHealth(entityplayer.getHealth());
        entityplayer.setSneaking(false); // Rinny - Fix MC-10657

        // CraftBukkit start
        // Don't fire on respawn
        if (fromWorld != location.getWorld()) {
        	final PlayerChangedWorldEvent event = new PlayerChangedWorldEvent((Player) entityplayer.getBukkitEntity(), fromWorld);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }

        // Save player file again if they were disconnected
        if (entityplayer.playerConnection.isDisconnected()) {
            this.b(entityplayer);
        }
        // CraftBukkit end

        return entityplayer;
    }

    // CraftBukkit start - Replaced the standard handling of portals with a more customised method.
    public void changeDimension(EntityPlayer entityplayer, int i, TeleportCause cause) {
        WorldServer exitWorld = null;
        if (entityplayer.dimension < CraftWorld.CUSTOM_DIMENSION_OFFSET) { // plugins must specify exit from custom Bukkit worlds
            // only target existing worlds (compensate for allow-nether/allow-end as false)
            for (WorldServer world : this.server.worlds) {
                if (world.dimension == i) {
                    exitWorld = world;
                }
            }
        }

        final Location enter = entityplayer.getBukkitEntity().getLocation();
        Location exit = null;
        boolean useTravelAgent = false; // don't use agent for custom worlds or return from THE_END
        if (exitWorld != null) {
            if ((cause == TeleportCause.END_PORTAL) && (i == 0)) {
                // THE_END -> NORMAL; use bed if available, otherwise default spawn
                exit = ((org.bukkit.craftbukkit.entity.CraftPlayer) entityplayer.getBukkitEntity()).getBedSpawnLocation();
                if (exit == null || ((CraftWorld) exit.getWorld()).getHandle().dimension != 0) {
                    exit = exitWorld.getWorld().getSpawnLocation();
                }
            } else {
                // NORMAL <-> NETHER or NORMAL -> THE_END
                exit = this.calculateTarget(enter, exitWorld);
                useTravelAgent = true;
            }
        }

        final TravelAgent agent = exit != null ? (TravelAgent) ((CraftWorld) exit.getWorld()).getHandle().getTravelAgent() : org.bukkit.craftbukkit.CraftTravelAgent.DEFAULT; // return arbitrary TA to compensate for implementation dependent plugins
        agent.setCanCreatePortal(cause != TeleportCause.END_PORTAL); // PaperSpigot - Configurable end credits, don't allow End Portals to create portals

        final PlayerPortalEvent event = new PlayerPortalEvent(entityplayer.getBukkitEntity(), enter, exit, agent, cause);
        event.useTravelAgent(useTravelAgent);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled() || event.getTo() == null) {
            return;
        }

        // PaperSpigot - Configurable end credits, if a plugin sets to use a travel agent even if the cause is an end portal, ignore it
        exit = cause != TeleportCause.END_PORTAL && event.useTravelAgent() ? event.getPortalTravelAgent().findOrCreate(event.getTo()) : event.getTo();
        if (exit == null) {
            return;
        }
        exitWorld = ((CraftWorld) exit.getWorld()).getHandle();
        final Vector velocity = entityplayer.getBukkitEntity().getVelocity();
        final boolean before = exitWorld.chunkProviderServer.forceChunkLoad;
        exitWorld.chunkProviderServer.forceChunkLoad = true;
        exitWorld.getTravelAgent().adjustExit(entityplayer, exit, velocity);
        exitWorld.chunkProviderServer.forceChunkLoad = before;

        this.moveToWorld(entityplayer, exitWorld.dimension, true, exit, false); // Vanilla doesn't check for suffocation when handling portals, so neither should we
        if (entityplayer.motX != velocity.getX() || entityplayer.motY != velocity.getY() || entityplayer.motZ != velocity.getZ()) {
            entityplayer.getBukkitEntity().setVelocity(velocity);
        }
        // CraftBukkit end
    }

    public void a(Entity entity, int i, WorldServer worldserver, WorldServer worldserver1) {
        // CraftBukkit start - Split into modular functions
    	final Location exit = this.calculateTarget(entity.getBukkitEntity().getLocation(), worldserver1);
        this.repositionEntity(entity, exit, true);
    }

    // Copy of original a(Entity, int, WorldServer, WorldServer) method with only location calculation logic
    public Location calculateTarget(Location enter, World target) {
    	final WorldServer worldserver = ((CraftWorld) enter.getWorld()).getHandle();
        WorldServer worldserver1 = ((CraftWorld) target.getWorld()).getHandle();
        final int i = worldserver.dimension;

        double y = enter.getY();
        float yaw = enter.getYaw();
        float pitch = enter.getPitch();
        double d0 = enter.getX();
        double d1 = enter.getZ();
        final double d2 = 8.0D;
        /*
        double d3 = entity.locX;
        double d4 = entity.locY;
        double d5 = entity.locZ;
        float f = entity.yaw;

        worldserver.methodProfiler.a("moving");
        */
        switch (worldserver1.dimension) {
        	case -1: {
        		d0 /= d2;
                d1 /= d2;
                break;
        	}
        	case 0: {
        		 d0 *= d2;
                 d1 *= d2;
                 break;
        	}
        	default: {
        		ChunkCoordinates chunkcoordinates;

                if (i == 1) {
                    // use default NORMAL world spawn instead of target
                    worldserver1 = this.server.worlds.get(0);
                    chunkcoordinates = worldserver1.getSpawn();
                } else {
                    chunkcoordinates = worldserver1.getDimensionSpawn();
                }

                d0 = (double) chunkcoordinates.x;
                y = (double) chunkcoordinates.y;
                d1 = (double) chunkcoordinates.z;
                yaw = 90.0F;
                pitch = 0.0F;
        		break;
        	}
        }
        /*if (worldserver1.dimension == -1) {
            d0 /= d2;
            d1 /= d2;
        } else if (worldserver1.dimension == 0) {
            d0 *= d2;
            d1 *= d2;
        } else {
            ChunkCoordinates chunkcoordinates;

            if (i == 1) {
                // use default NORMAL world spawn instead of target
                worldserver1 = this.server.worlds.get(0);
                chunkcoordinates = worldserver1.getSpawn();
            } else {
                chunkcoordinates = worldserver1.getDimensionSpawn();
            }

            d0 = (double) chunkcoordinates.x;
            y = (double) chunkcoordinates.y;
            d1 = (double) chunkcoordinates.z;
            yaw = 90.0F;
            pitch = 0.0F;
        }*/

        // worldserver.methodProfiler.b();
        if (i != 1) {
            // worldserver.methodProfiler.a("placing");
            d0 = (double) MathHelper.a((int) d0, -29999872, 29999872);
            d1 = (double) MathHelper.a((int) d1, -29999872, 29999872);
            /*
            if (entity.isAlive()) {
                worldserver1.addEntity(entity);
                entity.setPositionRotation(d0, entity.locY, d1, entity.yaw, entity.pitch);
                worldserver1.entityJoinedWorld(entity, false);
                worldserver1.getTravelAgent().a(entity, d3, d4, d5, f);
            }

            worldserver.methodProfiler.b();
            */
        }

        // entity.spawnIn(worldserver1);
        return new Location(worldserver1.getWorld(), d0, y, d1, yaw, pitch);
    }

    // copy of original a(Entity, int, WorldServer, WorldServer) method with only entity repositioning logic
    public void repositionEntity(Entity entity, Location exit, boolean portal) {
    	final int i = entity.dimension;
    	final WorldServer worldserver = (WorldServer) entity.world;
    	final WorldServer worldserver1 = ((CraftWorld) exit.getWorld()).getHandle();
        /*
        double d0 = entity.locX;
        double d1 = entity.locZ;
        double d2 = 8.0D;
        double d3 = entity.locX;
        double d4 = entity.locY;
        double d5 = entity.locZ;
        float f = entity.yaw;
        */

        entity.setPositionRotation(exit.getX(), exit.getY(), exit.getZ(), exit.getYaw(), exit.getPitch());
        if (entity.isAlive()) {
            worldserver.entityJoinedWorld(entity, false);
        }
        /*
        if (entity.dimension == -1) {
            d0 /= d2;
            d1 /= d2;
            entity.setPositionRotation(d0, entity.locY, d1, entity.yaw, entity.pitch);
            if (entity.isAlive()) {
                worldserver.entityJoinedWorld(entity, false);
            }
        } else if (entity.dimension == 0) {
            d0 *= d2;
            d1 *= d2;
            entity.setPositionRotation(d0, entity.locY, d1, entity.yaw, entity.pitch);
            if (entity.isAlive()) {
                worldserver.entityJoinedWorld(entity, false);
            }
        } else {
            ChunkCoordinates chunkcoordinates;

            if (i == 1) {
                chunkcoordinates = worldserver1.getSpawn();
            } else {
                chunkcoordinates = worldserver1.getDimensionSpawn();
            }

            d0 = (double) chunkcoordinates.x;
            entity.locY = (double) chunkcoordinates.y;
            d1 = (double) chunkcoordinates.z;
            entity.setPositionRotation(d0, entity.locY, d1, 90.0F, 0.0F);
            if (entity.isAlive()) {
                worldserver.entityJoinedWorld(entity, false);
            }
        }
        */

        if (i != 1) {
            /*
            d0 = (double) MathHelper.a((int) d0, -29999872, 29999872);
            d1 = (double) MathHelper.a((int) d1, -29999872, 29999872);
            */
            if (entity.isAlive()) {
                // entity.setPositionRotation(d0, entity.locY, d1, entity.yaw, entity.pitch)
                // worldserver1.getTravelAgent().a(entity, d3, d4, d5, f);
                if (portal) {
                	final Vector velocity = entity.getBukkitEntity().getVelocity();
                    worldserver1.getTravelAgent().adjustExit(entity, exit, velocity);
                    entity.setPositionRotation(exit.getX(), exit.getY(), exit.getZ(), exit.getYaw(), exit.getPitch());
                    if (entity.motX != velocity.getX() || entity.motY != velocity.getY() || entity.motZ != velocity.getZ()) {
                        entity.getBukkitEntity().setVelocity(velocity);
                    }
                }
                worldserver1.addEntity(entity);
                worldserver1.entityJoinedWorld(entity, false);
            }

        }

        entity.spawnIn(worldserver1);
        // CraftBukkit end
    }

    public void tick() {
    	// Rinny start
    	if (players.isEmpty()) {
    		return;
    	}
    	final byte size = (byte) Math.min(this.players.size(), 60);
    	this.tabIndex = (byte) ((this.tabIndex + 1) % size);
    	final EntityPlayer player = this.players.get(this.tabIndex);
    	
        if (player.lastPing == -1 || pingToBar(player.ping) != pingToBar(player.lastPing)) {
        	final Packet packet = new PacketPlayOutPlayerInfo(player, PlayerInfo.UPDATE_LATENCY);
            for (EntityPlayer splayer : this.players) {
                if (splayer.getBukkitEntity().canSee(player.getBukkitEntity()) && !splayer.getBukkitEntity().isHiddenFromTablist()) { // !!! KEEP THIS HERE !!!
                	splayer.playerConnection.sendPacket(packet);
                }
            } 
            player.lastPing = player.ping;
        }
        // Rinny end
    }
    
    private int pingToBar(short ping) {
    	if (ping <= 0) return 5;
    	if (ping < 150) return 0;
    	if (ping < 300) return 1;
    	if (ping < 600) return 2;
    	if (ping < 1000) return 3;
    	return 4;
    }
    
    public void sendAll(Packet packet) {
    	this.players.stream().forEach(player -> ((EntityPlayer) player).playerConnection.sendPacket(packet));
        /*for (int i = 0; i < this.players.size(); ++i) {
            ((EntityPlayer) this.players.get(i)).playerConnection.sendPacket(packet);
        }*/
    }

    public void a(Packet packet, int i) {
        for (int j = 0; j < this.players.size(); ++j) {
            EntityPlayer entityplayer = (EntityPlayer) this.players.get(j);

            if (entityplayer.dimension == i) {
                entityplayer.playerConnection.sendPacket(packet);
            }
        }
    }

    public String b(boolean flag) {
        /*String s = "";
        List<EntityPlayer> arraylist = Lists.newArrayList(this.players);

        for (int i = 0; i < arraylist.size(); ++i) {
            if (i > 0) {
                s = s + ", ";
            }

            s = s + ((EntityPlayer) arraylist.get(i)).getName();
            if (flag) {
                s = s + " (" + ((EntityPlayer) arraylist.get(i)).getUniqueID().toString() + ")";
            }
        }

        return s;*/
    	final StringBuilder sb = new StringBuilder(this.players.size() * 16); // estimated capacity
        for (EntityPlayer player : this.players) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(player.getName());
            if (flag) {
                sb.append(" (").append(player.getUniqueID().toString()).append(")");
            }
        }
        return sb.toString();
    }

    public String[] f() {
    	return this.players.stream().map(EntityPlayer::getName).toArray(String[]::new);
        /*String[] astring = new String[this.players.size()];

        for (int i = 0; i < this.players.size(); ++i) {
            astring[i] = ((EntityPlayer) this.players.get(i)).getName();
        }

        return astring;*/
    }

    public GameProfile[] g() {
    	return this.players.stream().map(EntityPlayer::getProfile).toArray(GameProfile[]::new);
        /*GameProfile[] agameprofile = new GameProfile[this.players.size()];

        for (int i = 0; i < this.players.size(); ++i) {
            agameprofile[i] = ((EntityPlayer) this.players.get(i)).getProfile();
        }

        return agameprofile;*/
    }

    public GameProfileBanList getProfileBans() {
        return this.j;
    }

    public IpBanList getIPBans() {
        return this.k;
    }

    public void addOp(GameProfile gameprofile) {
        this.operators.add(new OpListEntry(gameprofile, this.server.l()));

        // CraftBukkit start
        final Player player = server.server.getPlayer(gameprofile.getId());
        if (player != null) {
            player.recalculatePermissions();
        }
        // CraftBukkit end
    }

    public void removeOp(GameProfile gameprofile) {
        this.operators.remove(gameprofile);

        // CraftBukkit start
        final Player player = server.server.getPlayer(gameprofile.getId());
        if (player != null) {
            player.recalculatePermissions();
        }
        // CraftBukkit end
    }

    public boolean isWhitelisted(GameProfile gameprofile) {
        return !this.hasWhitelist || this.operators.d(gameprofile) || this.whitelist.d(gameprofile);
    }

    public boolean isOp(GameProfile gameprofile) {
        // CraftBukkit - fix reference to worldserver array
        return this.operators.d(gameprofile) || this.server.N() && this.server.worlds.get(0).getWorldData().allowCommands() && this.server.M().equalsIgnoreCase(gameprofile.getName()) || this.s;
    }

    public EntityPlayer getPlayer(String s) {
        /*if (true) { 
        	return playerMap.get(s); 
        } // PaperSpigot
        Iterator iterator = this.players.iterator();

        EntityPlayer entityplayer;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            entityplayer = (EntityPlayer) iterator.next();
        } while (!entityplayer.getName().equalsIgnoreCase(s));

        return entityplayer;*/
    	final EntityPlayer player = playerMap.get(s);
        if (player != null) {
            return player;
        }

        for (EntityPlayer entityplayer : players) {
            if (entityplayer.getName().equalsIgnoreCase(s)) {
                return entityplayer;
            }
        }

        return null;
    }

    public List<EntityPlayer> getPlayersInRange(ChunkCoordinates chunkcoordinates, int i, int j, int k, int l, int i1, int j1, Map map, String s, String s1, World world) {
        if (this.players.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<EntityPlayer> list = new ArrayList<>();
            final boolean flag = k < 0;
            final boolean flag1 = s != null && s.startsWith("!");
            final boolean flag2 = s1 != null && s1.startsWith("!");
            final int k1 = i * i;
            final int l1 = j * j;

            k = MathHelper.a(k);
            if (flag1) {
                s = s.substring(1);
            }

            if (flag2) {
                s1 = s1.substring(1);
            }

            for (int i2 = 0; i2 < this.players.size(); ++i2) {
                EntityPlayer entityplayer = (EntityPlayer) this.players.get(i2);

                if ((world == null || entityplayer.world == world) && (s == null || flag1 != s.equalsIgnoreCase(entityplayer.getName()))) {
                    if (s1 != null) {
                        ScoreboardTeamBase scoreboardteambase = entityplayer.getScoreboardTeam();
                        String s2 = scoreboardteambase == null ? "" : scoreboardteambase.getName();

                        if (flag2 == s1.equalsIgnoreCase(s2)) {
                            continue;
                        }
                    }

                    if (chunkcoordinates != null && (i > 0 || j > 0)) {
                        float f = chunkcoordinates.e(entityplayer.getChunkCoordinates());

                        if (i > 0 && f < (float) k1 || j > 0 && f > (float) l1) {
                            continue;
                        }
                    }

                    if (this.a((EntityHuman) entityplayer, map) && (l == EnumGamemode.NONE.getId() || l == entityplayer.playerInteractManager.getGameMode().getId()) && (i1 <= 0 || entityplayer.expLevel >= i1) && entityplayer.expLevel <= j1) {
                        list.add(entityplayer);
                    }
                }
            }

            if (chunkcoordinates != null) {
            	list.sort(new PlayerDistanceComparator(chunkcoordinates)); // TODO: check if it does shit
                //Collections.sort(list, new PlayerDistanceComparator(chunkcoordinates));
            }

            if (flag) {
                Collections.reverse(list);
            }

            if (k > 0) {
            	list = list.subList(0, Math.min(k, list.size()));
            }

            return list;
        }
    }

    private boolean a(EntityHuman entityhuman, Map map) {
        if (map != null && map.size() != 0) {
        	final Iterator iterator = map.entrySet().iterator();

            Entry entry;
            boolean flag;
            int i;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                entry = (Entry) iterator.next();
                String s = (String) entry.getKey();

                flag = false;
                if (s.endsWith("_min") && s.length() > 4) {
                    flag = true;
                    s = s.substring(0, s.length() - 4);
                }

                final Scoreboard scoreboard = entityhuman.getScoreboard();
                final ScoreboardObjective scoreboardobjective = scoreboard.getObjective(s);

                if (scoreboardobjective == null) {
                    return false;
                }

                final ScoreboardScore scoreboardscore = entityhuman.getScoreboard().getPlayerScoreForObjective(entityhuman.getName(), scoreboardobjective);

                i = scoreboardscore.getScore();
                if (i < ((Integer) entry.getValue()).intValue() && flag) {
                    return false;
                }
            } while (i <= ((Integer) entry.getValue()).intValue() || flag);

            return false;
        }
        return true;
    }

    public void sendPacketNearby(double d0, double d1, double d2, double d3, int i, Packet packet) {
        this.sendPacketNearby((EntityHuman) null, d0, d1, d2, d3, i, packet, false);
    }

    public void sendPacketNearby(EntityHuman entityhuman, double d0, double d1, double d2, double d3, int i, Packet packet, boolean includingSelf) {
    	final boolean entityHumanNull = entityhuman == null;
    	final WorldServer world = this.server.getWorldServer(i);
        for (double x = d0 - d3; x <= d0 + d3; x += 16.0D) {
        	for (double z = d2 - d3; z <= d2 + d3; z += 16.0D) {
        		Chunk chunk = world.getChunkAt((int)x >> 4, (int)z >> 4);
        		if (chunk == null) continue;
        		for (EntityPlayer entityplayer : chunk.playersInChunk) {

		            // CraftBukkit start - Test if player receiving packet can see the source of the packet
		            if (!entityHumanNull && entityhuman instanceof EntityPlayer && !entityplayer.getBukkitEntity().canSee(entityhuman.getBukkitEntity())) {
		                continue;
		            }
		            // CraftBukkit end
		
			        if ((!includingSelf && entityplayer != entityhuman || includingSelf) && entityplayer.dimension == i) {
			        	double d4 = d0 - entityplayer.locX;
			        	double d5 = d1 - entityplayer.locY;
			        	double d6 = d2 - entityplayer.locZ;
			
			        	if (d4 * d4 + d5 * d5 + d6 * d6 < d3 * d3) {
			        		entityplayer.playerConnection.sendPacket(packet);
			        	}
			        }
        		}
        	}
        }
    }

    public void savePlayers() {
        for (EntityPlayer entityplayer : this.players) {
            this.b(entityplayer);
        }
    }

    public void addWhitelist(GameProfile gameprofile) {
        this.whitelist.add(new WhiteListEntry(gameprofile));
    }

    public void removeWhitelist(GameProfile gameprofile) {
        this.whitelist.remove(gameprofile);
    }

    public WhiteList getWhitelist() {
        return this.whitelist;
    }

    public String[] getWhitelisted() {
        return this.whitelist.getEntries();
    }

    public OpList getOPs() {
        return this.operators;
    }

    public String[] n() {
        return this.operators.getEntries();
    }

    public void reloadWhitelist() {}

    public void b(EntityPlayer entityplayer, WorldServer worldserver) {
        entityplayer.playerConnection.sendPacket(new PacketPlayOutUpdateTime(worldserver.getTime(), worldserver.getDayTime(), worldserver.getGameRules().getBoolean("doDaylightCycle")));
        if (worldserver.Q()) {
            // CraftBukkit start - handle player weather
            // entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(1, 0.0F));
            // entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(7, worldserver.j(1.0F)));
            // entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(8, worldserver.h(1.0F)));
            entityplayer.setPlayerWeather(org.bukkit.WeatherType.DOWNFALL, false);
            // CraftBukkit end
        }
    }

    public void updateClient(EntityPlayer entityplayer) {
        entityplayer.updateInventory(entityplayer.defaultContainer);
        entityplayer.getBukkitEntity().updateScaledHealth(); // CraftBukkit - Update scaled health on respawn and worldchange
        entityplayer.playerConnection.sendPacket(new PacketPlayOutHeldItemSlot(entityplayer.inventory.itemInHandIndex));
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public String[] getSeenPlayers() {
        // CraftBukkit - fix reference to worldserver array
        return this.server.worlds.get(0).getDataManager().getPlayerFileData().getSeenPlayers();
    }

    public boolean getHasWhitelist() {
        return this.hasWhitelist;
    }

    public void setHasWhitelist(boolean flag) {
        this.hasWhitelist = flag;
    }

    public List b(String s) {
    	final List arraylist = new ArrayList();
    	final Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            if (entityplayer.s().equals(s)) {
                arraylist.add(entityplayer);
            }
        }

        return arraylist;
    }

    public int s() {
        return this.q;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public NBTTagCompound t() {
        return null;
    }

    private void a(EntityPlayer entityplayer, EntityPlayer entityplayer1, World world) {
        if (entityplayer1 != null) {
            entityplayer.playerInteractManager.setGameMode(entityplayer1.playerInteractManager.getGameMode());
        } else if (this.r != null) {
            entityplayer.playerInteractManager.setGameMode(this.r);
        }

        entityplayer.playerInteractManager.b(world.getWorldData().getGameType());
    }

    public void u() {
    	for (EntityPlayer player : this.players) {
    	    player.playerConnection.disconnect(this.server.server.getShutdownMessage());
    	}
    }

    // CraftBukkit start - Support multi-line messages
    public void sendMessage(IChatBaseComponent[] ichatbasecomponent) {
        for (IChatBaseComponent component : ichatbasecomponent) {
            sendMessage(component, true);
        }
    }
    // CraftBukkit end

    public void sendMessage(IChatBaseComponent ichatbasecomponent, boolean flag) {
        this.server.sendMessage(ichatbasecomponent);
        this.sendAll(new PacketPlayOutChat(ichatbasecomponent, flag));
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        this.sendMessage(ichatbasecomponent, true);
    }

    public ServerStatisticManager a(EntityHuman entityhuman) {
    	final UUID uuid = entityhuman.getUniqueID();
        ServerStatisticManager serverstatisticmanager = uuid == null ? null : this.statisticMap.get(uuid);

        if (serverstatisticmanager == null) {
        	final File file1 = new File(this.server.getWorldServer(0).getDataManager().getDirectory(), "stats");
        	final File file2 = new File(file1, uuid.toString() + ".json");

            if (!file2.exists()) {
            	final File file3 = new File(file1, entityhuman.getName() + ".json");

                if (file3.exists() && file3.isFile()) {
                    file3.renameTo(file2);
                }
            }

            serverstatisticmanager = new ServerStatisticManager(this.server, file2);
            serverstatisticmanager.a();
            this.statisticMap.put(uuid, serverstatisticmanager);
        }

        return serverstatisticmanager;
    }

    public void a(int i) {
        this.q = i;
        if (this.server.worldServer != null) {
        	final WorldServer[] aworldserver = this.server.worldServer;
        	final int j = aworldserver.length;

            for (int k = 0; k < j; ++k) {
                WorldServer worldserver = aworldserver[k];

                if (worldserver != null) {
                    worldserver.getPlayerChunkMap().a(i);
                }
            }
        }
    }
}
