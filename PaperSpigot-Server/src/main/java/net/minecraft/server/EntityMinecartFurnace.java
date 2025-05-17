package net.minecraft.server;

public class EntityMinecartFurnace extends EntityMinecartAbstract {
	private int c;

	public double a;

	public double b;

	public EntityMinecartFurnace(World paramWorld) {
		super(paramWorld);
	}

	public EntityMinecartFurnace(World paramWorld, double paramDouble1, double paramDouble2, double paramDouble3) {
		super(paramWorld, paramDouble1, paramDouble2, paramDouble3);
	}

	public int m() {
		return 2;
	}

	protected void c() {
		super.c();
		this.datawatcher.a(16, new Byte((byte) 0));
	}

	public void tick() {
		super.tick();
		if (this.c > 0)
			this.c--;
		if (this.c <= 0)
			this.a = this.b = 0.0D;
		f((this.c > 0));
		if (e() && this.random.nextInt(4) == 0)
			this.world.addParticle("largesmoke", this.locX, this.locY + 0.8D, this.locZ, 0.0D, 0.0D, 0.0D);
	}

	public void a(DamageSource paramDamageSource) {
		super.a(paramDamageSource);
		if (!paramDamageSource.isExplosion())
			a(new ItemStack(Blocks.FURNACE, 1), 0.0F);
	}

	protected void a(int paramInt1, int paramInt2, int paramInt3, double paramDouble1, double paramDouble2,
			Block paramBlock, int paramInt4) {
		super.a(paramInt1, paramInt2, paramInt3, paramDouble1, paramDouble2, paramBlock, paramInt4);
		double d = this.a * this.a + this.b * this.b;
		if (d > 1.0E-4D && this.motX * this.motX + this.motZ * this.motZ > 0.001D) {
			d = MathHelper.sqrt(d);
			this.a /= d;
			this.b /= d;
			if (this.a * this.motX + this.b * this.motZ < 0.0D) {
				this.a = 0.0D;
				this.b = 0.0D;
			} else {
				this.a = this.motX;
				this.b = this.motZ;
			}
		}
	}

	protected void i() {
		double d = this.a * this.a + this.b * this.b;
		if (d > 1.0E-4D) {
			d = MathHelper.sqrt(d);
			this.a /= d;
			this.b /= d;
			double d1 = 0.05D;
			this.motX *= 0.800000011920929D;
			this.motY *= 0.0D;
			this.motZ *= 0.800000011920929D;
			this.motX += this.a * d1;
			this.motZ += this.b * d1;
		} else {
			this.motX *= 0.9800000190734863D;
			this.motY *= 0.0D;
			this.motZ *= 0.9800000190734863D;
		}
		super.i();
	}

	public boolean c(EntityHuman paramEntityHuman) {
		ItemStack itemStack = paramEntityHuman.inventory.getItemInHand();
		if (itemStack != null && itemStack.getItem() == Items.COAL) {
			if (!paramEntityHuman.abilities.canInstantlyBuild && --itemStack.count == 0)
				paramEntityHuman.inventory.setItem(paramEntityHuman.inventory.itemInHandIndex, null);
			this.c += 3600;
		}
		this.a = this.locX - paramEntityHuman.locX;
		this.b = this.locZ - paramEntityHuman.locZ;
		return true;
	}

	protected void b(NBTTagCompound paramNBTTagCompound) {
		super.b(paramNBTTagCompound);
		paramNBTTagCompound.setDouble("PushX", this.a);
		paramNBTTagCompound.setDouble("PushZ", this.b);
		paramNBTTagCompound.setShort("Fuel", (short) this.c);
	}

	protected void a(NBTTagCompound paramNBTTagCompound) {
		super.a(paramNBTTagCompound);
		this.a = paramNBTTagCompound.getDouble("PushX");
		this.b = paramNBTTagCompound.getDouble("PushZ");
		this.c = paramNBTTagCompound.getShort("Fuel");
	}

	protected boolean e() {
		return ((this.datawatcher.getByte(16) & 0x1) != 0);
	}

	protected void f(boolean paramBoolean) {
		if (paramBoolean) {
			this.datawatcher.watch(16, Byte.valueOf((byte) (this.datawatcher.getByte(16) | 0x1)));
		} else {
			this.datawatcher.watch(16, Byte.valueOf((byte) (this.datawatcher.getByte(16) & 0xFFFFFFFE)));
		}
	}

	public Block o() {
		return Blocks.BURNING_FURNACE;
	}

	public int q() {
		return 2;
	}
}