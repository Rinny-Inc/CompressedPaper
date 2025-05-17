package net.minecraft.server;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.com.google.common.collect.Lists;
import net.minecraft.util.com.google.common.collect.Maps;

public class ItemPotion extends Item {
	private Map a;
	private static final Map b = new LinkedHashMap();

	public ItemPotion() {
		this.a = Maps.newHashMap();
		e(1);
		a(true);
		setMaxDurability(0);
		a(CreativeModeTab.k);
	}

	public List g(ItemStack paramItemStack) {
		if (!paramItemStack.hasTag() || !paramItemStack.getTag().hasKeyOfType("CustomPotionEffects", 9)) {
			List list = (List) this.a.get(Integer.valueOf(paramItemStack.getData()));

			if (list == null) {
				list = PotionBrewer.getEffects(paramItemStack.getData(), false);
				this.a.put(Integer.valueOf(paramItemStack.getData()), list);
			}

			return list;
		}
		final List<MobEffect> arrayList = Lists.newArrayList();
		final NBTTagList nBTTagList = paramItemStack.getTag().getList("CustomPotionEffects", 10);

		for (byte b1 = 0; b1 < nBTTagList.size(); b1++) {
			final NBTTagCompound nBTTagCompound = nBTTagList.get(b1);
			final MobEffect mobEffect = MobEffect.b(nBTTagCompound);
			if (mobEffect != null) {
				arrayList.add(mobEffect);
			}
		}

		return arrayList;
	}

	public List c(int paramInt) {
		List list = (List) this.a.get(Integer.valueOf(paramInt));
		if (list == null) {
			list = PotionBrewer.getEffects(paramInt, false);
			this.a.put(Integer.valueOf(paramInt), list);
		}
		return list;
	}

	public ItemStack b(ItemStack paramItemStack, World paramWorld, EntityHuman paramEntityHuman) {
		if (!paramEntityHuman.abilities.canInstantlyBuild)
			paramItemStack.count--;

		if (!paramWorld.isStatic) {
			final List<MobEffect> list = g(paramItemStack);
			if (list != null) {
				for (MobEffect mobEffect : list) {
					paramEntityHuman.addEffect(new MobEffect(mobEffect));
				}
			}
		}
		if (!paramEntityHuman.abilities.canInstantlyBuild) {
			if (paramItemStack.count <= 0) {
				return new ItemStack(Items.GLASS_BOTTLE);
			}
			paramEntityHuman.inventory.pickup(new ItemStack(Items.GLASS_BOTTLE));
		}

		return paramItemStack;
	}

	public int d_(ItemStack paramItemStack) {
		return 32;
	}

	public EnumAnimation d(ItemStack paramItemStack) {
		return EnumAnimation.DRINK;
	}

	public ItemStack a(ItemStack paramItemStack, World paramWorld, EntityHuman paramEntityHuman) {
		if (g(paramItemStack.getData())) {
			if (!paramEntityHuman.abilities.canInstantlyBuild) paramItemStack.count--;
			paramWorld.makeSound(paramEntityHuman, "random.bow", 0.5F, 0.4F / (g.nextFloat() * 0.4F + 0.8F));
			if (!paramWorld.isStatic) paramWorld.addEntity(new EntityPotion(paramWorld, paramEntityHuman, paramItemStack));
			return paramItemStack;
		}
		paramEntityHuman.a(paramItemStack, d_(paramItemStack));
		return paramItemStack;
	}

	public boolean interactWith(ItemStack paramItemStack, EntityHuman paramEntityHuman, World paramWorld, int paramInt1, int paramInt2, int paramInt3, int paramInt4, float paramFloat1, float paramFloat2, float paramFloat3) {
		return false;
	}

	public static boolean g(int paramInt) {
		return ((paramInt & 0x4000) != 0);
	}

	public String n(ItemStack paramItemStack) {
		if (paramItemStack.getData() == 0) {
			return LocaleI18n.get("item.emptyPotion.name").trim();
		}
		String str1 = "";
		if (g(paramItemStack.getData())) {
			str1 = LocaleI18n.get("potion.prefix.grenade").trim() + " ";
		}
		final List list = Items.POTION.g(paramItemStack);
		if (list != null && !list.isEmpty()) {
			String str = ((MobEffect) list.get(0)).f();
			str = str + ".postfix";
			return str1 + LocaleI18n.get(str).trim();
		}
		final String str2 = PotionBrewer.c(paramItemStack.getData());
		return LocaleI18n.get(str2).trim() + " " + super.n(paramItemStack);
	}
}