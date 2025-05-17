package org.spigotmc;

import org.bukkit.craftbukkit.util.LongHash;

public class FlatMap<V> {
	private static final int FLAT_LOOKUP_SIZE = 2048;
	private static final int FLAT_LOOKUP_SIZE_DIVIDED_BY_2 = 1024;

	private final Object[] flatLookup = new Object[4194304];

	public void put(int msw, int lsw, V value) {
		int acx = (msw >= 0) ? (msw + 1024) : (msw * -1);
		int acz = (lsw >= 0) ? (lsw + 1024) : (lsw * -1);
		if (acx < 2048 && acz < 2048)
			this.flatLookup[acx * 2048 + acz] = value;
	}

	public void put(long key, V value) {
		put(LongHash.msw(key), LongHash.lsw(key), value);
	}

	public void remove(long key) {
		put(key, null);
	}

	public void remove(int msw, int lsw) {
		put(msw, lsw, null);
	}

	public boolean contains(int msw, int lsw) {
		return (get(msw, lsw) != null);
	}

	public boolean contains(long key) {
		return (get(key) != null);
	}

	public V get(int msw, int lsw) {
		int acx = (msw >= 0) ? (msw + 1024) : (msw * -1);
		int acz = (lsw >= 0) ? (lsw + 1024) : (lsw * -1);
		if (acx < 2048 && acz < 2048)
			return (V) this.flatLookup[acx * 2048 + acz];
		return null;
	}

	public V get(long key) {
		return get(LongHash.msw(key), LongHash.lsw(key));
	}
}
