package net.minecraft.server;

import com.sathonay.interfaces.IEntitySpecificSpawnPacket;

import io.noks.interfaces.ITrack;

public class EntityEnderSignal extends Entity implements IEntitySpecificSpawnPacket, ITrack {
	private double a;
	private double b;
	private double c;
	private int d;
	private boolean e;

	public EntityEnderSignal(World paramWorld) {
		super(paramWorld);
		a(0.25F, 0.25F);
	}

	protected void c() {
	}

	public EntityEnderSignal(World paramWorld, double paramDouble1, double paramDouble2, double paramDouble3) {
		super(paramWorld);
		this.d = 0;
		a(0.25F, 0.25F);
		setPosition(paramDouble1, paramDouble2, paramDouble3);
		this.height = 0.0F;
	}

	public void a(double paramDouble1, int paramInt, double paramDouble2) {
		double d1 = paramDouble1 - this.locX, d2 = paramDouble2 - this.locZ;
		float f = MathHelper.sqrt(d1 * d1 + d2 * d2);
		if (f > 12.0F) {
			this.a = this.locX + d1 / f * 12.0D;
			this.c = this.locZ + d2 / f * 12.0D;
			this.b = this.locY + 8.0D;
		} else {
			this.a = paramDouble1;
			this.b = paramInt;
			this.c = paramDouble2;
		}
		this.d = 0;
		this.e = (this.random.nextInt(5) > 0);
	}

	public void tick() {
		this.S = this.locX;
		this.T = this.locY;
		this.U = this.locZ;
		super.tick();
		this.locX += this.motX;
		this.locY += this.motY;
		this.locZ += this.motZ;
		float f1 = MathHelper.sqrt(this.motX * this.motX + this.motZ * this.motZ);
		this.yaw = (float) (Math.atan2(this.motX, this.motZ) * 180.0D / 3.1415927410125732D);
		this.pitch = (float) (Math.atan2(this.motY, f1) * 180.0D / 3.1415927410125732D);
		while (this.pitch - this.lastPitch < -180.0F)
			this.lastPitch -= 360.0F;
		while (this.pitch - this.lastPitch >= 180.0F)
			this.lastPitch += 360.0F;
		while (this.yaw - this.lastYaw < -180.0F)
			this.lastYaw -= 360.0F;
		while (this.yaw - this.lastYaw >= 180.0F)
			this.lastYaw += 360.0F;
		this.pitch = this.lastPitch + (this.pitch - this.lastPitch) * 0.2F;
		this.yaw = this.lastYaw + (this.yaw - this.lastYaw) * 0.2F;
		if (!this.world.isStatic) {
			double d1 = this.a - this.locX, d2 = this.c - this.locZ;
			float f3 = (float) Math.sqrt(d1 * d1 + d2 * d2);
			float f4 = (float) Math.atan2(d2, d1);
			double d3 = f1 + (f3 - f1) * 0.0025D;
			if (f3 < 1.0F) {
				d3 *= 0.8D;
				this.motY *= 0.8D;
			}
			this.motX = Math.cos(f4) * d3;
			this.motZ = Math.sin(f4) * d3;
			if (this.locY < this.b) {
				this.motY += (1.0D - this.motY) * 0.014999999664723873D;
			} else {
				this.motY += (-1.0D - this.motY) * 0.014999999664723873D;
			}
		}
		float f2 = 0.25F;
		if (M()) {
			for (byte b = 0; b < 4; b++)
				this.world.addParticle("bubble", this.locX - this.motX * f2, this.locY - this.motY * f2,
						this.locZ - this.motZ * f2, this.motX, this.motY, this.motZ);
		} else {
			this.world.addParticle("portal", this.locX - this.motX * f2 + this.random.nextDouble() * 0.6D - 0.3D,
					this.locY - this.motY * f2 - 0.5D,
					this.locZ - this.motZ * f2 + this.random.nextDouble() * 0.6D - 0.3D, this.motX, this.motY,
					this.motZ);
		}
		if (!this.world.isStatic) {
			setPosition(this.locX, this.locY, this.locZ);
			this.d++;
			if (this.d > 80 && !this.world.isStatic) {
				die();
				if (this.e) {
					this.world.addEntity(new EntityItem(this.world, this.locX, this.locY, this.locZ,
							new ItemStack(Items.EYE_OF_ENDER)));
				} else {
					this.world.triggerEffect(2003, (int) Math.round(this.locX), (int) Math.round(this.locY),
							(int) Math.round(this.locZ), 0);
				}
			}
		}
	}

	public void b(NBTTagCompound paramNBTTagCompound) {
	}

	public void a(NBTTagCompound paramNBTTagCompound) {
	}

	public float d(float paramFloat) {
		return 1.0F;
	}

	public boolean av() {
		return false;
	}
	
	@Override
    public Packet createSpecificSpawnPacket() {
        return new PacketPlayOutSpawnEntity(this, 72);
    }

	@Override
	public void track(EntityTracker tracker) {
		tracker.addEntity(this, 64, 4, true);
	}
}