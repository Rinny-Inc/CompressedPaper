package io.noks.utils;

import java.util.List;

import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkPosition;
import net.minecraft.server.EnumCreatureType;
import net.minecraft.server.IChunkProvider;
import net.minecraft.server.IProgressUpdate;
import net.minecraft.server.World;

public class ChunkProviderVoid implements IChunkProvider {

    private final World world;

    public ChunkProviderVoid(World world) {
        this.world = world;
    }

    @Override
    public Chunk getOrCreateChunk(int x, int z) {
        Chunk chunk = new Chunk(this.world, x, z);
        chunk.initLighting();
        return chunk;
    }

    @Override
    public Chunk getChunkAt(int x, int z) {
        return getOrCreateChunk(x, z);
    }

    @Override
    public boolean isChunkLoaded(int x, int z) {
        return true;
    }

    @Override
    public void getChunkAt(IChunkProvider ichunkprovider, int x, int z) {
    }

    @Override
    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate) {
        return true;
    }

    @Override
    public boolean unloadChunks() {
        return false;
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public String getName() {
        return "VoidLevelSource";
    }

    @Override
    public List getMobsFor(EnumCreatureType type, int x, int y, int z) {
        return this.world.getBiome(x, z).getMobs(type);
    }

    @Override
    public ChunkPosition findNearestMapFeature(World world, String s, int x, int y, int z) {
        return null;
    }

    @Override
    public int getLoadedChunks() {
        return 0;
    }

    @Override
    public void recreateStructures(int x, int z) {
    }

    @Override
    public void c() {
    }
}