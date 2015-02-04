package com.johnston.test;

public class StaticLibrary {
	
	public static final int WHOLE = 0;
	public static final int TENTHS = 1;
	public static final int HUNDREDTHS = 2;
	public static final int THOUSANDTHS = 3;
	
	/**
	 * Rounds the specified number to the provided precision. Named precisions are:
	 * <ul>
	 * 	<li> WHOLE - rounds to nearest whole number</li>
	 * 	<li> TENS - rounds to nearest tenth</li>
	 * 	<li> HUNDREDTHS - rounds to nearest hundred</li>
	 * 	<li> THOUSANDTHS - rounds to nearest thousandth</li>
	 * </ul>
	 * Should a precision other than these be required, then provide the number of places
	 * to the right of the radix point to round the number. Negative precisions are supported.
	 * @param a - the number to be rounded
	 * @param precision - the number of places to the right of the radix point to round to
	 * @return the specified number rounded to the given precision.
	 */
	public static double roundToNearest(double a, int precision) {
		double precisionSize = Math.pow(10.0, precision);
		return Math.round(a * precisionSize) / precisionSize;
	}
}
