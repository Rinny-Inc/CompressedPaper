package net.minecraft.server;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

public abstract class EntityProjectile extends Entity implements IProjectile {
    private int blockX = -1;
    private int blockY = -1;
    private int blockZ = -1;
    private Block inBlockId;
    protected boolean inGround;
    public int shake;
    public EntityLiving shooter; // CraftBukkit - private -> public
    public @Nullable String shooterName; // CraftBukkit - private -> public
    private @Nullable UUID shooterUUID;
    protected int i;
    private int at;

    public EntityProjectile(World world) {
        super(world);
        this.a(0.25F, 0.25F);
    }

    protected void c() {}

    public EntityProjectile(World world, EntityLiving entityliving) {
        super(world);
        this.shooter = entityliving;
        this.projectileSource = (org.bukkit.entity.LivingEntity) entityliving.getBukkitEntity(); // CraftBukkit
        this.a(0.25F, 0.25F);
        this.setPositionRotation(entityliving.locX, entityliving.locY + (double) entityliving.getHeadHeight(), entityliving.locZ, entityliving.yaw, entityliving.pitch);
        
        final float radians = this.yaw / 180.0F * 3.1415927F; // Rinny - calculate yaw only once
        this.locX -= (double) (MathHelper.cos(radians) * 0.16F);
        this.locY -= 0.10000000149011612D;
        this.locZ -= (double) (MathHelper.sin(radians) * 0.16F);
        this.setPosition(this.locX, this.locY, this.locZ);
        this.height = 0.0F;
        final float f = 0.4F;
        
        final float p = this.pitch / 180.0F * 3.1415927F; // Rinny - calculate pitch only once
        this.motX = (double) (-MathHelper.sin(radians) * MathHelper.cos(p) * f);
        this.motZ = (double) (MathHelper.cos(radians) * MathHelper.cos(p) * f);
        this.motY = (double) (-MathHelper.sin((this.pitch + this.f()) / 180.0F * 3.1415927F) * f);
        this.shoot(this.motX, this.motY, this.motZ, this.e(), 1.0F);
    }

    public EntityProjectile(World world, double d0, double d1, double d2) {
        super(world);
        this.i = 0;
        this.a(0.25F, 0.25F);
        this.setPosition(d0, d1, d2);
        this.height = 0.0F;
    }

    protected float e() {
        return 1.5F;
    }

    protected float f() {
        return 0.0F;
    }

