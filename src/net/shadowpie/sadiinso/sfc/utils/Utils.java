package net.shadowpie.sadiinso.sfc.utils;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;

public class Utils {

	// Suppresses default constructor, ensuring non-instantiability.
	private Utils() {}
	
	/**
	 * Split a string at the last occurence of the given separator
	 * @param src The source string
	 * @param separator The separator
	 * @return The two parts or the source string if the separator was not found
	 */
	public static String[] splitLastIndexOf(String src, String separator, boolean omitSeparator) {
		int i = src.lastIndexOf(separator);
		if(i == -1)
			return new String[] { src };
		else
			return new String[] { src.substring(0, i), omitSeparator ? (i == (src.length() - 1) ? "" : src.substring(i + 1)) : src.substring(i) };
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
			search: for (; from < to - len; from++) {
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

	/*
	 * map that contain the emote alphabet, the glyphs cannot be stored as a simple
	 * char
	 */
	private static final TCharObjectMap<String> emoteAlphabet = new TCharObjectHashMap<>(26, 1, '#');
	static {
		String[] glyphs = new String[] { "🇦", "🇧", "🇨", "🇩", "🇪", "🇫", "🇬", "🇭", "🇮", "🇯", "🇰", "🇱", "🇲", "🇳", "🇴", "🇵", "🇶", "🇷", "🇸", "🇹",
				"🇺", "🇻", "🇼", "🇽", "🇾", "🇿" };
		char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();

		for (int t = 0; t < alphabet.length; t++)
			emoteAlphabet.put(alphabet[t], glyphs[t]);
	}

	/**
	 * Convert a String to it's emote representation
	 * 
	 * @param str A string that contains only letters
	 * @return The emote representation of the given string
	 */
	public static String strToEmote(String str) {
		if (str.length() == 1)
			return emoteAlphabet.get(str.charAt(0));

		StringBuilder builder = new StringBuilder(str.length() * 3 - 1);
		char[] chars = str.toCharArray();

		for (int t = 0, len = chars.length - 1; t < chars.length; t++) {
			builder.append(emoteAlphabet.get(chars[t]));

			if (t < len)
				builder.append(' ');
		}

		return builder.toString();
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
		
		if(!ImageIO.write(img, format, baos))
			throw new IOException("ImageIO.write returned false");
		
		baos.flush();
		result = baos.toByteArray();
		baos.close();

		return result;
	}
	
	/**
	 * Return a ressource file located in /data/ inside the jar file as a String
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
	 * @param name The ressource name
	 */
	public static InputStream getDataRessource(String name) {
		return getRessource("/data/" + name);
	}
	
	/**
	 * Return an inputStream pointing to a ressource inside the jar file
	 * @param name The ressource name
	 */
	public static InputStream getRessource(String name) {
		return Utils.class.getResourceAsStream(name);
	}

}
