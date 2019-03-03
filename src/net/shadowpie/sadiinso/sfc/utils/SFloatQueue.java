package net.shadowpie.sadiinso.sfc.utils;

public class SFloatQueue {

	private final float[] cache;
	private int head;
	private int size;
	private int tail;

	public SFloatQueue() {
		this(32);
	}

	public SFloatQueue(int size) {
		if(((size & (size - 1)) > 0) || (size <= 0))
			throw new RuntimeException("Invalid size (must be a power of 2)");
		
		this.cache = new float[size];
		this.size = this.tail = this.head = 0;
	}

	public SFloatQueue(float... values) {
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
	public float[] toArray() {
		if(size == 0)
			return new float[0];
		
		float[] arr = new float[size];
		int tmp = 0, index = tail;
		while(index != head) {
			arr[tmp] = cache[index];
			index = imod(index);
			++tmp;
		}
		
		arr[tmp] = cache[index];
		return arr;
	}

	/**
	 * Add a value at the start of the list
	 * @param value The value to add
	 */
	public void append(float value) {
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
	public void push(float value) {
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
	 * Do not throw any exception, but return 0f if it result in an invalid operation
	 */
	public float peek() {
		return cache[head];
	}
	
	/**
	 * Retrieves and removes the head of this queue
	 * Do not throw any exception, but return 0f if it result in an invalid operation
	 */
	public float pop() {
		if(size <= 0)
			return 0f;
		
		float tmp = cache[head];
		head = dmod(head);
		--size;
		return tmp;
	}
	
	/**
	 * Retrieves, but does not remove, the head of this queue.
	 * Do not throw any exception, but return 0f if it result in an invalid operation
	 */
	public float peekLast() {
		return cache[tail];
	}
	
	/**
	 * Retrieves and removes the head of this queue.
	 * Do not throw any exception, but return 0f if it result in an invalid operation
	 */
	public float popLast() {
		if(size <= 0)
			return 0f;
		
		float tmp = cache[tail];
		tail = imod(tail);
		--size;
		return tmp;
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
	
}
