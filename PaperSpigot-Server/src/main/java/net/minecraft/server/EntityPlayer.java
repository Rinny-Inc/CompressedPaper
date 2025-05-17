package net.minecraft.server;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// CraftBukkit start
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.avaje.ebean.validation.NotNull;
import com.google.common.base.Preconditions;
import com.sathonay.interfaces.IEntitySpecificSpawnPacket;

import io.noks.interfaces.ITrack;
import net.minecraft.util.com.google.common.collect.Sets;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.io.netty.buffer.Unpooled;
import net.minecraft.util.org.apache.commons.io.Charsets;

public class EntityPlayer extends EntityHuman implements ICrafting, IEntitySpecificSpawnPacket, ITrack  {
    private static final Logger bL = LogManager.getLogger();
    public String locale = "en_US"; // Spigot
    public PlayerConnection playerConnection;
    public final MinecraftServer server;
    public final PlayerInteractManager playerInteractManager;
    public double d;
    public double e;
    public final List<ChunkCoordIntPair> chunkCoordIntPairQueue = new LinkedList<>();
    public final @NotNull Deque<Integer> removeQueue = new ArrayDeque<>(); // Rinny
    private final ServerStatisticManager bO;
    private float bP = Float.MIN_VALUE;
    private float bQ = -1.0E8F;
    private int bR = -99999999;
    private boolean bS = true;
    public int lastSentExp = -99999999; // CraftBukkit - private -> public
    public int invulnerableTicks = 60; // CraftBukkit - private -> public
    private EnumChatVisibility bV;
    //private boolean bW = true; // Rinny - removed due to not being used
    private long bX = System.currentTimeMillis();
    private int containerCounter;
    public boolean g;
    public short ping;
    public boolean viewingCredits;
    // CraftBukkit start
    public @NotNull String displayName;
    public @NotNull String listName;
    public @Nullable org.bukkit.Location compassTarget;
    public int newExp = 0;
    public int newLevel = 0;
    public int newTotalExp = 0;
    public boolean keepLevel = false;
    public double maxHealthCache;
    public boolean joining = true;
    public short lastPing = -1; // Spigot
    // CraftBukkit end
    // Spigot start
    public boolean collidesWithEntities = true;
    public int viewDistance; // PaperSpigot - Player view distance API
    //private int containerUpdateDelay; // PaperSpigot > Rinny removed
    
    private @Nullable UUID msgedUUID;
    public void setMessaged(UUID uuid) {
    	if (this.msgedUUID == uuid) { // dont do useless action
    		return;
    	}
    	this.msgedUUID = uuid;
    }
    public UUID getMessagedUUID() {
    	return this.msgedUUID;
    }
    private final UUID bossBarUUID;
    public String bossBarMessage;
    public float bossBarHealth;
    public int bossBarEntityId;
    
    @Override
    public boolean R()
    {
        return this.collidesWithEntities && super.R(); // (first !this.isDead near bottom of EntityLiving)
    }

    @Override
    public boolean S()
    {
        return this.collidesWithEntities && super.S(); // (second !this.isDead near bottom of EntityLiving)
    }
    // Spigot end
    
    public EntityPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(worldserver, gameprofile);
        this.viewDistance = world.spigotConfig.viewDistance; // PaperSpigot - Player view distance API
        playerinteractmanager.player = this;
        this.playerInteractManager = playerinteractmanager;
        /*final ChunkCoordinates chunkcoordinates = worldserver.getSpawn();
        int i = chunkcoordinates.x;
        int j = chunkcoordinates.z;
        int k = chunkcoordinates.y;

        if (!worldserver.worldProvider.g && worldserver.getWorldData().getGameType() != EnumGamemode.ADVENTURE) {
            int l = Math.max(5, minecraftserver.getSpawnProtection() - 6);

            i += this.random.nextInt(l * 2) - l;
            j += this.random.nextInt(l * 2) - l;
            k = worldserver.i(i, j);
        }*/
        this.bossBarUUID = UUID.randomUUID();
        this.server = minecraftserver;
        this.bO = minecraftserver.getPlayerList().a((EntityHuman) this);
        this.W = 0.0F;
        this.height = 0.0F;
        //this.setPositionRotation((double) i /*+ 0.5D*/, (double) k, (double) j /*+ 0.5D*/, worldserver.getWorldData().spawnYaw(), worldserver.getWorldData().spawnPitch());

        while (!worldserver.getCubes(this, this.boundingBox).isEmpty()) {
            this.setPosition(this.locX, this.locY + 1.0D, this.locZ);
        }

