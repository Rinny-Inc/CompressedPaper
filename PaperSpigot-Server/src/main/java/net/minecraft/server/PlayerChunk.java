package net.minecraft.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// CraftBukkit start
import org.bukkit.craftbukkit.chunkio.ChunkIOExecutor;

class PlayerChunk {
    private final List<EntityPlayer> players;
    private final ChunkCoordIntPair location;
    private short[] dirtyBlocks;
    private int dirtyCount;
    private int f;
    private long lastUpdateTime;
    final PlayerChunkMap playerChunkMap;
    // CraftBukkit start - add fields
    private final HashMap<EntityPlayer, Runnable> playerRunnables = new HashMap<EntityPlayer, Runnable>();
    private boolean loaded = false;
    private Runnable loadedRunnable = this::markAsLoaded;
    // CraftBukkit end
    
    private void markAsLoaded() {
        loaded = true;
    }

    public PlayerChunk(PlayerChunkMap playerchunkmap, int i, int j) {
        this.playerChunkMap = playerchunkmap;
        this.players = new ArrayList<EntityPlayer>();
        this.dirtyBlocks = new short[64];
        this.location = new ChunkCoordIntPair(i, j);
        playerchunkmap.a().chunkProviderServer.getChunkAt(i, j, this.loadedRunnable); // CraftBukkit
    }

    public void a(final EntityPlayer entityplayer) { // CraftBukkit - added final to argument
        if (this.players.contains(entityplayer)) {
            PlayerChunkMap.c().debug("Failed to add player. {} already is in chunk {}, {}", new Object[] { entityplayer, Integer.valueOf(this.location.x), Integer.valueOf(this.location.z)});
        } else {
            if (this.players.isEmpty()) {
                this.lastUpdateTime = PlayerChunkMap.a(this.playerChunkMap).getTime();
            }

            this.players.add(entityplayer);
            // CraftBukkit start - use async chunk io
            Runnable playerRunnable;
            if (this.loaded) {
                playerRunnable = null;
                entityplayer.chunkCoordIntPairQueue.add(this.location);
            } else {
            	playerRunnable = () -> entityplayer.chunkCoordIntPairQueue.add(location);
                this.playerChunkMap.a().chunkProviderServer.getChunkAt(this.location.x, this.location.z, playerRunnable);
            }

            this.playerRunnables.put(entityplayer, playerRunnable);
            // CraftBukkit end
        }
    }

    public void b(EntityPlayer entityplayer) {
        if (this.players.contains(entityplayer)) {
            // CraftBukkit start - If we haven't loaded yet don't load the chunk just so we can clean it up
            if (!this.loaded) {
                ChunkIOExecutor.dropQueuedChunkLoad(this.playerChunkMap.a(), this.location.x, this.location.z, this.playerRunnables.get(entityplayer));
                this.players.remove(entityplayer);
                this.playerRunnables.remove(entityplayer);

                if (this.players.isEmpty()) {
                    ChunkIOExecutor.dropQueuedChunkLoad(this.playerChunkMap.a(), this.location.x, this.location.z, this.loadedRunnable);
                    long i = (long) this.location.x + 2147483647L | (long) this.location.z + 2147483647L << 32;
                    PlayerChunkMap.b(this.playerChunkMap).remove(i);
                    PlayerChunkMap.c(this.playerChunkMap).remove(this);
                }

                return;
            }
            // CraftBukkit end

            final Chunk chunk = PlayerChunkMap.a(this.playerChunkMap).getChunkAt(this.location.x, this.location.z);

            if (chunk.isReady()) {
            	if (entityplayer.playerConnection.networkManager.getVersion() < 107) {
                    entityplayer.playerConnection.sendPacket(new PacketPlayOutMapChunk(chunk, true, 0, entityplayer));
            	} else {
                    entityplayer.playerConnection.sendPacket(new PacketPlayOutUnloadChunk(chunk.locX, chunk.locZ));
            	}
            }

            this.playerRunnables.remove(entityplayer); // CraftBukkit
            this.players.remove(entityplayer);
            entityplayer.chunkCoordIntPairQueue.remove(this.location);
            if (this.players.isEmpty()) {
            	long i = (long) this.location.x + 2147483647L | (long) this.location.z + 2147483647L << 32;
                this.a(chunk);
                PlayerChunkMap.b(this.playerChunkMap).remove(i);
                PlayerChunkMap.c(this.playerChunkMap).remove(this);
                if (this.dirtyCount > 0) {
                    PlayerChunkMap.d(this.playerChunkMap).remove(this);
                }

                this.playerChunkMap.a().chunkProviderServer.queueUnload(this.location.x, this.location.z);
            }
        }
    }

    public void a() {
        this.a(PlayerChunkMap.a(this.playerChunkMap).getChunkAt(this.location.x, this.location.z));
    }

    private void a(Chunk chunk) {
        chunk.s += PlayerChunkMap.a(this.playerChunkMap).getTime() - this.lastUpdateTime;
        this.lastUpdateTime = PlayerChunkMap.a(this.playerChunkMap).getTime();
    }

