package net.minecraft.server;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spigotmc.SpigotDebreakifier;
import org.spigotmc.TrackingRange;

import io.noks.interfaces.ITrack;
import net.minecraft.util.com.google.common.collect.Lists;
import net.minecraft.util.com.google.common.collect.Sets;

public class EntityTracker implements TrackingRange {
    private static final Logger a = LogManager.getLogger();
    public Set<EntityTrackerEntry> c = Sets.newHashSet(); // FIXME: possible conccurrent error
    public IntHashMap trackedEntities = new IntHashMap(); // CraftBukkit - private -> public
    private int e;

    public EntityTracker(WorldServer worldserver) {
        this.e = worldserver.getMinecraftServer().getPlayerList().d();
    }

    public void track(Entity entity) {
    	// Rinny start
    	if (entity instanceof ITrack tracker) {
    		tracker.track(this);
    		return;
    	}
    	if (entity instanceof IAnimal) {
    		this.addEntity(entity, 80, 3, true);
    		return;
    	}
    	
    	throw new IllegalArgumentException("Don\'t know how to track " + entity.getClass() + "!");
    	// Rinny end
    	
    	// CompressedSpigot version
        /*if (entity instanceof EntityPlayer entityplayer) { // Rinny
            this.addEntity(entity, 512, 2);
            final Iterator<EntityTrackerEntry> iterator = this.c.iterator();

            while (iterator.hasNext()) {
                EntityTrackerEntry entitytrackerentry = iterator.next();

                if (entitytrackerentry.tracker != entityplayer) {
                    entitytrackerentry.updatePlayer(entityplayer);
                }
            }
        } else if (entity instanceof EntityFishingHook || entity instanceof EntityPotion || entity instanceof EntityEnderPearl) { // Rinny - moved here EntityPotion & EntityEnderPearl
            this.addEntity(entity, 64, 5, true);
        } else if (entity instanceof EntityArrow) {  // Rinny - tick every 10 ticks
            this.addEntity(entity, 64, 10);
        } else if (entity instanceof EntitySmallFireball || entity instanceof EntityFireball) { // Rinny - tick every 20 ticks since its way less used than other projectile
            this.addEntity(entity, 64, 20);
        } else if (entity instanceof EntitySnowball) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityEnderSignal) { // moved EntityEnderPearl
            this.addEntity(entity, 64, 4, true);
        } else if (entity instanceof EntityEgg || entity instanceof EntityThrownExpBottle || entity instanceof EntityFireworks) { // moved EntityPotion
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityItem) {
            this.addEntity(entity, 64, 20, true);
        } else if (entity instanceof EntityMinecartAbstract || entity instanceof EntityBoat) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntitySquid) {
            this.addEntity(entity, 64, 3, true);
        } else if (entity instanceof EntityWither || entity instanceof EntityBat) {
            this.addEntity(entity, 80, 3);
        } else if (entity instanceof IAnimal) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntityEnderDragon) {
            this.addEntity(entity, 160, 3, true);
        } else if (entity instanceof EntityTNTPrimed) {
            this.addEntity(entity, 160, 10, true);
        } else if (entity instanceof EntityFallingBlock) {
            this.addEntity(entity, 160, 20, true);
        } else if (entity instanceof EntityHanging) {
            this.addEntity(entity, 160, Integer.MAX_VALUE);
        } else if (entity instanceof EntityExperienceOrb) {
            this.addEntity(entity, 160, 20, true);
        } else if (entity instanceof EntityEnderCrystal) {
            this.addEntity(entity, 256, Integer.MAX_VALUE);
        }*/
        
    	// PaperSpigot version
        /*else if (entity instanceof EntityFishingHook) {
            this.addEntity(entity, 64, 5, true);
        } else if (entity instanceof EntityArrow) {
            this.addEntity(entity, 64, 20, false);
        } else if (entity instanceof EntitySmallFireball) {
            this.addEntity(entity, 64, 10, false);
        } else if (entity instanceof EntityFireball) {
            this.addEntity(entity, 64, 10, false);
        } else if (entity instanceof EntitySnowball) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityEnderPearl) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityEnderSignal) {
            this.addEntity(entity, 64, 4, true);
        } else if (entity instanceof EntityEgg) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityPotion) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityThrownExpBottle) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityFireworks) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityItem) {
            this.addEntity(entity, 64, 20, true);
        } else if (entity instanceof EntityMinecartAbstract) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntityBoat) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntitySquid) {
            this.addEntity(entity, 64, 3, true);
        } else if (entity instanceof EntityWither) {
            this.addEntity(entity, 80, 3, false);
        } else if (entity instanceof EntityBat) {
            this.addEntity(entity, 80, 3, false);
        } else if (entity instanceof IAnimal) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntityEnderDragon) {
            this.addEntity(entity, 160, 3, true);
        } else if (entity instanceof EntityTNTPrimed) {
            this.addEntity(entity, 160, 10, true);
        } else if (entity instanceof EntityFallingBlock) {
            this.addEntity(entity, 160, 20, true);
        } else if (entity instanceof EntityHanging) {
            this.addEntity(entity, 160, Integer.MAX_VALUE, false);
        } else if (entity instanceof EntityExperienceOrb) {
            this.addEntity(entity, 160, 20, true);
        } else if (entity instanceof EntityEnderCrystal) {
            this.addEntity(entity, 256, Integer.MAX_VALUE, false);
        }*/
    }

