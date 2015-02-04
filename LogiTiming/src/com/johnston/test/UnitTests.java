package com.johnston.test;

public class UnitTests {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		roundToNearestTestNoChange();
		roundToNearestTestRoundUp();
		roundToNearestTestRoundDown();
		roundToNearestTestRoundToWhole();
		roundToNearestTestGreaterPrecisionRoundUp();
		roundToNearestTestGreaterPrecisionRoundDown();
		roundToNearestTestLessPrecisionRoundUp();
		roundToNearestTestLessPrecisionRoundDown();
	}
	
	static void roundToNearestTestNoChange() {
		double inputNum = 108.500;
		int inputPrecision = StaticLibrary.HUNDREDTHS;
		double correctOutput = 108.50;
		assertTrue(StaticLibrary.roundToNearest(inputNum, inputPrecision) == correctOutput, getCurrentMethodName());
	}
	
	static void roundToNearestTestRoundUp() {
		double inputNum = 108.1495;
		int inputPrecision = StaticLibrary.THOUSANDTHS;
		double correctOutput = 108.150;
		assertTrue(StaticLibrary.roundToNearest(inputNum, inputPrecision) == correctOutput, getCurrentMethodName());
	}
	
	static void roundToNearestTestRoundDown() {
		double inputNum = 108.549;
		int inputPrecision = StaticLibrary.TENTHS;
		double correctOutput = 108.5;
		assertTrue(StaticLibrary.roundToNearest(inputNum, inputPrecision) == correctOutput, getCurrentMethodName());
	}
	
	static void roundToNearestTestRoundToWhole() {
		double inputNum = 108.500;
		int inputPrecision = StaticLibrary.WHOLE;
		double correctOutput = 109;
		assertTrue(StaticLibrary.roundToNearest(inputNum, inputPrecision) == correctOutput, getCurrentMethodName());
	}
	
	static void roundToNearestTestGreaterPrecisionRoundUp() {
		double inputNum = 108.51543956;
		int inputPrecision = 7;
		double correctOutput = 108.5154396;
		assertTrue(StaticLibrary.roundToNearest(inputNum, inputPrecision) == correctOutput, getCurrentMethodName());
	}
	
	static void roundToNearestTestGreaterPrecisionRoundDown() {
		double inputNum = 108.515408533383;
		int inputPrecision = 11;
		double correctOutput = 108.51540853338;
		assertTrue(StaticLibrary.roundToNearest(inputNum, inputPrecision) == correctOutput, getCurrentMethodName());
	}
	
	static void roundToNearestTestLessPrecisionRoundUp() {
		double inputNum = 108.515;
		int inputPrecision = -1;
		double correctOutput = 110.0;
		assertTrue(StaticLibrary.roundToNearest(inputNum, inputPrecision) == correctOutput, getCurrentMethodName());
	}
	
	static void roundToNearestTestLessPrecisionRoundDown() {
		double inputNum = 108.782409;
		int inputPrecision = -2;
		double correctOutput = 100.0;
		assertTrue(StaticLibrary.roundToNearest(inputNum, inputPrecision) == correctOutput, getCurrentMethodName());
	}
	
	static void assertTrue(boolean b, String testName) {
		System.out.println(testName + ((b)?" succeeded":" failed"));
	}
	
	/**
	 * A method name retrieval method courtesy of Evan Mulawaski on Stack Overflow.
	 * @return the name of method that called it
	 */
	static String getCurrentMethodName() {
	     StackTraceElement stackTraceElements[] = (new Throwable()).getStackTrace();
	     return stackTraceElements[1].toString();
	}

}
