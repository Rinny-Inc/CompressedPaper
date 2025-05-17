package net.minecraft.server;

public class EntityMinecartMobSpawner extends EntityMinecartAbstract {
	private final MobSpawnerAbstract a = new MobSpawnerMinecart(this);

	public EntityMinecartMobSpawner(World paramWorld) {
		super(paramWorld);
	}

	public EntityMinecartMobSpawner(World paramWorld, double paramDouble1, double paramDouble2, double paramDouble3) {
		super(paramWorld, paramDouble1, paramDouble2, paramDouble3);
	}

	public int m() {
		return 4;
	}

	public Block o() {
		return Blocks.MOB_SPAWNER;
	}

	protected void a(NBTTagCompound paramNBTTagCompound) {
		super.a(paramNBTTagCompound);
		this.a.a(paramNBTTagCompound);
	}

	protected void b(NBTTagCompound paramNBTTagCompound) {
		super.b(paramNBTTagCompound);
		this.a.b(paramNBTTagCompound);
	}

	public void tick() {
		super.tick();
		this.a.g();
	}
}
