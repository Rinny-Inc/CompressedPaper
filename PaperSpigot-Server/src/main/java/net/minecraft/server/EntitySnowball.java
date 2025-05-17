package net.minecraft.server;

import com.sathonay.interfaces.IEntitySpecificSpawnPacket;

import io.noks.interfaces.ITrack;

public class EntitySnowball extends EntityProjectile implements IEntitySpecificSpawnPacket, ITrack {
	public EntitySnowball(World paramWorld) {
		super(paramWorld);
	}

	public EntitySnowball(World paramWorld, EntityLiving paramEntityLiving) {
		super(paramWorld, paramEntityLiving);
	}

	public EntitySnowball(World paramWorld, double paramDouble1, double paramDouble2, double paramDouble3) {
		super(paramWorld, paramDouble1, paramDouble2, paramDouble3);
	}

	protected void hit(MovingObjectPosition paramMovingObjectPosition) {
		if (paramMovingObjectPosition.entity != null) {
			byte b1 = 0;
			if (paramMovingObjectPosition.entity instanceof EntityBlaze) {
				b1 = 3;
			}
			paramMovingObjectPosition.entity.damageEntity(DamageSource.projectile(this, getShooter()), b1);
		}
		for (byte b = 0; b < 8; b++) {
			this.world.addParticle("snowballpoof", this.locX, this.locY, this.locZ, 0.0D, 0.0D, 0.0D);
		}
		if (!this.world.isStatic) {
			die();
		}
	}
	
	@Override
    public Packet createSpecificSpawnPacket() {
        return new PacketPlayOutSpawnEntity(this, 61);
    }
	
	@Override
	public void track(EntityTracker tracker) {
		tracker.addEntity(this, 64, 10, true);
	}
}
