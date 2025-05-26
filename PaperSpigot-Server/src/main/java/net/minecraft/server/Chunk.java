package net.minecraft.server;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger; // PaperSpigot
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit; // CraftBukkit
import org.bukkit.entity.HumanEntity;

import io.noks.utils.EntityNPC;

public class Chunk {
    public static boolean a;
    private ChunkSection[] sections;
    private byte[] v;
    public int[] b;
    public boolean[] c;
    public boolean d;
    public World world;
    public int[] heightMap;
    public final int locX;
    public final int locZ;
    private boolean w;
    public Map<ChunkPosition, TileEntity> tileEntities;
    public List<net.minecraft.server.Entity>[] entitySlices;
    public boolean done;
    public boolean lit;
    public boolean m;
    public boolean n;
    public boolean o;
    public long lastSaved;
    public boolean q;
    public int r;
    public long s;
    private int x;
    protected net.minecraft.util.gnu.trove.map.hash.TObjectIntHashMap<Class> entityCount = new net.minecraft.util.gnu.trove.map.hash.TObjectIntHashMap<Class>(); // Spigot
    // PaperSpigot start - Asynchronous light updates
    public AtomicInteger pendingLightUpdates = new AtomicInteger();
    public long lightUpdateTime;
    // PaperSpigot end

    //private int emptySectionBits;
    public HashSet<EntityPlayer> playersInChunk = new HashSet<EntityPlayer>();

    // CraftBukkit start - Neighbor loaded cache for chunk lighting and entity ticking
    private int neighbors = 0x1 << 12;

    /*public boolean areNeighborsLoaded(final int radius) {
        switch(radius) {
            case 2:
                return this.neighbors == Integer.MAX_VALUE >> 6;
            case 1:
                final int mask =
                    //        x        z   offset            x        z   offset            x         z   offset
                    ( 0x1 << (1 * 5 +  1 + 12) ) | ( 0x1 << (0 * 5 +  1 + 12) ) | ( 0x1 << (-1 * 5 +  1 + 12) ) |
                    ( 0x1 << (1 * 5 +  0 + 12) ) | ( 0x1 << (0 * 5 +  0 + 12) ) | ( 0x1 << (-1 * 5 +  0 + 12) ) |
                    ( 0x1 << (1 * 5 + -1 + 12) ) | ( 0x1 << (0 * 5 + -1 + 12) ) | ( 0x1 << (-1 * 5 + -1 + 12) );
                return (this.neighbors & mask) == mask;
            default:
                throw new UnsupportedOperationException(String.valueOf(radius));
        }
    }*/
    public boolean areNeighborsLoaded(final int radius) {
        return switch(radius) {
            case 2 -> this.neighbors == Integer.MAX_VALUE >> 6;
            case 1 -> {
            	final byte offset = 12;
            	final byte five = 5;
            	final byte x = 0x1;
                final int mask =
                    //        x        z   offset            x        z   offset            x         z   offset
                    ( x << (1 * five +  1 + offset) ) | ( x << (0 * five +  1 + offset) ) | ( x << (-1 * five +  1 + offset) ) |
                    ( x << (1 * five +  0 + offset) ) | ( x << (0 * five +  0 + offset) ) | ( x << (-1 * five +  0 + offset) ) |
                    ( x << (1 * five + -1 + offset) ) | ( x << (0 * five + -1 + offset) ) | ( x << (-1 * five + -1 + offset) );
                yield (this.neighbors & mask) == mask;
            }
            default -> throw new UnsupportedOperationException(String.valueOf(radius));
        };
    }

    public void setNeighborLoaded(final int x, final int z) {
        this.neighbors |= 0x1 << (x * 5 + 12 + z);
    }

    public void setNeighborUnloaded(final int x, final int z) {
        this.neighbors &= ~(0x1 << (x * 5 + 12 + z));
    }
    // CraftBukkit end
    
    private final ChunkCoordIntPair chunkCoords; // PandaSpigot

    public Chunk(World world, int i, int j) {
        this.sections = new ChunkSection[16];
        this.v = new byte[256];
        this.b = new int[256];
        this.c = new boolean[256];
        this.tileEntities = new ConcurrentHashMap<>();
        this.x = 4096;
        this.entitySlices = new List[16];
        this.world = world;
        this.locX = i;
        this.locZ = j;
        this.heightMap = new int[256];

        for (int k = 0; k < this.entitySlices.length; ++k) {
            this.entitySlices[k] = new org.bukkit.craftbukkit.util.UnsafeList(); // CraftBukkit - ArrayList -> UnsafeList
        }

        Arrays.fill(this.b, -999);
        Arrays.fill(this.v, (byte) -1);

        // CraftBukkit start
        if (!(this instanceof EmptyChunk)) {
            this.bukkitChunk = new org.bukkit.craftbukkit.CraftChunk(this);
        }
        this.chunkCoords = new ChunkCoordIntPair(this.locX, this.locZ);
    }

    public org.bukkit.Chunk bukkitChunk;
    public boolean mustSave;
    // CraftBukkit end

    public Chunk(World world, Block[] ablock, int i, int j) {
        this(world, i, j);
        final int k = ablock.length / 256;
        final boolean flag = !world.worldProvider.g;

        for (int l = 0; l < 16; ++l) {
            for (int i1 = 0; i1 < 16; ++i1) {
                for (int j1 = 0; j1 < k; ++j1) {
                	final Block block = ablock[l << 11 | i1 << 7 | j1];

                    if (block != null && block.getMaterial() != Material.AIR) {
                    	final int k1 = j1 >> 4;

                        if (this.sections[k1] == null) {
                            this.sections[k1] = new ChunkSection(k1 << 4, flag);
                        }

                        this.sections[k1].setTypeId(l, j1 & 15, i1, block);
                    }
                }
            }
        }
    }

