package net.minecraft.server;

// CraftBukkit start
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.sathonay.interfaces.IEntitySpecificSpawnPacket;

import io.noks.interfaces.ITrack;
// CraftBukkit end

public class EntityEnderPearl extends EntityProjectile implements IEntitySpecificSpawnPacket, ITrack {
	private Location lastValidTeleport;

    public EntityEnderPearl(World world) {
        super(world);
        this.loadChunks = world.paperSpigotConfig.loadUnloadedEnderPearls; // PaperSpigot
    }

    public EntityEnderPearl(World world, EntityLiving entityliving) {
        super(world, entityliving);
        this.loadChunks = world.paperSpigotConfig.loadUnloadedEnderPearls; // PaperSpigot
        this.lastValidTeleport = entityliving.getBukkitEntity().getLocation();
    }
    
    @Override
    public void tick() {
    	final EntityLiving entityliving = this.getShooter();
        if (entityliving != null && entityliving instanceof EntityHuman && !entityliving.isAlive()) {
            this.die();
            return;
        }
		if (this.world.getCubes(this, this.boundingBox.grow(0.2D, 0.1D, 0.2D)).isEmpty()) {
			this.lastValidTeleport = getBukkitEntity().getLocation();
		}
		super.tick();
	}

    protected void hit(MovingObjectPosition movingobjectposition) {
        if (movingobjectposition.entity != null) {
            movingobjectposition.entity.damageEntity(DamageSource.projectile(this, this.getShooter()), 0.0F);
        }

        // PaperSpigot start - Remove entities in unloaded chunks
        if (inUnloadedChunk && world.paperSpigotConfig.removeUnloadedEnderPearls) {
            this.die();
            return;
        }
        // PaperSpigot end
        
        for (int i = 0; i < 32; ++i) {
            this.world.addParticle("portal", this.locX, this.locY + this.random.nextDouble() * 2.0D, this.locZ, this.random.nextGaussian(), 0.0D, this.random.nextGaussian());
        }

        if (!this.world.isStatic) {
            if (this.getShooter() != null && this.getShooter() instanceof EntityPlayer entityplayer && entityplayer.playerConnection.b().isConnected() && entityplayer.world == this.world) { // Rinny
                // CraftBukkit start - Fire PlayerTeleportEvent
                final org.bukkit.craftbukkit.entity.CraftPlayer player = entityplayer.getBukkitEntity();
                final Location location = (movingobjectposition.entity == null ? this.lastValidTeleport.clone() : getBukkitEntity().getLocation());
                // Rinny start - use entityplayer.pitch and yaw to correct tp behavior
                location.setPitch(entityplayer.pitch);
                location.setYaw(entityplayer.yaw);
                // Rinny end
                    
                final PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
                Bukkit.getPluginManager().callEvent(teleEvent);

                if (!teleEvent.isCancelled() && !entityplayer.playerConnection.isDisconnected()) {
                    if (entityplayer.am()) {
                        entityplayer.mount((Entity) null);
                    }

                    entityplayer.playerConnection.teleport(teleEvent.getTo());
                    entityplayer.fallDistance = 0.0F;
                    CraftEventFactory.entityDamage = this;
                    entityplayer.damageEntity(DamageSource.FALL, 5.0F);
                    CraftEventFactory.entityDamage = null;
                }
                    // CraftBukkit end
            }
            this.die();
        }
    }
    
	protected MovingObjectPosition projectileCollision(Vec3D vec3d, Vec3D vec3d1) {
		return this.world.paperSpigotConfig.pearlsNonSolidCollisions ? super.projectileCollision(vec3d, vec3d1) : this.world.rayTrace(vec3d, vec3d1, false, true, false);
	}
	
	@Override
    public Packet createSpecificSpawnPacket() {
        return new PacketPlayOutSpawnEntity(this, 65);
    }

	@Override
	public void track(EntityTracker tracker) {
		tracker.addEntity(this, 64, 5, true);
	}
}
