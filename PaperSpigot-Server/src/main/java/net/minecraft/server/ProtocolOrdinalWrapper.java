package net.minecraft.server;

// CraftBukkit - import package private class
class ProtocolOrdinalWrapper {
	// Rinny start
    static final int[] a;

    static {
        final EnumProtocol[] protocols = EnumProtocol.values();
        a = new int[protocols.length];
        for (EnumProtocol protocol : protocols) {
            switch (protocol) {
                case LOGIN -> a[protocol.ordinal()] = 1;
                case STATUS -> a[protocol.ordinal()] = 2;
                default -> {
                	continue;
                }
            }
        }
    }
    // Rinny end
}
