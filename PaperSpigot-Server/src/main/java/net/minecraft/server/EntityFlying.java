package net.minecraft.server;

public abstract class EntityFlying extends EntityInsentient {
	public EntityFlying(World paramWorld) {
		super(paramWorld);
	}

	protected void b(float paramFloat) {}

	protected void a(double paramDouble, boolean paramBoolean) {}

	public void e(float paramFloat1, float paramFloat2) {
		if (M()) {
			a(paramFloat1, paramFloat2, 0.02F);
			move(this.motX, this.motY, this.motZ);
			this.motX *= 0.800000011920929D;
			this.motY *= 0.800000011920929D;
			this.motZ *= 0.800000011920929D;
		} else if (P()) {
			a(paramFloat1, paramFloat2, 0.02F);
			move(this.motX, this.motY, this.motZ);
			this.motX *= 0.5D;
			this.motY *= 0.5D;
			this.motZ *= 0.5D;
		} else {
			float f1 = 0.91F;
			if (this.onGround)
				f1 = (this.world.getType(MathHelper.floor(this.locX), MathHelper.floor(this.boundingBox.b) - 1, MathHelper.floor(this.locZ))).frictionFactor * 0.91F;
			float f2 = 0.16277136F / f1 * f1 * f1;
			a(paramFloat1, paramFloat2, this.onGround ? (0.1F * f2) : 0.02F);
			f1 = 0.91F;
			if (this.onGround)
				f1 = (this.world.getType(MathHelper.floor(this.locX), MathHelper.floor(this.boundingBox.b) - 1, MathHelper.floor(this.locZ))).frictionFactor * 0.91F;
			move(this.motX, this.motY, this.motZ);
			this.motX *= f1;
			this.motY *= f1;
			this.motZ *= f1;
		}
		this.aE = this.aF;
		double d1 = this.locX - this.lastX;
		double d2 = this.locZ - this.lastZ;
		float f = MathHelper.sqrt(d1 * d1 + d2 * d2) * 4.0F;
		if (f > 1.0F)
			f = 1.0F;
		this.aF += (f - this.aF) * 0.4F;
		this.aG += this.aF;
	}

	public boolean h_() {
		return false;
	}
}
