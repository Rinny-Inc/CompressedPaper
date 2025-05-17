package net.minecraft.server;

import java.util.List;

public class EntityMinecartHopper extends EntityMinecartContainer implements IHopper {
	private boolean a = true;

	private int b = -1;

	public EntityMinecartHopper(World paramWorld) {
		super(paramWorld);
	}

	public EntityMinecartHopper(World paramWorld, double paramDouble1, double paramDouble2, double paramDouble3) {
		super(paramWorld, paramDouble1, paramDouble2, paramDouble3);
	}

	public int m() {
		return 5;
	}

	public Block o() {
		return Blocks.HOPPER;
	}

	public int s() {
		return 1;
	}

	public int getSize() {
		return 5;
	}

	public boolean c(EntityHuman paramEntityHuman) {
		if (!this.world.isStatic)
			paramEntityHuman.openMinecartHopper(this);
		return true;
	}

	public void a(int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean) {
		boolean bool = !paramBoolean;
		if (bool != v())
			f(bool);
	}

	public boolean v() {
		return this.a;
	}

	public void f(boolean paramBoolean) {
		this.a = paramBoolean;
	}

	public World getWorld() {
		return this.world;
	}

	public double x() {
		return this.locX;
	}

	public double aD() {
		return this.locY;
	}

	public double aE() {
		return this.locZ;
	}

	public void tick() {
		super.tick();
		if (!this.world.isStatic && isAlive() && v()) {
			this.b--;
			if (!aG()) {
				n(0);
				if (aF()) {
					n(4);
					update();
				}
			}
		}
	}

	public boolean aF() {
		if (TileEntityHopper.suckInItems(this))
			return true;
		List<Entity> list = this.world.a(EntityItem.class, this.boundingBox.grow(0.25D, 0.0D, 0.25D),
				IEntitySelector.a);
		if (!list.isEmpty()) // Rinny - replaced size > 0 by isEmpty
			TileEntityHopper.addEntityItem(this, (EntityItem) list.get(0));
		return false;
	}

	public void a(DamageSource paramDamageSource) {
		super.a(paramDamageSource);
		a(Item.getItemOf(Blocks.HOPPER), 1, 0.0F);
	}

	protected void b(NBTTagCompound paramNBTTagCompound) {
		super.b(paramNBTTagCompound);
		paramNBTTagCompound.setInt("TransferCooldown", this.b);
	}

	protected void a(NBTTagCompound paramNBTTagCompound) {
		super.a(paramNBTTagCompound);
		this.b = paramNBTTagCompound.getInt("TransferCooldown");
	}

	public void n(int paramInt) {
		this.b = paramInt;
	}

	public boolean aG() {
		return (this.b > 0);
	}
}