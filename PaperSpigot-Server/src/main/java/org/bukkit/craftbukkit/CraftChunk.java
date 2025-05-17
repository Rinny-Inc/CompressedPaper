package org.bukkit.craftbukkit;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Entity;

import io.noks.FakeMultiBlockChange;
import io.noks.cb.CraftFakeMultiBlockChange;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.ChunkPosition;
import net.minecraft.server.ChunkSection;
import net.minecraft.server.EmptyChunk;
import net.minecraft.server.PacketPlayOutMultiBlockChange;
import net.minecraft.server.WorldChunkManager;
import net.minecraft.server.WorldServer;

public class CraftChunk implements Chunk {
    private WeakReference<net.minecraft.server.Chunk> weakChunk;
    private final WorldServer worldServer;
    private final int x;
    private final int z;
    private static final byte[] emptyData = new byte[2048];
    private static final short[] emptyBlockIDs = new short[4096];
    private static final byte[] emptySkyLight = new byte[2048];

    public CraftChunk(net.minecraft.server.Chunk chunk) {
        if (!(chunk instanceof EmptyChunk)) {
            this.weakChunk = new WeakReference<net.minecraft.server.Chunk>(chunk);
        }

        worldServer = (WorldServer) getHandle().world;
        x = getHandle().locX;
        z = getHandle().locZ;
    }

    public World getWorld() {
        return worldServer.getWorld();
    }

    public CraftWorld getCraftWorld() {
        return (CraftWorld) getWorld();
    }

    public net.minecraft.server.Chunk getHandle() {
        net.minecraft.server.Chunk c = weakChunk.get();

        if (c == null) {
            c = worldServer.getChunkAt(x, z);

            if (!(c instanceof EmptyChunk)) {
                weakChunk = new WeakReference<net.minecraft.server.Chunk>(c);
            }
        }

        return c;
    }

    void breakLink() {
        weakChunk.clear();
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "CraftChunk{" + "x=" + getX() + "z=" + getZ() + '}';
    }

    public Block getBlock(int x, int y, int z) {
        return new CraftBlock(this, (getX() << 4) | (x & 0xF), y & 0xFF, (getZ() << 4) | (z & 0xF));
    }

    public Entity[] getEntities() {
        /*int count = 0, index = 0;
        net.minecraft.server.Chunk chunk = getHandle();

        for (int i = 0; i < 16; i++) {
            count += chunk.entitySlices[i].size();
        }

        Entity[] entities = new Entity[count];

        for (int i = 0; i < 16; i++) {
        	for (net.minecraft.server.Entity entity : chunk.entitySlices[i]) {
        		if (entity == null) {
        			continue;
                }

                entities[index++] = entity.getBukkitEntity();
            }
        }

        return entities;*/
    	// Rinny start
    	net.minecraft.server.Chunk chunk = getHandle();
        int estimatedSize = 0;
        for (int i = 0; i < 16; i++) {
            estimatedSize += chunk.entitySlices[i].size();
        }
        Entity[] entities = new Entity[estimatedSize];
        int index = 0;
        for (int i = 0; i < 16; i++) {
            for (net.minecraft.server.Entity entity : chunk.entitySlices[i]) {
                if (entity != null) {
                    entities[index++] = entity.getBukkitEntity();
                }
            }
        }
        return Arrays.copyOf(entities, index);
    	// Rinny end
    }

    public BlockState[] getTileEntities() {
        /*int index = 0;
        net.minecraft.server.Chunk chunk = getHandle();
        BlockState[] entities = new BlockState[chunk.tileEntities.size()];

        for (Object obj : chunk.tileEntities.keySet().toArray()) {
            if (!(obj instanceof ChunkPosition)) {
                continue;
            }

            ChunkPosition position = (ChunkPosition) obj;
            entities[index++] = worldServer.getWorld().getBlockAt(position.x + (chunk.locX << 4), position.y, position.z + (chunk.locZ << 4)).getState();
        }
        return entities;*/
    	
    	// Rinny start - TODO: check if good
    	net.minecraft.server.Chunk chunk = getHandle();
        BlockState[] entities = new BlockState[chunk.tileEntities.size()];
        int index = 0;

        for (ChunkPosition position : chunk.tileEntities.keySet()) {
            entities[index++] = worldServer.getWorld().getBlockAt(
                    position.x + (chunk.locX << 4),
                    position.y,
                    position.z + (chunk.locZ << 4)
            ).getState();
        }
        return entities;
    	// Rinny end
    }

