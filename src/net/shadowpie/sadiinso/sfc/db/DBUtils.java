package net.shadowpie.sadiinso.sfc.db;

import org.apache.commons.lang3.StringUtils;

public class DBUtils {

	/**
	 * Create a SQL Array
	 */
	public static String toSQLArray(Object[] array) {
		return join(array, true);
	}
	
	/**
	 * Create a SQL Set
	 */
	public static String toSQLSet(Object[] array) {
		return join(array, false);
	}
	
	/**
	 * Create a SQL Array
	 */
	public static String toSQLArray(long[] array) {
		return join(array, true);
	}
	
	/**
	 * Create a SQL Set
	 */
	public static String toSQLSet(long[] array) {
		return join(array, false);
	}
	
	/**
	 * Create a SQL Array
	 */
	public static String toSQLArray(int[] array) {
		return join(array, true);
	}
	
	/**
	 * Create a SQL Set
	 */
	public static String toSQLSet(int[] array) {
		return join(array, false);
	}
	
	//#######
	//HELPERS
	//#######
	
	private static String join(long[] array, boolean enclose) {
		if((array == null) || (array.length <= 0)) {
			return StringUtils.EMPTY;
		}
		
		StringBuilder builder = new StringBuilder();
		
		if(enclose) {
			builder.append('(');
		}
		
		for(int t = 0; t < array.length - 1; t++) {
			builder.append(array[t]);
			builder.append(',');
		}
		
		builder.append(array[array.length - 1]);
		
		if(enclose) {
			builder.append(')');
		}
		
		return builder.toString();
	}
	
	private static String join(int[] array, boolean enclose) {
		if((array == null) || (array.length <= 0)) {
			return StringUtils.EMPTY;
		}
		
		StringBuilder builder = new StringBuilder();
		
		if(enclose) {
			builder.append('(');
		}
		
		for(int t = 0; t < array.length - 1; t++) {
			builder.append(array[t]);
			builder.append(',');
		}
		
		builder.append(array[array.length - 1]);
		
		if(enclose) {
			builder.append(')');
		}
		
		return builder.toString();
	}
	
	private static String join(Object[] array, boolean enclose) {
		if((array == null) || (array.length <= 0))
			return StringUtils.EMPTY;
		
		StringBuilder builder = new StringBuilder();
		
		if(enclose) {
			builder.append('(');
		}
		
		for(int t = 0; t < array.length - 1; t++) {
			builder.append(array[t]);
			builder.append(',');
		}
		
		builder.append(array[array.length - 1]);
		
		if(enclose) {
			builder.append(')');
		}
		
		return builder.toString();
	}
	
}
