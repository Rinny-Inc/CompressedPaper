package org.bukkit.craftbukkit.enchantments;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

public class CraftEnchantment extends Enchantment {
    private final net.minecraft.server.Enchantment target;

    public CraftEnchantment(net.minecraft.server.Enchantment target) {
        super(target.id);
        this.target = target;
    }

    @Override
    public int getMaxLevel() {
        return target.getMaxLevel();
    }

    @Override
    public int getStartLevel() {
        return target.getStartLevel();
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return switch (target.slot) {
	        case ALL -> EnchantmentTarget.ALL;
	        case ARMOR -> EnchantmentTarget.ARMOR;
	        case ARMOR_FEET -> EnchantmentTarget.ARMOR_FEET;
	        case ARMOR_HEAD -> EnchantmentTarget.ARMOR_HEAD;
	        case ARMOR_LEGS -> EnchantmentTarget.ARMOR_LEGS;
	        case ARMOR_TORSO -> EnchantmentTarget.ARMOR_TORSO;
	        case DIGGER -> EnchantmentTarget.TOOL;
	        case WEAPON -> EnchantmentTarget.WEAPON;
	        case BOW -> EnchantmentTarget.BOW;
	        case FISHING_ROD -> EnchantmentTarget.FISHING_ROD;
	        default -> null;
        };
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return target.canEnchant(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public String getName() {
        return switch (target.id) {
	        case 0 -> "PROTECTION_ENVIRONMENTAL";
	        case 1 -> "PROTECTION_FIRE";
	        case 2 -> "PROTECTION_FALL";
	        case 3 -> "PROTECTION_EXPLOSIONS";
	        case 4 -> "PROTECTION_PROJECTILE";
	        case 5 -> "OXYGEN";
	        case 6 -> "WATER_WORKER";
	        case 7 -> "THORNS";
	        case 16 -> "DAMAGE_ALL";
	        case 17 -> "DAMAGE_UNDEAD";
	        case 18 -> "DAMAGE_ARTHROPODS";
	        case 19 -> "KNOCKBACK";
	        case 20 -> "FIRE_ASPECT";
	        case 21 -> "LOOT_BONUS_MOBS";
	        case 32 -> "DIG_SPEED";
	        case 33 -> "SILK_TOUCH";
	        case 34 -> "DURABILITY";
	        case 35 -> "LOOT_BONUS_BLOCKS";
	        case 48 -> "ARROW_DAMAGE";
	        case 49 -> "ARROW_KNOCKBACK";
	        case 50 -> "ARROW_FIRE";
	        case 51 -> "ARROW_INFINITE";
	        case 61 -> "LUCK";
	        case 62 -> "LURE";
	        default -> "UNKNOWN_ENCHANT_" + target.id;
        };
    }

    public static net.minecraft.server.Enchantment getRaw(Enchantment enchantment) {
        if (enchantment instanceof EnchantmentWrapper) {
            enchantment = ((EnchantmentWrapper) enchantment).getEnchantment();
        }

        if (enchantment instanceof CraftEnchantment) {
            return ((CraftEnchantment) enchantment).target;
        }

        return null;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        if (other instanceof EnchantmentWrapper) {
            other = ((EnchantmentWrapper) other).getEnchantment();
        }
        if (other instanceof CraftEnchantment ench) { // Rinny
            return !target.a(ench.target);
        }
        return false;
    }
}