    public boolean isLoaded() {
        return getWorld().isChunkLoaded(this);
    }

    public boolean load() {
        return getWorld().loadChunk(getX(), getZ(), true);
    }

    public boolean load(boolean generate) {
        return getWorld().loadChunk(getX(), getZ(), generate);
    }

    public boolean unload() {
        return getWorld().unloadChunk(getX(), getZ());
    }

    public boolean unload(boolean save) {
        return getWorld().unloadChunk(getX(), getZ(), save);
    }

    public boolean unload(boolean save, boolean safe) {
        return getWorld().unloadChunk(getX(), getZ(), save, safe);
    }

    public ChunkSnapshot getChunkSnapshot() {
        return getChunkSnapshot(true, false, false);
    }
    
    // BUKKIT VERSION - OLD
    /*public ChunkSnapshot getChunkSnapshot(boolean includeMaxBlockY, boolean includeBiome, boolean includeBiomeTempRain) {
        net.minecraft.server.Chunk chunk = getHandle();

        ChunkSection[] cs = chunk.getSections();
        short[][] sectionBlockIDs = new short[cs.length][];
        byte[][] sectionBlockData = new byte[cs.length][];
        byte[][] sectionSkyLights = new byte[cs.length][];
        byte[][] sectionEmitLights = new byte[cs.length][];
        boolean[] sectionEmpty = new boolean[cs.length];

        for (int i = 0; i < cs.length; i++) {
            if (cs[i] == null) {
                sectionBlockIDs[i] = emptyBlockIDs;
                sectionBlockData[i] = emptyData;
                sectionSkyLights[i] = emptySkyLight;
                sectionEmitLights[i] = emptyData;
                sectionEmpty[i] = true;
            } else {
                short[] blockids = new short[4096];
                byte[] baseids = cs[i].getIdArray();

                
                for (int j = 0; j < 4096; j++) {
                    blockids[j] = (short) (baseids[j] & 0xFF);
                }

                if (cs[i].getExtendedIdArray() != null) {
                    byte[] extids = cs[i].getExtendedIdArray().a;

                    for (int j = 0; j < 2048; j++) {
                        short b = (short) (extids[j] & 0xFF);

                        if (b == 0) {
                            continue;
                        }

                        blockids[j<<1] |= (b & 0x0F) << 8;
                        blockids[(j<<1)+1] |= (b & 0xF0) << 4;
                    }
                }

                sectionBlockIDs[i] = blockids;

                
                sectionBlockData[i] = new byte[2048];
                System.arraycopy(cs[i].getDataArray().a, 0, sectionBlockData[i], 0, 2048);
                if (cs[i].getSkyLightArray() == null) {
                    sectionSkyLights[i] = emptyData;
                } else {
                    sectionSkyLights[i] = new byte[2048];
                    System.arraycopy(cs[i].getSkyLightArray().a, 0, sectionSkyLights[i], 0, 2048);
                }
                sectionEmitLights[i] = new byte[2048];
                System.arraycopy(cs[i].getEmittedLightArray().a, 0, sectionEmitLights[i], 0, 2048);
            }
        }

        int[] hmap = null;

        if (includeMaxBlockY) {
            hmap = new int[256]; // Get copy of height map
            System.arraycopy(chunk.heightMap, 0, hmap, 0, 256);
        }

        BiomeBase[] biome = null;
        double[] biomeTemp = null;
        double[] biomeRain = null;

        if (includeBiome || includeBiomeTempRain) {
            WorldChunkManager wcm = chunk.world.getWorldChunkManager();

            if (includeBiome) {
                biome = new BiomeBase[256];
                for (int i = 0; i < 256; i++) {
                    biome[i] = chunk.getBiome(i & 0xF, i >> 4, wcm);
                }
            }

            if (includeBiomeTempRain) {
                biomeTemp = new double[256];
                biomeRain = new double[256];
                float[] dat = getTemperatures(wcm, getX() << 4, getZ() << 4);

                for (int i = 0; i < 256; i++) {
                    biomeTemp[i] = dat[i];
                }

                dat = wcm.getWetness(null, getX() << 4, getZ() << 4, 16, 16);

                for (int i = 0; i < 256; i++) {
                    biomeRain[i] = dat[i];
                }
            }
        }

        World world = getWorld();
        return new CraftChunkSnapshot(getX(), getZ(), world.getName(), world.getFullTime(), sectionBlockIDs, sectionBlockData, sectionSkyLights, sectionEmitLights, sectionEmpty, hmap, biome, biomeTemp, biomeRain);
    }*/