    public void addEntity(Entity entity, int i, int j) {
        this.addEntity(entity, i, j, false);
    }

    public void addEntity(Entity entity, int i, int j, boolean flag) {
        //i = Math.min(org.spigotmc.TrackingRange.getEntityTrackingRange(entity, i), this.e); // Spigot
        i = Math.min(this.getEntityTrackingRange(entity, i), this.e); // Spigot

        try {
            if (this.trackedEntities.b(entity.getId())) {
                throw new IllegalStateException("Entity is already tracked!");
            }

            final EntityTrackerEntry entitytrackerentry = new EntityTrackerEntry(entity, i, j, flag);

            this.c.add(entitytrackerentry);
            this.trackedEntities.a(entity.getId(), entitytrackerentry);
            entity.world.performOnInRangePlayers(entity, i, entitytrackerentry::updatePlayer);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Adding entity to track");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity To Track");

            crashreportsystemdetails.a("Tracking range", (i + " blocks"));
            crashreportsystemdetails.a("Update interval", new CrashReportEntityTrackerUpdateInterval(this, j));
            entity.a(crashreportsystemdetails);
            CrashReportSystemDetails crashreportsystemdetails1 = crashreport.a("Entity That Is Already Tracked");

            ((EntityTrackerEntry) this.trackedEntities.get(entity.getId())).tracker.a(crashreportsystemdetails1);

            try {
                throw new ReportedException(crashreport);
            } catch (ReportedException reportedexception) {
                a.error("\"Silently\" catching entity tracking error.", reportedexception);
            }
        }
    }

    public void untrackEntity(Entity entity) {
        if (entity instanceof EntityPlayer entityplayer) { // Rinny
        	final Iterator<EntityTrackerEntry> iterator = this.c.iterator();

            while (iterator.hasNext()) {
                EntityTrackerEntry entitytrackerentry = iterator.next();

                entitytrackerentry.a(entityplayer);
            }
        }

        final EntityTrackerEntry entitytrackerentry1 = (EntityTrackerEntry) this.trackedEntities.d(entity.getId());

        if (entitytrackerentry1 != null) {
            this.c.remove(entitytrackerentry1);
            entitytrackerentry1.a();
        }
    }

    public void updatePlayers() {
    	final List<EntityPlayer> arraylist = Lists.newLinkedList(); // Rinny - arrayList > linkedList
    	final Iterator<EntityTrackerEntry> iterator = this.c.iterator();

        while (iterator.hasNext()) {
            EntityTrackerEntry entitytrackerentry = iterator.next();

            entitytrackerentry.track();
            if (entitytrackerentry.n && entitytrackerentry.tracker instanceof EntityPlayer ep) {
                arraylist.add(ep);
            }
        }

        for (EntityPlayer entityPlayer : arraylist) {
            Iterator<EntityTrackerEntry> iterator1 = this.c.iterator();

            while (iterator1.hasNext()) {
                EntityTrackerEntry entitytrackerentry1 = iterator1.next();

                if (entitytrackerentry1.tracker != entityPlayer) {
                    entitytrackerentry1.updatePlayer(entityPlayer);
                }
            }
        }
    }

    public void a(Entity entity, Packet packet) {
    	final EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry) this.trackedEntities.get(entity.getId());

        if (entitytrackerentry != null) {
            entitytrackerentry.broadcast(packet);
        }
    }

    public void sendPacketToEntity(Entity entity, Packet packet) {
    	final EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry) this.trackedEntities.get(entity.getId());

        if (entitytrackerentry != null) {
            entitytrackerentry.broadcastIncludingSelf(packet);
        }
    }

    public void untrackPlayer(EntityPlayer entityplayer) {
    	final Iterator<EntityTrackerEntry> iterator = this.c.iterator();

        while (iterator.hasNext()) {
            EntityTrackerEntry entitytrackerentry = iterator.next();

            entitytrackerentry.clear(entityplayer);
        }
    }

    public void a(EntityPlayer entityplayer, Chunk chunk) {
    	final Iterator<EntityTrackerEntry> iterator = this.c.iterator();

        while (iterator.hasNext()) {
            EntityTrackerEntry entitytrackerentry = iterator.next();

            if (entitytrackerentry.tracker != entityplayer && entitytrackerentry.tracker.ah == chunk.locX && entitytrackerentry.tracker.aj == chunk.locZ) {
                entitytrackerentry.updatePlayer(entityplayer);
            }
        }
    }
    
    public void broadcastFallDamageParticles(Entity entity, Block block, int blockData) {
        final EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry)this.trackedEntities.get(entity.getId());
        Packet packetOld = null;
        Packet packetNew = null;
        if (entity instanceof EntityPlayer player) {
        	if (player.playerConnection.networkManager.getVersion() < 107) {
        		player.playerConnection.sendPacket(packetOld = dustParticlesOld(entity));
        	} else {
        		player.playerConnection.sendPacket(packetNew = dustParticlesNew(entity, block, blockData));
        	} 
        } 
        for (EntityPlayer player : entitytrackerentry.trackedPlayers) {
        	if (player.playerConnection.networkManager.getVersion() < 107) {
        		if (packetOld == null) {
        			packetOld = dustParticlesOld(entity); 
        		}
        		player.playerConnection.sendPacket(packetOld);
        		continue;
        	} 
        	if (packetNew == null) {
        		packetNew = dustParticlesNew(entity, block, blockData);
        	}
        	player.playerConnection.sendPacket(packetNew);
        } 
	}
    
    private static Packet dustParticlesOld(Entity entity) {
        return new PacketPlayOutWorldEvent(2006, MathHelper.floor(entity.locX), MathHelper.floor(entity.locY - 0.20000000298023224D - entity.height), MathHelper.floor(entity.locZ), MathHelper.f(entity.fallDistance - 3.0F), false);
	}
      
	private static Packet dustParticlesNew(Entity entity, Block block, int blockData) {
        int id = Block.getId(block);
        int data = SpigotDebreakifier.getCorrectedData(id, blockData);
        int particleCount = (int)(150.0D * Math.min((0.2F + MathHelper.f(entity.fallDistance - 3.0F) / 15.0F), 2.5D));
        return new PacketPlayOutWorldParticles("blockdust_" + id + "_" + data, (float)entity.locX, (float)entity.locY, (float)entity.locZ, 0.0F, 0.0F, 0.0F, 0.15F, particleCount);
	}
}
