package org.bukkit.craftbukkit.event;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftStatistic;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftDamageSource;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.meta.BookMeta;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import io.noks.enums.BodyParts;
import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.Container;
import net.minecraft.server.DamageSource;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityDamageSource;
import net.minecraft.server.EntityDamageSourceIndirect;
import net.minecraft.server.EntityEnderCrystal;
import net.minecraft.server.EntityEnderDragon;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityInsentient;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityPotion;
import net.minecraft.server.Explosion;
import net.minecraft.server.InventoryCrafting;
import net.minecraft.server.ItemStack;
import net.minecraft.server.Items;
import net.minecraft.server.MovingObjectPosition;
import net.minecraft.server.PacketPlayInCloseWindow;
import net.minecraft.server.PacketPlayOutSetSlot;
import net.minecraft.server.Slot;
import net.minecraft.server.World;
import net.minecraft.server.WorldServer;

public class CraftEventFactory {
    public static final DamageSource MELTING = CraftDamageSource.copyOf(DamageSource.BURN);
    public static final DamageSource POISON = CraftDamageSource.copyOf(DamageSource.MAGIC);
    public static org.bukkit.block.Block blockDamage; // For use in EntityDamageByBlockEvent
    public static Entity entityDamage; // For use in EntityDamageByEntityEvent

    // helper methods
    private static boolean canBuild(CraftWorld world, Player player, int x, int z) {
        final WorldServer worldServer = world.getHandle();
        final int spawnSize = Bukkit.getServer().getSpawnRadius();

        if (world.getHandle().dimension != 0) return true;
        if (spawnSize <= 0) return true;
        if (((CraftServer) Bukkit.getServer()).getHandle().getOPs().isEmpty()) return true;
        if (player.isOp()) return true;

        final ChunkCoordinates chunkcoordinates = worldServer.getSpawn();

        final int distanceFromSpawn = Math.max(Math.abs(x - chunkcoordinates.x), Math.abs(z - chunkcoordinates.z));
        return distanceFromSpawn > spawnSize;
    }

