package org.bukkit.craftbukkit.potion;

import net.minecraft.server.MobEffectList;

import org.bukkit.potion.PotionEffectType;

public class CraftPotionEffectType extends PotionEffectType {
    private final MobEffectList handle;

    public CraftPotionEffectType(MobEffectList handle) {
        super(handle.id);
        this.handle = handle;
    }

    @Override
    public double getDurationModifier() {
        return handle.getDurationModifier();
    }

    public MobEffectList getHandle() {
        return handle;
    }

    @Override
    public String getName() {
        return switch (handle.id) {
	        case 1 -> "SPEED";
	        case 2 -> "SLOW";
	        case 3 -> "FAST_DIGGING";
	        case 4 -> "SLOW_DIGGING";
	        case 5 -> "INCREASE_DAMAGE";
	        case 6 -> "HEAL";
	        case 7 -> "HARM";
	        case 8 -> "JUMP";
	        case 9 -> "CONFUSION";
	        case 10 -> "REGENERATION";
	        case 11 -> "DAMAGE_RESISTANCE";
	        case 12 -> "FIRE_RESISTANCE";
	        case 13 -> "WATER_BREATHING";
	        case 14 -> "INVISIBILITY";
	        case 15 -> "BLINDNESS";
	        case 16 -> "NIGHT_VISION";
	        case 17 -> "HUNGER";
	        case 18 -> "WEAKNESS";
	        case 19 -> "POISON";
	        case 20 -> "WITHER";
	        case 21 -> "HEALTH_BOOST";
	        case 22 -> "ABSORPTION";
	        case 23 -> "SATURATION";
	        default -> "UNKNOWN_EFFECT_TYPE_" + handle.id;
        };
    }

    @Override
    public boolean isInstant() {
        return handle.isInstant();
    }
}
