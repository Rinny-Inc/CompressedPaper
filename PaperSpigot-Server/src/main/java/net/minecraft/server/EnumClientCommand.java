package net.minecraft.server;

public enum EnumClientCommand {
	PERFORM_RESPAWN("PERFORM_RESPAWN", 0, 0), REQUEST_STATS("REQUEST_STATS", 1, 1),
	OPEN_INVENTORY_ACHIEVEMENT("OPEN_INVENTORY_ACHIEVEMENT", 2, 2);

	private static final EnumClientCommand[] f;
	private static final EnumClientCommand[] e;
	private final int d;

	static {
		e = new EnumClientCommand[(values()).length];
		f = new EnumClientCommand[] { PERFORM_RESPAWN, REQUEST_STATS, OPEN_INVENTORY_ACHIEVEMENT };
		EnumClientCommand[] aenumclientcommand = values();
		int i = aenumclientcommand.length;
		for (int j = 0; j < i; j++) {
			EnumClientCommand enumclientcommand = aenumclientcommand[j];
			e[enumclientcommand.d] = enumclientcommand;
		}
	}

	EnumClientCommand(String s, int i, int j) {
		this.d = j;
	}

	static EnumClientCommand[] a() {
		return e;
	}

	static int a(EnumClientCommand enumclientcommand) {
		return enumclientcommand.d;
	}
}
