package net.shadowpie.sadiinso.sfc.utils;

import net.dv8tion.jda.core.EmbedBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

public class SFUtils {
	
	private static final DecimalFormat df = new DecimalFormat();
	
	static {
		df.setGroupingSize(3);
		df.setMaximumFractionDigits(2);
	}
	
	// Suppresses default constructor, ensuring non-instantiability.
	private SFUtils() {
	}
	
	/**
	 * Redeclared here because {@link EmbedBuilder#ZERO_WIDTH_SPACE} is a String, not a char
	 */
	public static final char ZERO_WIDTH_SPACE = '​';
	
	/**
	 * Format a number to be more human readable
	 * <br>
	 * Example : <strong>123456.789</strong> will became <strong>123 456.78</strong>
	 *
	 * @param number The number to format
	 * @return A string representing the formatted number
	 */
	public static String formatNumber(Number number) {
		return df.format(number);
	}
	
	/**
	 * Split a string at the last occurence of the given separator
	 *
	 * @param src       The source string
	 * @param separator The separator
	 * @return The two parts or the source string if the separator was not found
	 */
	public static String[] splitLastIndexOf(String src, String separator, boolean omitSeparator) {
		int i = src.lastIndexOf(separator);
		if (i == -1)
			return new String[]{src};
		else
			return new String[]{src.substring(0, i), omitSeparator ? (i == (src.length() - 1) ? "" : src.substring(i + 1)) : src.substring(i)};
	}
	
	/**
	 * Return the index of the given sub-array in the given array
	 *
	 * @param array the source array
	 * @param value the sub-array to test for
	 * @return the position of the sub-array in the source array
	 */
	public static int arrayIndexOf(char[] array, char[] value) {
		return arrayIndexOf(array, value, 0, array.length);
	}
	
	/**
	 * Return the index of the given sub-array in the given array
	 *
	 * @param array the source array
	 * @param value the sub-array to test for
	 * @param from  the index from where to start the research
	 * @return the position of the sub-array in the source array
	 */
	public static int arrayIndexOf(char[] array, char[] value, int from) {
		return arrayIndexOf(array, value, from, array.length);
	}
	