    public void a(int i, int j, int k) {
        if (this.dirtyCount == 0) {
            PlayerChunkMap.d(this.playerChunkMap).add(this);
        }

        this.f |= 1 << (j >> 4);
        if (this.dirtyCount < 64) {
        	final short short1 = (short) (i << 12 | k << 8 | j);

            for (int l = 0; l < this.dirtyCount; ++l) {
                if (this.dirtyBlocks[l] == short1) {
                    return;
                }
            }

            this.dirtyBlocks[this.dirtyCount++] = short1;
        }
    }

    public void sendAll(Packet packet) {
    	for (EntityPlayer entityplayer : this.players) {
    		if (!entityplayer.chunkCoordIntPairQueue.contains(this.location)) {
                entityplayer.playerConnection.sendPacket(packet);
            }
    	}
    }

    public void b() {
        if (this.dirtyCount != 0) {
            int i;
            int j;
            int k;

            if (this.dirtyCount == 1) {
                i = this.location.x * 16 + (this.dirtyBlocks[0] >> 12 & 15);
                j = this.dirtyBlocks[0] & 255;
                k = this.location.z * 16 + (this.dirtyBlocks[0] >> 8 & 15);
                this.sendAll(new PacketPlayOutBlockChange(i, j, k, PlayerChunkMap.a(this.playerChunkMap)));
                if (PlayerChunkMap.a(this.playerChunkMap).getType(i, j, k).isTileEntity()) {
                    this.sendTileEntity(PlayerChunkMap.a(this.playerChunkMap).getTileEntity(i, j, k));
                }
            } else {
                int l;

                if (this.dirtyCount == 64) {
                    i = this.location.x * 16;
                    j = this.location.z * 16;
                    // Spigot start - protocol patch
                    //this.sendAll(new PacketPlayOutMapChunk(PlayerChunkMap.a(this.playerChunkMap).getChunkAt(this.location.x, this.location.z), (this.f == 0xFFFF), this.f)); // CraftBukkit - send everything (including biome) if all sections flagged
                    final Chunk chunk = PlayerChunkMap.a( this.playerChunkMap ).getChunkAt( this.location.x, this.location.z );
                    //final boolean flag = (this.f == 0xFFFF);
                    for (int idx = 0; idx < this.players.size(); ++idx) {
                        EntityPlayer entityplayer = (EntityPlayer) this.players.get(idx);

                        if (!entityplayer.chunkCoordIntPairQueue.contains(this.location)) {
                        	PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(chunk, (this.f == 0xFFFF), this.f, entityplayer);
                        	if (entityplayer.playerConnection.networkManager.getVersion() >= 110) {
                                packet.setTileEntities(chunk.tileEntities); 
                        	}
                            entityplayer.playerConnection.sendPacket(packet);
                        }
                    }
                    Iterator<Map.Entry<ChunkPosition, TileEntity>> iter = chunk.tileEntities.entrySet().iterator();
                    while (iter.hasNext()) {
                    	Map.Entry<ChunkPosition, TileEntity> entry = iter.next();
                    	if ((this.f & 1 << ((ChunkPosition)entry.getKey()).y >> 4) != 0) {
                    		Packet packet = ((TileEntity)entry.getValue()).getUpdatePacket();
                    		if (packet != null) {
                    			for (EntityPlayer entityplayer : this.players) {
                    				if (entityplayer.playerConnection.networkManager.getVersion() < 110) {
                    					entityplayer.playerConnection.sendPacket(packet); 
                    				}
                    			}  
                    		}
                    	} 
                    } 

                    // Spigot end - protocol patch
                    /*for (k = 0; k < 16; ++k) {
                        if ((this.f & 1 << k) != 0) {
                            l = k << 4;
                            List list = PlayerChunkMap.a(this.playerChunkMap).getTileEntities(i, l, j, i + 16, l + 16, j + 16);

                            for (int i1 = 0; i1 < list.size(); ++i1) {
                                this.sendTileEntity((TileEntity) list.get(i1));
                            }
                        }
                    }*/
                } else {
                    this.sendAll(new PacketPlayOutMultiBlockChange(this.dirtyCount, this.dirtyBlocks, PlayerChunkMap.a(this.playerChunkMap).getChunkAt(this.location.x, this.location.z)));

                    for (i = 0; i < this.dirtyCount; ++i) {
                        j = this.location.x * 16 + (this.dirtyBlocks[i] >> 12 & 15);
                        k = this.dirtyBlocks[i] & 255;
                        l = this.location.z * 16 + (this.dirtyBlocks[i] >> 8 & 15);
                        if (PlayerChunkMap.a(this.playerChunkMap).getType(j, k, l).isTileEntity()) {
                            this.sendTileEntity(PlayerChunkMap.a(this.playerChunkMap).getTileEntity(j, k, l));
                        }
                    }
                }
            }

            this.dirtyCount = 0;
            this.f = 0;
        }
    }

    private void sendTileEntity(TileEntity tileentity) {
        if (tileentity != null) {
        	final Packet packet = tileentity.getUpdatePacket();

            if (packet != null) {
                this.sendAll(packet);
            }
        }
    }

    static ChunkCoordIntPair a(PlayerChunk playerchunk) {
        return playerchunk.location;
    }

    static List b(PlayerChunk playerchunk) {
        return playerchunk.players;
    }
}
