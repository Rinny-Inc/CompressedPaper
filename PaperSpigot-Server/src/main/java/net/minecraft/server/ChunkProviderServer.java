package net.minecraft.server;

import java.util.Iterator;
import java.util.List;
// CraftBukkit start
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Server;
import org.bukkit.craftbukkit.chunkio.ChunkIOExecutor;
import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.craftbukkit.util.LongHashSet;
import org.bukkit.craftbukkit.util.LongObjectHashMap;
import org.bukkit.event.world.ChunkUnloadEvent;
// CraftBukkit end

public class ChunkProviderServer implements IChunkProvider {

    private static final Logger b = LogManager.getLogger();
    // CraftBukkit start - private -> public
    public LongHashSet unloadQueue = new LongHashSet(); // LongHashSet
    public Chunk emptyChunk;
    public IChunkProvider chunkProvider;
    public IChunkLoader chunkLoader;
    public boolean forceChunkLoad = false; // true -> false
    public LongObjectHashMap<Chunk> chunks = new LongObjectHashMap<Chunk>();
    public WorldServer world;
    // CraftBukkit end
    public ChunkProviderServer(WorldServer worldserver, IChunkLoader ichunkloader, IChunkProvider ichunkprovider) {
        this.emptyChunk = new EmptyChunk(worldserver, Integer.MIN_VALUE, Integer.MIN_VALUE);
        this.world = worldserver;
        this.chunkLoader = ichunkloader;
        this.chunkProvider = ichunkprovider;
    }

    public boolean isChunkLoaded(int i, int j) {
        return this.chunks.containsKey(LongHash.toLong(i, j)); // CraftBukkit
    }

    // CraftBukkit start - Change return type to Collection and return the values of our chunk map
    public java.util.Collection<Chunk> a() {
        // return this.chunkList;
        return this.chunks.values();
        // CraftBukkit end
    }

    public void queueUnload(int i, int j) {
    	final long key = LongHash.toLong(i, j);
        // PaperSpigot start - Asynchronous lighting updates
    	final Chunk chunk = this.chunks.get(key);
        if (chunk != null && chunk.world.paperSpigotConfig.useAsyncLighting && (chunk.pendingLightUpdates.get() > 0 || chunk.world.getTime() - chunk.lightUpdateTime < 20)) {
            return;
        }
        // PaperSpigot end
        // PaperSpigot start - Don't unload chunk if it contains an entity that loads chunks
        if (chunk != null) {
            for (List<Entity> entities : chunk.entitySlices) {
                for (Entity entity : entities) {
                    if (entity.loadChunks) {
                        return;
                    }
                }
            }
        }
        // PaperSpigot end
        if (this.world.worldProvider.e()) {
        	final ChunkCoordinates chunkcoordinates = this.world.getSpawn();
        	final int k = i * 16 + 8 - chunkcoordinates.x;
        	final int l = j * 16 + 8 - chunkcoordinates.z;
        	final short short1 = 128;

            // CraftBukkit start
            if (k < -short1 || k > short1 || l < -short1 || l > short1 || !(this.world.keepSpawnInMemory)) { // Added 'this.world.keepSpawnInMemory'
                this.unloadQueue.add(i, j);

                final Chunk c = this.chunks.get(key);
                if (c != null) {
                    c.mustSave = true;
                }
            }
            // CraftBukkit end
        } else {
            // CraftBukkit start
            this.unloadQueue.add(i, j);

            final Chunk c = this.chunks.get(key);
            if (c != null) {
                c.mustSave = true;
            }
            // CraftBukkit end
        }
    }

    public void b() {
    	this.chunks.values().forEach(map -> this.queueUnload(map.locX, map.locX));
    	/*
        Iterator iterator = this.chunks.values().iterator(); // CraftBukkit

        while (iterator.hasNext()) {
            Chunk chunk = (Chunk) iterator.next();

            this.queueUnload(chunk.locX, chunk.locZ);
        }*/
    }

    // CraftBukkit start - Add async variant, provide compatibility
    public Chunk getChunkIfLoaded(int x, int z) {
        return this.chunks.get(LongHash.toLong(x, z));
    }

    public Chunk getChunkAt(int i, int j) {
        return getChunkAt(i, j, null);
    }

