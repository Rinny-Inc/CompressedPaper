package org.bukkit;

import com.google.common.collect.ImmutableMap;

/**
 * Represents various types of worlds that may exist
 */
public enum WorldType {
    NORMAL("DEFAULT"),
    FLAT("FLAT"),
    VOID("VOID"),
    VERSION_1_1("DEFAULT_1_1"),
    LARGE_BIOMES("LARGEBIOMES"),
    AMPLIFIED("AMPLIFIED");

    private final static ImmutableMap<String, WorldType> BY_NAME;
    private final String name;

    private WorldType(String name) {
        this.name = name;
    }

    /**
     * Gets the name of this WorldType
     *
     * @return Name of this type
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a Worldtype by its name
     *
     * @param name Name of the WorldType to get
     * @return Requested WorldType, or null if not found
     */
    public static WorldType getByName(String name) {
        return BY_NAME.get(name.toUpperCase());
    }

    static {
    	ImmutableMap.Builder<String, WorldType> builder = ImmutableMap.builder();
        for (WorldType type : values()) {
            builder.put(type.name, type);
        }
        BY_NAME = builder.build();
    }
}