    public Chunk(World world, Block[] ablock, byte[] abyte, int i, int j) {
        this(world, i, j);
        final int k = ablock.length / 256;
        final boolean flag = !world.worldProvider.g;

        for (int l = 0; l < 16; ++l) {
            for (int i1 = 0; i1 < 16; ++i1) {
                for (int j1 = 0; j1 < k; ++j1) {
                    int k1 = l * k * 16 | i1 * k | j1;
                    final Block block = ablock[k1];

                    if (block != null && block != Blocks.AIR) {
                    	final int l1 = j1 >> 4;

                        if (this.sections[l1] == null) {
                            this.sections[l1] = new ChunkSection(l1 << 4, flag);
                        }

                        this.sections[l1].setTypeId(l, j1 & 15, i1, block);
                        this.sections[l1].setData(l, j1 & 15, i1, checkData( block, abyte[k1] ) );
                    }
                }
            }
        }
    }

    public boolean a(int i, int j) {
        return i == this.locX && j == this.locZ;
    }

    public int b(int i, int j) {
        return this.heightMap[j << 4 | i];
    }

    public int h() {
        for (int i = this.sections.length - 1; i >= 0; --i) {
            if (this.sections[i] != null) {
                return this.sections[i].getYPosition();
            }
        }

        return 0;
    }

    public ChunkSection[] getSections() {
        return this.sections;
    }

    public void initLighting() {
    	final int i = this.h();

        this.r = Integer.MAX_VALUE;

        for (int j = 0; j < 16; ++j) {
            int k = 0;

            while (k < 16) {
                this.b[j + (k << 4)] = -999;
                int l = i + 16 - 1;

                while (true) {
                    if (l > 0) {
                        if (this.b(j, l - 1, k) == 0) {
                            --l;
                            continue;
                        }

                        this.heightMap[k << 4 | j] = l;
                        if (l < this.r) {
                            this.r = l;
                        }
                    }

                    if (!this.world.worldProvider.g) {
                        l = 15;
                        int i1 = i + 16 - 1;

                        do {
                            int j1 = this.b(j, i1, k);

                            if (j1 == 0 && l != 15) {
                                j1 = 1;
                            }

                            l -= j1;
                            if (l > 0) {
                                ChunkSection chunksection = this.sections[i1 >> 4];

                                if (chunksection != null) {
                                    chunksection.setSkyLight(j, i1 & 15, k, l);
                                    this.world.m((this.locX << 4) + j, i1, (this.locZ << 4) + k);
                                }
                            }

                            --i1;
                        } while (i1 > 0 && l > 0);
                    }

                    ++k;
                    break;
                }
            }
        }

        this.n = true;
    }

    private void e(int i, int j) {
        this.c[i + j * 16] = true;
        this.w = true;
    }

    private void c(boolean flag) {
        if (this.world.areChunksLoaded(this.locX * 16 + 8, 0, this.locZ * 16 + 8, 16)) {
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    if (this.c[i + j * 16]) {
                        this.c[i + j * 16] = false;
                        final int k = this.b(i, j);
                        final int l = this.locX * 16 + i;
                        final int i1 = this.locZ * 16 + j;
                        int j1 = this.world.g(l - 1, i1);
                        final int k1 = this.world.g(l + 1, i1);
                        final int l1 = this.world.g(l, i1 - 1);
                        final int i2 = this.world.g(l, i1 + 1);

                        if (k1 < j1) {
                            j1 = k1;
                        }

                        if (l1 < j1) {
                            j1 = l1;
                        }

                        if (i2 < j1) {
                            j1 = i2;
                        }

                        this.g(l, i1, j1);
                        this.g(l - 1, i1, k);
                        this.g(l + 1, i1, k);
                        this.g(l, i1 - 1, k);
                        this.g(l, i1 + 1, k);
                        if (flag) {
                            return;
                        }
                    }
                }
            }