    public Chunk getChunkAt(int i, int j, Runnable runnable) {
    	final long key = LongHash.toLong(i, j);
        this.unloadQueue.remove(key); // i, j
        Chunk chunk = this.chunks.get(key);
        ChunkRegionLoader loader = null;

        if (this.chunkLoader instanceof ChunkRegionLoader crl) { // Rinny
            loader = crl;
        }

        // We can only use the queue for already generated chunks
        if (chunk == null && loader != null && loader.chunkExists(this.world, i, j)) {
            if (runnable != null) {
                ChunkIOExecutor.queueChunkLoad(this.world, loader, this, i, j, runnable);
                return null;
            }
            chunk = ChunkIOExecutor.syncChunkLoad(this.world, loader, this, i, j);
        } else if (chunk == null) {
            chunk = this.originalGetChunkAt(i, j);
        }

        // If we didn't load the chunk async and have a callback run it now
        if (runnable != null) {
            runnable.run();
        }

        return chunk;
    }

    public Chunk originalGetChunkAt(int i, int j) {
    	final long key = LongHash.toLong(i, j);
        this.unloadQueue.remove(key); // i, j
        Chunk chunk = (Chunk) this.chunks.get(key);
        boolean newChunk = false;

        if (chunk == null) {
            chunk = this.loadChunk(i, j);
            if (chunk == null) {
                if (this.chunkProvider == null) {
                    chunk = this.emptyChunk;
                } else {
                    try {
                        chunk = this.chunkProvider.getOrCreateChunk(i, j);
                    } catch (Throwable throwable) {
                    	final CrashReport crashreport = CrashReport.a(throwable, "Exception generating new chunk");
                    	final CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Chunk to be generated");

                        crashreportsystemdetails.a("Location", String.format("%d,%d", new Object[] { Integer.valueOf(i), Integer.valueOf(j)}));
                        crashreportsystemdetails.a("Position hash", key); // CraftBukkit - Use LongHash
                        crashreportsystemdetails.a("Generator", this.chunkProvider.getName());
                        throw new ReportedException(crashreport);
                    }
                }
                newChunk = true; // CraftBukkit
            }

            this.chunks.put(key, chunk); // CraftBukkit
            chunk.addEntities();

            // CraftBukkit start
            final Server server = this.world.getServer();
            if (server != null) {
                /*
                 * If it's a new world, the first few chunks are generated inside
                 * the World constructor. We can't reliably alter that, so we have
                 * no way of creating a CraftWorld/CraftServer at that point.
                 */
                server.getPluginManager().callEvent(new org.bukkit.event.world.ChunkLoadEvent(chunk.bukkitChunk, newChunk));
            }

            // Update neighbor counts
            for (int x = -2; x < 3; x++) {
                for (int z = -2; z < 3; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }

                    final Chunk neighbor = this.getChunkIfLoaded(chunk.locX + x, chunk.locZ + z);
                    if (neighbor != null) {
                        neighbor.setNeighborLoaded(-x, -z);
                        chunk.setNeighborLoaded(x, z);
                    }
                }
            }
            // CraftBukkit end
            chunk.loadNearby(this, this, i, j);
        }

