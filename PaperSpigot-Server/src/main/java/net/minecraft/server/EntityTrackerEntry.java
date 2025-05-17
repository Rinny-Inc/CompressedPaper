package net.minecraft.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

// CraftBukkit start
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerVelocityEvent;

import com.avaje.ebean.validation.NotNull;
import com.sathonay.interfaces.IEntitySpecificSpawnPacket;

import io.noks.utils.EntityNPC;
import net.minecraft.server.PacketPlayOutPlayerInfo.PlayerInfo;
// CraftBukkit end

public class EntityTrackerEntry {
	
	// TODO: Try to do a better position tracking

    //private static final Logger p = LogManager.getLogger();
    public @NotNull Entity tracker;
    public int b;
    public int c;
    public int xLoc;
    public int yLoc;
    public int zLoc;
    public int yRot;
    public int xRot;
    public int i;
    public double j;
    public double k;
    public double l;
    public int m;
    private double q;
    private double r;
    private double s;
    private boolean isMoving;
    private boolean u;
    private int v;
    private Entity w;
    private boolean x;
    public boolean n;
    // Replace trackedPlayers Set with a Map. The value is true until the player receives
    // their first update (which is forced to have absolute coordinates), false afterward.
    public @NotNull final Map<EntityPlayer, Boolean> trackedPlayerMap = new LinkedHashMap<EntityPlayer, Boolean>();
    public @NotNull final Set<EntityPlayer> trackedPlayers = trackedPlayerMap.keySet();

    public EntityTrackerEntry(Entity entity, int i, int j, boolean flag) {
        this.tracker = entity;
        this.b = i;
        this.c = j;
        this.u = flag;
        this.xLoc = (int) MathHelper.floor(entity.locX * 32.0D); 
        this.yLoc = (int) MathHelper.floor(entity.locY * 32.0D);
        this.zLoc = (int) MathHelper.floor(entity.locZ * 32.0D);
        this.yRot = MathHelper.d(entity.yaw * 256.0F / 360.0F);
        this.xRot = MathHelper.d(entity.pitch * 256.0F / 360.0F);
        this.i = MathHelper.d(entity.getHeadRotation() * 256.0F / 360.0F);
    }

    public boolean equals(Object object) {
        return object instanceof EntityTrackerEntry ? ((EntityTrackerEntry) object).tracker.getId() == this.tracker.getId() : false;
    }

    public int hashCode() {
        return this.tracker.getId();
    }

