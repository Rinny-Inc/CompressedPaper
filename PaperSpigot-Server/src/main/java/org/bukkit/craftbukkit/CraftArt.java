package org.bukkit.craftbukkit;

import net.minecraft.server.EnumArt;
import org.bukkit.Art;

// Safety class, will break if either side changes
public class CraftArt {

    public static Art NotchToBukkit(EnumArt art) {
        return switch (art) {
            case KEBAB -> Art.KEBAB;
            case AZTEC -> Art.AZTEC;
            case ALBAN -> Art.ALBAN;
            case AZTEC2 -> Art.AZTEC2;
            case BOMB -> Art.BOMB;
            case PLANT -> Art.PLANT;
            case WASTELAND -> Art.WASTELAND;
            case POOL -> Art.POOL;
            case COURBET -> Art.COURBET;
            case SEA -> Art.SEA;
            case SUNSET -> Art.SUNSET;
            case CREEBET -> Art.CREEBET;
            case WANDERER -> Art.WANDERER;
            case GRAHAM -> Art.GRAHAM;
            case MATCH -> Art.MATCH;
            case BUST -> Art.BUST;
            case STAGE -> Art.STAGE;
            case VOID -> Art.VOID;
            case SKULL_AND_ROSES -> Art.SKULL_AND_ROSES;
            case FIGHTERS -> Art.FIGHTERS;
            case POINTER -> Art.POINTER;
            case PIGSCENE -> Art.PIGSCENE;
            case BURNINGSKULL -> Art.BURNINGSKULL;
            case SKELETON -> Art.SKELETON;
            case DONKEYKONG -> Art.DONKEYKONG;
            case WITHER -> Art.WITHER;
            default -> throw new AssertionError(art);
        };
    }

    public static EnumArt BukkitToNotch(Art art) {
        return switch (art) {
            case KEBAB -> EnumArt.KEBAB;
            case AZTEC -> EnumArt.AZTEC;
            case ALBAN -> EnumArt.ALBAN;
            case AZTEC2 -> EnumArt.AZTEC2;
            case BOMB -> EnumArt.BOMB;
            case PLANT -> EnumArt.PLANT;
            case WASTELAND -> EnumArt.WASTELAND;
            case POOL -> EnumArt.POOL;
            case COURBET -> EnumArt.COURBET;
            case SEA -> EnumArt.SEA;
            case SUNSET -> EnumArt.SUNSET;
            case CREEBET -> EnumArt.CREEBET;
            case WANDERER -> EnumArt.WANDERER;
            case GRAHAM -> EnumArt.GRAHAM;
            case MATCH -> EnumArt.MATCH;
            case BUST -> EnumArt.BUST;
            case STAGE -> EnumArt.STAGE;
            case VOID -> EnumArt.VOID;
            case SKULL_AND_ROSES -> EnumArt.SKULL_AND_ROSES;
            case FIGHTERS -> EnumArt.FIGHTERS;
            case POINTER -> EnumArt.POINTER;
            case PIGSCENE -> EnumArt.PIGSCENE;
            case BURNINGSKULL -> EnumArt.BURNINGSKULL;
            case SKELETON -> EnumArt.SKELETON;
            case DONKEYKONG -> EnumArt.DONKEYKONG;
            case WITHER -> EnumArt.WITHER;
            default -> throw new AssertionError(art);
        };
    }
}
