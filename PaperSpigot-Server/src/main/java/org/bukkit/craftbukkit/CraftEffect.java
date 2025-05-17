package org.bukkit.craftbukkit;

import org.apache.commons.lang.Validate;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.potion.Potion;

public class CraftEffect {
    public static <T> int getDataValue(Effect effect, T data) {
        final int datavalue = switch (effect) {
	        case POTION_BREAK -> ((Potion) data).toDamageValue() & 0x3F;
	        case RECORD_PLAY -> {
	            Validate.isTrue(((Material) data).isRecord(), "Invalid record type!");
	            yield ((Material) data).getId();
	        }
	        case SMOKE -> switch ((BlockFace) data) { // TODO: Verify (Where did these values come from...?)
	            case SOUTH_EAST -> 0;
	            case SOUTH -> 1;
	            case SOUTH_WEST -> 2;
	            case EAST -> 3;
	            case UP, SELF -> 4;
	            case WEST -> 5;
	            case NORTH_EAST -> 6;
	            case NORTH -> 7;
	            case NORTH_WEST -> 8;
	            default -> throw new IllegalArgumentException("Bad smoke direction!");
	        };
	        case STEP_SOUND -> {
	            Validate.isTrue(((Material) data).isBlock(), "Material is not a block!");
	            yield ((Material) data).getId();
	        }
	        case ITEM_BREAK -> ((Material) data).getId();
	        default -> 0;
	    };
	    return datavalue;
    }
}