    public void track() {
        this.n = false;
        if (!this.isMoving || this.tracker.e(this.q, this.r, this.s) > 16.0D) {
            this.q = this.tracker.locX;
            this.r = this.tracker.locY;
            this.s = this.tracker.locZ;
            this.isMoving = true;
            this.n = true;
            this.tracker.world.performOnInRangePlayers(this.tracker, this.b, this::updatePlayer);
        }

        if (this.w != this.tracker.vehicle || this.tracker.vehicle != null && this.m % 60 == 0) {
        	PacketPlayOutMount packetPlayOutMount;
            Packet attach = new PacketPlayOutAttachEntity(0, this.tracker, this.tracker.vehicle);
            if (this.tracker.vehicle == null) {
              packetPlayOutMount = new PacketPlayOutMount(this.w, null);
            } else {
              packetPlayOutMount = new PacketPlayOutMount(this.tracker.vehicle, this.tracker);
            } 
            Iterator<EntityPlayer> iterator = this.trackedPlayers.iterator();
            while (iterator.hasNext()) {
              EntityPlayer entityplayer = iterator.next();
              if (entityplayer.playerConnection.networkManager.getVersion() < 107) {
                entityplayer.playerConnection.sendPacket(attach);
                continue;
              } 
              entityplayer.playerConnection.sendPacket((Packet)packetPlayOutMount);
            }
            this.w = this.tracker.vehicle;
            //this.broadcast(new PacketPlayOutAttachEntity(0, this.tracker, this.tracker.vehicle));
        }

        if (this.tracker instanceof EntityItemFrame i3 /*&& this.m % 10 == 0*/) { // CraftBukkit - Moved below, should always enter this block // Rinny add var
        	final ItemStack i4 = i3.getItem();

            if (this.m % 10 == 0 && i4 != null && i4.getItem() instanceof ItemWorldMap) { // CraftBukkit - Moved this.m % 10 logic here so item frames do not enter the other blocks
            	final WorldMap i6 = Items.MAP.getSavedMap(i4, this.tracker.world);
                /*Iterator i7 = this.trackedPlayers.iterator(); // CraftBukkit

                while (i7.hasNext()) {
                    EntityHuman i8 = (EntityHuman) i7.next();
                    EntityPlayer i9 = (EntityPlayer) i8;

                    i6.a(i9, i4);
                    Packet j0 = Items.MAP.c(i4, this.tracker.world, i9);

                    if (j0 != null) {
                        i9.playerConnection.sendPacket(j0);
                    }
                }*/
            	Packet packet; // Rinny
                for (EntityPlayer i9 : this.trackedPlayers) {
                    i6.a(i9, i4);
                    packet = Items.MAP.c(i4, this.tracker.world, i9);
                    if (packet != null) {
                        i9.playerConnection.sendPacket(packet);
                    }
                }
            }

            this.b();
        } else if (this.m % this.c == 0 || this.tracker.al || this.tracker.getDataWatcher().a()) {
            int i;
            int j;

            if (this.tracker.vehicle == null) {
                ++this.v;
                i = (int) Math.floor(this.tracker.locX * 32.0D);
                j = (int) Math.floor(this.tracker.locY * 32.0D);
                final int k = (int) Math.floor(this.tracker.locZ * 32.0D);
                final int l = MathHelper.d(this.tracker.yaw * 256.0F / 360.0F);
                final int i1 = MathHelper.d(this.tracker.pitch * 256.0F / 360.0F);
                final int j1 = i - this.xLoc;
                final int k1 = j - this.yLoc;
                final int l1 = k - this.zLoc;
                Packet packet = null;
                //final boolean flag = Math.abs(j1) >= 4 || Math.abs(k1) >= 4 || Math.abs(l1) >= 4 || this.m % 60 == 0; // Rinny - moved down
                //final boolean flag1 = Math.abs(l - this.yRot) >= 4 || Math.abs(i1 - this.xRot) >= 4; // Rinny - moved down

                if (this.m > 0 || this.tracker instanceof EntityArrow) { // PaperSpigot - Move up
                	final boolean flag = Math.abs(j1) >= 4 || Math.abs(k1) >= 4 || Math.abs(l1) >= 4 || this.m % 60 == 0;
                    // CraftBukkit start - Code moved from below
                    if (flag) {
                        this.xLoc = i;
                        this.yLoc = j;
                        this.zLoc = k;
                    }

                    final boolean flag1 = Math.abs(l - this.yRot) >= 4 || Math.abs(i1 - this.xRot) >= 4;
                    if (flag1) {
                        this.yRot = l;
                        this.xRot = i1;
                    }
                    // CraftBukkit end

                    if (j1 >= -128 && j1 < 128 && k1 >= -128 && k1 < 128 && l1 >= -128 && l1 < 128 && this.v <= 400 && !this.x) {
                        if (flag && flag1) {
                            packet = new PacketPlayOutRelEntityMoveLook(this.tracker.getId(), (byte) j1, (byte) k1, (byte) l1, (byte) l, (byte) i1, tracker.onGround); // Spigot - protocol patch
                        } else if (flag) {
                            packet = new PacketPlayOutRelEntityMove(this.tracker.getId(), (byte) j1, (byte) k1, (byte) l1, tracker.onGround); // Spigot - protocol patch
                        } else if (flag1) {
                            packet = new PacketPlayOutEntityLook(this.tracker.getId(), (byte) l, (byte) i1, tracker.onGround); // Spigot - protocol patch
                        }
                    } else {
                        this.v = 0;
                        // CraftBukkit start - Refresh list of who can see a player before sending teleport packet
                        if (this.tracker instanceof EntityPlayer) {
                            //this.scanPlayers(this.tracker.world.players);
                            this.tracker.world.performOnInRangePlayers(this.tracker, this.b, this::updatePlayer);
                        }
                        // CraftBukkit end
                        packet = new PacketPlayOutEntityTeleport(this.tracker.getId(), i, j, k, (byte) l, (byte) i1, tracker.onGround, tracker); // Spigot - protocol patch
                    }
                }

                if (this.u) {
                	final double d0 = this.tracker.motX - this.j;
                	final double d1 = this.tracker.motY - this.k;
                	final double d2 = this.tracker.motZ - this.l;
                	final double d3 = 0.02D;
                	final double d4 = d0 * d0 + d1 * d1 + d2 * d2;

                    if (d4 > d3 * d3 || d4 > 0.0D && this.tracker.motX == 0.0D && this.tracker.motY == 0.0D && this.tracker.motZ == 0.0D) {
                        this.j = this.tracker.motX;
                        this.k = this.tracker.motY;
                        this.l = this.tracker.motZ;
                        this.broadcast(new PacketPlayOutEntityVelocity(this.tracker.getId(), this.j, this.k, this.l));
                    }
                }

                if (packet != null) {
                	if(packet instanceof PacketPlayOutEntityTeleport) {
                		this.broadcast(packet);
                	} else {
                		PacketPlayOutEntityTeleport teleportPacket = null;
                		
                		for (Map.Entry<EntityPlayer, Boolean> viewer : trackedPlayerMap.entrySet()) {
                			if (viewer.getValue()) {
                				viewer.setValue(false);
                				if (teleportPacket == null) {
                					teleportPacket = new PacketPlayOutEntityTeleport(this.tracker);
                				}
                				viewer.getKey().playerConnection.sendPacket(teleportPacket);
                			} else {
                				viewer.getKey().playerConnection.sendPacket(packet);
                			}
                		}
                	}
                }

                this.b();
                /* CraftBukkit start - Code moved up
                if (flag) {
                    this.xLoc = i;
                    this.yLoc = j;
                    this.zLoc = k;
                }

                if (flag1) {
                    this.yRot = l;
                    this.xRot = i1;
                }
                // CraftBukkit end */

                this.x = false;
            } else {
                i = MathHelper.d(this.tracker.yaw * 256.0F / 360.0F);
                j = MathHelper.d(this.tracker.pitch * 256.0F / 360.0F);
                final boolean flag2 = Math.abs(i - this.yRot) >= 4 || Math.abs(j - this.xRot) >= 4;

                if (flag2) {
                    this.broadcast(new PacketPlayOutEntityLook(this.tracker.getId(), (byte) i, (byte) j, tracker.onGround)); // Spigot - protocol patch
                    this.yRot = i;
                    this.xRot = j;
                }

                this.xLoc = (int) Math.floor(this.tracker.locX * 32.0D);
                this.yLoc = (int) Math.floor(this.tracker.locY * 32.0D);
                this.zLoc = (int) Math.floor(this.tracker.locZ * 32.0D);
                this.b();
                this.x = true;
            }

            i = MathHelper.d(this.tracker.getHeadRotation() * 256.0F / 360.0F);
            if (Math.abs(i - this.i) >= 4) {
                this.broadcast(new PacketPlayOutEntityHeadRotation(this.tracker, (byte) i));
                this.i = i;
            }

            this.tracker.al = false;
        }

        ++this.m;
        if (this.tracker.velocityChanged) {
            // CraftBukkit start - Create PlayerVelocity event
            boolean cancelled = false;

            if (this.tracker instanceof EntityPlayer) {
            	final Player player = (Player) this.tracker.getBukkitEntity();
            	final org.bukkit.util.Vector velocity = player.getVelocity();

            	final PlayerVelocityEvent event = new PlayerVelocityEvent(player, velocity);
                this.tracker.world.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    cancelled = true;
                } else if (!velocity.equals(event.getVelocity())) {
                    player.setVelocity(velocity);
                }
            }

            if (!cancelled) {
                this.broadcastIncludingSelf(new PacketPlayOutEntityVelocity(this.tracker));
            }
            // CraftBukkit end

            this.tracker.velocityChanged = false;
        }
    }

    private void b() {
        final DataWatcher datawatcher = this.tracker.getDataWatcher();

        if (datawatcher.a()) {
        	if (this.tracker instanceof EntityPlayer) {
        		PacketPlayOutEntityMetadata broadcast = new PacketPlayOutEntityMetadata(this.tracker, false);
        		broadcast.obfuscateHealth();
        		broadcast(broadcast);
        		((EntityPlayer)this.tracker).playerConnection.sendPacket(new PacketPlayOutEntityMetadata(this.tracker, false));
        	} else {
        		broadcast(new PacketPlayOutEntityMetadata(this.tracker, false));
        	} 
        	datawatcher.e();
        }

        if (this.tracker instanceof EntityLiving) {
        	final AttributeMapServer attributemapserver = (AttributeMapServer) ((EntityLiving) this.tracker).getAttributeMap();
        	final Set set = attributemapserver.getAttributes();

            if (!set.isEmpty()) {
                // CraftBukkit start - Send scaled max health
                if (this.tracker instanceof EntityPlayer) {
                    ((EntityPlayer) this.tracker).getBukkitEntity().injectScaledMaxHealth(set, false);
                }
                // CraftBukkit end
                this.broadcastIncludingSelf(new PacketPlayOutUpdateAttributes(this.tracker.getId(), set));
            }

            set.clear();
        }
    }

    public void broadcast(Packet packet) {
    	final Iterator<EntityPlayer> iterator = this.trackedPlayers.iterator();

    	EntityPlayer entityplayer;
        while (iterator.hasNext()) {
            entityplayer = iterator.next();
            entityplayer.playerConnection.sendPacket(packet);
        }
    }

    public void broadcastIncludingSelf(Packet packet) {
        this.broadcast(packet);
        if (this.tracker instanceof EntityPlayer) {
            ((EntityPlayer) this.tracker).playerConnection.sendPacket(packet);
        }
    }

    public void a() {
    	final Iterator<EntityPlayer> iterator = this.trackedPlayers.iterator();

    	EntityPlayer entityplayer;
        while (iterator.hasNext()) {
            entityplayer = iterator.next();
            entityplayer.d(this.tracker);
        }
    }

    public void a(EntityPlayer entityplayer) {
        if (this.trackedPlayers.remove(entityplayer)) {
            entityplayer.d(this.tracker);
        }
    }

    public void updatePlayer(EntityPlayer entityplayer) {
        if (entityplayer != this.tracker) {
        	final double d0 = entityplayer.locX - (double) (this.xLoc / 32);
        	final double d1 = entityplayer.locZ - (double) (this.zLoc / 32);

            if (d0 >= (double) (-this.b) && d0 <= (double) this.b && d1 >= (double) (-this.b) && d1 <= (double) this.b) {
                if (!this.trackedPlayers.contains(entityplayer) && (this.d(entityplayer) || this.tracker.attachedToPlayer)) {
                    // CraftBukkit start - respect vanish API
                	if (!entityplayer.getBukkitEntity().canSee(this.tracker.getBukkitEntity())) {
                		return;
                	}

                    entityplayer.removeQueue.remove(Integer.valueOf(this.tracker.getId()));
                    // CraftBukkit end

                    this.trackedPlayerMap.put(entityplayer, true);
                    final Packet packet = this.c();

                    // Spigot start - protocol patch
                    if ( tracker instanceof EntityPlayer) {
                        entityplayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo((EntityPlayer) tracker, PlayerInfo.ADD_PLAYER));
                        if ( !entityplayer.getName().equals( entityplayer.listName ) && entityplayer.playerConnection.networkManager.getVersion() > 28 )
                        {
                            entityplayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo((EntityPlayer) tracker, PlayerInfo.UPDATE_DISPLAY_NAME));
                        }
                    }
                    // Spigot end
                    
                    entityplayer.playerConnection.sendPacket(packet);
                    if (!this.tracker.getDataWatcher().d()) {
                    	PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(this.tracker, true);
                    	if (this.tracker instanceof EntityPlayer) {
                    		meta.obfuscateHealth(); 
                    	}
                    	entityplayer.playerConnection.sendPacket(meta);
                    }

                    if (this.tracker instanceof EntityLiving) {
                        AttributeMapServer attributemapserver = (AttributeMapServer) ((EntityLiving) this.tracker).getAttributeMap();
                        Collection collection = attributemapserver.c();

                        // CraftBukkit start - If sending own attributes send scaled health instead of current maximum health
                        if (this.tracker.getId() == entityplayer.getId()) {
                            ((EntityPlayer) this.tracker).getBukkitEntity().injectScaledMaxHealth(collection, false);
                        }
                        // CraftBukkit end
                        if (!collection.isEmpty()) {
                            entityplayer.playerConnection.sendPacket(new PacketPlayOutUpdateAttributes(this.tracker.getId(), collection));
                        }
                    }

                    this.j = this.tracker.motX;
                    this.k = this.tracker.motY;
                    this.l = this.tracker.motZ;
                    if (this.u && !(packet instanceof PacketPlayOutSpawnEntityLiving)) {
                        entityplayer.playerConnection.sendPacket(new PacketPlayOutEntityVelocity(this.tracker.getId(), this.tracker.motX, this.tracker.motY, this.tracker.motZ));
                    }

                    if (this.tracker.vehicle != null) {
                    	if (entityplayer.playerConnection.networkManager.getVersion() < 107) {
                            entityplayer.playerConnection.sendPacket(new PacketPlayOutAttachEntity(0, this.tracker, this.tracker.vehicle));
                    	} else {
                            entityplayer.playerConnection.sendPacket((Packet)new PacketPlayOutMount(this.tracker.vehicle, this.tracker));
                    	} 
                    }

                    // CraftBukkit start
                    if (this.tracker.passenger != null) {
                    	if (entityplayer.playerConnection.networkManager.getVersion() < 107) {
                            entityplayer.playerConnection.sendPacket(new PacketPlayOutAttachEntity(0, this.tracker.passenger, this.tracker));
                    	} else {
                            entityplayer.playerConnection.sendPacket((Packet)new PacketPlayOutMount(this.tracker, this.tracker.passenger));
                    	}
                    }
                    // CraftBukkit end

                    if (this.tracker instanceof EntityInsentient && ((EntityInsentient) this.tracker).getLeashHolder() != null) {
                        entityplayer.playerConnection.sendPacket(new PacketPlayOutAttachEntity(1, this.tracker, ((EntityInsentient) this.tracker).getLeashHolder()));
                    }

                    if (this.tracker instanceof EntityHuman entityhuman) { // Rinny
                        if (entityhuman.isSleeping()) {
                            entityplayer.playerConnection.sendPacket(new PacketPlayOutBed(entityhuman, MathHelper.floor(this.tracker.locX), MathHelper.floor(this.tracker.locY), MathHelper.floor(this.tracker.locZ)));
                        }
                    }

                    // CraftBukkit start - Fix for nonsensical head yaw
                    //this.i = MathHelper.d(this.tracker.getHeadRotation() * 256.0F / 360.0F);
                    //this.broadcast(new PacketPlayOutEntityHeadRotation(this.tracker, (byte) i));

                    if (this.tracker instanceof EntityLiving entityliving) { // Rinny
                    	ItemStack itemstack; // Rinny
                        for (int i = 0; i < 5; ++i) {
                            itemstack = entityliving.getEquipment(i);

                            if (itemstack != null) {
                                entityplayer.playerConnection.sendPacket(new PacketPlayOutEntityEquipment(entityliving.getId(), i, itemstack));
                            }
                        }
                        
                    	this.i = MathHelper.d(entityliving.getHeadRotation() * 256.0F / 360.0F);
                    	entityplayer.playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(entityliving, (byte) i));
                    	// CraftBukkit end
                    	
                        Iterator iterator = entityliving.getEffects().iterator();

                        while (iterator.hasNext()) {
                            MobEffect mobeffect = (MobEffect) iterator.next();

                            entityplayer.playerConnection.sendPacket(new PacketPlayOutEntityEffect(this.tracker.getId(), mobeffect));
                        }
                    }
                    // Rinny start - remove NPC's from the Tablist TODO: is there a better way? YES
                    if (tracker instanceof EntityNPC) {
                    	MinecraftServer.getServer().processQueue.add(() -> {
                    		entityplayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo((EntityNPC) tracker, PlayerInfo.REMOVE_PLAYER));
                    	});
                    }
                    // Rinny end
                }
            } else if (this.trackedPlayers.remove(entityplayer)) {
                entityplayer.d(this.tracker);
            }
        }
    }

    private boolean d(EntityPlayer entityplayer) {
        return entityplayer.r().getPlayerChunkMap().a(entityplayer, this.tracker.ah, this.tracker.aj);
    }

    private Packet c() {
        if (this.tracker.dead) {
            // CraftBukkit start - Remove useless error spam, just return
            // p.warn("Fetching addPacket for removed entity");
            return null;
            // CraftBukkit end
        }
        
        // nPaper
        if (this.tracker instanceof IEntitySpecificSpawnPacket specificPacket) { // Rinny add var
            return specificPacket.createSpecificSpawnPacket();
        }
        // nPaper
        
        if (this.tracker instanceof IAnimal || this.tracker instanceof EntityEnderDragon) {
            this.i = MathHelper.d(this.tracker.getHeadRotation() * 256.0F / 360.0F);
            return new PacketPlayOutSpawnEntityLiving((EntityLiving) this.tracker);
        }

        throw new IllegalArgumentException("Don\'t know how to add " + this.tracker.getClass() + "!");

        // OLD
        /*if (this.tracker instanceof EntityItem) {
            return new PacketPlayOutSpawnEntity(this.tracker, 2, 1);
        } else if (this.tracker instanceof EntityPlayer) {
            return new PacketPlayOutNamedEntitySpawn((EntityHuman) this.tracker);
        } else if (this.tracker instanceof EntityMinecartAbstract) {
        	final EntityMinecartAbstract entityminecartabstract = (EntityMinecartAbstract) this.tracker;

            return new PacketPlayOutSpawnEntity(this.tracker, 10, entityminecartabstract.m());
        } else if (this.tracker instanceof EntityBoat) {
            return new PacketPlayOutSpawnEntity(this.tracker, 1);
        } else if (!(this.tracker instanceof IAnimal) && !(this.tracker instanceof EntityEnderDragon)) {
            if (this.tracker instanceof EntityFishingHook) {
            	final EntityHuman entityhuman = ((EntityFishingHook) this.tracker).owner;

                return new PacketPlayOutSpawnEntity(this.tracker, 90, entityhuman != null ? entityhuman.getId() : this.tracker.getId());
            } else if (this.tracker instanceof EntityArrow) {
            	final Entity entity = ((EntityArrow) this.tracker).shooter;

                return new PacketPlayOutSpawnEntity(this.tracker, 60, entity != null ? entity.getId() : this.tracker.getId());
            } else if (this.tracker instanceof EntitySnowball) {
                return new PacketPlayOutSpawnEntity(this.tracker, 61);
            } else if (this.tracker instanceof EntityPotion) {
                return new PacketPlayOutSpawnEntity(this.tracker, 73, ((EntityPotion) this.tracker).getPotionValue());
            } else if (this.tracker instanceof EntityThrownExpBottle) {
                return new PacketPlayOutSpawnEntity(this.tracker, 75);
            } else if (this.tracker instanceof EntityEnderPearl) {
                return new PacketPlayOutSpawnEntity(this.tracker, 65);
            } else if (this.tracker instanceof EntityEnderSignal) {
                return new PacketPlayOutSpawnEntity(this.tracker, 72);
            } else if (this.tracker instanceof EntityFireworks) {
                return new PacketPlayOutSpawnEntity(this.tracker, 76);
            } else {
                PacketPlayOutSpawnEntity packetplayoutspawnentity;

                if (this.tracker instanceof EntityFireball) {
                	final EntityFireball entityfireball = (EntityFireball) this.tracker;

                    packetplayoutspawnentity = null;
                    byte b0 = 63;

                    if (this.tracker instanceof EntitySmallFireball) {
                        b0 = 64;
                    } else if (this.tracker instanceof EntityWitherSkull) {
                        b0 = 66;
                    }

                    packetplayoutspawnentity = new PacketPlayOutSpawnEntity(this.tracker, b0, entityfireball.shooter != null ? ((EntityFireball) this.tracker).shooter.getId() : 0);
                    packetplayoutspawnentity.d((int) (entityfireball.dirX * 8000.0D));
                    packetplayoutspawnentity.e((int) (entityfireball.dirY * 8000.0D));
                    packetplayoutspawnentity.f((int) (entityfireball.dirZ * 8000.0D));
                    return packetplayoutspawnentity;
                } else if (this.tracker instanceof EntityEgg) {
                    return new PacketPlayOutSpawnEntity(this.tracker, 62);
                } else if (this.tracker instanceof EntityTNTPrimed) {
                    return new PacketPlayOutSpawnEntity(this.tracker, 50);
                } else if (this.tracker instanceof EntityEnderCrystal) {
                    return new PacketPlayOutSpawnEntity(this.tracker, 51);
                } else if (this.tracker instanceof EntityFallingBlock) {
                	final EntityFallingBlock entityfallingblock = (EntityFallingBlock) this.tracker;

                    return new PacketPlayOutSpawnEntity(this.tracker, 70, Block.getId(entityfallingblock.f()) | entityfallingblock.data << 16);
                } else if (this.tracker instanceof EntityPainting) {
                    return new PacketPlayOutSpawnEntityPainting((EntityPainting) this.tracker);
                } else if (this.tracker instanceof EntityItemFrame) {
                	final EntityItemFrame entityitemframe = (EntityItemFrame) this.tracker;

                    packetplayoutspawnentity = new PacketPlayOutSpawnEntity(this.tracker, 71, entityitemframe.direction);
                    packetplayoutspawnentity.a(MathHelper.d((float) (entityitemframe.x * 32)));
                    packetplayoutspawnentity.b(MathHelper.d((float) (entityitemframe.y * 32)));
                    packetplayoutspawnentity.c(MathHelper.d((float) (entityitemframe.z * 32)));
                    return packetplayoutspawnentity;
                } else if (this.tracker instanceof EntityLeash) {
                	final EntityLeash entityleash = (EntityLeash) this.tracker;

                    packetplayoutspawnentity = new PacketPlayOutSpawnEntity(this.tracker, 77);
                    packetplayoutspawnentity.a(MathHelper.d((float) (entityleash.x * 32)));
                    packetplayoutspawnentity.b(MathHelper.d((float) (entityleash.y * 32)));
                    packetplayoutspawnentity.c(MathHelper.d((float) (entityleash.z * 32)));
                    return packetplayoutspawnentity;
                } else if (this.tracker instanceof EntityExperienceOrb) {
                    return new PacketPlayOutSpawnEntityExperienceOrb((EntityExperienceOrb) this.tracker);
                } else {
                    throw new IllegalArgumentException("Don\'t know how to add " + this.tracker.getClass() + "!");
                }
            }
        } else {
            this.i = MathHelper.d(this.tracker.getHeadRotation() * 256.0F / 360.0F);
            return new PacketPlayOutSpawnEntityLiving((EntityLiving) this.tracker);
        }*/
    }

    public void clear(EntityPlayer entityplayer) {
        //if (this.trackedPlayers.contains(entityplayer)) {
        if (this.trackedPlayers.remove(entityplayer)) {
            //this.trackedPlayers.remove(entityplayer);
            entityplayer.d(this.tracker);
        }
    }
}
