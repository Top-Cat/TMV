package uk.co.thomasc.tmv.match;

public class Color {
	
	public static int getRed(int color) {
		return (color >> 16) & 0x000000FF;
	}
	
	public static int getGreen(int color) {
		return (color >> 8) & 0x000000FF;
	}

	public static int getBlue(int color) {
		return color & 0x000000FF;
	}
	
}