package io.noks.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

public class TtlArrayList<E> implements List<E> {
	private List<E> store;
	private HashMap<E, Long> timestamps;
	private long ttl;

	public TtlArrayList(TimeUnit ttlUnit, long ttlValue) {
		this.store = new ArrayList<>();
		this.timestamps = new HashMap<>();
		this.ttl = ttlUnit.toNanos(ttlValue);
	}

	public TtlArrayList(TimeUnit ttlUnit, long ttlValue, int initialSize) {
		this.store = new ArrayList<>(initialSize);
		this.timestamps = new HashMap<>();
		this.ttl = ttlUnit.toNanos(ttlValue);
	}

	private boolean expired(E value) {
		return (System.nanoTime() - this.timestamps.getOrDefault(value, 0L) > this.ttl);
	}

	private void cleanupExpiredElements() {
		Iterator<E> iterator = store.iterator();
		while (iterator.hasNext()) {
			E value = iterator.next();
			if (expired(value)) {
				iterator.remove();
				timestamps.remove(value);
			}
		}
	}

	@Override
	public E get(int index) {
		cleanupExpiredElements();
		E e = this.store.get(index);
		if (expired(e)) {
			this.store.remove(e);
			this.timestamps.remove(e);
			return null;
		}
		return e;
	}

	@Override
	public int size() {
		cleanupExpiredElements();
		return this.store.size();
	}

	@Override
	public boolean isEmpty() {
		cleanupExpiredElements();
		return this.store.isEmpty();
	}

	@Override
	public boolean contains(Object value) {
		cleanupExpiredElements();
		return this.store.contains(value);
	}

	@Override
	public boolean add(E value) {
		cleanupExpiredElements();
		this.timestamps.put(value, System.nanoTime());
		return this.store.add(value);
	}

	@Override
	public void add(int i, E value) {
		cleanupExpiredElements();
		this.timestamps.put(value, System.nanoTime());
		this.store.add(i, value);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		cleanupExpiredElements();
		long now = System.nanoTime();
		for (E value : c) {
			timestamps.put(value, now);
		}
		return this.store.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		cleanupExpiredElements();
		long now = System.nanoTime();
		for (E value : c) {
			timestamps.put(value, now);
		}
		return this.store.addAll(index, c);
	}

	@Override
	public boolean remove(Object value) {
		cleanupExpiredElements();
		timestamps.remove(value);
		return this.store.remove(value);
	}

	@Override
	public E remove(int i) {
		cleanupExpiredElements();
		E value = this.store.remove(i);
		timestamps.remove(value);
		return value;
	}

	@Override
	public void clear() {
		this.timestamps.clear();
		this.store.clear();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		cleanupExpiredElements();
		return this.store.containsAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		cleanupExpiredElements();
		for (Object object : c) {
			timestamps.remove(object);
		}
		return this.store.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		cleanupExpiredElements();
		Iterator<E> iterator = store.iterator();
		boolean modified = false;
		while (iterator.hasNext()) {
			E value = iterator.next();
			if (!c.contains(value)) {
				iterator.remove();
				timestamps.remove(value);
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public Object[] toArray() {
		cleanupExpiredElements();
		return this.store.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		cleanupExpiredElements();
		return this.store.toArray(a);
	}

	@Override
	public int indexOf(Object o) {
		cleanupExpiredElements();
		return this.store.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		cleanupExpiredElements();
		return this.store.lastIndexOf(o);
	}

	@Override
	public E set(int index, E element) {
		cleanupExpiredElements();
		this.timestamps.put(element, System.nanoTime());
		return this.store.set(index, element);
	}

	@Override
	public ListIterator<E> listIterator() {
		cleanupExpiredElements();
		return this.store.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		cleanupExpiredElements();
		return this.store.listIterator(index);
	}

	@Override
	public Iterator<E> iterator() {
		cleanupExpiredElements();
		return new Iterator<E>() {
			private final Iterator<E> innerIterator = store.iterator();

			@Override
			public boolean hasNext() {
				cleanupExpiredElements();
				return innerIterator.hasNext();
			}

			@Override
			public E next() {
				cleanupExpiredElements();
				return innerIterator.next();
			}
		};
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		cleanupExpiredElements();
		return this.store.subList(fromIndex, toIndex);
	}
}