    public static <T extends Event> T callEvent(T event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Block place methods
     */
    public static BlockMultiPlaceEvent callBlockMultiPlaceEvent(World world, EntityHuman who, List<BlockState> blockStates, int clickedX, int clickedY, int clickedZ) {
    	final CraftWorld craftWorld = world.getWorld();
    	final CraftServer craftServer = world.getServer();
    	final Player player = (who == null) ? null : (Player) who.getBukkitEntity();

    	final Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);

        boolean canBuild = true;
        for (int i = 0; i < blockStates.size(); i++) {
            if (!canBuild(craftWorld, player, blockStates.get(i).getX(), blockStates.get(i).getZ())) {
                canBuild = false;
                break;
            }
        }

        final BlockMultiPlaceEvent event = new BlockMultiPlaceEvent(blockStates, blockClicked, player.getItemInHand(), player, canBuild);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static BlockPlaceEvent callBlockPlaceEvent(World world, EntityHuman who, BlockState replacedBlockState, int clickedX, int clickedY, int clickedZ) {
    	final CraftWorld craftWorld = world.getWorld();
    	final CraftServer craftServer = world.getServer();

    	final Player player = (who == null) ? null : (Player) who.getBukkitEntity();

    	final Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
    	final Block placedBlock = replacedBlockState.getBlock();

    	final boolean canBuild = canBuild(craftWorld, player, placedBlock.getX(), placedBlock.getZ());

    	final BlockPlaceEvent event = new BlockPlaceEvent(placedBlock, replacedBlockState, blockClicked, player.getItemInHand(), player, canBuild);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * Mob spawner event
     */
    public static SpawnerSpawnEvent callSpawnerSpawnEvent(Entity spawnee, int spawnerX, int spawnerY, int spawnerZ) {
    	final org.bukkit.craftbukkit.entity.CraftEntity entity = spawnee.getBukkitEntity();
        BlockState state = entity.getWorld().getBlockAt(spawnerX, spawnerY, spawnerZ).getState();

        if (!(state instanceof CreatureSpawner)) {
            state = null;
        }

        final SpawnerSpawnEvent event = new SpawnerSpawnEvent(entity, (CreatureSpawner) state);
        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Bucket methods
     */
    public static PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(EntityHuman who, int clickedX, int clickedY, int clickedZ, int clickedFace, ItemStack itemInHand) {
        return (PlayerBucketEmptyEvent) getPlayerBucketEvent(false, who, clickedX, clickedY, clickedZ, clickedFace, itemInHand, Items.BUCKET);
    }

    public static PlayerBucketFillEvent callPlayerBucketFillEvent(EntityHuman who, int clickedX, int clickedY, int clickedZ, int clickedFace, ItemStack itemInHand, net.minecraft.server.Item bucket) {
        return (PlayerBucketFillEvent) getPlayerBucketEvent(true, who, clickedX, clickedY, clickedZ, clickedFace, itemInHand, bucket);
    }

    private static PlayerEvent getPlayerBucketEvent(boolean isFilling, EntityHuman who, int clickedX, int clickedY, int clickedZ, int clickedFace, ItemStack itemstack, net.minecraft.server.Item item) {
    	final Player player = (who == null) ? null : (Player) who.getBukkitEntity();
    	final CraftItemStack itemInHand = CraftItemStack.asNewCraftStack(item);
    	final Material bucket = CraftMagicNumbers.getMaterial(itemstack.getItem());

    	final CraftWorld craftWorld = (CraftWorld) player.getWorld();
    	final CraftServer craftServer = (CraftServer) player.getServer();

    	final Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
    	final BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);

    	final PlayerBucketEvent event = (isFilling ? new PlayerBucketFillEvent(player, blockClicked, blockFace, bucket, itemInHand) : new PlayerBucketEmptyEvent(player, blockClicked, blockFace, bucket, itemInHand));
        /*PlayerEvent event = null;
        if (isFilling) {
            event = new PlayerBucketFillEvent(player, blockClicked, blockFace, bucket, itemInHand);
            ((PlayerBucketFillEvent) event).setCancelled(!canBuild(craftWorld, player, clickedX, clickedZ));
        } else {
            event = new PlayerBucketEmptyEvent(player, blockClicked, blockFace, bucket, itemInHand);
            ((PlayerBucketEmptyEvent) event).setCancelled(!canBuild(craftWorld, player, clickedX, clickedZ));
        }*/

        event.setCancelled(!canBuild(craftWorld, player, clickedX, clickedZ));
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Player Interact event
     */
    public static PlayerInteractEvent callPlayerInteractEvent(EntityHuman who, Action action, ItemStack itemstack) {
        if (action != Action.LEFT_CLICK_AIR && action != Action.RIGHT_CLICK_AIR) {
            throw new IllegalArgumentException(String.format("%s performing %s with %s", who, action, itemstack)); // Spigot
        }
        return callPlayerInteractEvent(who, action, 0, 256, 0, 0, itemstack);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(EntityHuman who, Action action, int clickedX, int clickedY, int clickedZ, int clickedFace, ItemStack itemstack) {
    	final Player player = (who == null) ? null : (Player) who.getBukkitEntity();
    	CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

    	final CraftWorld craftWorld = (CraftWorld) player.getWorld();
    	final CraftServer craftServer = (CraftServer) player.getServer();

        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
        final BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);

        if (clickedY > 255) {
            blockClicked = null;
            switch (action) {
            case LEFT_CLICK_BLOCK:
                action = Action.LEFT_CLICK_AIR;
                break;
            case RIGHT_CLICK_BLOCK:
                action = Action.RIGHT_CLICK_AIR;
                break;
            }
        }

        if (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0) {
            itemInHand = null;
        }

        final PlayerInteractEvent event = new PlayerInteractEvent(player, action, itemInHand, blockClicked, blockFace);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * EntityShootBowEvent
     */
    public static EntityShootBowEvent callEntityShootBowEvent(EntityLiving who, ItemStack itemstack, EntityArrow entityArrow, float force) {
    	final LivingEntity shooter = (LivingEntity) who.getBukkitEntity();
    	CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);
    	final Arrow arrow = (Arrow) entityArrow.getBukkitEntity();

        if (itemInHand != null && (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0)) {
            itemInHand = null;
        }

        final EntityShootBowEvent event = new EntityShootBowEvent(shooter, itemInHand, arrow, force);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * BlockDamageEvent
     */
    public static BlockDamageEvent callBlockDamageEvent(EntityHuman who, int x, int y, int z, ItemStack itemstack, boolean instaBreak) {
    	final Player player = (who == null) ? null : (Player) who.getBukkitEntity();
    	final CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

    	final CraftWorld craftWorld = (CraftWorld) player.getWorld();
    	final CraftServer craftServer = (CraftServer) player.getServer();

    	final Block blockClicked = craftWorld.getBlockAt(x, y, z);

    	final BlockDamageEvent event = new BlockDamageEvent(player, blockClicked, itemInHand, instaBreak);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * CreatureSpawnEvent
     */
    public static CreatureSpawnEvent callCreatureSpawnEvent(EntityLiving entityliving, SpawnReason spawnReason) {
    	final LivingEntity entity = (LivingEntity) entityliving.getBukkitEntity();
    	final CraftServer craftServer = (CraftServer) entity.getServer();

    	final CreatureSpawnEvent event = new CreatureSpawnEvent(entity, spawnReason);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * EntityTameEvent
     */
    public static EntityTameEvent callEntityTameEvent(EntityInsentient entity, EntityHuman tamer) {
    	final org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
    	final org.bukkit.entity.AnimalTamer bukkitTamer = (tamer != null ? tamer.getBukkitEntity() : null);
    	final CraftServer craftServer = (CraftServer) bukkitEntity.getServer();

        entity.persistent = true;

        final EntityTameEvent event = new EntityTameEvent((LivingEntity) bukkitEntity, bukkitTamer);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemSpawnEvent
     */
    public static ItemSpawnEvent callItemSpawnEvent(EntityItem entityitem) {
    	final org.bukkit.entity.Item entity = (org.bukkit.entity.Item) entityitem.getBukkitEntity();
    	final CraftServer craftServer = (CraftServer) entity.getServer();

    	final ItemSpawnEvent event = new ItemSpawnEvent(entity, entity.getLocation());

        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemDespawnEvent
     */
    public static ItemDespawnEvent callItemDespawnEvent(EntityItem entityitem) {
    	final org.bukkit.entity.Item entity = (org.bukkit.entity.Item) entityitem.getBukkitEntity();

    	final ItemDespawnEvent event = new ItemDespawnEvent(entity, entity.getLocation());

        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * PotionSplashEvent
     */
    public static PotionSplashEvent callPotionSplashEvent(EntityPotion potion, Map<LivingEntity, Double> affectedEntities) {
    	final ThrownPotion thrownPotion = (ThrownPotion) potion.getBukkitEntity();

    	final PotionSplashEvent event = new PotionSplashEvent(thrownPotion, affectedEntities);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * BlockFadeEvent
     */
    public static BlockFadeEvent callBlockFadeEvent(Block block, net.minecraft.server.Block type) {
    	final BlockState state = block.getState();
        state.setTypeId(net.minecraft.server.Block.getId(type));

        final BlockFadeEvent event = new BlockFadeEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void handleBlockSpreadEvent(Block block, Block source, net.minecraft.server.Block type, int data) {
    	final BlockState state = block.getState();
        state.setTypeId(net.minecraft.server.Block.getId(type));
        state.setRawData((byte) data);

        final BlockSpreadEvent event = new BlockSpreadEvent(block, source, state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
    }

    public static EntityDeathEvent callEntityDeathEvent(EntityLiving victim) {
        return callEntityDeathEvent(victim, new ArrayList<org.bukkit.inventory.ItemStack>(0));
    }

    public static EntityDeathEvent callEntityDeathEvent(EntityLiving victim, List<org.bukkit.inventory.ItemStack> drops) {
    	final CraftLivingEntity entity = (CraftLivingEntity) victim.getBukkitEntity();
    	final EntityDeathEvent event = new EntityDeathEvent(entity, drops, victim.getExpReward());
    	final CraftWorld world = (CraftWorld) entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);

        victim.expToDrop = event.getDroppedExp();

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0) continue;

            world.dropItemNaturally(entity.getLocation(), stack, victim);
        }

        return event;
    }

    public static PlayerDeathEvent callPlayerDeathEvent(EntityPlayer victim, List<org.bukkit.inventory.ItemStack> drops, String deathMessage, boolean keepInventory) {
    	final CraftPlayer entity = victim.getBukkitEntity();
    	final PlayerDeathEvent event = new PlayerDeathEvent(entity, drops, victim.getExpReward(), 0, deathMessage);
        event.setKeepInventory(keepInventory);
        final CraftWorld world = (CraftWorld) entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);

        victim.keepLevel = event.getKeepLevel();
        victim.newLevel = event.getNewLevel();
        victim.newTotalExp = event.getNewTotalExp();
        victim.expToDrop = event.getDroppedExp();
        victim.newExp = event.getNewExp();

        if (event.getKeepInventory()) {
            return event;
        }

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR) continue;

            world.dropItemNaturally(entity.getLocation(), stack, victim);
        }

        return event;
    }

    /**
     * Server methods
     */
    public static ServerListPingEvent callServerListPingEvent(Server craftServer, InetAddress address, String motd, int numPlayers, int maxPlayers) {
    	final ServerListPingEvent event = new ServerListPingEvent(address, motd, numPlayers, maxPlayers);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    private static EntityDamageEvent handleEntityDamageEvent(Entity entity, DamageSource source, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions) {
        if (source.isExplosion()) {
            DamageCause damageCause;
            final Entity damager = entityDamage;
            entityDamage = null;
            EntityDamageEvent event;
            if (damager == null) {
                event = new EntityDamageByBlockEvent(null, entity.getBukkitEntity(), DamageCause.BLOCK_EXPLOSION, modifiers, modifierFunctions);
            } else if (entity instanceof EntityEnderDragon && ((EntityEnderDragon) entity).bC == damager) {
                event = new EntityDamageEvent(entity.getBukkitEntity(), DamageCause.ENTITY_EXPLOSION, modifiers, modifierFunctions);
            } else {
                if (damager instanceof org.bukkit.entity.TNTPrimed) {
                    damageCause = DamageCause.BLOCK_EXPLOSION;
                } else {
                    damageCause = DamageCause.ENTITY_EXPLOSION;
                }
                event = new EntityDamageByEntityEvent(damager.getBukkitEntity(), entity.getBukkitEntity(), damageCause, modifiers, modifierFunctions);
            }
            return callEvent(event);
        } else if (source instanceof EntityDamageSource) {
            Entity damager = source.getEntity();
            DamageCause cause = DamageCause.ENTITY_ATTACK;

            if (source instanceof EntityDamageSourceIndirect) {
                damager = ((EntityDamageSourceIndirect) source).getProximateDamageSource();
                if (damager.getBukkitEntity() instanceof ThrownPotion) {
                    cause = DamageCause.MAGIC;
                } else if (damager.getBukkitEntity() instanceof Projectile) {
                    cause = DamageCause.PROJECTILE;
                }
            } else if ("thorns".equals(source.translationIndex)) {
                cause = DamageCause.THORNS;
            }

            return callEntityDamageEvent(damager, entity, cause, modifiers, modifierFunctions);
        } else if (source == DamageSource.OUT_OF_WORLD) {
            return callEvent(new EntityDamageByBlockEvent(null, entity.getBukkitEntity(), DamageCause.VOID, modifiers, modifierFunctions));
        } else if (source == DamageSource.LAVA) {
            return callEvent(new EntityDamageByBlockEvent(null, entity.getBukkitEntity(), DamageCause.LAVA, modifiers, modifierFunctions));
        } else if (blockDamage != null) {
            DamageCause cause = null;
            final Block damager = blockDamage;
            blockDamage = null;
            if (source == DamageSource.CACTUS) {
                cause = DamageCause.CONTACT;
            } else {
                throw new RuntimeException(String.format("Unhandled damage of %s by %s from %s", entity, damager, source.translationIndex)); // Spigot
            }
            return callEvent(new EntityDamageByBlockEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions));
        } else if (entityDamage != null) {
            DamageCause cause = null;
            CraftEntity damager = entityDamage.getBukkitEntity();
            entityDamage = null;
            if (source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) {
                cause = DamageCause.FALLING_BLOCK;
            } else if (damager instanceof LightningStrike) {
                cause = DamageCause.LIGHTNING;
            } else if (source == DamageSource.FALL) {
                cause = DamageCause.FALL;
            } else {
                throw new RuntimeException(String.format("Unhandled damage of %s by %s from %s", entity, damager.getHandle(), source.translationIndex)); // Spigot
            }
            return callEvent(new EntityDamageByEntityEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions));
        }

        DamageCause cause = null;
        if (source == DamageSource.FIRE) {
            cause = DamageCause.FIRE;
        } else if (source == DamageSource.STARVE) {
            cause = DamageCause.STARVATION;
        } else if (source == DamageSource.WITHER) {
            cause = DamageCause.WITHER;
        } else if (source == DamageSource.STUCK) {
            cause = DamageCause.SUFFOCATION;
        } else if (source == DamageSource.DROWN) {
            cause = DamageCause.DROWNING;
        } else if (source == DamageSource.BURN) {
            cause = DamageCause.FIRE_TICK;
        } else if (source == MELTING) {
            cause = DamageCause.MELTING;
        } else if (source == POISON) {
            cause = DamageCause.POISON;
        } else if (source == DamageSource.MAGIC) {
            cause = DamageCause.MAGIC;
        } else if (source == DamageSource.FALL) {
            cause = DamageCause.FALL;
        } else if (source == DamageSource.GENERIC) {
            return new EntityDamageEvent(entity.getBukkitEntity(), null, modifiers, modifierFunctions);
        }

        if (cause != null) {
            return callEntityDamageEvent(null, entity, cause, modifiers, modifierFunctions);
        }

        throw new RuntimeException(String.format("Unhandled damage of %s from %s", entity, source.translationIndex)); // Spigot
    }

    private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions) {
    	final EntityDamageEvent event = (damager != null ? new EntityDamageByEntityEvent(damager.getBukkitEntity(), damagee.getBukkitEntity(), cause, modifiers, modifierFunctions) : new EntityDamageEvent(damagee.getBukkitEntity(), cause, modifiers, modifierFunctions));
        return callEvent(event);
    }

    private static final Function<? super Double, Double> ZERO = Functions.constant(-0.0);
    private static final EnumMap<DamageModifier, Function<? super Double, Double>> DEFAULT_MODIFIERS;

    static {
        DEFAULT_MODIFIERS = new EnumMap<>(DamageModifier.class);
        DEFAULT_MODIFIERS.put(DamageModifier.BASE, ZERO);
    }
    public static EntityDamageEvent handleLivingEntityDamageEvent(Entity damagee, DamageSource source, double rawDamage, double hardHatModifier, double blockingModifier, double armorModifier, double resistanceModifier, double magicModifier, double absorptionModifier, Function<Double, Double> hardHat, Function<Double, Double> blocking, Function<Double, Double> armor, Function<Double, Double> resistance, Function<Double, Double> magic, Function<Double, Double> absorption) {
    	final EnumMap<DamageModifier, Double> modifiers = new EnumMap<DamageModifier, Double>(DamageModifier.class);
    	final EnumMap<DamageModifier, Function<? super Double, Double>> modifierFunctions = DEFAULT_MODIFIERS.clone();
    	
        modifiers.put(DamageModifier.BASE, rawDamage);
        
        if (source == DamageSource.FALLING_BLOCK || source == DamageSource.ANVIL) {
            modifiers.put(DamageModifier.HARD_HAT, hardHatModifier);
            modifierFunctions.put(DamageModifier.HARD_HAT, hardHat);
        }
        
        if (damagee instanceof EntityHuman) {
            modifiers.put(DamageModifier.BLOCKING, blockingModifier);
            modifierFunctions.put(DamageModifier.BLOCKING, blocking);
        }
        
        modifiers.put(DamageModifier.ARMOR, armorModifier);
        modifierFunctions.put(DamageModifier.ARMOR, armor);
        
        modifiers.put(DamageModifier.RESISTANCE, resistanceModifier);
        modifierFunctions.put(DamageModifier.RESISTANCE, resistance);
        
        modifiers.put(DamageModifier.MAGIC, magicModifier);
        modifierFunctions.put(DamageModifier.MAGIC, magic);
        
        modifiers.put(DamageModifier.ABSORPTION, absorptionModifier);
        modifierFunctions.put(DamageModifier.ABSORPTION, absorption);
        
        return handleEntityDamageEvent(damagee, source, modifiers, modifierFunctions);
    }

    // Non-Living Entities such as EntityEnderCrystal and EntityFireball need to call this
    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage) {
        return handleNonLivingEntityDamageEvent(entity, source, damage, true);
    }

    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelOnZeroDamage) {
        if (entity instanceof EntityEnderCrystal && !(source instanceof EntityDamageSource)) {
            return false;
        }

        final EnumMap<DamageModifier, Double> modifiers = new EnumMap<DamageModifier, Double>(DamageModifier.class);
        final EnumMap<DamageModifier, Function<? super Double, Double>> functions = new EnumMap<DamageModifier, Function<? super Double, Double>>(DamageModifier.class);

        modifiers.put(DamageModifier.BASE, damage);
        functions.put(DamageModifier.BASE, ZERO);

        final EntityDamageEvent event = handleEntityDamageEvent(entity, source, modifiers, functions);
        if (event == null) {
            return false;
        }
        return event.isCancelled() || (cancelOnZeroDamage && event.getDamage() == 0);
    }

    public static PlayerLevelChangeEvent callPlayerLevelChangeEvent(Player player, int oldLevel, int newLevel) {
    	final PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(player, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerExpChangeEvent callPlayerExpChangeEvent(EntityHuman entity, int expAmount) {
    	final Player player = (Player) entity.getBukkitEntity();
    	final PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, expAmount);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void handleBlockGrowEvent(World world, int x, int y, int z, net.minecraft.server.Block type, int data) {
    	final Block block = world.getWorld().getBlockAt(x, y, z);
    	final CraftBlockState state = (CraftBlockState) block.getState();
        state.setTypeId(net.minecraft.server.Block.getId(type));
        state.setRawData((byte) data);

        final BlockGrowEvent event = new BlockGrowEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
    }

    public static FoodLevelChangeEvent callFoodLevelChangeEvent(EntityHuman entity, int level) {
    	final FoodLevelChangeEvent event = new FoodLevelChangeEvent(entity.getBukkitEntity(), level);
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PigZapEvent callPigZapEvent(Entity pig, Entity lightning, Entity pigzombie) {
    	final PigZapEvent event = new PigZapEvent((Pig) pig.getBukkitEntity(), (LightningStrike) lightning.getBukkitEntity(), (PigZombie) pigzombie.getBukkitEntity());
        pig.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static HorseJumpEvent callHorseJumpEvent(Entity horse, float power) {
    	final HorseJumpEvent event = new HorseJumpEvent((Horse) horse.getBukkitEntity(), power);
        horse.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(org.bukkit.entity.Entity entity, Block block, Material material) {
        return callEntityChangeBlockEvent(entity, block, material, 0);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, Block block, Material material) {
        return callEntityChangeBlockEvent(entity.getBukkitEntity(), block, material, 0);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, Block block, Material material, boolean cancelled) {
        return callEntityChangeBlockEvent(entity.getBukkitEntity(), block, material, 0, cancelled);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, int x, int y, int z, net.minecraft.server.Block type, int data) {
    	final Block block = entity.world.getWorld().getBlockAt(x, y, z);
    	final Material material = CraftMagicNumbers.getMaterial(type);

        return callEntityChangeBlockEvent(entity.getBukkitEntity(), block, material, data);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(org.bukkit.entity.Entity entity, Block block, Material material, int data) {
        return callEntityChangeBlockEvent(entity, block, material, data, false);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(org.bukkit.entity.Entity entity, Block block, Material material, int data, boolean cancelled) {
    	final EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity, block, material, (byte) data);
        event.setCancelled(cancelled);
        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static CreeperPowerEvent callCreeperPowerEvent(Entity creeper, Entity lightning, CreeperPowerEvent.PowerCause cause) {
    	final CreeperPowerEvent event = new CreeperPowerEvent((Creeper) creeper.getBukkitEntity(), (LightningStrike) lightning.getBukkitEntity(), cause);
        creeper.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetEvent callEntityTargetEvent(Entity entity, Entity target, EntityTargetEvent.TargetReason reason) {
    	final EntityTargetEvent event = new EntityTargetEvent(entity.getBukkitEntity(), target == null ? null : target.getBukkitEntity(), reason);
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetLivingEntityEvent callEntityTargetLivingEvent(Entity entity, EntityLiving target, EntityTargetEvent.TargetReason reason) {
    	final EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(entity.getBukkitEntity(), (LivingEntity) target.getBukkitEntity(), reason);
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityBreakDoorEvent callEntityBreakDoorEvent(Entity entity, int x, int y, int z) {
    	final org.bukkit.entity.Entity entity1 = entity.getBukkitEntity();
    	final Block block = entity1.getWorld().getBlockAt(x, y, z);

    	final EntityBreakDoorEvent event = new EntityBreakDoorEvent((LivingEntity) entity1, block);
        entity1.getServer().getPluginManager().callEvent(event);

        return event;
    }

    public static Container callInventoryOpenEvent(EntityPlayer player, Container container) {
        if (player.activeContainer != player.defaultContainer) { // fire INVENTORY_CLOSE if one already open
            player.playerConnection.a(new PacketPlayInCloseWindow(player.activeContainer.windowId));
        }

        final CraftServer server = player.world.getServer();
        final CraftPlayer craftPlayer = player.getBukkitEntity();
        player.activeContainer.transferTo(container, craftPlayer);

        final InventoryOpenEvent event = new InventoryOpenEvent(container.getBukkitView());
        server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            container.transferTo(player.activeContainer, craftPlayer);
            return null;
        }

        return container;
    }

    public static ItemStack callPreCraftEvent(InventoryCrafting matrix, ItemStack result, InventoryView lastCraftView, boolean isRepair) {
    	final CraftInventoryCrafting inventory = new CraftInventoryCrafting(matrix, matrix.resultInventory);
        inventory.setResult(CraftItemStack.asCraftMirror(result));

        final PrepareItemCraftEvent event = new PrepareItemCraftEvent(inventory, lastCraftView, isRepair);
        Bukkit.getPluginManager().callEvent(event);

        final org.bukkit.inventory.ItemStack bitem = event.getInventory().getResult();

        return CraftItemStack.asNMSCopy(bitem);
    }

    public static ProjectileLaunchEvent callProjectileLaunchEvent(Entity entity) {
    	final Projectile bukkitEntity = (Projectile) entity.getBukkitEntity();
    	final ProjectileLaunchEvent event = new ProjectileLaunchEvent(bukkitEntity);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
    
	public static ProjectileHitEvent callProjectileHitEvent(Entity entity, MovingObjectPosition position) {
		final Entity hit = position.entity;
		BodyParts part = null;
		
		ProjectileHitEvent event = new ProjectileHitEvent((Projectile)entity.getBukkitEntity(), (hit == null) ? null : hit.getBukkitEntity());
		if (hit != null && hit instanceof EntityPlayer player) {
			double hitY = entity.locY;
	        double playerMinY = player.locY;
	        double playerMaxY = playerMinY + player.getHeadHeight();
	        double heightDiff = playerMaxY - playerMinY;
	        
	        if (hitY >= (playerMaxY - heightDiff * 0.25)) {
	            part = BodyParts.HEAD;
	        } else if (hitY >= (playerMaxY - heightDiff * 0.50)) {
	            part = BodyParts.UPPER_BODY;
	        } else if (hitY >= (playerMaxY - heightDiff * 0.75)) {
	            part = BodyParts.LOWER_BODY;
	        } else {
	            part = BodyParts.FEET;
	        }
			event = new ProjectileHitEvent((Projectile)entity.getBukkitEntity(), player.getBukkitEntity(), part);
		}
		entity.world.getServer().getPluginManager().callEvent(event);
		return event;
	}

    public static ExpBottleEvent callExpBottleEvent(Entity entity, int exp) {
    	final ThrownExpBottle bottle = (ThrownExpBottle) entity.getBukkitEntity();
    	final ExpBottleEvent event = new ExpBottleEvent(bottle, exp);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockRedstoneEvent callRedstoneChange(World world, int x, int y, int z, int oldCurrent, int newCurrent) {
    	final BlockRedstoneEvent event = new BlockRedstoneEvent(world.getWorld().getBlockAt(x, y, z), oldCurrent, newCurrent);
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static NotePlayEvent callNotePlayEvent(World world, int x, int y, int z, byte instrument, byte note) {
    	final NotePlayEvent event = new NotePlayEvent(world.getWorld().getBlockAt(x, y, z), org.bukkit.Instrument.getByType(instrument), new org.bukkit.Note(note));
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void callPlayerItemBreakEvent(EntityHuman human, ItemStack brokenItem) {
    	final CraftItemStack item = CraftItemStack.asCraftMirror(brokenItem);
    	final PlayerItemBreakEvent event = new PlayerItemBreakEvent((Player) human.getBukkitEntity(), item);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, int igniterX, int igniterY, int igniterZ) {
    	final org.bukkit.World bukkitWorld = world.getWorld();
    	final Block igniter = bukkitWorld.getBlockAt(igniterX, igniterY, igniterZ);
    	final IgniteCause cause = switch (igniter.getType()) {
    		case LAVA, STATIONARY_LAVA -> IgniteCause.LAVA;
    		case DISPENSER -> IgniteCause.FLINT_AND_STEEL;
    		case FIRE -> IgniteCause.SPREAD;
    		default -> IgniteCause.SPREAD;
    	};

        final BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), cause, igniter);
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, Entity igniter) {
    	final org.bukkit.World bukkitWorld = world.getWorld();
    	final org.bukkit.entity.Entity bukkitIgniter = igniter.getBukkitEntity();
        final IgniteCause cause = switch (bukkitIgniter.getType()) {
        	case ENDER_CRYSTAL -> IgniteCause.ENDER_CRYSTAL;
        	case LIGHTNING -> IgniteCause.LIGHTNING;
        	case SMALL_FIREBALL, FIREBALL -> IgniteCause.FIREBALL;
        	default -> IgniteCause.FLINT_AND_STEEL;
        };

        final BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), cause, bukkitIgniter);
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, Explosion explosion) {
    	final org.bukkit.World bukkitWorld = world.getWorld();
    	final org.bukkit.entity.Entity igniter = explosion.source == null ? null : explosion.source.getBukkitEntity();

    	final BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), IgniteCause.EXPLOSION, igniter);
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, IgniteCause cause, Entity igniter) {
    	final BlockIgniteEvent event = new BlockIgniteEvent(world.getWorld().getBlockAt(x, y, z), cause, igniter.getBukkitEntity());
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void handleInventoryCloseEvent(EntityHuman human) {
        if (human.activeContainer == human.defaultContainer) return; // Spigot
        final InventoryCloseEvent event = new InventoryCloseEvent(human.activeContainer.getBukkitView());
        human.world.getServer().getPluginManager().callEvent(event);
        human.activeContainer.transferTo(human.defaultContainer, human.getBukkitEntity());
    }

    public static void handleEditBookEvent(EntityPlayer player, ItemStack newBookItem) {
    	final int itemInHandIndex = player.inventory.itemInHandIndex;

        final PlayerEditBookEvent editBookEvent = new PlayerEditBookEvent(player.getBukkitEntity(), player.inventory.itemInHandIndex, (BookMeta) CraftItemStack.getItemMeta(player.inventory.getItemInHand()), (BookMeta) CraftItemStack.getItemMeta(newBookItem), newBookItem.getItem() == Items.WRITTEN_BOOK);
        player.world.getServer().getPluginManager().callEvent(editBookEvent);
        final ItemStack itemInHand = player.inventory.getItem(itemInHandIndex);

        // If they've got the same item in their hand, it'll need to be updated.
        if (itemInHand != null && itemInHand.getItem() == Items.BOOK_AND_QUILL) {
            if (!editBookEvent.isCancelled()) {
                CraftItemStack.setItemMeta(itemInHand, editBookEvent.getNewBookMeta());
                if (editBookEvent.isSigning()) {
                    itemInHand.setItem(Items.WRITTEN_BOOK);
                }
            }

            // Client will have updated its idea of the book item; we need to overwrite that
            final Slot slot = player.activeContainer.getSlot(player.inventory, itemInHandIndex);
            player.playerConnection.sendPacket(new PacketPlayOutSetSlot(player.activeContainer.windowId, slot.rawSlotIndex, itemInHand));
        }
    }

    public static PlayerUnleashEntityEvent callPlayerUnleashEntityEvent(EntityInsentient entity, EntityHuman player) {
    	final PlayerUnleashEntityEvent event = new PlayerUnleashEntityEvent(entity.getBukkitEntity(), (Player) player.getBukkitEntity());
        entity.world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerLeashEntityEvent callPlayerLeashEntityEvent(EntityInsentient entity, Entity leashHolder, EntityHuman player) {
    	final PlayerLeashEntityEvent event = new PlayerLeashEntityEvent(entity.getBukkitEntity(), leashHolder.getBukkitEntity(), (Player) player.getBukkitEntity());
        entity.world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static Cancellable handleStatisticsIncrease(EntityHuman entityHuman, net.minecraft.server.Statistic statistic, int current, int incrementation) {
    	final Player player = ((EntityPlayer) entityHuman).getBukkitEntity();
        Event event;
        if (statistic instanceof net.minecraft.server.Achievement) {
            if (current != 0) {
                return null;
            }
            event = new PlayerAchievementAwardedEvent(player, CraftStatistic.getBukkitAchievement((net.minecraft.server.Achievement) statistic));
        } else {
        	final org.bukkit.Statistic stat = CraftStatistic.getBukkitStatistic(statistic);
            switch (stat) {
                case FALL_ONE_CM, 
                	 BOAT_ONE_CM,
                	 CLIMB_ONE_CM,
                	 DIVE_ONE_CM,
                	 FLY_ONE_CM,
                	 HORSE_ONE_CM,
                	 MINECART_ONE_CM,
                	 PIG_ONE_CM,
                	 PLAY_ONE_TICK,
                	 SWIM_ONE_CM,
                	 WALK_ONE_CM:
                    // Do not process event for these - too spammy
                    return null;
                default: break;
            }
            event = switch (stat.getType()) {
            	case UNTYPED -> new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation);
            	case ENTITY -> {
            		final EntityType entityType = CraftStatistic.getEntityTypeFromStatistic(statistic);
            		yield new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation, entityType);
            	}
            	default -> {
            		final Material material = CraftStatistic.getMaterialFromStatistic(statistic);
            		yield new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation, material);
            	}
            };
            /*if (stat.getType() == Type.UNTYPED) {
                event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation);
            } else if (stat.getType() == Type.ENTITY) {
            	final EntityType entityType = CraftStatistic.getEntityTypeFromStatistic(statistic);
                event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation, entityType);
            } else {
            	final Material material = CraftStatistic.getMaterialFromStatistic(statistic);
                event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation, material);
            }*/
        }
        entityHuman.world.getServer().getPluginManager().callEvent(event);
        return (Cancellable) event;
    }
}
