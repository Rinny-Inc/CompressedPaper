package io.noks.cb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import com.avaje.ebean.validation.NotNull;

import io.noks.Hologram;
import net.minecraft.server.Chunk;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityAgeable;
import net.minecraft.server.EntityInsentient;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MathHelper;
import net.minecraft.server.PacketPlayOutAttachEntity;
import net.minecraft.server.PacketPlayOutEntityDestroy;
import net.minecraft.server.PacketPlayOutEntityMetadata;
import net.minecraft.server.PacketPlayOutEntityTeleport;
import net.minecraft.server.PacketPlayOutSpawnEntity;
import net.minecraft.server.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.WorldServer;

public class CraftHologram implements Hologram {
	private static final List<CraftHologram> holograms = Collections.synchronizedList(new ArrayList<>());
	private String text;
	private Location location;
	private final int entityId;
	private @NotNull final UUID uuid;
	private Set<EntityPlayer> viewers = Collections.newSetFromMap(new WeakHashMap<>());
	private final int viewDistance = 32;
	private Hologram.MessageHandler messageHandler;
	private int playerSearchTick = 0;

	public CraftHologram(Location location, String text) {
		this.text = text;
		this.location = location;
		this.entityId = Entity.entityCount;
		Entity.entityCount += 2;
		this.uuid = UUID.randomUUID();
		holograms.add(this);
	}

	public static void tickAll() {
		synchronized (holograms) {
			Iterator<CraftHologram> iter = holograms.iterator();
			CraftHologram hologram;
			while (iter.hasNext()) {
				hologram = iter.next();
				if (hologram.location == null) {
					iter.remove();
					continue;
				}
				hologram.tick();
			}
		}
	}

