package org.bukkit;

import com.google.common.collect.ImmutableMap;

public enum Instrument {

    /**
     * Piano is the standard instrument for a note block.
     */
    PIANO(0x0),
    /**
     * Bass drum is normally played when a note block is on top of a
     * stone-like block
     */
    BASS_DRUM(0x1),
    /**
     * Snare drum is normally played when a note block is on top of a sandy
     * block.
     */
    SNARE_DRUM(0x2),
    /**
     * Sticks are normally played when a note block is on top of a glass
     * block.
     */
    STICKS(0x3),
    /**
     * Bass guitar is normally played when a note block is on top of a wooden
     * block.
     */
    BASS_GUITAR(0x4);

    private final byte type;
    private final static ImmutableMap<Byte, Instrument> BY_DATA;

    private Instrument(final int type) {
        this.type = (byte) type;
    }

    /**
     * @return The type ID of this instrument.
     * @deprecated Magic value
     */
    @Deprecated
    public byte getType() {
        return this.type;
    }

    /**
     * Get an instrument by its type ID.
     *
     * @param type The type ID
     * @return The instrument
     * @deprecated Magic value
     */
    @Deprecated
    public static Instrument getByType(final byte type) {
        return BY_DATA.get(type);
    }

    static {
    	ImmutableMap.Builder<Byte, Instrument> builder = ImmutableMap.builder();
        for (Instrument instrument : Instrument.values()) {
            builder.put(instrument.getType(), instrument);
        }
        BY_DATA = builder.build();
    }
}
