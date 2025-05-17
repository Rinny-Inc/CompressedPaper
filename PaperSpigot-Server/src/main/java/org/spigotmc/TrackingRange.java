package org.spigotmc;

import io.noks.utils.EntityNPC;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityExperienceOrb;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityItemFrame;
import net.minecraft.server.EntityPainting;
import net.minecraft.server.EntityPlayer;

public interface TrackingRange { // Rinny - class > interface

    /**
     * Gets the range an entity should be 'tracked' by players and visible in
     * the client.
     *
     * @param entity
     * @param defaultRange Default range defined by Mojang
     * @return
     */
    default short getEntityTrackingRange(Entity entity, int defaultRange) { // Rinny - public static > default
    	final SpigotWorldConfig config = entity.world.spigotConfig;
    	if (entity instanceof EntityPlayer && !(entity instanceof EntityNPC)) {
            return config.playerTrackingRange;
        }
        if (entity.activationType == 1) {
            return config.monsterTrackingRange;
        }
        if (entity instanceof EntityGhast) {
        	return (short) Math.max(config.monsterTrackingRange, config.monsterActivationRange);
        }
        if (entity.activationType == 2) {
            return config.animalTrackingRange;
        }
        if (entity instanceof EntityNPC) {
        	return config.npcTrackingRange;
        }
        return (entity instanceof EntityItemFrame || entity instanceof EntityPainting || entity instanceof EntityItem || entity instanceof EntityExperienceOrb ? config.miscTrackingRange : config.otherTrackingRange);
    }
}