        // CraftBukkit start
        this.displayName = this.getName();
        this.listName = this.getName();
        this.maxHealthCache = this.getMaxHealth();
        // CraftBukkit end
    }
    
    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (this.locY > 300) this.locY = 255;
        if (nbttagcompound.hasKeyOfType("playerGameType", 99)) {
            this.playerInteractManager.setGameMode((MinecraftServer.getServer().getForceGamemode() ? MinecraftServer.getServer().getGamemode() : EnumGamemode.getById(nbttagcompound.getInt("playerGameType"))));
        }
        this.getBukkitEntity().readExtraData(nbttagcompound); // CraftBukkit
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setInt("playerGameType", this.playerInteractManager.getGameMode().getId());
        this.getBukkitEntity().setExtraData(nbttagcompound); // CraftBukkit
    }

    // CraftBukkit start - World fallback code, either respawn location or global spawn
    public void spawnIn(World world) {
        super.spawnIn(world);
        if (world == null) {
            this.dead = false;
            ChunkCoordinates position = null;
            float yaw = 0, pitch = 0;
            if (this.spawnWorld != null && !this.spawnWorld.equals("")) {
            	final CraftWorld cworld = (CraftWorld) Bukkit.getServer().getWorld(this.spawnWorld);
                if (cworld != null && this.getBed() != null) {
                    world = cworld.getHandle();
                    position = EntityHuman.getBed(cworld.getHandle(), this.getBed(), false);
                }
            }
            if (world == null || position == null) {
                world = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();
                position = world.getSpawn();
                yaw = world.getWorldData().spawnYaw();
                pitch = world.getWorldData().spawnPitch();
            }
            this.world = world;
            this.setPositionRotation(position.x + 0.5, position.y, position.z + 0.5, yaw, pitch);
        }
        this.dimension = ((WorldServer) this.world).dimension;
        this.playerInteractManager.a((WorldServer) world);
    }
    // CraftBukkit end

    public void levelDown(int i) {
        super.levelDown(i);
        this.lastSentExp = -1;
    }

    public void syncInventory() {
        this.activeContainer.addSlotListener(this);
    }

    protected void e_() {
        this.height = 0.0F;
    }

    public float getHeadHeight() {
        return 1.62F;
    }
    
    public void tick() {
        // CraftBukkit start
        if (this.joining) {
            this.joining = false;
        }
        // CraftBukkit end

        this.playerInteractManager.a();
        if (this.invulnerableTicks > 0) {
        	--this.invulnerableTicks;
        }
        // Rinny - Living now handles it
        /*if (this.noDamageTicks > 0) {
            --this.noDamageTicks;
        }*/
        // Rinny
        this.activeContainer.b();
        if (!this.world.isStatic && !this.activeContainer.a((EntityHuman) this)) {
            this.closeInventory();
            this.activeContainer = this.defaultContainer;
        }

        // Rinny - don't init var 24/7
        int i;
        int[] aint;
        // Rinny end
        while (!this.removeQueue.isEmpty()) {
        	i = Math.min(this.removeQueue.size(), 127);
        	aint = new int[i];
            //Iterator iterator = this.removeQueue.iterator();
            int j = 0;

            /*while (iterator.hasNext() && j < i) {
                aint[j++] = ((Integer) iterator.next()).intValue();
                iterator.remove();
            }*/
            Integer integer;
            while (j < i && (integer = this.removeQueue.poll()) != null) {
            	aint[j++] = integer.intValue();
            }

            this.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(aint));
        }

        if (!this.chunkCoordIntPairQueue.isEmpty()) {
        	int version = this.playerConnection.networkManager.getVersion();
        	
        	final List<Chunk> arraylist = new ArrayList<>();
        	final Iterator<ChunkCoordIntPair> iterator1 = this.chunkCoordIntPairQueue.iterator();
        	final List<TileEntity> arraylist1 = new ArrayList<>();
            Chunk chunk;

            while (iterator1.hasNext() && arraylist.size() < this.world.spigotConfig.maxBulkChunk) { // Spigot
            	ChunkCoordIntPair chunkcoordintpair = iterator1.next();

                if (chunkcoordintpair != null) {
                    if (this.world.isLoaded(chunkcoordintpair.x << 4, 0, chunkcoordintpair.z << 4)) {
                        chunk = this.world.getChunkAt(chunkcoordintpair.x, chunkcoordintpair.z);
                        if (chunk.isReady() && chunk.areNeighborsLoaded(1)) { // Rinny - added areneighborsloaded
                            arraylist.add(chunk);
                            if (version < 110) {
                            	arraylist1.addAll(chunk.tileEntities.values()); 
                            }
                            iterator1.remove();
                        }
                    }
                } else {
                    iterator1.remove();
                }
            }

            if (!arraylist.isEmpty()) {
            	if (version < 107) {
            		this.playerConnection.sendPacket(new PacketPlayOutMapChunkBulk(arraylist, this));
            	} else {
            		for (Chunk ch : arraylist) {
            			PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(ch, true, 65535, this);
            			if (version >= 110)
            				packet.setTileEntities(ch.tileEntities); 
            			this.playerConnection.sendPacket(packet);
            		} 
            	} 
                Iterator iterator2 = arraylist1.iterator();

                TileEntity tileentity; // Rinny - don't init var 24/7
                while (iterator2.hasNext()) {
                    tileentity = (TileEntity) iterator2.next();

                    this.b(tileentity);
                }

                iterator2 = arraylist.iterator();

                while (iterator2.hasNext()) {
                    chunk = (Chunk) iterator2.next();
                    this.r().getTracker().a(this, chunk);
                }
            }
        }
        this.moveBossBarEntity();
    }

    public void i() {
        try {
            super.tick();

            ItemStack itemstack; // Rinny - don't init var 24/7
            for (int i = 0; i < this.inventory.getSize(); ++i) {
                itemstack = this.inventory.getItem(i);

                if (itemstack != null && itemstack.getItem().h()) {
                    Packet packet = ((ItemWorldMapBase) itemstack.getItem()).c(itemstack, this.world, this);

                    if (packet != null) {
                        this.playerConnection.sendPacket(packet);
                    }
                }
            }

            // CraftBukkit - Optionally scale health
            if (this.getHealth() != this.bQ || this.bR != this.foodData.getFoodLevel() || this.foodData.getSaturationLevel() == 0.0F != this.bS) {
                this.playerConnection.sendPacket(new PacketPlayOutUpdateHealth(this.getBukkitEntity().getScaledHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
                this.bQ = this.getHealth();
                this.bR = this.foodData.getFoodLevel();
                this.bS = this.foodData.getSaturationLevel() == 0.0F;
            }

            if (this.getHealth() + this.getAbsorptionHearts() != this.bP) {
                this.bP = this.getHealth() + this.getAbsorptionHearts();
                // CraftBukkit - Update ALL the scores!
                this.world.getServer().getScoreboardManager().updateAllScoresForList(IScoreboardCriteria.f, this.getName(), com.google.common.collect.ImmutableList.of(this));
            }

            // CraftBukkit start - Force max health updates
            if (this.maxHealthCache != this.getMaxHealth()) {
                this.getBukkitEntity().updateScaledHealth();
            }
            // CraftBukkit end

            if (this.expTotal != this.lastSentExp) {
                this.lastSentExp = this.expTotal;
                this.playerConnection.sendPacket(new PacketPlayOutExperience(this.exp, this.expTotal, this.expLevel));
            }

            if (this.ticksLived % 20 * 5 == 0 && !this.getStatisticManager().hasAchievement(AchievementList.L)) {
                this.j();
            }

            // CraftBukkit start - initialize oldLevel and fire PlayerLevelChangeEvent
            if (this.oldLevel == -1) {
                this.oldLevel = this.expLevel;
            }

            if (this.oldLevel != this.expLevel) {
                CraftEventFactory.callPlayerLevelChangeEvent(this.world.getServer().getPlayer((EntityPlayer) this), this.oldLevel, this.expLevel);
                this.oldLevel = this.expLevel;
            }
            // CraftBukkit end
        } catch (Throwable throwable) {
        	final CrashReport crashreport = CrashReport.a(throwable, "Ticking player");
        	final CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Player being ticked");

            this.a(crashreportsystemdetails);
            throw new ReportedException(crashreport);
        }
    }

    protected void j() {
    	final BiomeBase biomebase = this.world.getBiome(MathHelper.floor(this.locX), MathHelper.floor(this.locZ));

        if (biomebase != null) {
        	final String s = biomebase.af;
            AchievementSet achievementset = (AchievementSet) this.getStatisticManager().b((Statistic) AchievementList.L); // CraftBukkit - fix decompile error

            if (achievementset == null) {
                achievementset = (AchievementSet) this.getStatisticManager().a(AchievementList.L, new AchievementSet());
            }

            achievementset.add(s);
            if (this.getStatisticManager().b(AchievementList.L) && achievementset.size() == BiomeBase.n.size()) {
            	final Set hashset = Sets.newHashSet(BiomeBase.n);
            	final Iterator iterator = achievementset.iterator();

                while (iterator.hasNext()) {
                    String s1 = (String) iterator.next();
                    Iterator iterator1 = hashset.iterator();

                    while (iterator1.hasNext()) {
                        BiomeBase biomebase1 = (BiomeBase) iterator1.next();

                        if (biomebase1.af.equals(s1)) {
                            iterator1.remove();
                        }
                    }

                    if (hashset.isEmpty()) {
                        break;
                    }
                }

                if (hashset.isEmpty()) {
                    this.a((Statistic) AchievementList.L);
                }
            }
        }
    }

    public void die(DamageSource damagesource) {
        // CraftBukkit start - fire PlayerDeathEvent
        if (this.dead) {
            return;
        }

        final java.util.List<org.bukkit.inventory.ItemStack> loot = new java.util.ArrayList<org.bukkit.inventory.ItemStack>();
        final boolean keepInventory = this.world.getGameRules().getBoolean("keepInventory");

        if (!keepInventory) {
            for (int i = 0; i < this.inventory.items.length; ++i) {
                if (this.inventory.items[i] != null) {
                    loot.add(CraftItemStack.asCraftMirror(this.inventory.items[i]));
                }
            }

            for (int i = 0; i < this.inventory.armor.length; ++i) {
                if (this.inventory.armor[i] != null) {
                    loot.add(CraftItemStack.asCraftMirror(this.inventory.armor[i]));
                }
            }
        }

        final IChatBaseComponent chatmessage = this.aW().b();

        final String deathmessage = chatmessage.c();
        final org.bukkit.event.entity.PlayerDeathEvent event = CraftEventFactory.callPlayerDeathEvent(this, loot, deathmessage, keepInventory);

        final String deathMessage = event.getDeathMessage();

        if (deathMessage != null && deathMessage.length() > 0) {
            if (deathMessage.equals(deathmessage)) {
                this.server.getPlayerList().sendMessage(chatmessage);
            } else {
                this.server.getPlayerList().sendMessage(org.bukkit.craftbukkit.util.CraftChatMessage.fromString(deathMessage));
            }
        }

        // we clean the player's inventory after the EntityDeathEvent is called so plugins can get the exact state of the inventory.
        if (!event.getKeepInventory()) {
            for (int i = 0; i < this.inventory.items.length; ++i) {
                this.inventory.setItem(i, null);
            }

            for (int i = 0; i < this.inventory.armor.length; ++i) {
                this.inventory.player.setEquipment(i, null);
            }
        }

        this.closeInventory();
        // CraftBukkit end

        // CraftBukkit - Get our scores instead
        final Collection<ScoreboardScore> collection = this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreboardCriteria.c, this.getName(), new java.util.ArrayList<ScoreboardScore>());
        final Iterator<ScoreboardScore> iterator = collection.iterator();

        while (iterator.hasNext()) {
            ScoreboardScore scoreboardscore = iterator.next(); // CraftBukkit - Use our scores instead

            scoreboardscore.incrementScore();
        }

        final EntityLiving entityliving = this.aX();

        if (entityliving != null) {
        	final int i = EntityTypes.a(entityliving);
        	final MonsterEggInfo monsteregginfo = (MonsterEggInfo) EntityTypes.eggInfo.get(Integer.valueOf(i));

            if (monsteregginfo != null) {
                this.a(monsteregginfo.e, 1);
            }

            entityliving.b(this, this.ba);
        }

        this.a(StatisticList.v, 1);
        this.aW().g();
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable()) {
            return false;
        }
        // CraftBukkit - this.server.getPvP() -> this.world.pvpMode
        final boolean flag = this.server.X() && this.world.pvpMode && "fall".equals(damagesource.translationIndex);

        if (!flag && this.invulnerableTicks > 0 && damagesource != DamageSource.OUT_OF_WORLD) {
            return false;
        }
        if (damagesource instanceof EntityDamageSource) {
        	final Entity entity = damagesource.getEntity();

            if (entity instanceof EntityHuman && !this.a((EntityHuman) entity)) {
            	return false;
            }

            if (entity instanceof EntityArrow entityarrow) { // Rinny
                if (entityarrow.shooter instanceof EntityHuman && !this.a((EntityHuman) entityarrow.shooter)) {
                	return false;
                }
            }
        }
        return super.damageEntity(damagesource, f);
    }

    public boolean a(EntityHuman entityhuman) {
        // CraftBukkit - this.server.getPvP() -> this.world.pvpMode
        return !this.world.pvpMode ? false : super.a(entityhuman);
    }

    public void b(int i) {
        // PaperSpigot start - Allow configurable end portal credits
    	final boolean endPortal = this.dimension == 1 && i == 1;
        if (endPortal) {
            this.a((Statistic) AchievementList.D);
            if (!world.paperSpigotConfig.disableEndCredits) {
                this.world.kill(this);
                this.viewingCredits = true;
                this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(4, 0.0F));
            }
        // PaperSpigot end
        } else {
            if (this.dimension == 0 && i == 1) {
                this.a((Statistic) AchievementList.C);
                // CraftBukkit start - Rely on custom portal management
                /*
                ChunkCoordinates chunkcoordinates = this.server.getWorldServer(i).getDimensionSpawn();

                if (chunkcoordinates != null) {
                    this.playerConnection.a((double) chunkcoordinates.x, (double) chunkcoordinates.y, (double) chunkcoordinates.z, 0.0F, 0.0F);
                }

                i = 1;
                */
                // CraftBukkit end
            } else {
                this.a((Statistic) AchievementList.y);
            }
        }

        // PaperSpigot start - Allow configurable end portal credits
        if (!endPortal || world.paperSpigotConfig.disableEndCredits) {
            // CraftBukkit start
        	final TeleportCause cause = (endPortal || (this.dimension == 1 || i == 1)) ? TeleportCause.END_PORTAL : TeleportCause.NETHER_PORTAL;
            this.server.getPlayerList().changeDimension(this, i, cause);
            // CraftBukkit end
            this.lastSentExp = -1;
            this.bQ = -1.0F;
            this.setSneaking(false); // Rinny - Fix MC-10657
            this.bR = -1;
        }
        // PaperSpigot end
    }

    private void b(TileEntity tileentity) {
        if (tileentity != null) {
        	final Packet packet = tileentity.getUpdatePacket();

            if (packet != null) {
                this.playerConnection.sendPacket(packet);
            }
        }
    }

    public void receive(Entity entity) {
        super.receive(entity);
        this.activeContainer.b();
    }

    public EnumBedResult a(int i, int j, int k) {
    	final EnumBedResult enumbedresult = super.a(i, j, k);

        if (enumbedresult == EnumBedResult.OK) {
        	final PacketPlayOutBed packetplayoutbed = new PacketPlayOutBed(this, i, j, k);

            this.r().getTracker().a((Entity) this, (Packet) packetplayoutbed);
            this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
            this.playerConnection.sendPacket(packetplayoutbed);
        }

        return enumbedresult;
    }

    public void a(boolean flag, boolean flag1, boolean flag2) {
        if (!this.sleeping) return; // CraftBukkit - Can't leave bed if not in one!

        if (this.isSleeping()) {
            this.r().getTracker().sendPacketToEntity(this, new PacketPlayOutAnimation(this, 2));
        }

        super.a(flag, flag1, flag2);
        if (this.playerConnection != null) {
            this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
        }
    }

    public void mount(Entity entity) {
        // CraftBukkit start
        this.setPassengerOf(entity);
    }

    public void setPassengerOf(Entity entity) {
        // mount(null) doesn't really fly for overloaded methods,
        // so this method is needed
    	final Entity currentVehicle = this.vehicle;

        super.setPassengerOf(entity);

        // Check if the vehicle actually changed.
        if (currentVehicle != this.vehicle) {
        	if (this.playerConnection.networkManager.getVersion() < 107) {
                this.playerConnection.sendPacket(new PacketPlayOutAttachEntity(0, this, this.vehicle));
        	} else if (this.vehicle == null) {
                this.playerConnection.sendPacket(new PacketPlayOutMount(currentVehicle, null));
        	} else {
                this.playerConnection.sendPacket(new PacketPlayOutMount(this.vehicle, this));
        	} 
            //this.playerConnection.sendPacket(new PacketPlayOutAttachEntity(0, this, this.vehicle));
            this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
            if (this.vehicle instanceof EntityLiving living) {
            	AttributeMapServer attributemapserver = (AttributeMapServer)living.getAttributeMap();
            	Collection collection = attributemapserver.c();
            	if (!collection.isEmpty()) {
            		this.playerConnection.sendPacket(new PacketPlayOutUpdateAttributes(this.vehicle.getId(), collection));
            	}
            }
        }
        // CraftBukkit end
    }

    protected void a(double d0, boolean flag) {}

    public void b(double d0, boolean flag) {
        super.a(d0, flag);
    }

    public void a(TileEntity tileentity) {
        if (tileentity instanceof TileEntitySign) {
            ((TileEntitySign) tileentity).a((EntityHuman) this);
            this.playerConnection.sendPacket(new PacketPlayOutOpenSignEditor(tileentity.x, tileentity.y, tileentity.z));
        }
    }

    public int nextContainerCounter() { // CraftBukkit - private void -> public int
        this.containerCounter = this.containerCounter % 100 + 1;
        return this.containerCounter; // CraftBukkit
    }

    public void startCrafting(int i, int j, int k) {
        // CraftBukkit start - Inventory open hook
    	final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerWorkbench(this.inventory, this.world, i, j, k));
        if (container == null) {
            return;
        }
        // CraftBukkit end

        this.nextContainerCounter();
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, 1, "Crafting", 0, true)); // Spigot - protocol patch
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void startEnchanting(int i, int j, int k, String s) {
        // CraftBukkit start - Inventory open hook
    	final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerEnchantTable(this.inventory, this.world, i, j, k));
        if (container == null) {
            return;
        }
        // CraftBukkit end

        this.nextContainerCounter();
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, 4, s == null ? "" : s, 0, s != null)); // Spigot - protocol patch
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void openAnvil(int i, int j, int k) {
        // CraftBukkit start - Inventory open hook
    	final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerAnvil(this.inventory, this.world, i, j, k, this));
        if (container == null) {
            return;
        }
        // CraftBukkit end

        this.nextContainerCounter();
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, 8, "Repairing", 0, true)); // Spigot - protocol patch
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void openContainer(IInventory iinventory) {
        if (this.activeContainer != this.defaultContainer) {
        	if (this.world.paperSpigotConfig.maintainCursorInventoryOpens) {
        		CraftEventFactory.handleInventoryCloseEvent(this);
        		this.m();
        	} else {
        		this.closeInventory();
        	}  
        }

        // CraftBukkit start - Inventory open hook
        final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerChest(this.inventory, iinventory));
        if (container == null) {
            iinventory.closeContainer();
            return;
        }
        // CraftBukkit end

        this.nextContainerCounter();
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, 0, iinventory.getInventoryName(), iinventory.getSize(), iinventory.k_()));
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void openHopper(TileEntityHopper tileentityhopper) {
        // CraftBukkit start - Inventory open hook
    	final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerHopper(this.inventory, tileentityhopper));
        if (container == null) {
            tileentityhopper.closeContainer();
            return;
        }
        // CraftBukkit end

        this.nextContainerCounter();
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, 9, tileentityhopper.getInventoryName(), tileentityhopper.getSize(), tileentityhopper.k_()));
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void openMinecartHopper(EntityMinecartHopper entityminecarthopper) {
        // CraftBukkit start - Inventory open hook
    	final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerHopper(this.inventory, entityminecarthopper));
        if (container == null) {
            entityminecarthopper.closeContainer();
            return;
        }
        // CraftBukkit end

        this.nextContainerCounter();
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, 9, entityminecarthopper.getInventoryName(), entityminecarthopper.getSize(), entityminecarthopper.k_()));
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void openFurnace(TileEntityFurnace tileentityfurnace) {
        // CraftBukkit start - Inventory open hook
    	final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerFurnace(this.inventory, tileentityfurnace));
        if (container == null) {
            tileentityfurnace.closeContainer();
            return;
        }
        // CraftBukkit end

        this.nextContainerCounter();
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, 2, tileentityfurnace.getInventoryName(), tileentityfurnace.getSize(), tileentityfurnace.k_()));
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void openDispenser(TileEntityDispenser tileentitydispenser) {
        // CraftBukkit start - Inventory open hook
    	final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerDispenser(this.inventory, tileentitydispenser));
        if (container == null) {
            tileentitydispenser.closeContainer();
            return;
        }
        // CraftBukkit end

        this.nextContainerCounter();
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, tileentitydispenser instanceof TileEntityDropper ? 10 : 3, tileentitydispenser.getInventoryName(), tileentitydispenser.getSize(), tileentitydispenser.k_()));
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void openBrewingStand(TileEntityBrewingStand tileentitybrewingstand) {
        // CraftBukkit start - Inventory open hook
    	final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerBrewingStand(this.inventory, tileentitybrewingstand));
        if (container == null) {
            tileentitybrewingstand.closeContainer();
            return;
        }
        // CraftBukkit end

        this.nextContainerCounter();
        int size = (this.playerConnection.networkManager.getVersion() < 107) ? 4 : 5;
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, 5, tileentitybrewingstand.getInventoryName(), size, tileentitybrewingstand.k_()));
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void openBeacon(TileEntityBeacon tileentitybeacon) {
        // CraftBukkit start - Inventory open hook
    	final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerBeacon(this.inventory, tileentitybeacon));
        if (container == null) {
            tileentitybeacon.closeContainer();
            return;
        }
        // CraftBukkit end

        this.nextContainerCounter();
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, 7, tileentitybeacon.getInventoryName(), tileentitybeacon.getSize(), tileentitybeacon.k_()));
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void openTrade(IMerchant imerchant, String s) {
        // CraftBukkit start - Inventory open hook
    	final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerMerchant(this.inventory, imerchant, this.world));
        if (container == null) {
            return;
        }
        // CraftBukkit end

        this.nextContainerCounter();
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
        final InventoryMerchant inventorymerchant = ((ContainerMerchant) this.activeContainer).getMerchantInventory();

        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, 6, s == null ? "" : s, inventorymerchant.getSize(), s != null));
        final MerchantRecipeList merchantrecipelist = imerchant.getOffers(this);

        if (merchantrecipelist != null) {
        	final PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer(), playerConnection.networkManager.getVersion()); // Spigot

            try {
                packetdataserializer.writeInt(this.containerCounter);
                merchantrecipelist.a(packetdataserializer);
                this.playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|TrList", packetdataserializer));
            } catch (Exception ioexception) { // CraftBukkit - IOException -> Exception
                bL.error("Couldn\'t send trade list", ioexception);
            } finally {
                packetdataserializer.release();
            }
        }
    }

    public void openHorseInventory(EntityHorse entityhorse, IInventory iinventory) {
        // CraftBukkit start - Inventory open hook
    	final Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerHorse(this.inventory, iinventory, entityhorse));
        if (container == null) {
            iinventory.closeContainer();
            return;
        }
        // CraftBukkit end

        if (this.activeContainer != this.defaultContainer) {
            this.closeInventory();
        }

        this.nextContainerCounter();
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, 11, iinventory.getInventoryName(), iinventory.getSize(), iinventory.k_(), entityhorse.getId()));
        this.activeContainer = container; // CraftBukkit - Use container we passed to event
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void a(Container container, int i, ItemStack itemstack) {
        if (!(container.getSlot(i) instanceof SlotResult)) {
            if (!this.g) {
                this.playerConnection.sendPacket(new PacketPlayOutSetSlot(container.windowId, i, itemstack));
            }
        }
    }

    public void updateInventory(Container container) {
        this.a(container, container.a());
    }

    public void a(Container container, List list) {
    	if (container.windowId == 0 && this.playerConnection.networkManager.getVersion() >= 107) {
    		list.add(isBlocking() ? new ItemStack(Items.DIAMOND_SWORD, 1) : null);
    	}
        this.playerConnection.sendPacket(new PacketPlayOutWindowItems(container.windowId, list));
        this.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.inventory.getCarried()));
        // CraftBukkit start - Send a Set Slot to update the crafting result slot
        if (java.util.EnumSet.of(InventoryType.CRAFTING,InventoryType.WORKBENCH).contains(container.getBukkitView().getType())) {
            this.playerConnection.sendPacket(new PacketPlayOutSetSlot(container.windowId, 0, container.getSlot(0).getItem()));
        }
        // CraftBukkit end
    }

    public void setContainerData(Container container, int i, int j) {
        // Spigot start - protocol patch
        if ( container instanceof ContainerFurnace && playerConnection.networkManager.getVersion() >= 47 )
        {
            switch ( i ) {
                case 0:
                    i = 2;
                    this.playerConnection.sendPacket(new PacketPlayOutWindowData(container.windowId, 3, 200));
                    break;
                case 1:
                    i = 0;
                    break;
                case 2:
                    i = 1;
                    break;
            }
        }
        // Spigot end
        this.playerConnection.sendPacket(new PacketPlayOutWindowData(container.windowId, i, j));
    }

    public void closeInventory() {
        CraftEventFactory.handleInventoryCloseEvent(this); // CraftBukkit
        this.playerConnection.sendPacket(new PacketPlayOutCloseWindow(this.activeContainer.windowId));
        this.m();
    }

    public void broadcastCarriedItem() {
        if (!this.g) {
            this.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.inventory.getCarried()));
        }
    }

    public void m() {
        this.activeContainer.b((EntityHuman) this);
        this.activeContainer = this.defaultContainer;
    }

    public void a(float f, float f1, boolean flag, boolean flag1) {
        if (this.vehicle != null) {
            if (f >= -1.0F && f <= 1.0F) {
                this.bd = f;
            }

            if (f1 >= -1.0F && f1 <= 1.0F) {
                this.be = f1;
            }

            this.bc = flag;
            this.setSneaking(flag1);
        }
    }

    public void a(Statistic statistic, int i) {
        if (statistic != null) {
            this.bO.b(this, statistic, i);
            final Iterator<ScoreboardObjective> iterator = this.getScoreboard().getObjectivesForCriteria(statistic.k()).iterator();

            while (iterator.hasNext()) {
                ScoreboardObjective scoreboardobjective = iterator.next();

                this.getScoreboard().getPlayerScoreForObjective(this.getName(), scoreboardobjective).incrementScore();
            }

            if (this.bO.e()) {
                this.bO.a(this);
            }
        }
    }

    public void n() {
        if (this.passenger != null) {
            this.passenger.mount(this);
        }

        if (this.sleeping) {
            this.a(true, false, false);
        }
    }

    public void triggerHealthUpdate() {
        this.bQ = -1.0E8F;
        this.lastSentExp = -1; // CraftBukkit - Added to reset
    }

    public void b(IChatBaseComponent ichatbasecomponent) {
        this.playerConnection.sendPacket(new PacketPlayOutChat(ichatbasecomponent));
    }

    protected void p() {
        this.playerConnection.sendPacket(new PacketPlayOutEntityStatus(this, (byte) 9));
        super.p();
    }

    public void a(ItemStack itemstack, int i) {
        super.a(itemstack, i);
        if (itemstack != null && itemstack.getItem() != null && itemstack.getItem().d(itemstack) == EnumAnimation.EAT) {
            this.r().getTracker().sendPacketToEntity(this, new PacketPlayOutAnimation(this, 3));
        }
    }

    public void copyTo(EntityHuman entityhuman, boolean flag) {
        super.copyTo(entityhuman, flag);
        this.lastSentExp = -1;
        this.bQ = -1.0F;
        this.bR = -1;
        //this.removeQueue.addAll(((EntityPlayer) entityhuman).removeQueue);
        if (this.removeQueue != ((EntityPlayer) entityhuman).removeQueue) {
        	this.removeQueue.addAll(((EntityPlayer) entityhuman).removeQueue);
        }
    }

    protected void a(MobEffect mobeffect) {
        super.a(mobeffect);
        this.playerConnection.sendPacket(new PacketPlayOutEntityEffect(this.getId(), mobeffect));
    }

    protected void a(MobEffect mobeffect, boolean flag) {
        super.a(mobeffect, flag);
        this.playerConnection.sendPacket(new PacketPlayOutEntityEffect(this.getId(), mobeffect));
    }

    protected void b(MobEffect mobeffect) {
        super.b(mobeffect);
        this.playerConnection.sendPacket(new PacketPlayOutRemoveEntityEffect(this.getId(), mobeffect));
    }

    public void enderTeleportTo(double d0, double d1, double d2) {
        this.playerConnection.a(d0, d1, d2, this.yaw, this.pitch);
    }

    public void b(Entity entity) {
        this.r().getTracker().sendPacketToEntity(this, new PacketPlayOutAnimation(entity, 4));
    }

    public void c(Entity entity) {
        this.r().getTracker().sendPacketToEntity(this, new PacketPlayOutAnimation(entity, 5));
    }

    public void updateAbilities() {
        if (this.playerConnection != null) {
            this.playerConnection.sendPacket(new PacketPlayOutAbilities(this.abilities));
        }
    }

    public WorldServer r() {
        return (WorldServer) this.world;
    }

    public void a(EnumGamemode enumgamemode) {
        this.playerInteractManager.setGameMode(enumgamemode);
        this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, (float) enumgamemode.getId()));
    }

    // CraftBukkit start - Support multi-line messages
    public void sendMessage(IChatBaseComponent[] ichatbasecomponent) {
        for (IChatBaseComponent component : ichatbasecomponent) {
            this.sendMessage(component);
        }
    }
    // CraftBukkit end

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        this.playerConnection.sendPacket(new PacketPlayOutChat(ichatbasecomponent));
    }

    public boolean a(int i, String s) {
        if ("seed".equals(s) && !this.server.X()) {
            return true;
        }
        if (!"tell".equals(s) && !"help".equals(s) && !"me".equals(s)) {
            if (this.server.getPlayerList().isOp(this.getProfile())) {
            	final OpListEntry oplistentry = (OpListEntry) this.server.getPlayerList().getOPs().get(this.getProfile());

                return oplistentry != null ? oplistentry.a() >= i : this.server.l() >= i;
            }
            return false;
        }
        return true;
    }

    public String s() {
        /*String s = this.playerConnection.networkManager.getSocketAddress().toString();

        s = s.substring(s.indexOf("/") + 1);
        s = s.substring(0, s.indexOf(":"));
        return s;*/
    	// TODO: test
        final String socketAddress = this.playerConnection.networkManager.getSocketAddress().toString();
        final int startIndex = socketAddress.lastIndexOf('/') + 1;
        final int endIndex = socketAddress.indexOf(':');

        if (startIndex >= 0 && endIndex > startIndex) {
            return socketAddress.substring(startIndex, endIndex);
        }
        // Handle invalid socket address or no '/' character found
        return "";
    }

    public void a(PacketPlayInSettings packetplayinsettings) {
    	// Badlion
    	this.locale = packetplayinsettings.c();
        //int i = 256 >> packetplayinsettings.d();
        this.bV = packetplayinsettings.e();
        //this.bW = packetplayinsettings.f();
        if (this.server.N() && this.server.M().equals(getName()))
          this.server.a(packetplayinsettings.g()); 
        this.datawatcher.watch(16, Byte.valueOf((byte)packetplayinsettings.skinFlags));
    }
    
    public EnumChatVisibility getChatFlags() {
        return this.bV;
    }

    public void setResourcePack(String s) {
        this.playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|RPack", s.getBytes(Charsets.UTF_8)));
        // Spigot start - protocol patch
        if ( playerConnection.networkManager.getVersion() >= 36 )
        {
            playerConnection.sendPacket( new org.spigotmc.ProtocolInjector.PacketPlayResourcePackSend( s, "thinkislazy" ) );
        }
        // Spigot end
    }

    public ChunkCoordinates getChunkCoordinates() {
        return new ChunkCoordinates(MathHelper.floor(this.locX), MathHelper.floor(this.locY + 0.5D), MathHelper.floor(this.locZ));
    }

    public void resetIdleTimer() {
        this.bX = MinecraftServer.ar();
    }

    public ServerStatisticManager getStatisticManager() {
        return this.bO;
    }

    public void d(Entity entity) {
        if (entity instanceof EntityHuman) {
            this.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(new int[] { entity.getId()}));
            return;
        }
        this.removeQueue.add(Integer.valueOf(entity.getId()));
    }

    public long x() {
        return this.bX;
    }

    // CraftBukkit start - Add per-player time and weather.
    public long timeOffset = 0;
    public boolean relativeTime = true;

    public long getPlayerTime() {
        if (this.relativeTime) {
            // Adds timeOffset to the current server time.
            return this.world.getDayTime() + this.timeOffset;
        }
        // Adds timeOffset to the beginning of this day.
        return this.world.getDayTime() - (this.world.getDayTime() % 24000) + this.timeOffset;
    }

    public WeatherType weather = null;

    public WeatherType getPlayerWeather() {
        return this.weather;
    }

    public void setPlayerWeather(WeatherType type, boolean plugin) {
        if (!plugin && this.weather != null) {
            return;
        }
        if (plugin) {
            this.weather = type;
        }
        this.playerConnection.sendPacket(new PacketPlayOutGameStateChange((type == WeatherType.DOWNFALL ? 2 : 1), 0));
    }

    public void resetPlayerWeather() {
        this.weather = null;
        this.setPlayerWeather(this.world.getWorldData().hasStorm() ? WeatherType.DOWNFALL : WeatherType.CLEAR, false);
    }
    
    public void setBossBar(String message, float health) {
    	Preconditions.checkNotNull(message);
    	if (message.length() > 64) {
    		message = message.substring(0, 64); 
    	}
    	if (health > 1.0F) {
    		health = 1.0F;
    	} else if (health < 0.001F) {
    		health = 0.001F;
    	} 
    	int version = this.playerConnection.networkManager.getVersion();
    	if (this.bossBarMessage == null) {
    		if (version < 107) {
    			PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving();
    			spawnPacket.a = this.bossBarEntityId = Entity.entityCount++;
    			spawnPacket.clss = (Class)EntityEnderDragon.class;
    	        spawnPacket.metadata = new Object[32];
    	        spawnPacket.metadata[10] = message;
    	        if (version == 47) {
    	        	spawnPacket.b = 64;
    	        	spawnPacket.metadata[0] = Byte.valueOf((byte)32);
    	        	spawnPacket.metadata[6] = Float.valueOf(health * 300.0F);
    	        	spawnPacket.metadata[20] = Integer.valueOf(880);
    	        	double pitch = Math.toRadians(this.pitch);
    	        	double yaw = Math.toRadians(this.yaw);
    	        	spawnPacket.c = (int)((this.locX - Math.sin(yaw) * Math.cos(pitch) * 32.0D) * 32.0D);
    	        	spawnPacket.d = (int)((this.locY - Math.sin(pitch) * 32.0D) * 32.0D);
    	        	spawnPacket.e = (int)((this.locZ + Math.cos(yaw) * Math.cos(pitch) * 32.0D) * 32.0D);
    	        } else {
    	        	spawnPacket.b = 63;
    	        	spawnPacket.metadata[6] = Float.valueOf(health * 200.0F);
    	        	spawnPacket.c = (int)(this.locX * 32.0D);
    	        	spawnPacket.d = -9600;
    	        	spawnPacket.e = (int)(this.locZ * 32.0D);
    	        } 
    	        this.playerConnection.sendPacket(spawnPacket);
    		} else {
    			PacketPlayOutBossBar packet = new PacketPlayOutBossBar();
    	        packet.uuid = this.bossBarUUID;
    	        packet.action = 0;
    	        packet.title = message;
    	        packet.health = health;
    	        this.playerConnection.sendPacket((Packet)packet);
    		} 
    	} else if (!message.equals(this.bossBarMessage) || health != this.bossBarHealth) {
    		if (version < 107) {
    	        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata();
    	        metadataPacket.a = this.bossBarEntityId;
    	        metadataPacket.clss = (Class)EntityEnderDragon.class;
    	        metadataPacket.metadata = new Object[32];
    	        if (health != this.bossBarHealth) {
    	            metadataPacket.metadata[6] = Float.valueOf(health * (version == 47 ? 300.0F : 200.0F));
    	        }
    	        if (!message.equals(this.bossBarMessage)) {
    	        	metadataPacket.metadata[10] = message; 
    	        }
    	        this.playerConnection.sendPacket(metadataPacket);
    		} else {
    			if (health != this.bossBarHealth) {
    				PacketPlayOutBossBar packet = new PacketPlayOutBossBar();
    				packet.uuid = this.bossBarUUID;
    				packet.action = 2;
    				packet.health = health;
    				this.playerConnection.sendPacket((Packet)packet);
    			} 
    			if (message != this.bossBarMessage) {
    				PacketPlayOutBossBar packet = new PacketPlayOutBossBar();
    				packet.uuid = this.bossBarUUID;
    				packet.action = 3;
    				packet.title = message;
    				this.playerConnection.sendPacket((Packet)packet);
    			} 
    		} 
    	} 
    	this.bossBarMessage = message;
    	this.bossBarHealth = health;
	}
      
	public void removeBossBar() {
		if (this.bossBarMessage == null) {
			return;
		} 
		if (this.playerConnection.networkManager.getVersion() < 107) {
	        this.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(new int[] { this.bossBarEntityId }));
		} else {
	        PacketPlayOutBossBar packet = new PacketPlayOutBossBar();
	        packet.uuid = this.bossBarUUID;
	        packet.action = 1;
	        this.playerConnection.sendPacket((Packet)packet);
		} 
		this.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(this.bossBarEntityId));
		this.bossBarMessage = null;
	}
      
	private void moveBossBarEntity() {
		if (this.bossBarMessage == null) {
			return;
		}
		final int version = this.playerConnection.networkManager.getVersion();
		if (version >= 107) {
	        return; 
		}
		if (this.ticksLived % ((version == 47) ? 5 : 50) != 0) {
			return; 
		}
		PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport();
    	teleportPacket.a = this.bossBarEntityId;
    	if (version == 47) {
    		double pitch = Math.toRadians(this.pitch);
    		double yaw = Math.toRadians(this.yaw);
    		teleportPacket.b = (int)((this.locX - Math.sin(yaw) * Math.cos(pitch) * 32.0D) * 32.0D);
    		teleportPacket.c = (int)((this.locY - Math.sin(pitch) * 32.0D) * 32.0D);
    		teleportPacket.d = (int)((this.locZ + Math.cos(yaw) * Math.cos(pitch) * 32.0D) * 32.0D);
    	} else {
    		teleportPacket.b = (int)(this.locX * 32.0D);
    		teleportPacket.c = -9600;
    		teleportPacket.d = (int)(this.locZ * 32.0D);
    	} 
    	this.playerConnection.sendPacket(teleportPacket);
	}

    @Override
    public String toString() {
        return super.toString() + "(" + this.getName() + " at " + this.locX + "," + this.locY + "," + this.locZ + ")";
    }

    public void reset() {
        float exp = 0;
        final boolean keepInventory = this.world.getGameRules().getBoolean("keepInventory");

        if (this.keepLevel || keepInventory) {
            exp = this.exp;
            this.newTotalExp = this.expTotal;
            this.newLevel = this.expLevel;
        }

        this.setHealth(this.getMaxHealth());
        this.fireTicks = 0;
        this.fallDistance = 0;
        this.foodData = new FoodMetaData(this);
        this.expLevel = this.newLevel;
        this.expTotal = this.newTotalExp;
        this.exp = 0;
        this.deathTicks = 0;
        this.removeAllEffects();
        this.updateEffects = true;
        // Clear potion metadata now, because new effects might get added
        // before the update in the tick has a chance to run, and if they
        // match the old effects, the metadata will never be marked dirty
        // and will go out of sync with the client.
        this.datawatcher.watch(8, Byte.valueOf((byte) 0));
        this.datawatcher.watch(7, Integer.valueOf(0));
        this.setInvisible(false);
        this.activeContainer = this.defaultContainer;
        this.killer = null;
        this.lastDamager = null;
        this.combatTracker = new CombatTracker(this);
        this.lastSentExp = -1;
        this.p(0);
        if (this.keepLevel || keepInventory) {
            this.exp = exp;
        } else {
            this.giveExp(this.newExp);
        }
        this.keepLevel = false;
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        return (CraftPlayer) super.getBukkitEntity();
    }
    // CraftBukkit end
    
    @Override
    public Packet createSpecificSpawnPacket() {
        return new PacketPlayOutNamedEntitySpawn(this);
    }
    
	@Override
	public void track(EntityTracker tracker) {
		tracker.addEntity(this, 512, 2);
        final Iterator<EntityTrackerEntry> iterator = tracker.c.iterator();

        while (iterator.hasNext()) {
            EntityTrackerEntry entitytrackerentry = iterator.next();

            if (entitytrackerentry.tracker != this) {
                entitytrackerentry.updatePlayer(this);
            }
        }
	}
}