        return chunk;
    }

    public Chunk getOrCreateChunk(int i, int j) {
        // CraftBukkit start
        Chunk chunk = (Chunk) this.chunks.get(LongHash.toLong(i, j));

        chunk = chunk == null ? (!this.world.isLoading && !this.forceChunkLoad ? this.emptyChunk : this.getChunkAt(i, j)) : chunk;
        if (chunk == this.emptyChunk) return chunk;
        if (i != chunk.locX || j != chunk.locZ) {
            b.error("Chunk (" + chunk.locX + ", " + chunk.locZ + ") stored at  (" + i + ", " + j + ") in world '" + world.getWorld().getName() + "'");
            b.error(chunk.getClass().getName());
            final Throwable ex = new Throwable();
            ex.fillInStackTrace();
            ex.printStackTrace();
        }
        return chunk;
        // CraftBukkit end
    }

    public Chunk loadChunk(int i, int j) { // CraftBukkit - private -> public
        if (this.chunkLoader == null) {
            return null;
        } else {
            try {
            	final Chunk chunk = this.chunkLoader.a(this.world, i, j);

                if (chunk != null) {
                    chunk.lastSaved = this.world.getTime();
                    if (this.chunkProvider != null) {
                        this.chunkProvider.recreateStructures(i, j);
                    }
                }

                return chunk;
            } catch (Exception exception) {
                b.error("Couldn\'t load chunk", exception);
                return null;
            }
        }
    }

    public void saveChunkNOP(Chunk chunk) { // CraftBukkit - private -> public
        if (canSave() && this.chunkLoader != null) {
            try {
                this.chunkLoader.b(this.world, chunk);
            } catch (Exception exception) {
                b.error("Couldn\'t save entities", exception);
            }
        }
    }

    public void saveChunk(Chunk chunk) { // CraftBukkit - private -> public
        if (canSave() && this.chunkLoader != null) {
            try {
                chunk.lastSaved = this.world.getTime();
                this.chunkLoader.a(this.world, chunk);
                // CraftBukkit start - IOException to Exception
            } catch (Exception ioexception) {
                b.error("Couldn\'t save chunk", ioexception);
                /* Remove extra exception
            } catch (ExceptionWorldConflict exceptionworldconflict) {
                b.error("Couldn\'t save chunk; already in use by another instance of Minecraft?", exceptionworldconflict);
                // CraftBukkit end */
            }
        }
    }

    public void getChunkAt(IChunkProvider ichunkprovider, int i, int j) {
    	final Chunk chunk = this.getOrCreateChunk(i, j);

        if (!chunk.done) {
            chunk.p();
            if (this.chunkProvider != null) {
                this.chunkProvider.getChunkAt(ichunkprovider, i, j);

                // CraftBukkit start
                BlockSand.instaFall = true;
                final Random random = new Random();
                random.setSeed(world.getSeed());
                final long xRand = random.nextLong() / 2L * 2L + 1L;
                final long zRand = random.nextLong() / 2L * 2L + 1L;
                random.setSeed((long) i * xRand + (long) j * zRand ^ world.getSeed());

                org.bukkit.World world = this.world.getWorld();
                if (world != null) {
                    this.world.populating = true;
                    try {
                        for (org.bukkit.generator.BlockPopulator populator : world.getPopulators()) {
                            populator.populate(world, random, chunk.bukkitChunk);
                        }
                    } finally {
                        this.world.populating = false;
                    }
                }
                BlockSand.instaFall = false;
                this.world.getServer().getPluginManager().callEvent(new org.bukkit.event.world.ChunkPopulateEvent(chunk.bukkitChunk));
                // CraftBukkit end

                chunk.e();
            }
        }
    }

    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate) {
        int i = 0;
        // CraftBukkit start
        final Iterator iterator = this.chunks.values().iterator();

        while (iterator.hasNext()) {
        	final Chunk chunk = (Chunk) iterator.next();
            // CraftBukkit end

            if (flag) {
                this.saveChunkNOP(chunk);
            }

            if (chunk.a(flag)) {
                this.saveChunk(chunk);
                chunk.n = false;
                ++i;
                if (i == 24 && !flag) {
                    return false;
                }
            }
        }

        return true;
    }

    public void c() {
        if (this.chunkLoader != null) {
            this.chunkLoader.b();
        }
    }

    public boolean unloadChunks() {
    	return unloadChunks(false);
    }
    public boolean unloadChunks(boolean force) {
        if (canSave() || force) {
            // CraftBukkit start
        	final Server server = this.world.getServer();
            for (int i = 0; i < 100 && !this.unloadQueue.isEmpty(); i++) {
            	final long chunkcoordinates = this.unloadQueue.popFirst();
            	final Chunk chunk = this.chunks.get(chunkcoordinates);
                if (chunk == null) continue;

                final ChunkUnloadEvent event = new ChunkUnloadEvent(chunk.bukkitChunk);
                server.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    if (chunk != null) {
                        chunk.removeEntities();
                        this.saveChunk(chunk);
                        this.saveChunkNOP(chunk);
                        this.chunks.remove(chunkcoordinates); // CraftBukkit
                    }

                    // this.unloadQueue.remove(olong);
                    // this.chunks.remove(olong.longValue());

                    // Update neighbor counts
                    for (int x = -2; x < 3; x++) {
                        for (int z = -2; z < 3; z++) {
                            if (x == 0 && z == 0) {
                                continue;
                            }

                            final Chunk neighbor = this.getChunkIfLoaded(chunk.locX + x, chunk.locZ + z);
                            if (neighbor != null) {
                                neighbor.setNeighborUnloaded(-x, -z);
                                chunk.setNeighborUnloaded(x, z);
                            }
                        }
                    }
                }
            }
            // CraftBukkit end

            if (this.chunkLoader != null) {
                this.chunkLoader.a();
            }
        }

        return this.chunkProvider.unloadChunks();
    }

    public boolean canSave() {
        return !this.world.savingDisabled;
    }

    public String getName() {
        // CraftBukkit - this.chunks.count() -> .values().size()
        return "ServerChunkCache: " + this.chunks.values().size() + " Drop: " + this.unloadQueue.size();
    }

    public List getMobsFor(EnumCreatureType enumcreaturetype, int i, int j, int k) {
        return this.chunkProvider.getMobsFor(enumcreaturetype, i, j, k);
    }

    public ChunkPosition findNearestMapFeature(World world, String s, int i, int j, int k) {
        return this.chunkProvider.findNearestMapFeature(world, s, i, j, k);
    }

    public int getLoadedChunks() {
        // CraftBukkit - this.chunks.count() -> this.chunks.size()
        return this.chunks.size();
    }

    public void recreateStructures(int i, int j) {}
}
