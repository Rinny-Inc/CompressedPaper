package org.bukkit.craftbukkit.block;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TileEntitySkull;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.CraftWorld;

public class CraftSkull extends CraftBlockState implements Skull {
    private static final int MAX_OWNER_LENGTH = 16;
    private final TileEntitySkull skull;
    private GameProfile profile;
    private SkullType skullType;
    private byte rotation;

    public CraftSkull(final Block block) {
        super(block);

        CraftWorld world = (CraftWorld) block.getWorld();
        skull = (TileEntitySkull) world.getTileEntityAt(getX(), getY(), getZ());
        profile = skull.getGameProfile();
        skullType = getSkullType(skull.getSkullType());
        rotation = (byte) skull.getRotation();
    }

    static SkullType getSkullType(int id) {
        return switch (id) {
            case 0 -> SkullType.SKELETON;
            case 1 -> SkullType.WITHER;
            case 2 -> SkullType.ZOMBIE;
            case 3 -> SkullType.PLAYER;
            case 4 -> SkullType.CREEPER;
            default -> throw new AssertionError(id);
        };
    }

    static int getSkullType(SkullType type) {
        return switch(type) {
            case SKELETON -> 0;
            case WITHER -> 1;
            case ZOMBIE -> 2;
            case PLAYER -> 3;
            case CREEPER -> 4;
            default -> throw new AssertionError(type);
        };
    }

    static byte getBlockFace(BlockFace rotation) {
        return switch (rotation) {
            case NORTH -> 0;
            case NORTH_NORTH_EAST -> 1;
            case NORTH_EAST -> 2;
            case EAST_NORTH_EAST -> 3;
            case EAST -> 4;
            case EAST_SOUTH_EAST -> 5;
            case SOUTH_EAST -> 6;
            case SOUTH_SOUTH_EAST -> 7;
            case SOUTH -> 8;
            case SOUTH_SOUTH_WEST -> 9;
            case SOUTH_WEST -> 10;
            case WEST_SOUTH_WEST -> 11;
            case WEST -> 12;
            case WEST_NORTH_WEST -> 13;
            case NORTH_WEST -> 14;
            case NORTH_NORTH_WEST -> 15;
            default -> throw new IllegalArgumentException("Invalid BlockFace rotation: " + rotation);
        };
    }

    static BlockFace getBlockFace(byte rotation) {
        return switch (rotation) {
            case 0 -> BlockFace.NORTH;
            case 1 -> BlockFace.NORTH_NORTH_EAST;
            case 2 -> BlockFace.NORTH_EAST;
            case 3 -> BlockFace.EAST_NORTH_EAST;
            case 4 -> BlockFace.EAST;
            case 5 -> BlockFace.EAST_SOUTH_EAST;
            case 6 -> BlockFace.SOUTH_EAST;
            case 7 -> BlockFace.SOUTH_SOUTH_EAST;
            case 8 -> BlockFace.SOUTH;
            case 9 -> BlockFace.SOUTH_SOUTH_WEST;
            case 10 -> BlockFace.SOUTH_WEST;
            case 11 -> BlockFace.WEST_SOUTH_WEST;
            case 12 -> BlockFace.WEST;
            case 13 -> BlockFace.WEST_NORTH_WEST;
            case 14 -> BlockFace.NORTH_WEST;
            case 15 -> BlockFace.NORTH_NORTH_WEST;
            default -> throw new AssertionError(rotation);
        };
    }

    public boolean hasOwner() {
        return profile != null;
    }

    public String getOwner() {
        return hasOwner() ? profile.getName() : null;
    }

    public boolean setOwner(String name) {
        if (name == null || name.length() > MAX_OWNER_LENGTH) {
            return false;
        }

        GameProfile profile = MinecraftServer.getServer().getUserCache().getProfile(name);
        if (profile == null) {
            return false;
        }

        if (skullType != SkullType.PLAYER) {
            skullType = SkullType.PLAYER;
        }

        this.profile = profile;
        return true;
    }

    public BlockFace getRotation() {
    	return getBlockFace(rotation);
    }

    public void setRotation(BlockFace rotation) {
        this.rotation = getBlockFace(rotation);
    }

    public SkullType getSkullType() {
        return skullType;
    }

    public void setSkullType(SkullType skullType) {
        this.skullType = skullType;

        if (skullType != SkullType.PLAYER) {
            profile = null;
        }
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        final boolean result = super.update(force, applyPhysics);

        if (result) {
            if (skullType == SkullType.PLAYER) {
                skull.setGameProfile(profile);
            } else {
                skull.setSkullType(getSkullType(skullType));
            }

            skull.setRotation(rotation);
            skull.update();
        }

        return result;
    }
}
