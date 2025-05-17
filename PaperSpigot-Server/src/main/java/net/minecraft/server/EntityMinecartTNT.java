package net.minecraft.server;

public class EntityMinecartTNT extends EntityMinecartAbstract {
	private int fuse = -1;

	public EntityMinecartTNT(World paramWorld) {
		super(paramWorld);
	}

	public EntityMinecartTNT(World paramWorld, double paramDouble1, double paramDouble2, double paramDouble3) {
		super(paramWorld, paramDouble1, paramDouble2, paramDouble3);
	}

	public int m() {
		return 3;
	}

	public Block o() {
		return Blocks.TNT;
	}

	public void tick() {
		super.tick();
		if (this.fuse > 0) {
			this.fuse--;
			this.world.addParticle("smoke", this.locX, this.locY + 0.5D, this.locZ, 0.0D, 0.0D, 0.0D);
		} else if (this.fuse == 0) {
			c(this.motX * this.motX + this.motZ * this.motZ);
		}
		if (this.positionChanged) {
			double d = this.motX * this.motX + this.motZ * this.motZ;
			if (d >= 0.009999999776482582D)
				c(d);
		}
	}

	public void a(DamageSource paramDamageSource) {
		super.a(paramDamageSource);
		double d = this.motX * this.motX + this.motZ * this.motZ;
		if (!paramDamageSource.isExplosion())
			a(new ItemStack(Blocks.TNT, 1), 0.0F);
		if (paramDamageSource.o() || paramDamageSource.isExplosion() || d >= 0.009999999776482582D)
			c(d);
	}

	protected void c(double paramDouble) {
		if (!this.world.isStatic) {
			double d = Math.sqrt(paramDouble);
			if (d > 5.0D)
				d = 5.0D;
			this.world.explode(this, this.locX, this.locY, this.locZ,
					(float) (4.0D + this.random.nextDouble() * 1.5D * d), true);
			die();
		}
	}

	protected void b(float paramFloat) {
		if (paramFloat >= 3.0F) {
			float f = paramFloat / 10.0F;
			c((f * f));
		}
		super.b(paramFloat);
	}

	public void a(int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean) {
		if (paramBoolean && this.fuse < 0)
			e();
	}

	public void e() {
		this.fuse = 80;
		if (!this.world.isStatic) {
			this.world.broadcastEntityEffect(this, (byte) 10);
			this.world.makeSound(this, "game.tnt.primed", 1.0F, 1.0F);
		}
	}

	public boolean v() {
		return (this.fuse > -1);
	}

	public float a(Explosion paramExplosion, World paramWorld, int paramInt1, int paramInt2, int paramInt3,
			Block paramBlock) {
		if (v() && (BlockMinecartTrackAbstract.a(paramBlock)
				|| BlockMinecartTrackAbstract.b_(paramWorld, paramInt1, paramInt2 + 1, paramInt3)))
			return 0.0F;
		return super.a(paramExplosion, paramWorld, paramInt1, paramInt2, paramInt3, paramBlock);
	}

	public boolean a(Explosion paramExplosion, World paramWorld, int paramInt1, int paramInt2, int paramInt3,
			Block paramBlock, float paramFloat) {
		if (v() && (BlockMinecartTrackAbstract.a(paramBlock)
				|| BlockMinecartTrackAbstract.b_(paramWorld, paramInt1, paramInt2 + 1, paramInt3)))
			return false;
		return super.a(paramExplosion, paramWorld, paramInt1, paramInt2, paramInt3, paramBlock, paramFloat);
	}

	protected void a(NBTTagCompound paramNBTTagCompound) {
		super.a(paramNBTTagCompound);
		if (paramNBTTagCompound.hasKeyOfType("TNTFuse", 99))
			this.fuse = paramNBTTagCompound.getInt("TNTFuse");
	}

	protected void b(NBTTagCompound paramNBTTagCompound) {
		super.b(paramNBTTagCompound);
		paramNBTTagCompound.setInt("TNTFuse", this.fuse);
	}
}