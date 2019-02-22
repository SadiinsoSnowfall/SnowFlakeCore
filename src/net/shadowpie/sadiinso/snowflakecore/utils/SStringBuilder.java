package net.shadowpie.sadiinso.snowflakecore.utils;

import java.util.Arrays;

public class SStringBuilder {

	private char[] chars;
	private int length = 0;
	private int hashcode = 0;
	
	/**
	 * Default constructor, set internal buffer size to 16
	 */
	public SStringBuilder() {
		this(16);
	}
	
	/**
	 * Constructor that allow to set the initial buffer size
	 * @param size initial size of the internal buffer
	 */
	public SStringBuilder(int size) {
		chars = new char[size];
	}
	
	/**
	 * Constructor with initial buffer value, set internal buffer size to 17
	 * @param c char to push
	 */
	public SStringBuilder(char c) {
		chars = new char[17];
		chars[0] = c;
		length = 1;
	}
	
	/**
	 * Constructor with initial buffer value, set internal buffer size to str.length() + 16
	 * @param str String to push
	 */
	public SStringBuilder(String str) {
		this(str.toCharArray());
	}
	
	/**
	 * Constructor with initial buffer value, set internal buffer size to chs.length + 16
	 * @param chs chars to push
	 */
	public SStringBuilder(char[] chs) {
		chars = Arrays.copyOf(chs, chs.length + 16);
		length = chs.length;
	}
	
	/**
	 * @return total buffer size
	 */
	public int allocated() {
		return chars.length;
	}
	
	/**
	 * @return used buffer size
	 */
	public int length() {
		return length;
	}
	
	/**
	 * Ensure that the internal buffer has a size at least equal to {capacity}
	 * @param capacity minimum capacity to ensure
	 * @return this
	 */
	public SStringBuilder ensureCapacity(int capacity) {
		if(capacity > chars.length)
			chars = Arrays.copyOf(chars, Math.max(chars.length << 1, capacity));
		return this;
	}
	
	/**
	 * Push a char to the internal buffer
	 * @param c char to push
	 * @return this
	 */
	public SStringBuilder push(char c) {
		ensureCapacity(length + 1);
		chars[length++] = c;
		return this;
	}
	
	/**
	 * Push a String to the internal buffer
	 * @param str String to push
	 * @return this
	 */
	public SStringBuilder push(String str) {
		ensureCapacity(length + str.length());
		str.getChars(0, str.length(), chars, length);
		length += str.length();
		return this;
	}
	
	/**
	 * Push chars to the internal buffer
	 * @param chs chars to push
	 * @return this
	 */
	public SStringBuilder push(char[] chs) {
		ensureCapacity(chs.length + length);
		System.arraycopy(chs, 0, chars, length, chs.length);
		length += chs.length;
		return this;
	}
	
	/**
	 * Push chars to the internal buffer from the given index to the end of the given array
	 * @param chs chars to push
	 * @param from index to start at
	 */
	public SStringBuilder push(char[] chs, int from) {
		return push(chs, from, chs.length - from);
	}
	
	/**
	 * Push the given number of chars to the internal buffer from the given index
	 * @param chs chars to push
	 * @param from index to start at
	 * @param count number of chars to push
	 */
	public SStringBuilder push(char[] chs, int from, int count) {
		ensureCapacity(length + count);
		System.arraycopy(chs, from, chars, length, count);
		length += count;
		return this;
	}
	
	/**
	 * Insert a char at the given position in the internal buffer
	 * @param offset given position
	 * @param c char to insert
	 * @return this
	 */
	public SStringBuilder insert(int offset, char c) {
		if(offset >= length)
			return push(c);
		
		ensureCapacity(length + 1);
		System.arraycopy(chars, offset, chars, offset + 1, length - offset);
		chars[offset] = c;
		++length;
		return this;
	}
	
	/**
	 * Insert chars at the given position in the internal buffer
	 * @param offset given position
	 * @param chs chars to insert
	 * @return this
	 */
	public SStringBuilder insert(int offset, char[] chs) {
		if(offset >= length)
			return push(chs);
		
		ensureCapacity(length + chs.length);
		System.arraycopy(chars, offset, chars, offset + chs.length, length - offset);
		System.arraycopy(chs, 0, chars, offset, chs.length);
		length += chs.length;
		return this;
	}
	
	/**
	 * Insert a String at the given position in the internal buffer
	 * @param offset given position
	 * @param str String to insert
	 * @return this
	 */
	public SStringBuilder insert(int offset, String str) {
		if(offset >= length)
			return push(str);
		
		ensureCapacity(length + str.length());
		System.arraycopy(chars, offset, chars, offset + str.length(), length - offset);
		str.getChars(0, str.length(), chars, offset);
		length += str.length();
		return this;
	}
	
	/**
	 * Return whether the internal buffer is empty or not
	 */
	public boolean empty() {
		return length == 0;
	}
	
	/**
	 * Reset the internal buffer (empty)
	 * @return this
	 */
	public SStringBuilder reset() {
		length = 0;
		return this;
	}
	
	/**
	 * Return a copy of the used part of the internal buffer
	 */
	public char[] copy() {
		return Arrays.copyOfRange(chars, 0, length);
	}
	
	/**
	 * Return a copy of the used part of the internal buffer from the given index
	 * @param from index to start from
	 */
	public char[] copy(int from) {
		return Arrays.copyOfRange(chars, from, chars.length - from);
	}
	
	/**
	 * Return a copy of the used part of the internal buffer from the given index to the second given index
	 * @param from index to start from
	 * @param count number of char to push
	 */
	public char[] copy(int from, int count) {
		return Arrays.copyOfRange(chars, from, count);
	}
	
	/**
	 * Return a view of the whole internal buffer, both used and not used
	 */
	public String bufferView() {
		return Arrays.toString(chars);
	}
	
	public char[] chars() {
		return chars;
	}
	
	@Override
	public String toString() {
		return String.valueOf(chars, 0, length);
	}
	
	@Override
	public int hashCode() {
		if(hashcode == 0)
			for(int t = 0; t < length; t++)
				hashcode = hashcode * 31 + chars[t];
		
		return hashcode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof SStringBuilder))
			return false;
		
		return Arrays.equals(((SStringBuilder) obj).chars, chars);
	}
	
	public static void main(String[] args) {
		var ss = new SStringBuilder("foo");
		ss.push("meow");
		System.out.println(ss);
	}
	
}