    public void shoot(double d0, double d1, double d2, float f, float f1) {
    	final float f2 = (float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        d0 /= (double) f2;
        d1 /= (double) f2;
        d2 /= (double) f2;
        d0 += this.random.nextGaussian() * 0.007499999832361937D * (double) f1;
        d1 += this.random.nextGaussian() * 0.007499999832361937D * (double) f1;
        d2 += this.random.nextGaussian() * 0.007499999832361937D * (double) f1;
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

    public void tick() {
        this.S = this.locX;
        this.T = this.locY;
        this.U = this.locZ;
        super.tick();
        if (this.shake > 0) {
            --this.shake;
        }

        if (this.inGround) {
            if (this.world.getType(this.blockX, this.blockY, this.blockZ) == this.inBlockId) {
                ++this.i;
                if (this.i == 1200) {
                    this.die();
                }
                return;
            }

            this.inGround = false;
            this.motX *= (double) (this.random.nextFloat() * 0.2F);
            this.motY *= (double) (this.random.nextFloat() * 0.2F);
            this.motZ *= (double) (this.random.nextFloat() * 0.2F);
            this.i = 0;
            this.at = 0;
        } else {
        	++this.at;
        }

        Vec3D vec3d = Vec3D.a(this.locX, this.locY, this.locZ);
        Vec3D vec3d1 = Vec3D.a(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
        MovingObjectPosition movingobjectposition = this.projectileCollision(vec3d, vec3d1);

        vec3d = Vec3D.a(this.locX, this.locY, this.locZ);
        vec3d1 = Vec3D.a(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
        if (movingobjectposition != null) {
            vec3d1 = Vec3D.a(movingobjectposition.pos.a, movingobjectposition.pos.b, movingobjectposition.pos.c);
        }

        if (!this.world.isStatic) {
            Entity entity = null;
            final List<Entity> list = this.world.getEntities(this, this.boundingBox.a(this.motX, this.motY, this.motZ).grow(1.0D, 1.0D, 1.0D));
            double closestDistanceSquared = Double.MAX_VALUE;
            final EntityLiving entityliving = this.getShooter();
            float f = 0.3F; // Rinny - don't init useless stuff

            Iterator<Entity> iterator = list.iterator();
            while (iterator.hasNext()) {
                Entity entity1 = iterator.next();

                if (entity1.R() && (entity1 != entityliving || this.at >= 5)) {
                    AxisAlignedBB axisalignedbb = entity1.boundingBox.grow((double) f, (double) f, (double) f);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.a(vec3d, vec3d1);
                    
                    if (movingobjectposition1 != null) {
                        double distanceSquared = vec3d.distanceSquared(movingobjectposition1.pos); // CraftBukkit - distance efficiency

                        if (distanceSquared < closestDistanceSquared) {
                            entity = entity1;
                            closestDistanceSquared = distanceSquared;
                        }
                    }
                }
            }

            if (entity != null) {
                movingobjectposition = new MovingObjectPosition(entity);
            }
        }

        // PaperSpigot start - Allow projectiles to fly through players the shooter can't see
        if (movingobjectposition != null && movingobjectposition.entity instanceof EntityPlayer && shooter != null && shooter instanceof EntityPlayer) {
            if (!((EntityPlayer) shooter).getBukkitEntity().canSee(((EntityPlayer) movingobjectposition.entity).getBukkitEntity())) {
                movingobjectposition = null;
            }
        }
        // PaperSpigot end
        collide:
        if (movingobjectposition != null) {
            if (this instanceof EntityEnderPearl && world.paperSpigotConfig.enderpearlCollision) {
            	final Block thisBlock = this.world.getType(movingobjectposition.b, movingobjectposition.c, movingobjectposition.d);
                if (thisBlock == Blocks.FENCE_GATE) {
                    if (BlockFenceGate.b(this.world.getData(movingobjectposition.b, movingobjectposition.c, movingobjectposition.d))) {
                        break collide;
                    }
                } else if (thisBlock == Blocks.WEB || thisBlock == Blocks.TRIPWIRE) {
                    break collide;
                }
            }
            if (movingobjectposition.type == EnumMovingObjectType.BLOCK && this.world.getType(movingobjectposition.b, movingobjectposition.c, movingobjectposition.d) == Blocks.PORTAL) {
                this.ah();
            } else {
                this.hit(movingobjectposition);
                // CraftBukkit start
                if (this.dead) {
                    org.bukkit.craftbukkit.event.CraftEventFactory.callProjectileHitEvent(this, movingobjectposition);
                }
                return; // Rinny - execute less code - TODO: CHECK IF IT DOES SHIT!!!
                // CraftBukkit end
            }
        }

        this.locX += this.motX;
        this.locY += this.motY;
        this.locZ += this.motZ;
        final float f1 = MathHelper.sqrt(this.motX * this.motX + this.motZ * this.motZ);

        this.yaw = (float) (Math.atan2(this.motX, this.motZ) * 180.0D / 3.1415927410125732D);
        for (this.pitch = (float) (Math.atan2(this.motY, (double) f1) * 180.0D / 3.1415927410125732D); this.pitch - this.lastPitch < -180.0F; this.lastPitch -= 360.0F) {}
        while (this.pitch - this.lastPitch >= 180.0F) {
            this.lastPitch += 360.0F;
        }
        while (this.yaw - this.lastYaw < -180.0F) {
            this.lastYaw -= 360.0F;
        }
        while (this.yaw - this.lastYaw >= 180.0F) {
            this.lastYaw += 360.0F;
        }

        this.pitch = this.lastPitch + (this.pitch - this.lastPitch) * 0.2F;
        this.yaw = this.lastYaw + (this.yaw - this.lastYaw) * 0.2F;
        float f2 = 0.99F;
        final float f3 = this.i();

        if (this.M()) {
            for (int j = 0; j < 4; ++j) {
                float f4 = 0.25F;

                this.world.addParticle("bubble", this.locX - this.motX * (double) f4, this.locY - this.motY * (double) f4, this.locZ - this.motZ * (double) f4, this.motX, this.motY, this.motZ);
            }

            f2 = 0.8F;
        }

        this.motX *= (double) f2;
        this.motY *= (double) f2;
        this.motZ *= (double) f2;
        this.motY -= (double) f3;
        this.setPosition(this.locX, this.locY, this.locZ);
    }

    protected float i() {
        return 0.03F;
    }

    protected abstract void hit(MovingObjectPosition movingobjectposition);

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setShort("xTile", (short) this.blockX);
        nbttagcompound.setShort("yTile", (short) this.blockY);
        nbttagcompound.setShort("zTile", (short) this.blockZ);
        nbttagcompound.setByte("inTile", (byte) Block.getId(this.inBlockId));
        nbttagcompound.setByte("shake", (byte) this.shake);
        nbttagcompound.setByte("inGround", (byte) (this.inGround ? 1 : 0));
        if ((this.shooterName == null || this.shooterName.length() == 0) && this.shooter != null && this.shooter instanceof EntityHuman) {
            this.shooterName = this.shooter.getName();
            this.shooterUUID = this.shooter.getUniqueID();
        }

        nbttagcompound.setString("ownerName", this.shooterName == null ? "" : this.shooterName);
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.blockX = nbttagcompound.getShort("xTile");
        this.blockY = nbttagcompound.getShort("yTile");
        this.blockZ = nbttagcompound.getShort("zTile");
        this.inBlockId = Block.getById(nbttagcompound.getByte("inTile") & 255);
        this.shake = nbttagcompound.getByte("shake") & 255;
        this.inGround = nbttagcompound.getByte("inGround") == 1;
        this.shooterName = nbttagcompound.getString("ownerName");
        if (this.shooterName != null && this.shooterName.length() == 0) {
            this.shooterName = null;
            this.shooterUUID = null;
        }
    }

    public EntityLiving getShooter() {
        if (this.shooter == null && this.shooterUUID != null) {
            this.shooter = this.world.a(this.shooterUUID);
        }
        return this.shooter;
    }
    
	protected MovingObjectPosition projectileCollision(Vec3D vec3d, Vec3D vec3d1) {
		return this.world.a(vec3d, vec3d1);
	}
}
