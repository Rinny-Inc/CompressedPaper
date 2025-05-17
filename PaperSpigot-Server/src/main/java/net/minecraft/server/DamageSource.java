package net.minecraft.server;

public class DamageSource {
	public static DamageSource FIRE = (new DamageSource("inFire")).setExplosion();
	public static DamageSource BURN = (new DamageSource("onFire")).setIgnoreArmor().setExplosion();
	public static DamageSource LAVA = (new DamageSource("lava")).setExplosion();
	public static DamageSource STUCK = (new DamageSource("inWall")).setIgnoreArmor();
	public static DamageSource DROWN = (new DamageSource("drown")).setIgnoreArmor();
	public static DamageSource STARVE = (new DamageSource("starve")).setIgnoreArmor().m();
	public static DamageSource CACTUS = new DamageSource("cactus");
	public static DamageSource FALL = (new DamageSource("fall")).setIgnoreArmor();
	public static DamageSource OUT_OF_WORLD = (new DamageSource("outOfWorld")).setIgnoreArmor().l();
	public static DamageSource GENERIC = (new DamageSource("generic")).setIgnoreArmor();
	public static DamageSource MAGIC = (new DamageSource("magic")).setIgnoreArmor().setMagic();
	public static DamageSource WITHER = (new DamageSource("wither")).setIgnoreArmor();
	public static DamageSource ANVIL = new DamageSource("anvil");
	private boolean p;
	private boolean q;
	private boolean r;
	public static DamageSource FALLING_BLOCK = new DamageSource("fallingBlock");
	private float s;
	private boolean t;

	public static DamageSource mobAttack(EntityLiving paramEntityLiving) {
		return new EntityDamageSource("mob", paramEntityLiving);
	}

	private boolean u;
	private boolean v;
	private boolean w;
	private boolean x;
	public String translationIndex;

	public static DamageSource playerAttack(EntityHuman paramEntityHuman) {
		return new EntityDamageSource("player", paramEntityHuman);
	}

	public static DamageSource arrow(EntityArrow paramEntityArrow, Entity paramEntity) {
		return (new EntityDamageSourceIndirect("arrow", paramEntityArrow, paramEntity)).b();
	}

	public static DamageSource fireball(EntityFireball paramEntityFireball, Entity paramEntity) {
		if (paramEntity == null) {
			return (new EntityDamageSourceIndirect("onFire", paramEntityFireball, paramEntityFireball)).setExplosion().b();
		}
		return (new EntityDamageSourceIndirect("fireball", paramEntityFireball, paramEntity)).setExplosion().b();
	}

	public static DamageSource projectile(Entity paramEntity1, Entity paramEntity2) {
		return (new EntityDamageSourceIndirect("thrown", paramEntity1, paramEntity2)).b();
	}

	public static DamageSource b(Entity paramEntity1, Entity paramEntity2) {
		return (new EntityDamageSourceIndirect("indirectMagic", paramEntity1, paramEntity2)).setIgnoreArmor().setMagic();
	}

	public static DamageSource a(Entity paramEntity) {
		return (new EntityDamageSource("thorns", paramEntity)).setMagic();
	}

	public static DamageSource explosion(Explosion paramExplosion) {
		if (paramExplosion != null && paramExplosion.c() != null) {
			return (new EntityDamageSource("explosion.player", paramExplosion.c())).q().d();
		}
		return (new DamageSource("explosion")).q().d();
	}

	public boolean a() {
		return this.u;
	}

	public DamageSource b() {
		this.u = true;
		return this;
	}

	public boolean isExplosion() {
		return this.x;
	}

	public DamageSource d() {
		this.x = true;
		return this;
	}

	public boolean ignoresArmor() {
		return this.p;
	}

	public float getExhaustionCost() {
		return this.s;
	}

	public boolean ignoresInvulnerability() {
		return this.q;
	}

	public boolean isStarvation() {
		return this.r;
	}

	protected DamageSource(String paramString) {
		this.s = 0.3F;
		this.translationIndex = paramString;
	}

	public Entity i() {
		return getEntity();
	}

	public Entity getEntity() {
		return null;
	}

	protected DamageSource setIgnoreArmor() {
		this.p = true;

		this.s = 0.0F;
		return this;
	}

	protected DamageSource l() {
		this.q = true;
		return this;
	}

	protected DamageSource m() {
		this.r = true;

		this.s = 0.0F;
		return this;
	}

	protected DamageSource setExplosion() {
		this.t = true;
		return this;
	}

	public IChatBaseComponent getLocalizedDeathMessage(EntityLiving paramEntityLiving) {
		EntityLiving entityLiving = paramEntityLiving.aX();
		String str1 = "death.attack." + this.translationIndex;
		String str2 = str1 + ".player";

		if (entityLiving != null && LocaleI18n.c(str2)) {
			return new ChatMessage(str2, new Object[] { paramEntityLiving.getScoreboardDisplayName(), entityLiving.getScoreboardDisplayName() });
		}
		return new ChatMessage(str1, new Object[] { paramEntityLiving.getScoreboardDisplayName() });
	}

	public boolean o() {
		return this.t;
	}

	public String p() {
		return this.translationIndex;
	}

	public DamageSource q() {
		this.v = true;
		return this;
	}

	public boolean r() {
		return this.v;
	}

	public boolean isMagic() {
		return this.w;
	}

	public DamageSource setMagic() {
		this.w = true;
		return this;
	}
}
