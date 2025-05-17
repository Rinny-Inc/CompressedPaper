package net.minecraft.server;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class ItemBucket extends Item {

    private Block a;

    public ItemBucket(Block block) {
        this.maxStackSize = 1;
        this.a = block;
        this.a(CreativeModeTab.f);
    }

    public ItemStack a(ItemStack itemstack, World world, EntityHuman entityhuman) {
    	final boolean flag = this.a == Blocks.AIR;
    	final MovingObjectPosition movingobjectposition = this.a(world, entityhuman, flag);

        if (movingobjectposition == null) {
            return itemstack;
        } else {
            if (movingobjectposition.type == EnumMovingObjectType.BLOCK) {
                int i = movingobjectposition.b;
                int j = movingobjectposition.c;
                int k = movingobjectposition.d;

                if (!world.a(entityhuman, i, j, k)) {
                    return itemstack;
                }

                if (flag) {
                    if (!entityhuman.a(i, j, k, movingobjectposition.face, itemstack)) {
                        return itemstack;
                    }

                    final Material material = world.getType(i, j, k).getMaterial();
                    final int l = world.getData(i, j, k);

                    //if ((material == Material.WATER || material == Material.LAVA) && l == 0) {
                    if (material.isLiquid() && l == 0) {
                    	final Item bucketType = (material == Material.LAVA ? Items.LAVA_BUCKET : Items.WATER_BUCKET);
                        // CraftBukkit start
                    	final PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent(entityhuman, i, j, k, -1, itemstack, bucketType);

                        if (event.isCancelled()) {
                            return itemstack;
                        }
                        // CraftBukkit end
                        world.setAir(i, j, k);
                        return this.a(itemstack, entityhuman, bucketType, event.getItemStack()); // CraftBukkit - added Event stack
                    }
                } else {
                    if (this.a == Blocks.AIR) {
                        // CraftBukkit start
                    	final PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent(entityhuman, i, j, k, movingobjectposition.face, itemstack);

                        if (event.isCancelled()) {
                            return itemstack;
                        }

                        return CraftItemStack.asNMSCopy(event.getItemStack());
                    }

                    final int clickedX = i, clickedY = j, clickedZ = k;
                    // CraftBukkit end

                    // Rinny start - faster
                    switch (movingobjectposition.face) {
						case 0 -> --j;
						case 1 -> ++j;
						case 2 -> --k;
						case 3 -> ++k;
						case 4 -> --i;
						case 5 -> ++i;
                    }
                    // Rinny end
                    /*if (movingobjectposition.face == 0) {
                        --j;
                    }

                    if (movingobjectposition.face == 1) {
                        ++j;
                    }

                    if (movingobjectposition.face == 2) {
                        --k;
                    }

                    if (movingobjectposition.face == 3) {
                        ++k;
                    }

                    if (movingobjectposition.face == 4) {
                        --i;
                    }

                    if (movingobjectposition.face == 5) {
                        ++i;
                    }*/

                    if (!entityhuman.a(i, j, k, movingobjectposition.face, itemstack)) {
                        return itemstack;
                    }

                    // CraftBukkit start
                    final PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent(entityhuman, clickedX, clickedY, clickedZ, movingobjectposition.face, itemstack);

                    if (event.isCancelled()) {
                        return itemstack;
                    }
                    // CraftBukkit end

                    if (this.a(world, i, j, k) && !entityhuman.abilities.canInstantlyBuild) {
                        return CraftItemStack.asNMSCopy(event.getItemStack()); // CraftBukkit
                    }
                }
            }

            return itemstack;
        }
    }

    // CraftBukkit - added ob.ItemStack result - TODO: Is this... the right way to handle this?
    private ItemStack a(ItemStack itemstack, EntityHuman entityhuman, Item item, org.bukkit.inventory.ItemStack result) {
        if (entityhuman.abilities.canInstantlyBuild) {
            return itemstack;
        }
        if (--itemstack.count <= 0) {
            return CraftItemStack.asNMSCopy(result); // CraftBukkit
        }
        
        if (!entityhuman.inventory.pickup(CraftItemStack.asNMSCopy(result))) { // CraftBukkit
        	entityhuman.drop(CraftItemStack.asNMSCopy(result), false); // CraftBukkit
        }
        return itemstack;
    }

    public boolean a(World world, int i, int j, int k) {
        if (this.a == Blocks.AIR) {
            return false;
        }
        final Material material = world.getType(i, j, k).getMaterial();
        final boolean flag = !material.isBuildable();

        if (!world.isEmpty(i, j, k) && !flag) {
            return false;
        }
        if (world.worldProvider.f && this.a == Blocks.WATER) {
            world.makeSound((double) ((float) i + 0.5F), (double) ((float) j + 0.5F), (double) ((float) k + 0.5F), "random.fizz", 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

            for (int l = 0; l < 8; ++l) {
                world.addParticle("largesmoke", (double) i + Math.random(), (double) j + Math.random(), (double) k + Math.random(), 0.0D, 0.0D, 0.0D);
            }
        } else {
            if (!world.isStatic && flag && !material.isLiquid()) {
                world.setAir(i, j, k, true);
            }

            world.setTypeAndData(i, j, k, this.a, 0, 3);
        }
        return true;
    }
}
