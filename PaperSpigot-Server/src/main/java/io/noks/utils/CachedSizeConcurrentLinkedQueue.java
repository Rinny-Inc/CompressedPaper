package io.noks.utils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;

public class CachedSizeConcurrentLinkedQueue<E> extends ConcurrentLinkedQueue<E> {
	private final LongAdder cachedSize = new LongAdder();

	@Override
	public boolean add(E e) {
		final boolean result = super.add(e);
		if (result) {
			cachedSize.increment();
		}
		return result;
	}

	@Override
	public E poll() {
		final E result = super.poll();
		if (result != null) {
			cachedSize.decrement();
		}
		return result;
	}

	@Override
	public int size() {
		return cachedSize.intValue();
	}
}
