package net.shadowpie.sadiinso.sfc.utils;

import java.util.Iterator;

public class SDoubleCyclicQueue implements Iterable<Double> {

	private final double[] cache;
	private int head;
	private int size;
	private int tail;

	public SDoubleCyclicQueue() {
		this(32);
	}

	public SDoubleCyclicQueue(int size) {
		if(((size & (size - 1)) > 0) || (size <= 0))
			throw new RuntimeException("Invalid size (must be a power of 2)");
		
		this.cache = new double[size];
		this.size = this.tail = this.head = 0;
	}

	public SDoubleCyclicQueue(double... values) {
		if((values.length & (values.length - 1)) > 0)
			throw new RuntimeException("Invalid size (must be a power of 2)");
		
		this.cache = values;
		this.head = values.length - 1;
		this.tail = 0;
		this.size = values.length;
	}

	/**
	 * Return the size of the queue
	 */
	public int size() {
		return size;
	}

	/**
	 * Return true if the queue is empty else false
	 */
	public boolean isEmpty() {
		return (size == 0);
	}

	/**
	 * Helper function
	 * @param val The value to compute
	 * @return The variable increased by one, modulus the size of the queue
	 */
	private int imod(int val) {
		return (val + 1) & (cache.length - 1);
	}
	
	/**
	 * Helper function
	 * @param val The value to compute
	 * @return The variable decreased by one, modulus the size of the queue
	 */
	private int dmod(int val) {
		return (val - 1) & (cache.length - 1);
	}
	
	/**
	 * Return the array representation of the queue.
	 * This is not the array backed by the queue.
	 */
	public double[] toArray() {
		if(size == 0)
			return new double[0];
		
		double[] arr = new double[size];
		int tmp = 0, index = tail;
		while(index != head) {
			arr[tmp] = cache[index];
			index = imod(index);
			++tmp;
		}
		
		arr[tmp] = cache[index];
		return arr;
	}
	
	public double[] toArray(double[] arr) {
		return toArray(arr, 0);
	}
	
	public double[] toArray(double[] arr, int offset) {
		if(size == 0)
			return arr;
		
		if(offset + size > arr.length)
			throw new ArrayIndexOutOfBoundsException(offset);
		
		int tmp = offset, index = tail;
		while(index != head) {
			arr[tmp] = cache[index];
			index = imod(index);
			++tmp;
		}
		
		return arr;
	}

	/**
	 * Add a value at the start of the list
	 * @param value The value to add
	 */
	public void append(double value) {
		if(size == 0) {
			cache[head = 0] = value;
		} else {
			cache[head = imod(head)] = value;
			if(head == tail)
				tail = imod(tail);
		}
		
		if(size < cache.length)
			++size;
	}
	
	/**
	 * Add a value at the end of the list
	 * @param value The value to add
	 */
	public void push(double value) {
		cache[tail = dmod(tail)] = value;
		if(tail == head)
			head = dmod(head);
		
		if(size < cache.length)
			++size;
	}
	
	/**
	 * Clear the queue
	 */
	public void clear() {
		size = head = tail = 0;
	}

	/**
	 * Retrieves, but does not remove, the head of this queue
	 * Do not throw any exception, but return 0d if it result in an invalid operation
	 */
	public double peek() {
		return cache[head];
	}
	
	/**
	 * Retrieves and removes the head of this queue
	 * Do not throw any exception, but return 0d if it result in an invalid operation
	 */
	public double pop() {
		if(size <= 0)
			return 0d;
		
		double tmp = cache[head];
		head = dmod(head);
		--size;
		return tmp;
	}
	
	/**
	 * Retrieves, but does not remove, the head of this queue.
	 * Do not throw any exception, but return 0d if it result in an invalid operation
	 */
	public double peekLast() {
		return cache[tail];
	}
	
	/**
	 * Retrieves and removes the head of this queue.
	 * Do not throw any exception, but return 0d if it result in an invalid operation
	 */
	public double popLast() {
		if(size <= 0)
			return 0d;
		
		double tmp = cache[tail];
		tail = imod(tail);
		--size;
		return tmp;
	}
	
	@Override
	public Iterator<Double> iterator() {
		return new SDoubleQueueIterator(this);
	}

	@Override
	public String toString() {
		if(size == 0)
			return "[]";
		
		StringBuilder builder = new StringBuilder(size << 3);// ensure no resize
		builder.append('[');
		
		int tmp = tail;
		while(tmp != head) {
			builder.append(cache[tmp]);
			builder.append(", ");
			tmp = imod(tmp);
		}

		builder.append(cache[tmp]);
		builder.append(']');
		
		return builder.toString();
	}
	
	public static class SDoubleQueueIterator implements Iterator<Double> {
		private int index;
		private final SDoubleCyclicQueue queue;
		
		public SDoubleQueueIterator(SDoubleCyclicQueue queue) {
			this.queue = queue;
			this.index = queue.tail - 1;
		}
		
		@Override
		public boolean hasNext() {
			return (index != queue.head);
		}

		@Override
		public Double next() {
			index = queue.imod(index);
			return queue.cache[index];
		}
		
	}
	
}
