package io.noks.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TtlHashMap<K, V> implements Map<K, V> {
	private HashMap<K, V> store;
	private HashMap<K, Long> timestamps;
	private long ttl;

	public TtlHashMap(TimeUnit ttlUnit, long ttlValue) {
		this.store = new HashMap<>();
		this.timestamps = new HashMap<>();
		this.ttl = ttlUnit.toNanos(ttlValue);
	}

	public TtlHashMap(TimeUnit ttlUnit, long ttlValue, int initialSize) {
		this.store = new HashMap<>(initialSize);
		this.timestamps = new HashMap<>();
		this.ttl = ttlUnit.toNanos(ttlValue);
	}

	private boolean expired(Object key) {
		Long timestamp = this.timestamps.get(key);
		return timestamp != null && (System.nanoTime() - timestamp > this.ttl);
	}

	private void clearExpired() {
		Iterator<K> iterator = timestamps.keySet().iterator();
		while (iterator.hasNext()) {
			K key = iterator.next();
			if (expired(key)) {
				iterator.remove();
				store.remove(key);
			}
		}
	}

	@Override
	public V get(Object key) {
		if (expired(key)) {
			store.remove(key);
			timestamps.remove(key);
			return null;
		}
		return store.get(key);
	}

	@Override
	public V put(K key, V value) {
		clearExpired();
		timestamps.put(key, System.nanoTime());
		return store.put(key, value);
	}

	@Override
	public int size() {
		clearExpired();
		return store.size();
	}

	@Override
	public boolean isEmpty() {
		clearExpired();
		return store.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		clearExpired();
		return store.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		clearExpired();
		return store.containsValue(value);
	}

	@Override
	public V remove(Object key) {
		timestamps.remove(key);
		return store.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		clearExpired();
		long now = System.nanoTime();
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			timestamps.put(e.getKey(), now);
			store.put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		timestamps.clear();
		store.clear();
	}

	@Override
	public Set<K> keySet() {
		clearExpired();
		return new HashSet<>(store.keySet());
	}

	@Override
	public Collection<V> values() {
		clearExpired();
		return new ArrayList<>(store.values());
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		clearExpired();
		return new HashSet<>(store.entrySet());
	}
}
