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
	
	public static int diff(int x, int y) {
		return Math.abs((x & 0xFF) - (y & 0xFF))
			+ Math.abs(((x & 0xFF00) >> 8) - ((y & 0xFF00) >> 8))
			+ Math.abs(((x & 0xFF0000) >> 16) - ((y & 0xFF0000) >> 16 ));
	}
	
	public static int combine(int r, int g, int b) {
		return (r << 16) | (g << 8) | b;
	}
	
}