	private void tick() {
		if (--this.playerSearchTick < 0) {
			this.playerSearchTick = 10;
			final double viewDistanceSq = this.viewDistance * this.viewDistance;
			Iterator<EntityPlayer> iter = this.viewers.iterator();
			while (iter.hasNext()) {
				EntityPlayer player = iter.next();
				if (shouldRemovePlayer(player, this.location, viewDistanceSq)) {
					sendDestroyPacket(player);
					iter.remove();
				}
			}
			WorldServer world = ((CraftWorld) this.location.getWorld()).getHandle();
			int chunkX = this.location.getBlockX() >> 4;
			int chunkZ = this.location.getBlockZ() >> 4;
			Chunk chunk;
			for (int x = chunkX - 1; x <= chunkX + 1; x++) {
				for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
					chunk = world.getChunkIfLoaded(x, z);
					
					if (chunk == null)
						continue;

					for (EntityPlayer player : chunk.playersInChunk) {
						if (!this.viewers.contains(player) && shouldAddPlayer(player, this.location, viewDistanceSq)) {
							this.viewers.add(player);
							sendSpawnPackets(player);
						}
					}
				}
			}
		}
	}

	private static boolean shouldRemovePlayer(EntityPlayer player, Location hologramLocation, double viewDistanceSq) {
		return player.dead || player.world.getWorld() != hologramLocation.getWorld() || player.getBukkitEntity().getLocation().distanceSquared(hologramLocation) > viewDistanceSq;
	}

	private static boolean shouldAddPlayer(EntityPlayer player, Location hologramLocation, double viewDistanceSq) {
		return player.getBukkitEntity().getLocation().distanceSquared(hologramLocation) < viewDistanceSq;
	}

	public void delete() {
		this.location = null;
		this.text = null;
		for (EntityPlayer player : this.viewers) {
			sendDestroyPacket(player);
		}
		this.viewers = null;
	}

	public String getMessage() {
		return this.text;
	}

	public void setMessage(String msg) {
		this.text = msg;
		for (EntityPlayer player : this.viewers) {
			sendMessagePacket(player);
		}
	}

	public Location getLocation() {
		return this.location;
	}

	public void setMessageHandler(Hologram.MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	private void sendSpawnPackets(EntityPlayer player) {
		PacketPlayOutEntityTeleport packetPlayOutEntityTeleport = new PacketPlayOutEntityTeleport();
		packetPlayOutEntityTeleport.a = this.entityId;
		packetPlayOutEntityTeleport.b = MathHelper.floor(this.location.getX() * 32.0D);
		packetPlayOutEntityTeleport.c = MathHelper.floor((this.location.getY() - 2.25D) * 32.0D);
		packetPlayOutEntityTeleport.d = MathHelper.floor(this.location.getZ() * 32.0D);
		packetPlayOutEntityTeleport.onGround = true;
		
		if (player.playerConnection.networkManager.getVersion() < 47) {
			PacketPlayOutSpawnEntity skullPacket = new PacketPlayOutSpawnEntity();
			skullPacket.a = this.entityId + 1;
			skullPacket.b = MathHelper.floor(this.location.getX() * 32.0D);
			skullPacket.c = MathHelper.floor((this.location.getY() + 54.55D) * 32.0D);
			skullPacket.d = MathHelper.floor(this.location.getZ() * 32.0D);
			skullPacket.j = 64;
			
			PacketPlayOutAttachEntity attachEntityPacket = new PacketPlayOutAttachEntity();
			attachEntityPacket.leash = 0;
			attachEntityPacket.entityId = this.entityId;
			attachEntityPacket.vehicleEntityId = this.entityId + 1;
			
			PacketPlayOutSpawnEntityLiving horsePacket = new PacketPlayOutSpawnEntityLiving();
			horsePacket.a = this.entityId;
			horsePacket.b = 100;
			horsePacket.c = MathHelper.floor(this.location.getX() * 32.0D);
			horsePacket.d = MathHelper.floor((this.location.getY() + 54.55D) * 32.0D);
			horsePacket.e = MathHelper.floor(this.location.getZ() * 32.0D);
			horsePacket.metadata = new Object[32];
			horsePacket.metadata[10] = getMessageForPlayer(player);
			horsePacket.metadata[11] = Byte.valueOf((byte) 1);
			horsePacket.metadata[12] = Integer.valueOf(-1700000);
			horsePacket.clss = EntityAgeable.class;
			
			player.playerConnection.sendPacket(horsePacket);
			player.playerConnection.sendPacket(skullPacket);
			player.playerConnection.sendPacket(attachEntityPacket);
			return;
		}
		PacketPlayOutSpawnEntityLiving armorStandPacket = new PacketPlayOutSpawnEntityLiving();
		armorStandPacket.a = this.entityId;
		armorStandPacket.uuid = this.uuid;
		armorStandPacket.b = 30;
		armorStandPacket.c = packetPlayOutEntityTeleport.b;
		armorStandPacket.d = packetPlayOutEntityTeleport.c;
		armorStandPacket.e = packetPlayOutEntityTeleport.d;
		armorStandPacket.metadata = new Object[32];
		armorStandPacket.metadata[0] = Byte.valueOf((byte) 32);
		armorStandPacket.metadata[10] = getMessageForPlayer(player);
		armorStandPacket.metadata[11] = Byte.valueOf((byte) 1);
		armorStandPacket.clss = EntityInsentient.class;
		
		player.playerConnection.sendPacket(armorStandPacket);
		player.playerConnection.sendPacket(packetPlayOutEntityTeleport);
	}
	
	private void sendMessagePacket(EntityPlayer player) {
		PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata();
	    metadataPacket.a = this.entityId;
	    metadataPacket.metadata = new Object[32];
	    metadataPacket.metadata[10] = getMessageForPlayer(player);
	    metadataPacket.clss = EntityInsentient.class;
	    
	    player.playerConnection.sendPacket(metadataPacket);
	}

	private void sendDestroyPacket(EntityPlayer player) {
		player.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(new int[] { this.entityId, this.entityId + 1 }));
	}

	private String getMessageForPlayer(EntityPlayer player) {
		String message = this.text;
		if (message.contains("{player}"))
			message = message.replace("{player}", player.getName());
		if (message.contains("{displayname}"))
			message = message.replace("{displayname}", player.getBukkitEntity().getDisplayName());
		if (this.messageHandler != null)
			try {
				message = this.messageHandler.getMessage((Player) player.getBukkitEntity(), message);
			} catch (Exception ex) {
				Bukkit.getLogger().severe("Exception in CraftHologram.messageHandler:");
				ex.printStackTrace();
			}
		if (message.length() > 64)
			message = message.substring(0, 64);
		return message;
	}

	@Override
	public Hologram addLineBelow(String text) {
		final Hologram holow = new CraftHologram(location.clone().subtract(0, 0.25, 0), text);
		return holow;
	}
}
