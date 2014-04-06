package com.harmoneye.tonecircle;

public class Modulo {
	public static int modulo(int value, int base) {
		return ((value % base) + base) % base;
	}

	public static double modulo(double value, double base) {
		return ((value % base) + base) % base;
	}
}
