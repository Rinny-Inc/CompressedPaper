package org.bukkit.craftbukkit.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.bukkit.Achievement;
import org.bukkit.BanList;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.Statistic.Type;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ManuallyAbandonedConversationCanceller;
import org.bukkit.craftbukkit.CraftEffect;
import org.bukkit.craftbukkit.CraftOfflinePlayer;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.CraftStatistic;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftSign;
import org.bukkit.craftbukkit.conversations.ConversationTracker;
import org.bukkit.craftbukkit.map.CraftMapView;
import org.bukkit.craftbukkit.map.RenderData;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerUnregisterChannelEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import org.spigotmc.ProtocolInjector.PacketTabHeaderFooter;
import org.spigotmc.ProtocolInjector.PacketTitle;
import org.spigotmc.ProtocolInjector.PacketTitle.EnumTitleAction;

import com.avaje.ebean.validation.NotNull;
import com.destroystokyo.paper.Title;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.AttributeInstance;
import net.minecraft.server.AttributeMapServer;
import net.minecraft.server.AttributeModifiable;
import net.minecraft.server.AttributeRanged;
import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.Container;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityProjectile;
import net.minecraft.server.EntityTracker;
import net.minecraft.server.EntityTrackerEntry;
import net.minecraft.server.EnumGamemode;
import net.minecraft.server.IAttribute;
import net.minecraft.server.IChatBaseComponent;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.Packet;
import net.minecraft.server.PacketPlayOutBlockChange;
import net.minecraft.server.PacketPlayOutChat;
import net.minecraft.server.PacketPlayOutCustomPayload;
import net.minecraft.server.PacketPlayOutEntityVelocity;
import net.minecraft.server.PacketPlayOutGameStateChange;
import net.minecraft.server.PacketPlayOutMap;
import net.minecraft.server.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.PacketPlayOutPlayerInfo;
import net.minecraft.server.PacketPlayOutPlayerInfo.PlayerInfo;
import net.minecraft.server.PacketPlayOutSpawnPosition;
import net.minecraft.server.PacketPlayOutUpdateAttributes;
import net.minecraft.server.PacketPlayOutUpdateHealth;
import net.minecraft.server.PacketPlayOutUpdateSign;
import net.minecraft.server.PacketPlayOutWorldEvent;
import net.minecraft.server.PacketPlayOutWorldParticles;
import net.minecraft.server.PlayerConnection;
import net.minecraft.server.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;

@DelegateDeserialization(CraftOfflinePlayer.class)
public class CraftPlayer extends CraftHumanEntity implements Player {
    private long firstPlayed = 0;
    private long lastPlayed = 0;
    private long lastLogout = 0;
    private boolean hasPlayedBefore = false;
    private @NotNull final ConversationTracker conversationTracker = new ConversationTracker();
    private @NotNull final Set<String> channels = new HashSet<String>();
    private @NotNull final Set<UUID> hiddenPlayers = new HashSet<UUID>();
    private int hash = 0;
    private double health = 20;
    private boolean scaledHealth = false;
    private double healthScale = 20;
    private boolean showInvisibles = false;
    private boolean hiddenFromTablist = false;
    private boolean fastOp;
    private short token;

    public CraftPlayer(CraftServer server, EntityPlayer entity) {
        super(server, entity);

        firstPlayed = System.currentTimeMillis();
        this.fastOp = server.getHandle().isOp(getProfile());
        if (this.fastOp) {
        	this.perm.recalculatePermissions(); 
        }
        this.token = 0;  
    }

    public GameProfile getProfile() {
        return getHandle().getProfile();
    }

    @Override
    public boolean isOp() {
        return this.fastOp;
	}

    @Override
    public void setOp(boolean value) {
        if (value == isOp()) return;

        if (value) {
            server.getHandle().addOp(getProfile());
        } else {
            server.getHandle().removeOp(getProfile());
        }

        this.fastOp = value;
        perm.recalculatePermissions();
    }

    public boolean isOnline() {
        return server.getHandle().uuidMap.get(getUniqueId()) != null; // PaperSpigot  - replace whole method
    }

    public InetSocketAddress getAddress() {
        if (getHandle().playerConnection == null) return null;

        final SocketAddress addr = getHandle().playerConnection.networkManager.getSocketAddress();
        if (addr instanceof InetSocketAddress) {
            return (InetSocketAddress) addr;
        }
        return null;
    }
      
	public int getProtocolVersion() {
		if (getHandle().playerConnection == null) {
			return -1;
		}
		return getHandle().playerConnection.networkManager.protocolVersions;
	}
      
	public InetSocketAddress getVirtualHost() {
		if (getHandle().playerConnection == null) {
			return null; 
		}
		return getHandle().playerConnection.networkManager.virtualHost;
	}

    @Override
    public double getEyeHeight() {
        return getEyeHeight(false);
    }

    @Override
    public double getEyeHeight(boolean ignoreSneaking) {
        if (ignoreSneaking) {
            return 1.62D;
        }
        if (isSneaking()) {
            return 1.54D;
        }
        return 1.62D;
    }

    @Override
    public void sendRawMessage(String message) {
        if (getHandle().playerConnection == null) return;

        for (IChatBaseComponent component : CraftChatMessage.fromString(message)) {
            getHandle().playerConnection.sendPacket(new PacketPlayOutChat(component));
        }
    }

    @Override
    public void sendMessage(String message) {
        if (!conversationTracker.isConversingModaly()) {
            this.sendRawMessage(message);
        }
    }

