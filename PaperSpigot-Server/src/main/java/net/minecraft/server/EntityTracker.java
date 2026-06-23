package net.minecraft.server;

import io.noks.interfaces.ITrack;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spigotmc.SpigotDebreakifier;
import org.spigotmc.TrackingRange;

public class EntityTracker implements TrackingRange {

    private static final Logger a = LogManager.getLogger();

    // Rinny
    public List<EntityTrackerEntry> c = new ArrayList<>(64); // FIXME: possible conccurrent error
    public IntHashMap trackedEntities = new IntHashMap(); // CraftBukkit - private -> public
    private int e;

    // Rinny
    private final Map<EntityPlayer, List<EntityTrackerEntry>> byPlayer =
        new IdentityHashMap<>();

    // Rinny
    private final List<EntityPlayer> movedPlayers = new ArrayList<>();

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

        throw new IllegalArgumentException(
            "Don\'t know how to track " + entity.getClass() + "!"
        );
        // Rinny end
    }

    public void addEntity(Entity entity, int i, int j) {
        this.addEntity(entity, i, j, false);
    }

    public void addEntity(Entity entity, int i, int j, boolean flag) {
        i = Math.min(this.getEntityTrackingRange(entity, i), this.e); // Spigot

        try {
            if (this.trackedEntities.b(entity.getId())) {
                throw new IllegalStateException("Entity is already tracked!");
            }

            final EntityTrackerEntry entitytrackerentry =
                new EntityTrackerEntry(entity, i, j, flag, this);

            entitytrackerentry.listIndex = this.c.size();
            this.c.add(entitytrackerentry);
            this.trackedEntities.a(entity.getId(), entitytrackerentry);
            entity.world.performOnInRangePlayers(
                entity,
                i,
                entitytrackerentry::updatePlayer
            );
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(
                throwable,
                "Adding entity to track"
            );
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a(
                "Entity To Track"
            );

            crashreportsystemdetails.a("Tracking range", (i + " blocks"));
            crashreportsystemdetails.a(
                "Update interval",
                new CrashReportEntityTrackerUpdateInterval(this, j)
            );
            entity.a(crashreportsystemdetails);
            CrashReportSystemDetails crashreportsystemdetails1 = crashreport.a(
                "Entity That Is Already Tracked"
            );

            (
                (EntityTrackerEntry) this.trackedEntities.get(entity.getId())
            ).tracker.a(crashreportsystemdetails1);

            try {
                throw new ReportedException(crashreport);
            } catch (ReportedException reportedexception) {
                a.error(
                    "\"Silently\" catching entity tracking error.",
                    reportedexception
                );
            }
        }
    }

    // Rinny
    private void removeEntry(EntityTrackerEntry entry) {
        int idx = entry.listIndex;
        if (idx < 0) {
            return;
        }
        int last = this.c.size() - 1;
        if (idx != last) {
            EntityTrackerEntry moved = this.c.get(last);
            this.c.set(idx, moved);
            moved.listIndex = idx;
        }
        this.c.remove(last);
        entry.listIndex = -1;
    }

    // Rinny
    void linkPlayer(EntityPlayer player, EntityTrackerEntry entry) {
        this.byPlayer
            .computeIfAbsent(player, p -> new ArrayList<>(8))
            .add(entry);
    }

    // Rinny
    void unlinkPlayer(EntityPlayer player, EntityTrackerEntry entry) {
        List<EntityTrackerEntry> list = this.byPlayer.get(player);
        if (list == null) {
            return;
        }
        list.remove(entry);
        if (list.isEmpty()) {
            this.byPlayer.remove(player);
        }
    }

    public void untrackEntity(Entity entity) {
        if (entity instanceof EntityPlayer entityplayer) {
            // Rinny
            this.untrackPlayer(entityplayer);
        }

        final EntityTrackerEntry entitytrackerentry1 =
            (EntityTrackerEntry) this.trackedEntities.d(entity.getId());

        if (entitytrackerentry1 != null) {
            this.removeEntry(entitytrackerentry1);
            entitytrackerentry1.a();
        }
    }

    public void updatePlayers() {
        final List<EntityTrackerEntry> entries = this.c;
        final List<EntityPlayer> moved = this.movedPlayers;
        moved.clear();

        for (int idx = 0; idx < entries.size(); idx++) {
            EntityTrackerEntry entitytrackerentry = entries.get(idx);

            entitytrackerentry.track();
            if (
                entitytrackerentry.n &&
                entitytrackerentry.tracker instanceof EntityPlayer ep
            ) {
                moved.add(ep);
            }
        }

        // Rinny
        if (moved.isEmpty()) {
            return;
        }

        for (int p = 0; p < moved.size(); p++) {
            EntityPlayer entityPlayer = moved.get(p);

            for (int idx = 0; idx < entries.size(); idx++) {
                EntityTrackerEntry entitytrackerentry1 = entries.get(idx);

                if (entitytrackerentry1.tracker != entityPlayer) {
                    entitytrackerentry1.updatePlayer(entityPlayer);
                }
            }
        }
    }

    public void a(Entity entity, Packet packet) {
        final EntityTrackerEntry entitytrackerentry =
            (EntityTrackerEntry) this.trackedEntities.get(entity.getId());

        if (entitytrackerentry != null) {
            entitytrackerentry.broadcast(packet);
        }
    }

    public void sendPacketToEntity(Entity entity, Packet packet) {
        final EntityTrackerEntry entitytrackerentry =
            (EntityTrackerEntry) this.trackedEntities.get(entity.getId());

        if (entitytrackerentry != null) {
            entitytrackerentry.broadcastIncludingSelf(packet);
        }
    }

    public void untrackPlayer(EntityPlayer entityplayer) {
        // Rinny
        List<EntityTrackerEntry> tracked = this.byPlayer.remove(entityplayer);
        if (tracked == null || tracked.isEmpty()) {
            return;
        }

        for (EntityTrackerEntry entitytrackerentry : new ArrayList<>(tracked)) {
            entitytrackerentry.clear(entityplayer);
        }
    }

    public void a(EntityPlayer entityplayer, Chunk chunk) {
        final List<EntityTrackerEntry> entries = this.c;

        for (int idx = 0; idx < entries.size(); idx++) {
            EntityTrackerEntry entitytrackerentry = entries.get(idx);

            if (
                entitytrackerentry.tracker != entityplayer &&
                entitytrackerentry.tracker.ah == chunk.locX &&
                entitytrackerentry.tracker.aj == chunk.locZ
            ) {
                entitytrackerentry.updatePlayer(entityplayer);
            }
        }
    }

    public void broadcastFallDamageParticles(
        Entity entity,
        Block block,
        int blockData
    ) {
        final EntityTrackerEntry entitytrackerentry =
            (EntityTrackerEntry) this.trackedEntities.get(entity.getId());
        Packet packetOld = null;
        Packet packetNew = null;
        if (entity instanceof EntityPlayer player) {
            if (player.playerConnection.networkManager.getVersion() < 107) {
                player.playerConnection.sendPacket(
                    packetOld = dustParticlesOld(entity)
                );
            } else {
                player.playerConnection.sendPacket(
                    packetNew = dustParticlesNew(entity, block, blockData)
                );
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
        return new PacketPlayOutWorldEvent(
            2006,
            MathHelper.floor(entity.locX),
            MathHelper.floor(
                entity.locY - 0.20000000298023224D - entity.height
            ),
            MathHelper.floor(entity.locZ),
            MathHelper.f(entity.fallDistance - 3.0F),
            false
        );
    }

    private static Packet dustParticlesNew(
        Entity entity,
        Block block,
        int blockData
    ) {
        int id = Block.getId(block);
        int data = SpigotDebreakifier.getCorrectedData(id, blockData);
        int particleCount = (int) (150.0D *
            Math.min(
                (0.2F + MathHelper.f(entity.fallDistance - 3.0F) / 15.0F),
                2.5D
            ));
        return new PacketPlayOutWorldParticles(
            "blockdust_" + id + "_" + data,
            (float) entity.locX,
            (float) entity.locY,
            (float) entity.locZ,
            0.0F,
            0.0F,
            0.0F,
            0.15F,
            particleCount
        );
    }
}
