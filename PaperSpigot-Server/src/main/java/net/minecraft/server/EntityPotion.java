package net.minecraft.server;

// CraftBukkit start
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

import com.sathonay.interfaces.IEntitySpecificSpawnPacket;

import io.noks.interfaces.ITrack;
// CraftBukkit end

public class EntityPotion extends EntityProjectile implements IEntitySpecificSpawnPacket, ITrack {
    public ItemStack item; // CraftBukkit private -> public

    public EntityPotion(World world) {
        super(world);
    }

    public EntityPotion(World world, EntityLiving entityliving, int i) {
        this(world, entityliving, new ItemStack(Items.POTION, 1, i));
    }

    public EntityPotion(World world, EntityLiving entityliving, ItemStack itemstack) {
        super(world, entityliving);
        this.item = itemstack;
    }

    public EntityPotion(World world, double d0, double d1, double d2, ItemStack itemstack) {
        super(world, d0, d1, d2);
        this.item = itemstack;
    }

    protected float i() {
        return 0.05F;
    }

    protected float e() {
        return 0.5F;
    }

    protected float f() {
        return -20.0F;
    }
    
    public void shoot(double d0, double d1, double d2, float f, float f1) {
        float f2 = (float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        d0 /= (double) f2;
        d1 /= (double) f2;
        d2 /= (double) f2;
        d0 += 0.007499999832361937D * (double) f1;
        d1 += 0.007499999832361937D * (double) f1;
        d2 += 0.007499999832361937D * (double) f1;
        d0 *= (double) f;
        d1 *= (double) f;
        d2 *= (double) f;
        this.motX = d0;
        this.motY = d1;
        this.motZ = d2;
        final float f3 = (float)Math.sqrt(d0 * d0 + d2 * d2);

        this.lastYaw = this.yaw = (float) (Math.atan2(d0, d2) * 180.0D / 3.1415927410125732D);
        this.lastPitch = this.pitch = (float) (Math.atan2(d1, (double) f3) * 180.0D / 3.1415927410125732D);
        this.i = 0;
    }

    public void setPotionValue(int i) {
        if (this.item == null) {
            this.item = new ItemStack(Items.POTION, 1, 0);
        }
        this.item.setData(i);
    }

    public int getPotionValue() {
        if (this.item == null) {
            this.item = new ItemStack(Items.POTION, 1, 0);
        }
        return this.item.getData();
    }

    protected void hit(MovingObjectPosition movingobjectposition) {
        if (!this.world.isStatic) {
        	final List list = Items.POTION.g(this.item);

            // CraftBukkit - Call event even if no effects to apply
        	final AxisAlignedBB axisalignedbb = this.boundingBox.grow(4.0D, 2.0D, 4.0D);
            final List list1 = this.world.a(EntityLiving.class, axisalignedbb);
            // CraftBukkit
            final Map<LivingEntity, Double> affected = new HashMap<LivingEntity, Double>();

            for (EntityLiving entityliving : (List<EntityLiving>)list1) {
            	final double d0 = this.f(entityliving);

                if (d0 < 16.0D) {
                	double d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                    if (entityliving == movingobjectposition.entity) {
                        d1 = 1.0D;
                    }
                    // CraftBukkit start
                    affected.put((LivingEntity) entityliving.getBukkitEntity(), d1);
                }
            }
            org.bukkit.event.entity.PotionSplashEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callPotionSplashEvent(this, affected);
            if (!event.isCancelled() && list != null && !list.isEmpty()) { // do not process effects if there are no effects to process
                for (LivingEntity victim : event.getAffectedEntities()) {
                    if (!(victim instanceof CraftLivingEntity)) {
                        continue;
                    }

                    final EntityLiving entityliving = ((CraftLivingEntity) victim).getHandle();

                    // If entity is a player and the shooter is not null (so NPCs don't break?) and the player cannot see the shooter, skip.
                    if (entityliving instanceof EntityPlayer && (this.getShooter() != null && !((EntityPlayer) entityliving).getBukkitEntity().canSee(this.getShooter().getBukkitEntity()))) {
                        continue;
                    }

                    final double d1 = event.getIntensity(victim);
                    // CraftBukkit end

                    for (MobEffect mobeffect : (List<MobEffect>)list) {
                    	int i = mobeffect.getEffectId();

                        // CraftBukkit start - Abide by PVP settings - for players only!
                        if (!this.world.pvpMode && this.getShooter() instanceof EntityPlayer && entityliving instanceof EntityPlayer && entityliving != this.getShooter()) {
                            // Block SLOWER_MOVEMENT, SLOWER_DIG, HARM, BLINDNESS, HUNGER, WEAKNESS and POISON potions
                            if (i == 2 || i == 4 || i == 7 || i == 15 || i == 17 || i == 18 || i == 19)
                                continue;
                        }
                        // CraftBukkit end

                        if (MobEffectList.byId[i].isInstant()) {
                        	MobEffectList.byId[i].applyInstantEffect(this.getShooter(), entityliving, mobeffect.getAmplifier(), d1, this);
                        } else {
                        	final int j = (int) (d1 * (double) mobeffect.getDuration() + 0.5D);

                            if (j > 20) {
                                entityliving.addEffect(new MobEffect(i, j, mobeffect.getAmplifier()));
                            }
                        }
                    }
                }
            }
            if (getShooter() instanceof EntityHuman) {
            	this.world.a((EntityHuman)getShooter(), 2002, (int)Math.round(this.locX), (int)Math.round(this.locY), (int)Math.round(this.locZ), getPotionValue());
            } else {
            	this.world.triggerEffect(2002, (int)Math.round(this.locX), (int)Math.round(this.locY), (int)Math.round(this.locZ), getPotionValue());
            } 
            this.die();
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.hasKeyOfType("Potion", 10)) {
            this.item = ItemStack.createStack(nbttagcompound.getCompound("Potion"));
        } else {
            this.setPotionValue(nbttagcompound.getInt("potionValue"));
        }
        if (this.item == null) {
            this.die();
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        if (this.item != null) {
            nbttagcompound.set("Potion", this.item.save(new NBTTagCompound()));
        }
    }
    
    @Override
	public Packet createSpecificSpawnPacket() {
		return new PacketPlayOutSpawnEntity(this, 73, ((EntityPotion) this).getPotionValue());
	}

	@Override
	public void track(EntityTracker tracker) {
		tracker.addEntity(this, 64, 5, true);
	}
}
