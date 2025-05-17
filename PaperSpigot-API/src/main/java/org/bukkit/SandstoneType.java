package org.bukkit;

import com.google.common.collect.ImmutableMap;

/**
 * Represents the three different types of Sandstone
 */
public enum SandstoneType {
    CRACKED(0x0),
    GLYPHED(0x1),
    SMOOTH(0x2);

    private final byte data;
    private final static ImmutableMap<Byte, SandstoneType> BY_DATA;

    private SandstoneType(final int data) {
        this.data = (byte) data;
    }

    /**
     * Gets the associated data value representing this type of sandstone
     *
     * @return A byte containing the data value of this sandstone type
     * @deprecated Magic value
     */
    @Deprecated
    public byte getData() {
        return data;
    }

    /**
     * Gets the type of sandstone with the given data value
     *
     * @param data Data value to fetch
     * @return The {@link SandstoneType} representing the given value, or null
     *     if it doesn't exist
     * @deprecated Magic value
     */
    @Deprecated
    public static SandstoneType getByData(final byte data) {
        return BY_DATA.get(data);
    }

    static {
    	ImmutableMap.Builder<Byte, SandstoneType> builder = ImmutableMap.builder();
        for (SandstoneType type : values()) {
            builder.put(type.data, type);
        }
        BY_DATA = builder.build();
    }
}