    // Rinny version - new // TODO: check if this does shit
    public ChunkSnapshot getChunkSnapshot(boolean includeMaxBlockY, boolean includeBiome, boolean includeBiomeTempRain) {
        net.minecraft.server.Chunk chunk = getHandle();
        ChunkSection[] cs = chunk.getSections();
        final int csLength = cs.length;
        short[][] sectionBlockIDs = new short[csLength][];
        byte[][] sectionBlockData = new byte[csLength][];
        byte[][] sectionSkyLights = new byte[csLength][];
        byte[][] sectionEmitLights = new byte[csLength][];
        boolean[] sectionEmpty = new boolean[csLength];

        for (int i = 0; i < csLength; i++) {
            if (cs[i] == null) { /* Section is empty? */
                sectionBlockIDs[i] = emptyBlockIDs;
                sectionBlockData[i] = emptyData;
                sectionSkyLights[i] = emptySkyLight;
                sectionEmitLights[i] = emptyData;
                sectionEmpty[i] = true;
            } else {
                /*short[] blockids = new short[4096];
                byte[] baseids = cs[i].getIdArray();

                for (int j = 0; j < 4096; j++) {
                    blockids[j] = (short) (baseids[j] & 0xFF);
                }

                if (cs[i].getExtendedIdArray() != null) {
                    byte[] extids = cs[i].getExtendedIdArray().a;

                    for (int j = 0; j < 2048; j++) {
                        short b = (short) (extids[j] & 0xFF);

                        if (b == 0) {
                            continue;
                        }

                        blockids[j << 1] |= (b & 0x0F) << 8;
                        blockids[(j << 1) + 1] |= (b & 0xF0) << 4;
                    }
                }

                sectionBlockIDs[i] = blockids;
                sectionBlockData[i] = cs[i].getDataArray().a;
                sectionSkyLights[i] = cs[i].getSkyLightArray() != null ? cs[i].getSkyLightArray().a : emptyData;
                sectionEmitLights[i] = cs[i].getEmittedLightArray().a;*/
            	short[] blockids = new short[4096];
                byte[] baseids = cs[i].getIdArray();
                System.arraycopy(baseids, 0, blockids, 0, baseids.length);

                byte[] extidsArray = cs[i].getExtendedIdArray() != null ? cs[i].getExtendedIdArray().a : null;
                if (extidsArray != null) { 
                    for (int j = 0; j < 2048; j++) {
                        short b = (short) (extidsArray[j] & 0xFF);
                        if (b != 0) {
                            blockids[j << 1] |= (b & 0x0F) << 8;
                            blockids[(j << 1) + 1] |= (b & 0xF0) << 4;
                        }
                    }
                }

                sectionBlockIDs[i] = blockids;
                sectionBlockData[i] = cs[i].getDataArray().a;
                sectionSkyLights[i] = cs[i].getSkyLightArray() != null ? cs[i].getSkyLightArray().a : emptyData;
                sectionEmitLights[i] = cs[i].getEmittedLightArray().a;
            }
        }

        int[] hmap = includeMaxBlockY ? chunk.heightMap.clone() : null;

        BiomeBase[] biome = null;
        double[] biomeTemp = null;
        double[] biomeRain = null;

        if (includeBiome || includeBiomeTempRain) {
            WorldChunkManager wcm = chunk.world.getWorldChunkManager();

            if (includeBiome) {
                biome = new BiomeBase[256];
                for (int i = 0; i < 256; i++) {
                    biome[i] = chunk.getBiome(i & 0xF, i >> 4, wcm);
                }
            }

            if (includeBiomeTempRain) {
                biomeTemp = new double[256];
                biomeRain = new double[256];
                /*float[] dat = getTemperatures(wcm, getX() << 4, getZ() << 4);

                for (int i = 0; i < 256; i++) {
                    biomeTemp[i] = dat[i];
                }

                dat = wcm.getWetness(null, getX() << 4, getZ() << 4, 16, 16);

                for (int i = 0; i < 256; i++) {
                    biomeRain[i] = dat[i];
                }*/
                float[] temperatures = getTemperatures(wcm, getX() << 4, getZ() << 4);
                System.arraycopy(temperatures, 0, biomeTemp, 0, 256);

                float[] wetness = wcm.getWetness(null, getX() << 4, getZ() << 4, 16, 16);
                System.arraycopy(wetness, 0, biomeRain, 0, 256);
            }
        }
        return new CraftChunkSnapshot(getX(), getZ(), getWorld().getName(), getWorld().getFullTime(), sectionBlockIDs, sectionBlockData, sectionSkyLights, sectionEmitLights, sectionEmpty, hmap, biome, biomeTemp, biomeRain);
    }

