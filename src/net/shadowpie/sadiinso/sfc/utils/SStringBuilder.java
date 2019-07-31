package net.shadowpie.sadiinso.sfc.utils;

import java.util.Arrays;
import java.util.stream.IntStream;

public class SStringBuilder implements Appendable, CharSequence {

	private char[] chars;
	private int length;
	
	//############
	//CONSTRUCTORS
	//############
	
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
		length = 0;
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
	 * @see SStringBuilder#SStringBuilder(char[], boolean)
	 */
	public SStringBuilder(char[] chs) {
		chars = Arrays.copyOf(chs, chs.length + 16);
		length = chs.length;
	}
	
	/**
	 * Wrap the given char array with a SStringBuilder, all changes made to the array will be refleted in the 
	 * SStringBuilder and vice versa
	 * @param chs The char array to wrap
	 * @param useRef This boolean serve no purpose except differentiating this constructor from {@link SStringBuilder#SStringBuilder(char[])}
	 */
	public SStringBuilder(char[] chs, @SuppressWarnings("unused") boolean useRef) {
		chars = chs;
		length = chs.length;
	}
	
	//#######
	//GETTERS
	//#######
	
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
	 * Return the character at the given index
	 * @param index The char index
	 * @return A character
	 */
	public char charAt(int index) {
		if((index >= 0) && (index < length)) {
			return chars[index];
		} else {
			throw new IndexOutOfBoundsException(index);
		}
	}
	
	/**
	 * Return the character at the given index without checking for OOB
	 * @param index The char index
	 * @return A character
	 */
	public char fastCharAt(int index) {
		return chars[index];
	}
	
	//#######
	//SETTERS
	//#######
	
