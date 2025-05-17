package net.minecraft.server;

import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent; // CraftBukkit
import org.bukkit.event.player.PlayerFishEvent.State;

public class ItemFishingRod extends Item {

    public ItemFishingRod() {
        this.setMaxDurability(64);
        this.e(1);
        this.a(CreativeModeTab.i);
    }

    public ItemStack a(ItemStack itemstack, World world, EntityHuman entityhuman) {
        if (entityhuman.hookedFish != null) {
        	final int i = entityhuman.hookedFish.e();
            itemstack.damage(i, entityhuman);
            //entityhuman.ba();
        } else {
            // CraftBukkit start
        	final EntityFishingHook hook = new EntityFishingHook(world, entityhuman);
        	final PlayerFishEvent playerFishEvent = new PlayerFishEvent((org.bukkit.entity.Player) entityhuman.getBukkitEntity(), null, (FishHook) hook.getBukkitEntity(), State.FISHING);
            world.getServer().getPluginManager().callEvent(playerFishEvent);

            if (playerFishEvent.isCancelled()) {
                entityhuman.hookedFish = null;
                return itemstack;
            }
            // CraftBukkit end
            world.makeSound(entityhuman, "random.bow", 0.5F, 0.4F / (g.nextFloat() * 0.4F + 0.8F));
            if (!world.isStatic) {
                world.addEntity(hook); // CraftBukkit - moved creation up
            }
            //entityhuman.ba();
        }
        entityhuman.ba(); // SET IT HERE DU TO DUPLICATION CODE

        return itemstack;
    }

    public boolean e_(ItemStack itemstack) {
        return super.e_(itemstack);
    }

    public int c() {
        return 1;
    }
}
