package net.minecraft.server;

public class ItemEnderPearl extends Item {
	public ItemEnderPearl() {
		this.maxStackSize = 16;
		a(CreativeModeTab.f);
	}

	public ItemStack a(ItemStack paramItemStack, World paramWorld, EntityHuman paramEntityHuman) {
		if (paramEntityHuman.abilities.canInstantlyBuild)
			return paramItemStack;
		paramItemStack.count--;
		paramWorld.makeSound(paramEntityHuman, "random.bow", 0.5F, 0.4F / (g.nextFloat() * 0.4F + 0.8F));
		if (!paramWorld.isStatic)
			paramWorld.addEntity(new EntityEnderPearl(paramWorld, paramEntityHuman));
		return paramItemStack;
	}
}