	/**
	 * Ensure that the internal buffer has a size at least equal to {capacity}
	 * @param capacity minimum capacity to ensure
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder ensureCapacity(int capacity) {
		if(capacity > chars.length) {
			chars = Arrays.copyOf(chars, Math.max(chars.length << 1, capacity));
		}
		
		return this;
	}
	
	/**
	 * Set the internal buffer size to the length of the current string
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder trimToSize() {
		if(length != chars.length) {
			chars = Arrays.copyOf(chars, length);
		}
		
		return this;
	}
	
	/**
	 * Push a char to the internal buffer
	 * @param c char to push
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder append(char c) {
		ensureCapacity(length + 1);
		chars[length++] = c;
		return this;
	}
	
	/**
	 * Push a String to the internal buffer
	 * @param str String to push
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder append(String str) {
		ensureCapacity(length + str.length());
		str.getChars(0, str.length(), chars, length);
		length += str.length();
		return this;
	}
	
	/**
	 * Push chars to the internal buffer
	 * @param chs chars to push
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder append(char[] chs) {
		ensureCapacity(chs.length + length);
		System.arraycopy(chs, 0, chars, length, chs.length);
		length += chs.length;
		return this;
	}
	
	/**
	 * Append the given {@link Object} to the internal buffer
	 * @param obj The {@link Object} to append
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder append(Object obj) {
		return append(String.valueOf(obj));
	}
	
	/**
	 * Append the given {@link SStringBuilder} to the internal buffer
	 * @param sb The {@link SStringBuilder} to append
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder append(SStringBuilder sb) {
		ensureCapacity(sb.length + length);
		System.arraycopy(sb.chars, 0, chars, length, sb.length);
		length += sb.length;
		return this;
	}
	
	/**
	 * Append the given {@link SStringBuilder} to the internal buffer
	 * @param sb The {@link SStringBuilder} to append
	 * @param beginIndex The first char to append
	 * @param endIndex The last char to append
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder append(SStringBuilder sb, int beginIndex, int endIndex) {
		if(beginIndex >= endIndex) {
			return this;
		}
		
		if(endIndex >= sb.length) {
			throw new IndexOutOfBoundsException(endIndex);
		}
		
		int len = beginIndex - endIndex;
		ensureCapacity(length + len);
		System.arraycopy(sb.chars, beginIndex, chars, length, len);
		length += len;
		return this;
	}
	
	/**
	 * Append the given {@link CharSequence} to the internal buffer
	 * @param s The {@link CharSequence} to append
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder append(CharSequence s) {
		if (s instanceof String) {
			return this.append((String) s);
		}
		
		if (s instanceof SStringBuilder) {
			return this.append((SStringBuilder) s);
		}
		
		return this.append(s, 0, s.length());
	}
	
	/**
	 * Append the given {@link CharSequence} to the internal buffer
	 * @param s The {@link CharSequence} to append
	 * @param beginIndex The first char to append
	 * @param endIndex The last char to append
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder append(CharSequence s, int beginIndex, int endIndex) {
		if(beginIndex >= endIndex) {
			return this;
		}
		
		ensureCapacity(length + (endIndex - beginIndex));
		for(int i = beginIndex; i < endIndex; i++) {
			chars[length++] = s.charAt(i);
		}
		
		return this;
	}
	
	/**
	 * Push chars to the internal buffer from the given index to the end of the given array
	 * @param chs chars to push
	 * @param from index to start at
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder append(char[] chs, int from) {
		return append(chs, from, chs.length - from);
	}
	
	/**
	 * Push the given number of chars to the internal buffer from the given index
	 * @param chs chars to push
	 * @param from index to start at
	 * @param count number of chars to push
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder append(char[] chs, int from, int count) {
		ensureCapacity(length + count);
		System.arraycopy(chs, from, chars, length, count);
		length += count;
		return this;
	}
	
	/**
	 * Insert a char at the given position in the internal buffer
	 * @param offset given position
	 * @param c char to insert
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder insert(int offset, char c) {
		if(offset >= length)
			return append(c);
		
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
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder insert(int offset, char[] chs) {
		if(offset >= length)
			return append(chs);
		
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
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder insert(int offset, String str) {
		if(offset >= length)
			return append(str);
		
		ensureCapacity(length + str.length());
		System.arraycopy(chars, offset, chars, offset + str.length(), length - offset);
		str.getChars(0, str.length(), chars, offset);
		length += str.length();
		return this;
	}
	
	/**
	 * Return whether the internal buffer is empty or not
	 */
	public boolean isEmpty() {
		return length == 0;
	}
	
	/**
	 * Set the length of the SStringBuilder
	 * @param len The length
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder setLength(int len) {
		if(len >= 0) {
			if(len >= chars.length) {
				ensureCapacity(len);
			}
			
			length = len;
		}
		
		return this;
	}
	
	/**
	 * Reset the internal buffer (empty)
	 * @return This {@link SStringBuilder}
	 */
	public SStringBuilder reset() {
		length = 0;
		return this;
	}
	
	/**
	 * Return a copy of the used part of the internal buffer or a
	 * reference to the internal buffer if trimmed
	 */
	public char[] copyOrRef() {
		if(length == chars.length) {
			return chars;
		} else {
			return Arrays.copyOfRange(chars, 0, length);
		}
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
	 * Return the internal buffer backed by this SStringBuilder
	 */
	public char[] getInternalBuffer() {
		return chars;
	}
	
	@Override
	public IntStream chars() {
		// don't look at this
		return new String(chars).chars();
	}
	
	@Override
	public IntStream codePoints() {
		// don't look at this
		return new String(chars).codePoints();
	}
	
	@Override
	public CharSequence subSequence(int beginIndex, int endIndex) {
		return new String(chars, beginIndex, endIndex);
	}
	
	@Override
	public String toString() {
		return String.valueOf(chars, 0, length);
	}
	
	@Override
	public int hashCode() {
		int hashcode = 0;
		for (int t = 0; t < length; t++) {
			hashcode = hashcode * 31 + chars[t];
		}
		
		return hashcode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(!(obj instanceof SStringBuilder)) {
			return false;
		}
		
		return Arrays.equals(((SStringBuilder) obj).chars, chars);
	}
	
}