    @Override
    public void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }
    
    @Override
	public void setPlayerListHeaderFooter(BaseComponent[] header, BaseComponent[] footer) {
		if (getHandle().playerConnection.networkManager.getVersion() < 28) {
			return;
		}
		final PacketTabHeaderFooter packet = new PacketTabHeaderFooter();
		packet.header = header;
		packet.footer = footer;
		getHandle().playerConnection.sendPacket(packet);
	}
      
    @Override
	public void setPlayerListHeaderFooter(BaseComponent header, BaseComponent footer) {
		this.setPlayerListHeaderFooter(header == null ? null : new BaseComponent[]{header}, footer == null ? null : new BaseComponent[]{footer});
	}
    
    @Override
    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks) {
    	if (getHandle().playerConnection.networkManager.getVersion() < 28) {
			return;
		}
        getHandle().playerConnection.sendPacket(new PacketTitle(EnumTitleAction.TIMES, (BaseComponent[]) null, fadeInTicks, stayTicks, fadeOutTicks));
    }

    @Override
    public void setSubtitle(BaseComponent[] subtitle) {
    	if (getHandle().playerConnection.networkManager.getVersion() < 28) {
			return;
		}
        getHandle().playerConnection.sendPacket(new PacketTitle(EnumTitleAction.SUBTITLE, subtitle, 0, 0, 0));
    }

    @Override
    public void setSubtitle(BaseComponent subtitle) {
        setSubtitle(new BaseComponent[]{subtitle});
    }

    @Override
    public void showTitle(BaseComponent[] title) {
    	if (getHandle().playerConnection.networkManager.getVersion() < 28) {
			return;
		}
        getHandle().playerConnection.sendPacket(new PacketTitle(EnumTitleAction.TITLE, title, 0, 0, 0));
    }

    @Override
    public void showTitle(BaseComponent title) {
        showTitle(new BaseComponent[]{title});
    }

    @Override
    public void showTitle(BaseComponent[] title, BaseComponent[] subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        setTitleTimes(fadeInTicks, stayTicks, fadeOutTicks);
        setSubtitle(subtitle);
        showTitle(title);
    }

    @Override
    public void showTitle(BaseComponent title, BaseComponent subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        setTitleTimes(fadeInTicks, stayTicks, fadeOutTicks);
        setSubtitle(subtitle);
        showTitle(title);
    }

    @Override
    public void sendTitle(Title title) {
        Preconditions.checkNotNull(title, "Title is null");
        setTitleTimes(title.getFadeIn(), title.getStay(), title.getFadeOut());
        setSubtitle(title.getSubtitle() == null ? new BaseComponent[0] : title.getSubtitle());
        showTitle(title.getTitle());
    }

    @Override
    public void updateTitle(Title title) {
        Preconditions.checkNotNull(title, "Title is null");
        setTitleTimes(title.getFadeIn(), title.getStay(), title.getFadeOut());
        if (title.getSubtitle() != null) {
            setSubtitle(title.getSubtitle());
        }
        showTitle(title.getTitle());
    }

    @Override
    public void hideTitle() {
    	if (getHandle().playerConnection.networkManager.getVersion() < 28) {
			return;
		}
        getHandle().playerConnection.sendPacket(new PacketTitle(EnumTitleAction.CLEAR, (BaseComponent[]) null, 0, 0, 0));
    }
    // Paper end
    
    @Override
	public void sendActionBar(BaseComponent... message) {
    	if(getHandle().playerConnection == null) return;
    	if (getHandle().playerConnection.networkManager.getVersion() < 28) {
			return;
		}
    	final PacketPlayOutChat packet = new PacketPlayOutChat(null, (byte) 2);
    	packet.components = message;
    	getHandle().playerConnection.sendPacket(packet);
	}

    @Override
    public String getDisplayName() {
        return getHandle().displayName;
    }

    @Override
    public void setDisplayName(final String name) {
        getHandle().displayName = name == null ? getName() : name;
    }

    @Override
    public String getPlayerListName() {
        return getHandle().listName;
    }

    @Override
    public void setPlayerListName(String name) {
    	setPlayerListName(name, false);
    }
    
    @Override
    public void setPlayerListName(String name, boolean updateNameTag) {
    	final String oldName = getHandle().listName;

        if (name == null) {
            name = getName();
        }

        if (oldName.equals(name)) {
            return;
        }

        // Collisions will make for invisible people
        for (int i = 0; i < server.getHandle().players.size(); ++i) {
            if (((EntityPlayer) server.getHandle().players.get(i)).listName.equals(name)) {
                throw new IllegalArgumentException(name + " is already assigned as a player list name for someone");
            }
        }

        getHandle().listName = name;

        // Change the name on the client side
        // Spigot start - protocol patch
        final String temp = getHandle().listName;
        getHandle().listName = oldName;
        final PacketPlayOutPlayerInfo oldpacket = new PacketPlayOutPlayerInfo(getHandle(), PlayerInfo.REMOVE_PLAYER);
        getHandle().listName = temp; 
        final PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(getHandle(), PlayerInfo.ADD_PLAYER);
        final PacketPlayOutPlayerInfo newPacket = new PacketPlayOutPlayerInfo(getHandle(), PlayerInfo.UPDATE_DISPLAY_NAME);
        for (EntityPlayer entityplayer : server.getHandle().players) {
            if (entityplayer.playerConnection == null) continue;
            
            if (entityplayer.playerConnection.networkManager.getVersion() < 28) {
            	entityplayer.playerConnection.sendPacket( oldpacket );
            	entityplayer.playerConnection.sendPacket( packet );
            	continue;
            } 
            entityplayer.playerConnection.sendPacket( newPacket );
        }
        /*if (updateNameTag) {
        	EntityTrackerEntry tracker = (EntityTrackerEntry)(((WorldServer)this.entity.world).getTracker()).trackedEntities.get(getEntityId());
            if (tracker == null) {
            	return;
            }
            PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn((EntityHuman)getHandle());
            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(getEntityId());
            tracker.broadcastIncludingSelf(destroyPacket);
            tracker.broadcastIncludingSelf(spawnPacket);
        }*/
        // Spigot end
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OfflinePlayer)) {
            return false;
        }
        final OfflinePlayer other = (OfflinePlayer) obj;
        if ((this.getUniqueId() == null) || (other.getUniqueId() == null)) {
            return false;
        }

        final boolean uuidEquals = this.getUniqueId().equals(other.getUniqueId());
        boolean idEquals = true;

        if (other instanceof CraftPlayer) {
            idEquals = this.getEntityId() == ((CraftPlayer) other).getEntityId();
        }

        return uuidEquals && idEquals;
    }

    @Override
    public void kickPlayer(String message) {
        if (getHandle().playerConnection == null) return;

        getHandle().playerConnection.disconnect(message == null ? "" : message);
    }

    @Override
    public void setCompassTarget(Location loc) {
        if (getHandle().playerConnection == null) return;

        // Do not directly assign here, from the packethandler we'll assign it.
        getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    @Override
    public Location getCompassTarget() {
        return getHandle().compassTarget;
    }

    @Override
    public void chat(String msg) {
        if (getHandle().playerConnection == null) return;

        getHandle().playerConnection.chat(msg, false);
    }

    @Override
    public boolean performCommand(String command) {
        return server.dispatchCommand(this, command);
    }

    @Override
    public void playNote(Location loc, byte instrument, byte note) {
        if (getHandle().playerConnection == null) return;

        String instrumentName = switch (instrument) {
        	case 0 -> "harp";
        	case 1 -> "bd";
        	case 2 -> "snare";
        	case 3 -> "hat";
        	case 4 -> "bassattack";
        	default -> null;
        };
        getHandle().playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect("note."+instrumentName, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 3.0f, note));
    }

    @Override
    public void playNote(Location loc, Instrument instrument, Note note) {
        if (getHandle().playerConnection == null) return;

        String instrumentName = switch (instrument.ordinal()) {
	    	case 0 -> "harp";
	    	case 1 -> "bd";
	    	case 2 -> "snare";
	    	case 3 -> "hat";
	    	case 4 -> "bassattack";
	    	default -> null;
        };
        getHandle().playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect("note."+instrumentName, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 3.0f, note.getId()));
    }

    @Override
    public void playSound(Location loc, Sound sound, float volume, float pitch) {
        if (sound == null) {
            return;
        }
        playSound(loc, CraftSound.getSound(sound), volume, pitch);
    }

    @Override
    public void playSound(Location loc, String sound, float volume, float pitch) {
        if (loc == null || sound == null || getHandle().playerConnection == null) return;

        final double x = loc.getBlockX() + 0.5;
        final double y = loc.getBlockY() + 0.5;
        final double z = loc.getBlockZ() + 0.5;

        final PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(sound, x, y, z, volume, pitch);
        getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void playEffect(Location loc, Effect effect, int data) {
        if (getHandle().playerConnection == null) return;
        final int packetData = effect.getId();
        final PacketPlayOutWorldEvent packet = new PacketPlayOutWorldEvent(packetData, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), data, false);
        getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public <T> void playEffect(Location loc, Effect effect, T data) {
        Validate.isTrue(data != null ? data.getClass().equals(effect.getData()) : effect.getData() == null, "Wrong kind of data for this effect!");
        final int datavalue = data == null ? 0 : CraftEffect.getDataValue(effect, data);
        playEffect(loc, effect, datavalue);
    }

    @Override
    public void sendBlockChange(Location loc, Material material, byte data) {
        sendBlockChange(loc, material.getId(), data);
    }

    @Override
    public void sendBlockChange(Location loc, int material, byte data) {
        if (getHandle().playerConnection == null) return;

        final PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), ((CraftWorld) loc.getWorld()).getHandle());

        packet.block = CraftMagicNumbers.getBlock(material);
        packet.data = data;
        getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void sendSignChange(Location loc, String[] lines) {
        if (getHandle().playerConnection == null) {
            return;
        }

        if (lines == null) {
            lines = new String[4];
        }

        Validate.notNull(loc, "Location can not be null");
        if (lines.length < 4) {
            throw new IllegalArgumentException("Must have at least 4 lines");
        }

        // Limit to 15 chars per line and set null lines to blank
        final String[] astring = CraftSign.sanitizeLines(lines);

        getHandle().playerConnection.sendPacket(new PacketPlayOutUpdateSign(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), astring));
    }

    @Override
    public boolean sendChunkChange(Location loc, int sx, int sy, int sz, byte[] data) {
        if (getHandle().playerConnection == null) return false;

        /*
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        int cx = x >> 4;
        int cz = z >> 4;

        if (sx <= 0 || sy <= 0 || sz <= 0) {
            return false;
        }

        if ((x + sx - 1) >> 4 != cx || (z + sz - 1) >> 4 != cz || y < 0 || y + sy > 128) {
            return false;
        }

        if (data.length != (sx * sy * sz * 5) / 2) {
            return false;
        }

        Packet51MapChunk packet = new Packet51MapChunk(x, y, z, sx, sy, sz, data);

        getHandle().playerConnection.sendPacket(packet);

        return true;
        */

        throw new NotImplementedException("Chunk changes do not yet work"); // TODO: Chunk changes.
    }

    @Override
    public void sendMap(MapView map) {
        if (getHandle().playerConnection == null) return;

        final RenderData data = ((CraftMapView) map).render(this);
        for (int x = 0; x < 128; ++x) {
        	final byte[] bytes = new byte[131];
            bytes[1] = (byte) x;
            for (int y = 0; y < 128; ++y) {
                bytes[y + 3] = data.buffer[y * 128 + x];
            }
            final PacketPlayOutMap packet = new PacketPlayOutMap(map.getId(), bytes, map.getScale().getValue()); // Spigot - protocol patch
            getHandle().playerConnection.sendPacket(packet);
        }
    }

    @Override
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause) {
    	final EntityPlayer entity = getHandle();

        if (getHealth() == 0 || entity.dead) {
            return false;
        }

        if (entity.playerConnection == null || entity.playerConnection.isDisconnected()) {
            return false;
        }

        if (entity.passenger != null) {
            return false;
        }

        // From = Players current Location
        Location from = this.getLocation();
        // To = Players new Location if Teleport is Successful
        Location to = location;
        // Create & Call the Teleport Event.
        final PlayerTeleportEvent event = new PlayerTeleportEvent(this, from, to, cause);
        server.getPluginManager().callEvent(event);

        // Return False to inform the Plugin that the Teleport was unsuccessful/cancelled.
        if (event.isCancelled()) {
            return false;
        }
        // If this player is riding another entity, we must dismount before teleporting.
        entity.mount(null);

        // PaperSpigot start
        Entity vehicle = entity.vehicle;
        Entity passenger = entity.passenger;
        if (vehicle != null) {
            vehicle.passenger = null;
            vehicle.teleportTo(location, false);
            vehicle = vehicle.getBukkitEntity().getHandle();
            entity.vehicle = vehicle;
            vehicle.passenger = entity;
        }

        if (passenger != null) {
            passenger.vehicle = null;
            passenger.teleportTo(location, false);
            passenger = passenger.getBukkitEntity().getHandle();
            entity.passenger = passenger;
            entity.vehicle = entity;
        }
        // PaperSpigot end

        // Update the From Location
        from = event.getFrom();
        // Grab the new To Location dependent on whether the event was cancelled.
        to = event.getTo();
        // Grab the To and From World Handles.
        final WorldServer fromWorld = ((CraftWorld) from.getWorld()).getHandle();
        final WorldServer toWorld = ((CraftWorld) to.getWorld()).getHandle();

        // Rinny - load chunk before tp
        final Chunk chunk = to.getChunk();
        if (!chunk.isLoaded()) {
        	chunk.load();
        }
        // Rinny
        
        // Close any foreign inventory
        if (getHandle().activeContainer != getHandle().defaultContainer) {
            getHandle().closeInventory();
        }
        
        // Check if the fromWorld and toWorld are the same.
        if (fromWorld == toWorld) {
            entity.playerConnection.teleport(to);
        } else {
            server.getHandle().moveToWorld(entity, toWorld.dimension, true, to, false); // Fix teleportation glitch: https://github.com/IPVP-MC/Paper-1.7/commit/729d4d8e59f79126854d234705973f1d1f1d94ac
        }

        // PaperSpigot start
        if (vehicle != null) {
            vehicle.retrack();
            //entity.retrack();
        }
        if (passenger != null) {
            passenger.retrack();
        }
        // PaperSpigot end
        return true;
    }

    @Override
    public void setSneaking(boolean sneak) {
        getHandle().setSneaking(sneak);
    }

    @Override
    public boolean isSneaking() {
        return getHandle().isSneaking();
    }

    @Override
    public boolean isSprinting() {
        return getHandle().isSprinting();
    }

    @Override
    public void setSprinting(boolean sprinting) {
        getHandle().setSprinting(sprinting);
    }

    @Override
    public void loadData() {
        server.getHandle().playerFileData.load(getHandle());
    }

    @Override
    public void saveData() {
        server.getHandle().playerFileData.save(getHandle());
    }

    @Deprecated
    @Override
    public void updateInventory() {
        getHandle().updateInventory(getHandle().activeContainer);
    }

    @Override
    public void setSleepingIgnored(boolean isSleeping) {
        getHandle().fauxSleeping = isSleeping;
        ((CraftWorld) getWorld()).getHandle().checkSleepStatus();
    }

    @Override
    public boolean isSleepingIgnored() {
        return getHandle().fauxSleeping;
    }

    @Override
    public void awardAchievement(Achievement achievement) {
        Validate.notNull(achievement, "Achievement cannot be null");
        if (achievement.hasParent() && !hasAchievement(achievement.getParent())) {
            awardAchievement(achievement.getParent());
        }
        getHandle().getStatisticManager().setStatistic(getHandle(), CraftStatistic.getNMSAchievement(achievement), 1);
        getHandle().getStatisticManager().updateStatistics(getHandle());
    }

    @Override
    public void removeAchievement(Achievement achievement) {
        Validate.notNull(achievement, "Achievement cannot be null");
        for (Achievement achieve : Achievement.values()) {
            if (achieve.getParent() == achievement && hasAchievement(achieve)) {
                removeAchievement(achieve);
            }
        }
        getHandle().getStatisticManager().setStatistic(getHandle(), CraftStatistic.getNMSAchievement(achievement), 0);
    }

    @Override
    public boolean hasAchievement(Achievement achievement) {
        Validate.notNull(achievement, "Achievement cannot be null");
        return getHandle().getStatisticManager().hasAchievement(CraftStatistic.getNMSAchievement(achievement));
    }

    @Override
    public void incrementStatistic(Statistic statistic) {
        incrementStatistic(statistic, 1);
    }

    @Override
    public void decrementStatistic(Statistic statistic) {
        decrementStatistic(statistic, 1);
    }

    @Override
    public int getStatistic(Statistic statistic) {
        Validate.notNull(statistic, "Statistic cannot be null");
        Validate.isTrue(statistic.getType() == Type.UNTYPED, "Must supply additional paramater for this statistic");
        return getHandle().getStatisticManager().getStatisticValue(CraftStatistic.getNMSStatistic(statistic));
    }

    @Override
    public void incrementStatistic(Statistic statistic, int amount) {
        Validate.isTrue(amount > 0, "Amount must be greater than 0");
        setStatistic(statistic, getStatistic(statistic) + amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, int amount) {
        Validate.isTrue(amount > 0, "Amount must be greater than 0");
        setStatistic(statistic, getStatistic(statistic) - amount);
    }

    @Override
    public void setStatistic(Statistic statistic, int newValue) {
        Validate.notNull(statistic, "Statistic cannot be null");
        Validate.isTrue(statistic.getType() == Type.UNTYPED, "Must supply additional paramater for this statistic");
        Validate.isTrue(newValue >= 0, "Value must be greater than or equal to 0");
        final net.minecraft.server.Statistic nmsStatistic = CraftStatistic.getNMSStatistic(statistic);
        getHandle().getStatisticManager().setStatistic(getHandle(), nmsStatistic, newValue);
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) {
        incrementStatistic(statistic, material, 1);
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) {
        decrementStatistic(statistic, material, 1);
    }

    @Override
    public int getStatistic(Statistic statistic, Material material) {
        Validate.notNull(statistic, "Statistic cannot be null");
        Validate.notNull(material, "Material cannot be null");
        Validate.isTrue(statistic.getType() == Type.BLOCK || statistic.getType() == Type.ITEM, "This statistic does not take a Material parameter");
        final net.minecraft.server.Statistic nmsStatistic = CraftStatistic.getMaterialStatistic(statistic, material);
        Validate.notNull(nmsStatistic, "The supplied Material does not have a corresponding statistic");
        return getHandle().getStatisticManager().getStatisticValue(nmsStatistic);
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int amount) {
        Validate.isTrue(amount > 0, "Amount must be greater than 0");
        setStatistic(statistic, material, getStatistic(statistic, material) + amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int amount) {
        Validate.isTrue(amount > 0, "Amount must be greater than 0");
        setStatistic(statistic, material, getStatistic(statistic, material) - amount);
    }

    @Override
    public void setStatistic(Statistic statistic, Material material, int newValue) {
        Validate.notNull(statistic, "Statistic cannot be null");
        Validate.notNull(material, "Material cannot be null");
        Validate.isTrue(newValue >= 0, "Value must be greater than or equal to 0");
        Validate.isTrue(statistic.getType() == Type.BLOCK || statistic.getType() == Type.ITEM, "This statistic does not take a Material parameter");
        final net.minecraft.server.Statistic nmsStatistic = CraftStatistic.getMaterialStatistic(statistic, material);
        Validate.notNull(nmsStatistic, "The supplied Material does not have a corresponding statistic");
        getHandle().getStatisticManager().setStatistic(getHandle(), nmsStatistic, newValue);
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) {
        incrementStatistic(statistic, entityType, 1);
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) {
        decrementStatistic(statistic, entityType, 1);
    }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) {
        Validate.notNull(statistic, "Statistic cannot be null");
        Validate.notNull(entityType, "EntityType cannot be null");
        Validate.isTrue(statistic.getType() == Type.ENTITY, "This statistic does not take an EntityType parameter");
        final net.minecraft.server.Statistic nmsStatistic = CraftStatistic.getEntityStatistic(statistic, entityType);
        Validate.notNull(nmsStatistic, "The supplied EntityType does not have a corresponding statistic");
        return getHandle().getStatisticManager().getStatisticValue(nmsStatistic);
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        Validate.isTrue(amount > 0, "Amount must be greater than 0");
        setStatistic(statistic, entityType, getStatistic(statistic, entityType) + amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        Validate.isTrue(amount > 0, "Amount must be greater than 0");
        setStatistic(statistic, entityType, getStatistic(statistic, entityType) - amount);
    }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
        Validate.notNull(statistic, "Statistic cannot be null");
        Validate.notNull(entityType, "EntityType cannot be null");
        Validate.isTrue(newValue >= 0, "Value must be greater than or equal to 0");
        Validate.isTrue(statistic.getType() == Type.ENTITY, "This statistic does not take an EntityType parameter");
        final net.minecraft.server.Statistic nmsStatistic = CraftStatistic.getEntityStatistic(statistic, entityType);
        Validate.notNull(nmsStatistic, "The supplied EntityType does not have a corresponding statistic");
        getHandle().getStatisticManager().setStatistic(getHandle(), nmsStatistic, newValue);
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        getHandle().timeOffset = time;
        getHandle().relativeTime = relative;
    }

    @Override
    public long getPlayerTimeOffset() {
        return getHandle().timeOffset;
    }

    @Override
    public long getPlayerTime() {
        return getHandle().getPlayerTime();
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return getHandle().relativeTime;
    }

    @Override
    public void resetPlayerTime() {
        setPlayerTime(0, true);
    }

    @Override
    public void setPlayerWeather(WeatherType type) {
        getHandle().setPlayerWeather(type, true);
    }

    @Override
    public WeatherType getPlayerWeather() {
        return getHandle().getPlayerWeather();
    }

    @Override
    public void resetPlayerWeather() {
        getHandle().resetPlayerWeather();
    }

    @Override
    public boolean isBanned() {
        return server.getBanList(BanList.Type.NAME).isBanned(getName());
    }

    @Override
    public void setBanned(boolean value) {
        if (value) {
            server.getBanList(BanList.Type.NAME).addBan(getName(), null, null, null);
        } else {
            server.getBanList(BanList.Type.NAME).pardon(getName());
        }
    }
    
    @Override
    public void setBanned(String reason, Date expirement, String source) {
    	if (isBanned()) {
    		return;
    	}
        server.getBanList(BanList.Type.NAME).addBan(getName(), reason, expirement, source);
    }

    @Override
    public boolean isWhitelisted() {
        return server.getHandle().getWhitelist().isWhitelisted(getProfile());
    }

    @Override
    public void setWhitelisted(boolean value) {
        if (value) {
            server.getHandle().addWhitelist(getProfile());
        } else {
            server.getHandle().removeWhitelist(getProfile());
        }
    }

    @Override
    public void setGameMode(GameMode mode) {
        if (getHandle().playerConnection == null) return;

        if (mode == null) {
            throw new IllegalArgumentException("Mode cannot be null");
        }

        if (mode != getGameMode()) {
        	final PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(this, mode);
            server.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            getHandle().playerInteractManager.setGameMode(EnumGamemode.getById(mode.getValue()));
            getHandle().fallDistance = 0;
            getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, mode.getValue()));
        }
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.getByValue(getHandle().playerInteractManager.getGameMode().getId());
    }

    public void giveExp(int exp) {
        getHandle().giveExp(exp);
    }

    public void giveExpLevels(int levels) {
        getHandle().levelDown(levels);
    }

    public float getExp() {
        return getHandle().exp;
    }

    public void setExp(float exp) {
        getHandle().exp = exp;
        getHandle().lastSentExp = -1;
    }

    public int getLevel() {
        return getHandle().expLevel;
    }

    public void setLevel(int level) {
        getHandle().expLevel = level;
        getHandle().lastSentExp = -1;
    }

    public int getTotalExperience() {
        return getHandle().expTotal;
    }

    public void setTotalExperience(int exp) {
        getHandle().expTotal = exp;
    }

    public float getExhaustion() {
        return getHandle().getFoodData().exhaustionLevel;
    }

    public void setExhaustion(float value) {
        getHandle().getFoodData().exhaustionLevel = value;
    }

    public float getSaturation() {
        return getHandle().getFoodData().saturationLevel;
    }

    public void setSaturation(float value) {
        getHandle().getFoodData().saturationLevel = value;
        getHandle().triggerHealthUpdate(); // Rinny fix saturation desync
    }

    public int getFoodLevel() {
        return getHandle().getFoodData().foodLevel;
    }

    public void setFoodLevel(int value) {
        getHandle().getFoodData().foodLevel = value;
    }

    public Location getBedSpawnLocation() {
    	final World world = getServer().getWorld(getHandle().spawnWorld);
        ChunkCoordinates bed = getHandle().getBed();

        if (world != null && bed != null) {
            bed = EntityHuman.getBed(((CraftWorld) world).getHandle(), bed, getHandle().isRespawnForced());
            if (bed != null) {
                return new Location(world, bed.x, bed.y, bed.z);
            }
        }
        return null;
    }

    public void setBedSpawnLocation(Location location) {
        setBedSpawnLocation(location, false);
    }

    public void setBedSpawnLocation(Location location, boolean override) {
        if (location == null) {
            getHandle().setRespawnPosition(null, override);
        } else {
            getHandle().setRespawnPosition(new ChunkCoordinates(location.getBlockX(), location.getBlockY(), location.getBlockZ()), override);
            getHandle().spawnWorld = location.getWorld().getName();
        }
    }

    @Override
    public void hidePlayer(Player player) {
        hidePlayer(player, true);
    }
    
    @Override
    public void hidePlayer(Player player, boolean hideFromTab) {
    	Validate.notNull(player, "hidden player cannot be null");
        if (getHandle().playerConnection == null) return;
        if (equals(player)) return;
        if (!this.hiddenPlayers.add(player.getUniqueId())) return; 
        
        //remove this player from the hidden player's EntityTrackerEntry
        final EntityTracker tracker = ((WorldServer) entity.world).tracker;
        final EntityPlayer other = ((CraftPlayer) player).getHandle();
        final EntityTrackerEntry entry = (EntityTrackerEntry) tracker.trackedEntities.get(other.getId());
        if (entry != null) {
            entry.clear(getHandle());
        }

        if (hideFromTab) {
        	getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(((CraftPlayer)player).getHandle(), PlayerInfo.REMOVE_PLAYER)); // Spigot - protocol patch
        }
    }

    @Override
    public void showPlayer(Player player) {
        Validate.notNull(player, "shown player cannot be null");
        if (getHandle().playerConnection == null) return;
        if (equals(player)) return;
        if (!this.hiddenPlayers.remove(player.getUniqueId())) return; 
        
        final EntityTracker tracker = ((WorldServer) entity.world).tracker;
        final EntityPlayer other = ((CraftPlayer) player).getHandle();
        final EntityTrackerEntry entry = (EntityTrackerEntry) tracker.trackedEntities.get(other.getId());
        if (entry != null && !entry.trackedPlayers.contains(getHandle())) {
            entry.updatePlayer(getHandle());
        }

        /*if (this.hiddenFromTablist) {
        	getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(((CraftPlayer)player).getHandle(), PlayerInfo.ADD_PLAYER)); // Spigot - protocol patch
        }*/
    }
    
    public void removeDisconnectingPlayer(Player player) {
    	if (!hiddenPlayers.contains(player.getUniqueId())) {
    		return;
    	}
    	hiddenPlayers.remove(player.getUniqueId());
    }
    
	public boolean canSee(org.bukkit.entity.Entity entity) {
		final Entity nmsEntity = ((CraftEntity)entity).getHandle();
		if (nmsEntity instanceof EntityItem entityItem) {
			if (entityItem.owner instanceof EntityPlayer) {
				return canSee(((EntityPlayer)entityItem.owner).getBukkitEntity()); 
			}
		}
		if (nmsEntity instanceof EntityProjectile entityProjectile) {
			if (entityProjectile.getShooter() instanceof EntityPlayer) {
				return canSee(((EntityPlayer)entityProjectile.getShooter()).getBukkitEntity()); 
			}
		} 
		if (nmsEntity instanceof EntityArrow entityArrow) {
			if (entityArrow.shooter instanceof EntityPlayer && !((EntityPlayer)entityArrow.shooter).getBukkitEntity().canSee(this)) {
				return canSee(((EntityPlayer)entityArrow.shooter).getBukkitEntity()); 
			}
		} 
		return (!(entity instanceof Player) || canSee((Player)entity));
	}
	
	public void hideFromTablist(boolean value) {
		this.hiddenFromTablist = value;
		for (EntityPlayer player : getHandle().server.getPlayerList().players) {
			if (value) {
				player.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(getHandle(), PlayerInfo.REMOVE_PLAYER));
				continue;
			}
			player.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(getHandle(), PlayerInfo.ADD_PLAYER));
		}
	}

    public boolean canSee(Player player) {
        return !this.hiddenPlayers.contains(player.getUniqueId());
    }
    
	public boolean canSeeInvisibles() {
		return this.showInvisibles;
	}
      
	// Badlion
	public void showInvisibles(boolean show) {
		/*if (show == this.showInvisibles) {
			return;
		}
		this.showInvisibles = show;
		final EntityTracker tracker = ((WorldServer)this.entity.world).tracker;
		for (Entity entity : getHandle().world.entityList) {
			if (!entity.isInvisible()) {
				continue;
			}
			final EntityTrackerEntry trackerEntry = (EntityTrackerEntry)tracker.trackedEntities.get(entity.getId());
			if (trackerEntry != null && trackerEntry.trackedPlayers.contains(getHandle())) {
				getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true)); 
			}
		} */
	}

    public Map<String, Object> serialize() {
    	final Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", getName());
        return result;
    }

    public Player getPlayer() {
        return this;
    }

    @Override
    public EntityPlayer getHandle() {
        return (EntityPlayer) entity;
    }

    public void setHandle(final EntityPlayer entity) {
        super.setHandle(entity);
    }

    @Override
    public String toString() {
        return "CraftPlayer{" + "name=" + getName() + '}';
    }

    @Override
    public int hashCode() {
        if (hash == 0 || hash == 485) {
            hash = 97 * 5 + (this.getUniqueId() != null ? this.getUniqueId().hashCode() : 0);
        }
        return hash;
    }

    public long getFirstPlayed() {
        return firstPlayed;
    }

    @Deprecated
    public long getLastPlayed() {
        return getLastLogin();
    }
    
    @Override
	public long getLastLogin() {
    	return lastPlayed;
	}

	@Override
	public long getLastLogout() {
		return lastLogout;
	}

    public boolean hasPlayedBefore() {
        return hasPlayedBefore;
    }

    public void setFirstPlayed(long firstPlayed) {
        this.firstPlayed = firstPlayed;
    }

    public void readExtraData(NBTTagCompound nbttagcompound) {
        hasPlayedBefore = true;
        if (nbttagcompound.hasKey("bukkit")) {
        	final NBTTagCompound data = nbttagcompound.getCompound("bukkit");

            if (data.hasKey("firstPlayed")) {
                firstPlayed = data.getLong("firstPlayed");
                lastPlayed = data.getLong("lastPlayed");
            }
            
            if (data.hasKey("lastLogout")) {
            	lastLogout = data.getLong("lastLogout");
            }

            if (data.hasKey("newExp")) {
            	final EntityPlayer handle = getHandle();
                handle.newExp = data.getInt("newExp");
                handle.newTotalExp = data.getInt("newTotalExp");
                handle.newLevel = data.getInt("newLevel");
                handle.expToDrop = data.getInt("expToDrop");
                handle.keepLevel = data.getBoolean("keepLevel");
            }
            if (data.hasKey("token")) {
            	token = data.getShort("token");
            }
        }
    }

    public void setExtraData(NBTTagCompound nbttagcompound) {
        if (!nbttagcompound.hasKey("bukkit")) {
            nbttagcompound.set("bukkit", new NBTTagCompound());
        }

        final NBTTagCompound data = nbttagcompound.getCompound("bukkit");
        final EntityPlayer handle = getHandle();
        data.setInt("newExp", handle.newExp);
        data.setInt("newTotalExp", handle.newTotalExp);
        data.setInt("newLevel", handle.newLevel);
        data.setInt("expToDrop", handle.expToDrop);
        data.setBoolean("keepLevel", handle.keepLevel);
        data.setLong("firstPlayed", getFirstPlayed());
        data.setLong("lastPlayed", this.lastPlayed);
        data.setLong("lastLogout", this.lastLogout);
        data.setString("lastKnownName", handle.getName());
        if (token != 0) {
        	data.setShort("token", this.token);
        }
    }

    public boolean beginConversation(Conversation conversation) {
        return conversationTracker.beginConversation(conversation);
    }

    public void abandonConversation(Conversation conversation) {
        conversationTracker.abandonConversation(conversation, new ConversationAbandonedEvent(conversation, new ManuallyAbandonedConversationCanceller()));
    }

    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
        conversationTracker.abandonConversation(conversation, details);
    }

    public void acceptConversationInput(String input) {
        conversationTracker.acceptConversationInput(input);
    }

    public boolean isConversing() {
        return conversationTracker.isConversing();
    }

    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(server.getMessenger(), source, channel, message);
        if (getHandle().playerConnection == null) return;

        if (channels.contains(channel)) {
        	final PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(channel, message);
            getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void setTexturePack(String url) {
        setResourcePack(url);
    }

    @Override
    public void setResourcePack(String url) {
        Validate.notNull(url, "Resource pack URL cannot be null");

        getHandle().setResourcePack(url);
    }

    public void addChannel(String channel) {
        com.google.common.base.Preconditions.checkState( channels.size() < 128, "Too many channels registered" ); // Spigot
        if (channels.add(channel)) {
            server.getPluginManager().callEvent(new PlayerRegisterChannelEvent(this, channel));
        }
    }

    public void removeChannel(String channel) {
        if (channels.remove(channel)) {
            server.getPluginManager().callEvent(new PlayerUnregisterChannelEvent(this, channel));
        }
    }

    public Set<String> getListeningPluginChannels() {
        return ImmutableSet.copyOf(channels);
    }

    public void sendSupportedChannels() {
        if (getHandle().playerConnection == null) return;
        final Set<String> listening = server.getMessenger().getIncomingChannels();

        if (!listening.isEmpty()) {
        	final ByteArrayOutputStream stream = new ByteArrayOutputStream();

            for (String channel : listening) {
                try {
                    stream.write(channel.getBytes("UTF8"));
                    stream.write((byte) 0);
                } catch (IOException ex) {
                    Logger.getLogger(CraftPlayer.class.getName()).log(Level.SEVERE, "Could not send Plugin Channel REGISTER to " + getName(), ex);
                }
            }

            getHandle().playerConnection.sendPacket(new PacketPlayOutCustomPayload("REGISTER", stream.toByteArray()));
        }
    }
    
    // Kohi start
	@Override
	public void setVelocity(Vector vel) {
		// To be consistent with old behavior, set the velocity before firing the event
		this.setVelocityDirect(vel);
		final PlayerVelocityEvent event = new PlayerVelocityEvent(this, vel.clone());
		this.getServer().getPluginManager().callEvent(event);
		if(!event.isCancelled()) {
			// Set the velocity again in case it was changed by event handlers
			this.setVelocityDirect(event.getVelocity());
			// Send the new velocity to the player's client immediately, so it isn't affected by
			// any movement packets from this player that may be processed before the end of the tick.
			// Without this, player velocity changes tend to be very inconsistent.
			this.getHandle().playerConnection.sendPacket(new PacketPlayOutEntityVelocity(this.getHandle()));
		}
		// Note that cancelling the event does not restore the old velocity, it only prevents
		// the packet from sending. Again, this is to be consistent with old behavior.
	}
 	public void setVelocityDirect(Vector vel) {
 		entity.motX = vel.getX();
 		entity.motY = vel.getY();
 		entity.motZ = vel.getZ();
 		if (this.getHandle().world.paperSpigotConfig.knockbackResetFallDistance && entity.motY > 0) {
 			entity.fallDistance = 0.0f;
 		}
 	}
	// Kohi end

    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        server.getPlayerMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return server.getPlayerMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return server.getPlayerMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        server.getPlayerMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }
    
    @Override
    public boolean setWindowProperty(Property prop, int value) {
    	final Container container = getHandle().activeContainer;
        if (container.getBukkitView().getType() != prop.getType()) {
            return false;
        }
        getHandle().setContainerData(container, prop.getId(), value);
        return true;
    }
    
    public void connect() {
    	this.lastPlayed = System.currentTimeMillis();
    }

    public void disconnect(String reason) {
    	this.lastLogout = System.currentTimeMillis();
        conversationTracker.abandonAllConversations();
        perm.clearPermissions();
    }

    public boolean isFlying() {
        return getHandle().abilities.isFlying;
    }

    public void setFlying(boolean value) {
    	final boolean needsUpdate = getHandle().abilities.isFlying != value; // PaperSpigot - Only refresh abilities if needed
        if (!getAllowFlight() && value) {
            throw new IllegalArgumentException("Cannot make player fly if getAllowFlight() is false");
        }

        getHandle().abilities.isFlying = value;
        if (needsUpdate) getHandle().updateAbilities(); // PaperSpigot - Only refresh abilities if needed
    }

    public boolean getAllowFlight() {
        return getHandle().abilities.canFly;
    }

    public void setAllowFlight(boolean value) {
        if (isFlying() && !value) {
            getHandle().abilities.isFlying = false;
        }

        getHandle().abilities.canFly = value;
        getHandle().updateAbilities();
    }

    @Override
    public int getNoDamageTicks() {
    	return Math.max(getHandle().invulnerableTicks, Math.max(0, getHandle().noDamageTicks - getHandle().maxNoDamageTicks / 2));
    }

    public void setFlySpeed(float value) {
        validateSpeed(value);
        final EntityPlayer player = getHandle();
        player.abilities.flySpeed = Math.max( value, 0.0001f ) / 2f; // Spigot
        player.updateAbilities();

    }

    public void setWalkSpeed(float value) {
        validateSpeed(value);
        final EntityPlayer player = getHandle();
        player.abilities.walkSpeed = Math.max( value, 0.0001f ) / 2f; // Spigot
        player.updateAbilities();
    }

    public float getFlySpeed() {
        return getHandle().abilities.flySpeed * 2f;
    }

    public float getWalkSpeed() {
        return getHandle().abilities.walkSpeed * 2f;
    }

    private void validateSpeed(float value) {
        if (value < 0) {
            if (value < -1f) {
                throw new IllegalArgumentException(value + " is too low");
            }
        } else {
            if (value > 1f) {
                throw new IllegalArgumentException(value + " is too high");
            }
        }
    }

    @Override
    public void setMaxHealth(double amount) {
        super.setMaxHealth(amount);
        this.health = Math.min(this.health, health);
        getHandle().triggerHealthUpdate();
    }

    @Override
    public void resetMaxHealth() {
        super.resetMaxHealth();
        getHandle().triggerHealthUpdate();
    }

    public CraftScoreboard getScoreboard() {
        return this.server.getScoreboardManager().getPlayerBoard(this);
    }

    public void setScoreboard(Scoreboard scoreboard) {
        Validate.notNull(scoreboard, "Scoreboard cannot be null");
        final PlayerConnection playerConnection = getHandle().playerConnection;
        if (playerConnection == null) {
            throw new IllegalStateException("Cannot set scoreboard yet");
        }
        /*if (playerConnection.isDisconnected()) {
            // throw new IllegalStateException("Cannot set scoreboard for invalid CraftPlayer"); // Spigot - remove this as Mojang's semi asynchronous Netty implementation can lead to races
        }*/

        this.server.getScoreboardManager().setPlayerBoard(this, scoreboard);
    }

    public void setHealthScale(double value) {
        Validate.isTrue((float) value > 0F, "Must be greater than 0");
        healthScale = value;
        scaledHealth = true;
        updateScaledHealth();
    }

    public double getHealthScale() {
        return healthScale;
    }

    public void setHealthScaled(boolean scale) {
        if (scaledHealth != (scaledHealth = scale)) {
            updateScaledHealth();
        }
    }

    public boolean isHealthScaled() {
        return scaledHealth;
    }

    public float getScaledHealth() {
        return (float) (isHealthScaled() ? getHealth() * getHealthScale() / getMaxHealth() : getHealth());
    }

    @Override
    public double getHealth() {
        return health;
    }

    public void setRealHealth(double health) {
        this.health = health;
    }

    public void updateScaledHealth() {
    	final AttributeMapServer attributemapserver = (AttributeMapServer) getHandle().getAttributeMap();
        final Set set = attributemapserver.getAttributes();

        injectScaledMaxHealth(set, true);

        getHandle().getDataWatcher().watch(6, (float) getScaledHealth());
        getHandle().playerConnection.sendPacket(new PacketPlayOutUpdateHealth(getScaledHealth(), getHandle().getFoodData().getFoodLevel(), getHandle().getFoodData().getSaturationLevel()));
        getHandle().playerConnection.sendPacket(new PacketPlayOutUpdateAttributes(getHandle().getId(), set));

        set.clear();
        getHandle().maxHealthCache = getMaxHealth();
    }

    public void injectScaledMaxHealth(Collection collection, boolean force) {
        if (!scaledHealth && !force) {
            return;
        }
        for (Object genericInstance : collection) {
        	final IAttribute attribute = ((AttributeInstance) genericInstance).getAttribute();
            if (attribute.getName().equals("generic.maxHealth")) {
                collection.remove(genericInstance);
                break;
            }
        }
        // Spigot start
        double healthMod = scaledHealth ? healthScale : getMaxHealth();
        if ( healthMod >= Float.MAX_VALUE || healthMod <= 0 )
        {
            healthMod = 20; // Reset health
            getServer().getLogger().warning( getName() + " tried to crash the server with a large health attribute" );
        }
        collection.add(new AttributeModifiable(getHandle().getAttributeMap(), (new AttributeRanged("generic.maxHealth", healthMod, 0.0D, Float.MAX_VALUE)).a("Max Health").a(true)));
        // Spigot end
    }
    
    @Override
    public short getPing() {
    	return this.getHandle().ping;
    }
    
    @Override
    public boolean isHiddenFromTablist() {
    	return this.hiddenFromTablist;
    }
    
    @Override
	public void setBossBar(String message, float health) {
		getHandle().setBossBar(message, health);
	}
      
    @Override
	public void removeBossBar() {
		getHandle().removeBossBar();
	}
    
    @Override
    public short getToken() {
    	return this.token;
    }
    @Override
    public void addToken(short token) {
    	this.token += token;
    }
    @Override
    public void removeToken() {
    	this.token--;
    }

    // Spigot start
    private final Player.Spigot spigot = new Player.Spigot()
    {

        @Override
        public InetSocketAddress getRawAddress()
        {
            return (InetSocketAddress) getHandle().playerConnection.networkManager.getRawAddress();
        }

        @Override
        public boolean getCollidesWithEntities()
        {
            return getHandle().collidesWithEntities;
        }

        @Override
        public void setCollidesWithEntities(boolean collides)
        {
            getHandle().collidesWithEntities = collides;
            getHandle().k = collides; // First boolean of Entity
        }

        @Override
        public void respawn()
        {
            if ( getHealth() <= 0 && isOnline() )
            {
                server.getServer().getPlayerList().moveToWorld( getHandle(), 0, false );
            }
        }

        @Override
        public void playEffect( Location location, Effect effect, int id, int data, float offsetX, float offsetY, float offsetZ, float speed, int particleCount, int radius )
        {
            Validate.notNull( location, "Location cannot be null" );
            Validate.notNull( effect, "Effect cannot be null" );
            Validate.notNull( location.getWorld(), "World cannot be null" );
            Packet packet;
            if ( effect.getType() != Effect.Type.PARTICLE )
            {
            	final int packetData = effect.getId();
                packet = new PacketPlayOutWorldEvent( packetData, location.getBlockX(), location.getBlockY(), location.getBlockZ(), id, false );
            } else
            {
            	final StringBuilder particleFullName = new StringBuilder();
                particleFullName.append( effect.getName() );
                if ( effect.getData() != null && ( effect.getData().equals( Material.class ) || effect.getData().equals( org.bukkit.material.MaterialData.class ) ) )
                {
                    particleFullName.append( '_' ).append( id );
                }
                if ( effect.getData() != null && effect.getData().equals( org.bukkit.material.MaterialData.class ) )
                {
                    particleFullName.append( '_' ).append( data );
                }
                packet = new PacketPlayOutWorldParticles( particleFullName.toString(), (float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, particleCount );
            }
            int distance;
            radius *= radius;
            if ( getHandle().playerConnection == null )
            {
                return;
            }
            if ( !location.getWorld().equals( getWorld() ) )
            {
                return;
            }

            distance = (int) getLocation().distanceSquared( location );
            if ( distance <= radius )
            {
                getHandle().playerConnection.sendPacket( packet );
            }
        }

        @Override
        public String getLocale()
        {
           return getHandle().locale;
        }

        @Override
        public Set<Player> getHiddenPlayers() {
        	return java.util.Collections.unmodifiableSet(getServer().getOnlinePlayers().stream().filter(Player::isHiddenFromTablist).collect(Collectors.toSet()));
        }

        /**
         * Sends the component to the player
         *
         * @param component the components to send
         */
        @Override
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent component)
        {
            sendMessage( new BaseComponent[] { component } );
        }

        /**
         * Sends an array of components as a single message to the
         * player
         *
         * @param components the components to send
         */
        @Override
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent ...components)
        {
            if ( getHandle().playerConnection == null ) return;

            final PacketPlayOutChat packet = new PacketPlayOutChat();
            packet.components = components;
            getHandle().playerConnection.sendPacket( packet );
        }
        
        // PaperSpigot start - Add affects spawning API
        public void setAffectsSpawning(boolean affects) {
            getHandle().affectsSpawning = affects;
        }

        public boolean getAffectsSpawning() {
            return getHandle().affectsSpawning;
        }
        // PaperSpigot end

        // PaperSpigot start - Player view distance API
        @Override
        public int getViewDistance() {
            return getHandle().viewDistance;
        }
        // PaperSpigot end
    };

    public Player.Spigot spigot()
    {
        return spigot;
    }
    // Spigot end
}
