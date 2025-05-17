package net.minecraft.server;

public class ItemFood extends Item {
	private final int b;
	private final float c;
	private final boolean d;
	private boolean m;
	private int n;
	private int o;
	private int p;
	private float q;

	public ItemFood(int paramInt, float paramFloat, boolean paramBoolean) {
		this.b = paramInt;
		this.d = paramBoolean;
		this.c = paramFloat;
		a(CreativeModeTab.h);
	}

	public ItemFood(int paramInt, boolean paramBoolean) {
		this(paramInt, 0.6F, paramBoolean);
	}

	@Override
	public ItemStack b(ItemStack paramItemStack, World paramWorld, EntityHuman paramEntityHuman) {
		paramItemStack.count--;
		paramEntityHuman.getFoodData().a(this, paramItemStack);
		paramWorld.makeSound(paramEntityHuman, "random.burp", 0.5F, paramWorld.random.nextFloat() * 0.1F + 0.9F);
		c(paramItemStack, paramWorld, paramEntityHuman);
		return paramItemStack;
	}

	protected void c(ItemStack paramItemStack, World paramWorld, EntityHuman paramEntityHuman) {
		if (!paramWorld.isStatic && this.n > 0 && paramWorld.random.nextFloat() < this.q)
			paramEntityHuman.addEffect(new MobEffect(this.n, this.o * 20, this.p));
	}

	@Override
	public int d_(ItemStack paramItemStack) {
		return 32;
	}

	@Override
	public EnumAnimation d(ItemStack paramItemStack) {
		return EnumAnimation.EAT;
	}

	@Override
	public ItemStack a(ItemStack paramItemStack, World paramWorld, EntityHuman paramEntityHuman) {
		if (paramEntityHuman.g(this.m))
			paramEntityHuman.a(paramItemStack, d_(paramItemStack));
		return paramItemStack;
	}

	public int getNutrition(ItemStack paramItemStack) {
		return this.b;
	}

	public float getSaturationModifier(ItemStack paramItemStack) {
		return this.c;
	}

	public boolean i() {
		return this.d;
	}

	public ItemFood a(int paramInt1, int paramInt2, int paramInt3, float paramFloat) {
		this.n = paramInt1;
		this.o = paramInt2;
		this.p = paramInt3;
		this.q = paramFloat;
		return this;
	}

	public ItemFood j() {
		this.m = true;
		return this;
	}
}