    public static ChunkSnapshot getEmptyChunkSnapshot(int x, int z, CraftWorld world, boolean includeBiome, boolean includeBiomeTempRain) {
    	// Rinny start
    	BiomeBase[] biome = null;
        double[] biomeTemp = null;
        double[] biomeRain = null;

        if (includeBiome || includeBiomeTempRain) {
            WorldChunkManager wcm = world.getHandle().getWorldChunkManager();

            if (includeBiome) {
                biome = new BiomeBase[256];
                for (int i = 0; i < 256; i++) {
                    biome[i] = world.getHandle().getBiome((x << 4) + (i & 0xF), (z << 4) + (i >> 4));
                }
            }

            if (includeBiomeTempRain) {
                biomeTemp = new double[256];
                biomeRain = new double[256];
                /*float[] dat = getTemperatures(wcm, x << 4, z << 4);

                for (int i = 0; i < 256; i++) {
                    biomeTemp[i] = dat[i];
                }

                dat = wcm.getWetness(null, x << 4, z << 4, 16, 16);

                for (int i = 0; i < 256; i++) {
                    biomeRain[i] = dat[i];
                }*/
                float[] temperatures = getTemperatures(wcm, x << 4, z << 4);
                System.arraycopy(temperatures, 0, biomeTemp, 0, 256);

                float[] wetness = wcm.getWetness(null, x << 4, z << 4, 16, 16);
                System.arraycopy(wetness, 0, biomeRain, 0, 256);
            }
        }

        /* Fill with empty data */
        int hSection = world.getMaxHeight() >> 4;
        short[][] blockIDs = new short[hSection][];
        byte[][] skyLight = new byte[hSection][];
        byte[][] emitLight = new byte[hSection][];
        byte[][] blockData = new byte[hSection][];
        boolean[] empty = new boolean[hSection];

        Arrays.fill(empty, true);
        Arrays.fill(blockIDs, emptyBlockIDs);
        Arrays.fill(skyLight, emptySkyLight);
        Arrays.fill(emitLight, emptyData);
        Arrays.fill(blockData, emptyData);
        int[] hmap = new int[256];
        return new CraftChunkSnapshot(x, z, world.getName(), world.getFullTime(), blockIDs, blockData, skyLight, emitLight, empty, hmap, biome, biomeTemp, biomeRain);
        // Rinny end
    }

    private static float[] getTemperatures(WorldChunkManager chunkmanager, int chunkX, int chunkZ) {
        BiomeBase[] biomes = chunkmanager.getBiomes(null, chunkX, chunkZ, 16, 16);
        float[] temps = new float[biomes.length];

        for (int i = 0; i < biomes.length; i++) {
            float temp = biomes[i].temperature; // Vanilla of olde: ((int) biomes[i].temperature * 65536.0F) / 65536.0F

            if (temp > 1F) {
                temp = 1F;
            }

            temps[i] = temp;
        }

        return temps;
    }
    
    // From BadlionSpigot
	public FakeMultiBlockChange createFakeBlockUpdate(Location[] locations, int[] ids, int[] datas) {
		int[] xs = new int[locations.length];
		int[] ys = new int[locations.length];
		int[] zs = new int[locations.length];
		for (int i = 0; i < locations.length; i++) {
			xs[i] = locations[i].getBlockX() & 0xF;
			ys[i] = locations[i].getBlockY();
			zs[i] = locations[i].getBlockZ() & 0xF;
		} 
		return (FakeMultiBlockChange)new CraftFakeMultiBlockChange(new PacketPlayOutMultiBlockChange(this.x, this.z, xs, ys, zs, ids, datas));
	}

    static {
        Arrays.fill(emptySkyLight, (byte) 0xFF);
    }
}