	/**
	 * Return the index of the given sub-array in the given array
	 *
	 * @param array the source array
	 * @param value the sub-array to test for
	 * @param from  the index from where to start the research
	 * @param to    the maximum index to include in the research (exclusive)
	 * @return the position of the sub-array in the source array
	 */
	public static int arrayIndexOf(char[] array, char[] value, int from, int to) {
		// return NOT_FOUND if value or array are null or empty
		if ((value == null) || (value.length == 0) || (array == null) || (array.length == 0))
			return -1;
		
		// set upper bound
		if (to > array.length)
			to = array.length;
		
		// set lower bound
		if (from < 0)
			from = 0;
		
		if (value.length == 1) { // if len = 1 do simple indexof
			for (; from < to; from++)
				if (array[from] == value[0])
					return from;
		} else {
			int tmp;
			int len = value.length - 1;
			search:
			for (; from < to - len; from++) {
				if ((array[from] == value[0]) && (array[from + len] == value[len])) { // check first and last chars first
					for (tmp = 1; tmp < len; tmp++) // check intermediate chars
						if (array[from + tmp] != value[tmp])
							continue search;
					
					return from;
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * Convert a Color object to it's hex code representation
	 *
	 * @param color The Color to convert
	 * @return The hex code representation of the given color
	 */
	public static String colorToHex(Color color) {
		return (color == null ? "null" : '#' + Integer.toHexString(color.getRGB()).substring(2).toUpperCase());
	}
	
	/**
	 * Convert an image to a byte array representing the image file By default, the
	 * jpg format will be used
	 *
	 * @param img The image to convert
	 * @return The converted image
	 * @throws IOException
	 */
	public static byte[] imgToByteArray(RenderedImage img) throws IOException {
		return imgToByteArray(img, "png");
	}
	
	/**
	 * Convert an image to a byte array representing the image file
	 *
	 * @param img    The image to convert
	 * @param format The format to use
	 * @return The converted image
	 * @throws IOException
	 */
	public static byte[] imgToByteArray(RenderedImage img, String format) throws IOException {
		byte[] result;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		if (!ImageIO.write(img, format, baos))
			throw new IOException("ImageIO.write returned false");
		
		baos.flush();
		result = baos.toByteArray();
		baos.close();
		
		return result;
	}
	
	/**
	 * Return a ressource file located in /data/ inside the jar file as a String
	 *
	 * @param name The ressource name
	 */
	public static String getDataResourceAsString(String name) {
		InputStream is = getDataRessource(name);
		
		try {
			return new String(is.readAllBytes());
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Return an inputStream pointing to a ressource located in /data/ inside the jar file
	 *
	 * @param name The ressource name
	 */
	public static InputStream getDataRessource(String name) {
		return getRessource("/data/" + name);
	}
	
	/**
	 * Return an inputStream pointing to a ressource inside the jar file
	 *
	 * @param name The ressource name
	 */
	public static InputStream getRessource(String name) {
		return SFUtils.class.getResourceAsStream(name);
	}
	
	/**
	 * Format a list of strings into columns (ensure compatibility with discord by inserting zero-space-width between normal spaces)
	 *
	 * @param strs       The strings to format
	 * @param maxColSize The maximum column size (number of strings per column)
	 * @param spacing    The spacing between columns
	 * @return The formatted String
	 */
	public static String snapFormat(List<String> strs, int maxColSize, int spacing) {
		return SFUtils.snapFormat(strs.toArray(String[]::new), maxColSize, spacing);
	}
	
	/**
	 * Format an array of strings into columns (ensure compatibility with discord by inserting zero-space-width between normal spaces)
	 *
	 * @param strs       The strings to format
	 * @param maxColSize The maximum column size (number of strings per column)
	 * @param spacing    The spacing between columns
	 * @return The formatted String
	 */
	public static String snapFormat(String[] strs, int maxColSize, int spacing) {
		// only one column
		if (strs.length <= maxColSize) {
			int end = strs.length - 1;
			StringBuilder sb = new StringBuilder(strs.length << 2);
			
			for (int t = 0; t < end; t++) {
				sb.append(strs[t]);
				sb.append('\n');
			}
			
			sb.append(strs[end]);
			return sb.toString();
		}
		
		// multiples columns
		int lastCol = strs.length % maxColSize;
		if (lastCol == 0)
			lastCol = maxColSize;
		
		// init padding
		int maxPad = 0;
		for (int t = 0; t < strs.length; t++)
			if (strs[t].length() > maxPad)
				maxPad = strs[t].length();
		char[] pad = new char[(maxPad + spacing) * 2];
		for (int t = 0; t < pad.length; t += 2) {
			pad[t] = ' ';
			pad[t + 1] = ZERO_WIDTH_SPACE;
		}
		
		// init lines builders
		StringBuilder[] lines = new StringBuilder[maxColSize];
		for (int t = 0; t < lines.length; t++)
			lines[t] = new StringBuilder(maxPad * (strs.length / maxColSize));
		
		// compute lines
		for (int t = 0; t < strs.length - lastCol; t += maxColSize) {
			int padding = 0;
			int end = t + maxColSize;
			
			for (int u = t; u < end; u++)
				if (strs[u].length() > padding)
					padding = strs[u].length();
			padding += spacing;
			
			int index, curPad;
			for (int u = t; u < end; u++) {
				index = u % maxColSize;
				curPad = padding - strs[u].length();
				
				lines[index].append(strs[u]);
				
				if (curPad > 0)
					lines[index].append(pad, 0, curPad * 2);
			}
		}
		
		// add last column
		for (int t = strs.length - lastCol; t < strs.length; t++)
			lines[t % maxColSize].append(strs[t]);
		
		// build final string
		StringBuilder sb = new StringBuilder(64);
		for (int t = 0; t < lines.length - 1; t++) {
			sb.append(lines[t]);
			sb.append('\n');
		}
		
		sb.append(lines[lines.length - 1]);
		return sb.toString();
	}
	
	/**
	 * Add ``` chars at the begining and the end of the string
	 *
	 * @param str The string
	 * @return A string
	 */
	public static String monospace(String str) {
		StringBuilder sb = new StringBuilder(str.length() + 7);
		sb.append("```\n");
		sb.append(str);
		sb.append("```");
		return sb.toString();
	}
	
	/**
	 *
	 * @param o The object to scan
	 * @return A string representation of {@code o}
	 * @see #deepToString(Object, boolean)
	 */
	public static String deepToString(Object o) {
		return deepToString(o, true);
	}
	
	/**
	 *
	 * @param o The object to scan
	 * @param preventLoop Whether or not to prevent reference loops when scanning the object
	 * @return A string representation of {@code o}
	 * @see #deepToString(Object)
	 */
	public static String deepToString(Object o, boolean preventLoop) {
		if (o == null) {
			return "null";
		}
		
		StringBuilder buf = new StringBuilder(64);
		deepToString(o, buf, (preventLoop ? new HashSet<>() : null));
		return buf.toString();
	}
	
	/**
	 * Internal function, scan an object and get its string representation
	 * @param o An object to scan
	 * @param buf A String buffer to prevent high alloc count
	 * @param dejaVu A Set of object already scanned to prevent reference loop
	 */
	private static void deepToString(Object o, StringBuilder buf, Set<Object> dejaVu) {
		if (o == null) {
			buf.append("null");
			return;
		}
		
		if(dejaVu != null) {
			if (dejaVu.contains(o)) {
				buf.append("[...]");
				return;
			}
			
			dejaVu.add(o);
		}
		
		Class<?> clazz = o.getClass();
		if (clazz.isArray()) {
			if (clazz == byte[].class) {
				buf.append(Arrays.toString((byte[]) o));
			} else if (clazz == short[].class) {
				buf.append(Arrays.toString((short[]) o));
			} else if (clazz == int[].class) {
				buf.append(Arrays.toString((int[]) o));
			} else if (clazz == long[].class) {
				buf.append(Arrays.toString((long[]) o));
			} else if (clazz == char[].class) {
				buf.append(Arrays.toString((char[]) o));
			} else if (clazz == float[].class) {
				buf.append(Arrays.toString((float[]) o));
			} else if (clazz == double[].class) {
				buf.append(Arrays.toString((double[]) o));
			} else if (clazz == boolean[].class) {
				buf.append(Arrays.toString((boolean[]) o));
			} else { // element is an array of object references
				Object[] objs = (Object[]) o;
				if(objs.length == 0) {
					buf.append("[]");
				} else {
					int last = objs.length - 1;
					buf.append('[');
					for (int t = 0; t < last; t++) {
						deepToString(objs[t], buf, dejaVu);
						buf.append(", ");
					}
					deepToString(objs[last], buf, dejaVu);
					buf.append(']');
				}
			}
		} else if (o instanceof Map) {
			Iterator<Map.Entry<?, ?>> iter = ((Set<Map.Entry<?, ?>>) ((Map) o).entrySet()).iterator();
			if(iter.hasNext()) {
				buf.append('{');
				Map.Entry<?, ? > first = iter.next();
				deepToString(first.getKey(), buf, dejaVu);
				buf.append("->");
				deepToString(first.getValue(), buf, dejaVu);
				
				iter.forEachRemaining(entry -> {
					buf.append(", ");
					deepToString(entry.getKey(), buf, dejaVu);
					buf.append("->");
					deepToString(entry.getValue(), buf, dejaVu);
				});
				buf.append('}');
			} else {
				buf.append("{}");
			}
		} else if (o instanceof Collection) {
			Iterator<Object> iter = ((Collection) o).iterator();
			if(iter.hasNext()) {
				buf.append('(');
				deepToString(iter.next(), buf, dejaVu);
				
				iter.forEachRemaining(elem -> {
					buf.append(", ");
					deepToString(elem, buf, dejaVu);
				});
				
				buf.append(')');
			} else {
				buf.append("()");
			}
		} else if (o instanceof String) {
			buf.append('\"').append((String) o).append('\"');
		} else if (clazz == Object.class) {
			buf.append("obj");
		} else {
			buf.append(o.toString());
		}
	}
	
}