            this.w = false;
        }
    }

    private void g(int i, int j, int k) {
    	final int l = this.world.getHighestBlockYAt(i, j);

        if (l > k) {
            this.c(i, j, k, l + 1);
        } else if (l < k) {
            this.c(i, j, l, k + 1);
        }
    }

    private void c(int i, int j, int k, int l) {
        if (l > k && this.world.areChunksLoaded(i, 0, j, 16)) {
            for (int i1 = k; i1 < l; ++i1) {
                //this.world.updateLight(EnumSkyBlock.SKY, i, i1, j); // PaperSpigot - Asynchronous lighting updates
            	this.world.updateBrightness(EnumSkyBlock.SKY, i, i1, j, this); // PaperSpigot - Asynchronous lighting updates
            }

            this.n = true;
        }
    }

    private void h(int i, int j, int k) {
    	final int l = this.heightMap[k << 4 | i] & 255;
        int i1 = l;

        if (j > l) {
            i1 = j;
        }

        while (i1 > 0 && this.b(i, i1 - 1, k) == 0) {
            --i1;
        }

        if (i1 != l) {
            this.world.b(i + this.locX * 16, k + this.locZ * 16, i1, l);
            this.heightMap[k << 4 | i] = i1;
            final int j1 = this.locX * 16 + i;
            final int k1 = this.locZ * 16 + k;
            int l1;
            int i2;

            if (!this.world.worldProvider.g) {
                ChunkSection chunksection;

                if (i1 < l) {
                    for (l1 = i1; l1 < l; ++l1) {
                        chunksection = this.sections[l1 >> 4];
                        if (chunksection != null) {
                            chunksection.setSkyLight(i, l1 & 15, k, 15);
                            this.world.m((this.locX << 4) + i, l1, (this.locZ << 4) + k);
                        }
                    }
                } else {
                    for (l1 = l; l1 < i1; ++l1) {
                        chunksection = this.sections[l1 >> 4];
                        if (chunksection != null) {
                            chunksection.setSkyLight(i, l1 & 15, k, 0);
                            this.world.m((this.locX << 4) + i, l1, (this.locZ << 4) + k);
                        }
                    }
                }

                l1 = 15;

                while (i1 > 0 && l1 > 0) {
                    --i1;
                    i2 = this.b(i, i1, k);
                    if (i2 == 0) {
                        i2 = 1;
                    }

                    l1 -= i2;
                    if (l1 < 0) {
                        l1 = 0;
                    }

                    final ChunkSection chunksection1 = this.sections[i1 >> 4];

                    if (chunksection1 != null) {
                        chunksection1.setSkyLight(i, i1 & 15, k, l1);
                    }
                }
            }

            l1 = this.heightMap[k << 4 | i];
            i2 = l;
            int j2 = l1;

            if (l1 < l) {
                i2 = l1;
                j2 = l;
            }

            if (l1 < this.r) {
                this.r = l1;
            }

            if (!this.world.worldProvider.g) {
                this.c(j1 - 1, k1, i2, j2);
                this.c(j1 + 1, k1, i2, j2);
                this.c(j1, k1 - 1, i2, j2);
                this.c(j1, k1 + 1, i2, j2);
                this.c(j1, k1, i2, j2);
            }

            this.n = true;
        }
    }

    public int b(int i, int j, int k) {
        return this.getType(i, j, k).k();
    }

    /*public Block getType(int i, int j, int k) {
        Block block = Blocks.AIR;

        if (j >> 4 < this.sections.length) {
        	final ChunkSection chunksection = this.sections[j >> 4];

            if (chunksection != null) {
                try {
                    block = chunksection.getTypeId(i, j & 15, k);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.a(throwable, "Getting block");
                    CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being got");

                    crashreportsystemdetails.a("Location", new CrashReportLocation(this, i, j, k));
                    throw new ReportedException(crashreport);
                }
            }
        }

        return block;
    }*/
	public Block getType(int i, int j, int k) {
		Block block = Blocks.AIR;
		if (j >> 4 < this.sections.length && j >> 4 >= 0) {
			ChunkSection chunksection = this.sections[j >> 4];
			if (chunksection != null)
				try {
					block = chunksection.getTypeId(i, j & 0xF, k);
				} catch (Throwable throwable) {
					CrashReport crashreport = CrashReport.a(throwable, "Getting block");
					CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being got");
					crashreportsystemdetails.a("Location", new CrashReportLocation(this, i, j, k));
					throw new ReportedException(crashreport);
				}  
		} 
		return block;
	}

    public int getData(int i, int j, int k) {
        if (j >> 4 >= this.sections.length) {
            return 0;
        }
        final ChunkSection chunksection = this.sections[j >> 4];
        return chunksection != null ? chunksection.getData(i, j & 15, k) : 0;
    }

    // Spigot start - prevent invalid data values
    public static int checkData( Block block, int data )
    {
        if (block == Blocks.DOUBLE_PLANT )
        {
            return data < 6 || data >= 8 ? data : 0;
        }
        return data;
    }
    // Spigot end

    public boolean a(int i, int j, int k, Block block, int l) {
        int i1 = k << 4 | i;

        if (j >= this.b[i1] - 1) {
            this.b[i1] = -999;
        }

        final int j1 = this.heightMap[i1];
        final Block block1 = this.getType(i, j, k);
        final int k1 = this.getData(i, j, k);

        if (block1 == block && k1 == l) {
            return false;
        } else {
            ChunkSection chunksection = this.sections[j >> 4];
            boolean flag = false;

            if (chunksection == null) {
                if (block == Blocks.AIR) {
                    return false;
                }

                chunksection = this.sections[j >> 4] = new ChunkSection(j >> 4 << 4, !this.world.worldProvider.g);
                flag = j >= j1;
            }

            final int l1 = this.locX * 16 + i;
            final int i2 = this.locZ * 16 + k;

            if (!this.world.isStatic) {
                block1.f(this.world, l1, j, i2, k1);
            }

            // CraftBukkit start - Delay removing containers until after they're cleaned up
            if (!(block1 instanceof IContainer)) {
                chunksection.setTypeId(i, j & 15, k, block);
            }
            // CraftBukkit end

            if (!this.world.isStatic) {
                block1.remove(this.world, l1, j, i2, block1, k1);
            } else if (block1 instanceof IContainer && block1 != block) {
                this.world.p(l1, j, i2);
            }

            // CraftBukkit start - Remove containers now after cleanup
            if (block1 instanceof IContainer) {
                chunksection.setTypeId(i, j & 15, k, block);
            }
            // CraftBukkit end

            if (chunksection.getTypeId(i, j & 15, k) != block) {
                return false;
            } else {
                chunksection.setData(i, j & 15, k, checkData( block, l ) );
                if (flag) {
                    this.initLighting();
                } else {
                	final int j2 = block.k();
                	final int k2 = block1.k();

                    if (j2 > 0) {
                        if (j >= j1) {
                            this.h(i, j + 1, k);
                        }
                    } else if (j == j1 - 1) {
                        this.h(i, j, k);
                    }

                    if (j2 != k2 && (j2 < k2 || this.getBrightness(EnumSkyBlock.SKY, i, j, k) > 0 || this.getBrightness(EnumSkyBlock.BLOCK, i, j, k) > 0)) {
                        this.e(i, k);
                    }
                }

                TileEntity tileentity;

                if (block1 instanceof IContainer) {
                    tileentity = this.e(i, j, k);
                    if (tileentity != null) {
                        tileentity.u();
                    }
                }

                // CraftBukkit - Don't place while processing the BlockPlaceEvent, unless it's a BlockContainer. Prevents blocks such as TNT from activating when cancelled.
                if (!this.world.isStatic && (!this.world.captureBlockStates || block instanceof BlockContainer)) {
                    block.onPlace(this.world, l1, j, i2);
                }

                if (block instanceof IContainer container) { // Rinny
                    tileentity = this.e(i, j, k);
                    if (tileentity == null) {
                        tileentity = container.a(this.world, l);
                        this.world.setTileEntity(l1, j, i2, tileentity);
                    }

                    if (tileentity != null) {
                        tileentity.u();
                    }
                }

                this.n = true;
                return true;
            }
        }
    }

    public boolean a(int i, int j, int k, int l) {
    	final ChunkSection chunksection = this.sections[j >> 4];

        if (chunksection == null) {
            return false;
        } else {
            int i1 = chunksection.getData(i, j & 15, k);

            if (i1 == l) {
                return false;
            } else {
                this.n = true;
                final Block block = chunksection.getTypeId( i, j & 15, k );
                chunksection.setData(i, j & 15, k, checkData( block, l ) );
                if (block instanceof IContainer) {
                	final TileEntity tileentity = this.e(i, j, k);

                    if (tileentity != null) {
                        tileentity.u();
                        tileentity.g = l;
                    }
                }

                return true;
            }
        }
    }

    public int getBrightness(EnumSkyBlock enumskyblock, int i, int j, int k) {
    	final ChunkSection chunksection = this.sections[j >> 4];

        return chunksection == null ? (this.d(i, j, k) ? enumskyblock.c : 0) : (enumskyblock == EnumSkyBlock.SKY ? (this.world.worldProvider.g ? 0 : chunksection.getSkyLight(i, j & 15, k)) : (enumskyblock == EnumSkyBlock.BLOCK ? chunksection.getEmittedLight(i, j & 15, k) : enumskyblock.c));
    }

    public void a(EnumSkyBlock enumskyblock, int i, int j, int k, int l) {
        ChunkSection chunksection = this.sections[j >> 4];

        if (chunksection == null) {
            chunksection = this.sections[j >> 4] = new ChunkSection(j >> 4 << 4, !this.world.worldProvider.g);
            this.initLighting();
        }

        this.n = true;
        switch (enumskyblock) { // Rinny
        	case SKY: {
        		if (!this.world.worldProvider.g) {
                    chunksection.setSkyLight(i, j & 15, k, l);
                }
        		break;
        	}
        	case BLOCK: {
        		chunksection.setEmittedLight(i, j & 15, k, l);
        		break;
        	}
        }
    }

    public int b(int i, int j, int k, int l) {
    	final ChunkSection chunksection = this.sections[j >> 4];

        if (chunksection == null) {
            return !this.world.worldProvider.g && l < EnumSkyBlock.SKY.c ? EnumSkyBlock.SKY.c - l : 0;
        }
        int i1 = this.world.worldProvider.g ? 0 : chunksection.getSkyLight(i, j & 15, k);

        if (i1 > 0) {
            a = true;
        }

        i1 -= l;
        final int j1 = chunksection.getEmittedLight(i, j & 15, k);

        if (j1 > i1) {
            i1 = j1;
        }

        return i1;
    }
    
    private static final Map<Class<?>, EnumCreatureType> CREATURE_TYPE_MAP = Arrays.stream(EnumCreatureType.values()).collect(Collectors.toMap(EnumCreatureType::a, Function.identity())); // Rinny

    public void a(Entity entity) {
        this.o = true;
        final int i = MathHelper.floor(entity.locX / 16.0D);
        final int j = MathHelper.floor(entity.locZ / 16.0D);

        if (i != this.locX || j != this.locZ) {
            // CraftBukkit start
            Bukkit.getLogger().warning("Wrong location for " + entity + " in world '" + world.getWorld().getName() + "'!");
            // t.warn("Wrong location! " + entity + " (at " + i + ", " + j + " instead of " + this.locX + ", " + this.locZ + ")");
            // Thread.dumpStack();
            Bukkit.getLogger().warning("Entity is at " + entity.locX + "," + entity.locZ + " (chunk " + i + "," + j + ") but was stored in chunk " + this.locX + "," + this.locZ);
            // CraftBukkit end
        }

        int k = MathHelper.floor(entity.locY / 16.0D);

        if (k < 0) {
            k = 0;
        }

        if (k >= this.entitySlices.length) {
            k = this.entitySlices.length - 1;
        }

        entity.ag = true;
        entity.ah = this.locX;
        entity.ai = k;
        entity.aj = this.locZ;
        this.entitySlices[k].add(entity);
        // Spigot start - increment creature type count
        // Keep this synced up with World.a(Class)
        if (entity instanceof EntityPlayer player) { // Rinny
            this.playersInChunk.add(player);
        }
        if (entity instanceof EntityInsentient entityinsentient) { // Rinny
            if (entityinsentient.isTypeNotPersistent() && entityinsentient.isPersistent()) {
                return;
            }
        }
        for (Map.Entry<Class<?>, EnumCreatureType> entry : CREATURE_TYPE_MAP.entrySet()) {
            if (entry.getKey().isAssignableFrom(entity.getClass())) {
                this.entityCount.adjustOrPutValue(entry.getKey(), 1, 1);
            }
        }
        // Spigot end
    }

    public void b(Entity entity) {
        this.a(entity, entity.ai);
    }

    public void a(Entity entity, int i) {
        if (i < 0) {
            i = 0;
        }

        if (i >= this.entitySlices.length) {
            i = this.entitySlices.length - 1;
        }

        this.entitySlices[i].remove(entity);
        // Spigot start - decrement creature type count
        // Keep this synced up with World.a(Class)
        if (entity instanceof EntityPlayer player && !(entity instanceof EntityNPC)) { // Rinny
            this.playersInChunk.remove(player);
        }
        if (entity instanceof EntityInsentient entityinsentient) { // Rinny
            if (entityinsentient.isTypeNotPersistent() && entityinsentient.isPersistent()) {
                return;
            }
        }
        for (Map.Entry<Class<?>, EnumCreatureType> entry : CREATURE_TYPE_MAP.entrySet()) {
            if (entry.getKey().isAssignableFrom(entity.getClass())) {
                this.entityCount.adjustOrPutValue(entry.getKey(), 1, 1);
            }
        }
        // Spigot end
    }

    public boolean d(int i, int j, int k) {
        return j >= this.heightMap[k << 4 | i];
    }

    public TileEntity e(int i, int j, int k) {
    	final ChunkPosition chunkposition = new ChunkPosition(i, j, k);
        TileEntity tileentity = (TileEntity) this.tileEntities.get(chunkposition);

        if (tileentity == null) {
        	final Block block = this.getType(i, j, k);

            if (!block.isTileEntity()) {
                return null;
            }

            tileentity = ((IContainer) block).a(this.world, this.getData(i, j, k));
            this.world.setTileEntity(this.locX * 16 + i, j, this.locZ * 16 + k, tileentity);
        }

        if (tileentity != null && tileentity.r()) {
            this.tileEntities.remove(chunkposition);
            return null;
        }
        return tileentity;
    }

    public void a(TileEntity tileentity) {
    	final int i = tileentity.x - this.locX * 16;
    	final int j = tileentity.y;
    	final int k = tileentity.z - this.locZ * 16;

        this.a(i, j, k, tileentity);
        if (this.d) {
            this.world.tileEntityList.add(tileentity);
        }
    }

    public void a(int i, int j, int k, TileEntity tileentity) {
    	final ChunkPosition chunkposition = new ChunkPosition(i, j, k);

        tileentity.a(this.world);
        tileentity.x = this.locX * 16 + i;
        tileentity.y = j;
        tileentity.z = this.locZ * 16 + k;
        if (this.getType(i, j, k) instanceof IContainer) {
            /*if (this.tileEntities.containsKey(chunkposition)) {
                this.tileEntities.get(chunkposition).s();
            }*/
        	// Rinny
        	TileEntity existing = this.tileEntities.get(chunkposition);
            if (existing != null) {
                existing.s();
            }
            // Rinny

            tileentity.t();
            this.tileEntities.put(chunkposition, tileentity);
            // Spigot start - The tile entity has a world, now hoppers can be born ticking.
            if (this.world.spigotConfig.altHopperTicking) {
                this.world.triggerHoppersList.add(tileentity);
            }
            // Spigot end
            // PaperSpigot start - Remove invalid mob spawner Tile Entities
        } else if (this.world.paperSpigotConfig.removeInvalidMobSpawnerTEs && tileentity instanceof TileEntityMobSpawner && org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(getType(i, j, k)) != org.bukkit.Material.MOB_SPAWNER) {
            this.tileEntities.remove(chunkposition);
            // PaperSpigot end
            // CraftBukkit start
        } else {
            System.out.println("Attempted to place a tile entity (" + tileentity + ") at " + tileentity.x + "," + tileentity.y + "," + tileentity.z
                    + " (" + org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(getType(i, j, k)) + ") where there was no entity tile!");
            System.out.println("Chunk coordinates: " + (this.locX * 16) + "," + (this.locZ * 16));
            new Exception().printStackTrace();
            // CraftBukkit end
        }
    }

    public void f(int i, int j, int k) {
    	final ChunkPosition chunkposition = new ChunkPosition(i, j, k);

        if (this.d) {
        	final TileEntity tileentity = (TileEntity) this.tileEntities.remove(chunkposition);

            if (tileentity != null) {
                tileentity.s();
            }
        }
    }

    public void addEntities() {
        this.d = true;
        this.world.a(this.tileEntities.values());

        for (int i = 0; i < this.entitySlices.length; ++i) {
        	final Iterator<Entity> iterator = this.entitySlices[i].iterator();

            while (iterator.hasNext()) {
            	final Entity entity = iterator.next();

                if (entity instanceof EntityPlayer ep && !(entity instanceof EntityNPC)) {
                    this.playersInChunk.add(ep);
                }
                entity.X();
            }

            this.world.a(this.entitySlices[i]);
        }
    }

    public void removeEntities() {
        this.d = false;
        final Iterator<TileEntity> iterator = this.tileEntities.values().iterator();

        while (iterator.hasNext()) {
        	final TileEntity tileentity = iterator.next();
            // Spigot Start
            if ( tileentity instanceof IInventory inv)
            {
                /*for ( org.bukkit.entity.HumanEntity h : new ArrayList<org.bukkit.entity.HumanEntity>( (List) ( (IInventory) tileentity ).getViewers() ) )
                {
                    if ( h instanceof org.bukkit.craftbukkit.entity.CraftHumanEntity )
                    {
                       ( (org.bukkit.craftbukkit.entity.CraftHumanEntity) h).getHandle().closeInventory();
                    }
                }*/
            	List<org.bukkit.entity.HumanEntity> viewers = inv.getViewers();
                for (int f = 0; f < viewers.size(); f++) {
                	org.bukkit.entity.HumanEntity h = (HumanEntity) viewers.get(f);
                    if (h instanceof org.bukkit.craftbukkit.entity.CraftHumanEntity che) {
                        che.getHandle().closeInventory();
                    }
                }
            }
            // Spigot End

            this.world.a(tileentity);
        }

        for (int i = 0; i < this.entitySlices.length; ++i) {
            // CraftBukkit start
        	final java.util.Iterator<Entity> iter = this.entitySlices[i].iterator();
            while (iter.hasNext()) {
            	final Entity entity = iter.next();
                // Spigot Start
                if ( entity instanceof IInventory inv)
                {
                    /*for ( org.bukkit.entity.HumanEntity h : new ArrayList<org.bukkit.entity.HumanEntity>( (List) ( (IInventory) entity ).getViewers() ) )
                    {
                        if ( h instanceof org.bukkit.craftbukkit.entity.CraftHumanEntity )
                        {
                           ( (org.bukkit.craftbukkit.entity.CraftHumanEntity) h).getHandle().closeInventory();
                        }
                    }*/
                	List<org.bukkit.entity.HumanEntity> viewers = inv.getViewers();
                    for (int f = 0; f < viewers.size(); f++) {
                    	org.bukkit.entity.HumanEntity h = (HumanEntity) viewers.get(f);
                        if (h instanceof org.bukkit.craftbukkit.entity.CraftHumanEntity che) {
                            che.getHandle().closeInventory();
                        }
                    }
                }
                // Spigot End

                // Do not pass along players, as doing so can get them stuck outside of time.
                // (which for example disables inventory icon updates and prevents block breaking)
                if (entity instanceof EntityPlayer && !(entity instanceof EntityNPC)) {
                    iter.remove();
                    this.playersInChunk.remove(entity);
                }
            }
            // CraftBukkit end

            this.world.b(this.entitySlices[i]);
        }
    }

    public void e() {
        this.n = true;
    }

    public void a(Entity entity, AxisAlignedBB axisalignedbb, List<Entity> list, IEntitySelector ientityselector) {
        int i = MathHelper.floor((axisalignedbb.b - 2.0D) / 16.0D);
        int j = MathHelper.floor((axisalignedbb.e + 2.0D) / 16.0D);

        i = MathHelper.a(i, 0, this.entitySlices.length - 1);
        j = MathHelper.a(j, 0, this.entitySlices.length - 1);

        for (int k = i; k <= j; ++k) {
        	final List<Entity> list1 = this.entitySlices[k];

            for (int l = 0; l < list1.size(); ++l) {
                Entity entity1 = list1.get(l);

                if (entity1 != entity && entity1.boundingBox.b(axisalignedbb) && (ientityselector == null || ientityselector.a(entity1))) {
                    list.add(entity1);
                    final  Entity[] aentity = entity1.at();

                    if (aentity != null) {
                        for (int i1 = 0; i1 < aentity.length; ++i1) {
                            entity1 = aentity[i1];
                            if (entity1 != entity && entity1.boundingBox.b(axisalignedbb) && (ientityselector == null || ientityselector.a(entity1))) {
                                list.add(entity1);
                            }
                        }
                    }
                }
            }
        }
    }

    public void a(Class<? extends Entity> oclass, AxisAlignedBB axisalignedbb, List<Entity> list, IEntitySelector ientityselector) {
        int i = MathHelper.floor((axisalignedbb.b - 2.0D) / 16.0D);
        int j = MathHelper.floor((axisalignedbb.e + 2.0D) / 16.0D);

        i = MathHelper.a(i, 0, this.entitySlices.length - 1);
        j = MathHelper.a(j, 0, this.entitySlices.length - 1);

        for (int k = i; k <= j; ++k) {
        	final List<Entity> list1 = this.entitySlices[k];

            for (int l = 0; l < list1.size(); ++l) {
            	final Entity entity = (Entity) list1.get(l);

                if (oclass.isAssignableFrom(entity.getClass()) && entity.boundingBox.b(axisalignedbb) && (ientityselector == null || ientityselector.a(entity))) {
                    list.add(entity);
                }
            }
        }
    }

    public boolean a(boolean flag) {
        if (flag) {
            if (this.o && this.world.getTime() != this.lastSaved || this.n) {
                return true;
            }
        } else if (this.o && this.world.getTime() >= this.lastSaved + MinecraftServer.getServer().autosavePeriod * 4) { // PaperSpigot - Only save if we've passed 2 auto save intervals without modification
            return true;
        }

        return this.n;
    }

    public Random a(long i) {
        return new Random(this.world.getSeed() + (long) (this.locX * this.locX * 4987142) + (long) (this.locX * 5947611) + (long) (this.locZ * this.locZ) * 4392871L + (long) (this.locZ * 389711) ^ i);
    }

    public boolean isEmpty() {
        return false;
    }

    /*public void loadNearby(IChunkProvider ichunkprovider, IChunkProvider ichunkprovider1, int i, int j) {
        if (!this.done && ichunkprovider.isChunkLoaded(i + 1, j + 1) && ichunkprovider.isChunkLoaded(i, j + 1) && ichunkprovider.isChunkLoaded(i + 1, j)) {
            ichunkprovider.getChunkAt(ichunkprovider1, i, j);
        }

        if (ichunkprovider.isChunkLoaded(i - 1, j) && !ichunkprovider.getOrCreateChunk(i - 1, j).done && ichunkprovider.isChunkLoaded(i - 1, j + 1) && ichunkprovider.isChunkLoaded(i, j + 1) && ichunkprovider.isChunkLoaded(i - 1, j + 1)) {
            ichunkprovider.getChunkAt(ichunkprovider1, i - 1, j);
        }

        if (ichunkprovider.isChunkLoaded(i, j - 1) && !ichunkprovider.getOrCreateChunk(i, j - 1).done && ichunkprovider.isChunkLoaded(i + 1, j - 1) && ichunkprovider.isChunkLoaded(i + 1, j - 1) && ichunkprovider.isChunkLoaded(i + 1, j)) {
            ichunkprovider.getChunkAt(ichunkprovider1, i, j - 1);
        }

        if (ichunkprovider.isChunkLoaded(i - 1, j - 1) && !ichunkprovider.getOrCreateChunk(i - 1, j - 1).done && ichunkprovider.isChunkLoaded(i, j - 1) && ichunkprovider.isChunkLoaded(i - 1, j)) {
            ichunkprovider.getChunkAt(ichunkprovider1, i - 1, j - 1);
        }
    }*/
    
    // TODO: test
    public void loadNearby(IChunkProvider ichunkprovider, IChunkProvider ichunkprovider1, int i, int j) {
        Chunk[][] chunks = new Chunk[3][3];
        boolean[][] loaded = new boolean[3][3];

        for (byte dx = -1; dx <= 1; dx++) {
            for (byte dz = -1; dz <= 1; dz++) {
                int x = i + dx;
                int z = j + dz;
                loaded[dx + 1][dz + 1] = ichunkprovider.isChunkLoaded(x, z);
                if (loaded[dx + 1][dz + 1]) {
                    chunks[dx + 1][dz + 1] = ichunkprovider.getOrCreateChunk(x, z);
                }
            }
        }

        final byte[][] neighbors = {
            {0, 0, 1, 1}, 
            {-1, 0, -1, 1},
            {0, -1, 1, -1},
            {-1, -1, 0, -1}
        };

        for (byte[] neighbor : neighbors) {
            int x = neighbor[0] + 1;
            int z = neighbor[1] + 1;

            if (loaded[x][z] && !chunks[x][z].done) {
                boolean allLoaded = true;
                for (byte k = 2; k < neighbor.length; k += 2) {
                    int nx = neighbor[k] + 1;
                    int nz = neighbor[k + 1] + 1;
                    if (!loaded[nx][nz]) {
                        allLoaded = false;
                        break;
                    }
                }
                if (allLoaded) {
                    ichunkprovider.getChunkAt(ichunkprovider1, i + neighbor[0], j + neighbor[1]);
                }
            }
        }
    }

    public int d(int i, int j) {
    	final int k = i | j << 4;
        int l = this.b[k];

        if (l == -999) {
            int i1 = this.h() + 15;

            l = -1;

            while (i1 > 0 && l == -1) {
            	final Block block = this.getType(i, i1, j);
            	final Material material = block.getMaterial();

                if (!material.isSolid() && !material.isLiquid()) {
                    --i1;
                } else {
                    l = i1 + 1;
                }
            }

            this.b[k] = l;
        }

        return l;
    }

    public void b(boolean flag) {
        if (this.w && !this.world.worldProvider.g && !flag) {
            this.recheckGaps(this.world.isStatic); // PaperSpigot - Asynchronous lighting updates
        }

        this.m = true;
        if (!this.lit && this.done && this.world.spigotConfig.randomLightUpdates) { // Spigot - also use random light updates setting to determine if we should relight
            this.p();
        }
    }

    /**
     * PaperSpigot - Recheck gaps asynchronously.
     */
    public void recheckGaps(final boolean isStatic) {
        if (!world.paperSpigotConfig.useAsyncLighting) {
            this.c(isStatic);
            return;
        }

        world.lightingExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Chunk.this.c(isStatic);
            }
        });
    }

    public boolean isReady() {
        // Spigot Start
        /*
         * As of 1.7, Mojang added a check to make sure that only chunks which have been lit are sent to the client.
         * Unfortunately this interferes with our modified chunk ticking algorithm, which will only tick chunks distant from the player on a very infrequent basis.
         * We cannot unfortunately do this lighting stage during chunk gen as it appears to put a lot more noticeable load on the server, than when it is done at play time.
         * For now at least we will simply send all chunks, in accordance with pre 1.7 behaviour.
         */
        return true;
        // Spigot End
    }

    public ChunkCoordIntPair l() {
        return this.chunkCoords;
    }

    public boolean c(int i, int j) {
        if (i < 0) {
            i = 0;
        }

        if (j >= 256) {
            j = 255;
        }

        for (int k = i; k <= j; k += 16) {
        	final ChunkSection chunksection = this.sections[k >> 4];

            if (chunksection != null && !chunksection.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void a(ChunkSection[] achunksection) {
        this.sections = achunksection;
    }

    public BiomeBase getBiome(int i, int j, WorldChunkManager worldchunkmanager) {
        int k = this.v[j << 4 | i] & 255;

        if (k == 255) {
        	final BiomeBase biomebase = worldchunkmanager.getBiome((this.locX << 4) + i, (this.locZ << 4) + j);

            k = biomebase.id;
            this.v[j << 4 | i] = (byte) (k & 255);
        }

        return BiomeBase.getBiome(k) == null ? BiomeBase.PLAINS : BiomeBase.getBiome(k);
    }

    public byte[] m() {
        return this.v;
    }

    public void a(byte[] abyte) {
        this.v = abyte;
    }

    public void n() {
        this.x = 0;
    }

    public void o() {
        for (int i = 0; i < 8; ++i) {
            if (this.x >= 4096) {
                return;
            }

            final int j = this.x % 16;
            final int k = this.x / 16 % 16;
            final int l = this.x / 256;

            ++this.x;
            final int i1 = (this.locX << 4) + k;
            final int j1 = (this.locZ << 4) + l;

            for (int k1 = 0; k1 < 16; ++k1) {
            	final int l1 = (j << 4) + k1;

                if (this.sections[j] == null && (k1 == 0 || k1 == 15 || k == 0 || k == 15 || l == 0 || l == 15) || this.sections[j] != null && this.sections[j].getTypeId(k, k1, l).getMaterial() == Material.AIR) {
                    if (this.world.getType(i1, l1 - 1, j1).m() > 0) {
                        this.world.t(i1, l1 - 1, j1);
                    }

                    if (this.world.getType(i1, l1 + 1, j1).m() > 0) {
                        this.world.t(i1, l1 + 1, j1);
                    }

                    if (this.world.getType(i1 - 1, l1, j1).m() > 0) {
                        this.world.t(i1 - 1, l1, j1);
                    }

                    if (this.world.getType(i1 + 1, l1, j1).m() > 0) {
                        this.world.t(i1 + 1, l1, j1);
                    }

                    if (this.world.getType(i1, l1, j1 - 1).m() > 0) {
                        this.world.t(i1, l1, j1 - 1);
                    }

                    if (this.world.getType(i1, l1, j1 + 1).m() > 0) {
                        this.world.t(i1, l1, j1 + 1);
                    }

                    this.world.t(i1, l1, j1);
                }
            }
        }
    }

    public void p() {
        this.done = true;
        this.lit = true;
        if (!this.world.worldProvider.g) {
            if (this.world.b(this.locX * 16 - 1, 0, this.locZ * 16 - 1, this.locX * 16 + 1, 63, this.locZ * 16 + 1)) {
                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 16; ++j) {
                        if (!this.f(i, j)) {
                            this.lit = false;
                            break;
                        }
                    }
                }

                if (this.lit) {
                    Chunk chunk = this.world.getChunkAtWorldCoords(this.locX * 16 - 1, this.locZ * 16);

                    chunk.a(3);
                    chunk = this.world.getChunkAtWorldCoords(this.locX * 16 + 16, this.locZ * 16);
                    chunk.a(1);
                    chunk = this.world.getChunkAtWorldCoords(this.locX * 16, this.locZ * 16 - 1);
                    chunk.a(0);
                    chunk = this.world.getChunkAtWorldCoords(this.locX * 16, this.locZ * 16 + 16);
                    chunk.a(2);
                }
            } else {
                this.lit = false;
            }
        }
    }

    private void a(int i) {
        if (this.done) {
            int j;
            switch (i) {
				case 0: {
					for (j = 0; j < 16; ++j) {
	                    this.f(j, 15);
	                }
					break;
				}
				case 1: {
					for (j = 0; j < 16; ++j) {
	                    this.f(0, j);
	                }
					break;
				}
				case 2: {
					for (j = 0; j < 16; ++j) {
	                    this.f(j, 0);
	                }
					break;
				}
				case 3: {
					for (j = 0; j < 16; ++j) {
	                    this.f(15, j);
	                }
					break;
				}
            }
            /*if (i == 3) {
                for (j = 0; j < 16; ++j) {
                    this.f(15, j);
                }
            } else if (i == 1) {
                for (j = 0; j < 16; ++j) {
                    this.f(0, j);
                }
            } else if (i == 0) {
                for (j = 0; j < 16; ++j) {
                    this.f(j, 15);
                }
            } else if (i == 2) {
                for (j = 0; j < 16; ++j) {
                    this.f(j, 0);
                }
            }*/
        }
    }

    private boolean f(int i, int j) {
    	final int k = this.h();
        boolean flag = false;
        boolean flag1 = false;

        int l;

        for (l = k + 16 - 1; l > 63 || l > 0 && !flag1; --l) {
        	final int i1 = this.b(i, l, j);

            if (i1 == 255 && l < 63) {
                flag1 = true;
            }

            if (!flag && i1 > 0) {
                flag = true;
            } else if (flag && i1 == 0 && !this.world.t(this.locX * 16 + i, l, this.locZ * 16 + j)) {
                return false;
            }
        }

        for (; l > 0; --l) {
            if (this.getType(i, l, j).m() > 0) {
                this.world.t(this.locX * 16 + i, l, this.locZ * 16 + j);
            }
        }

        return true;
    }
}
