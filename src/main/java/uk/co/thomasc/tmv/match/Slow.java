package uk.co.thomasc.tmv.match;

import java.awt.image.BufferedImage;

import uk.co.thomasc.tmv.image.Cell;

public class Slow implements Matcher {
	
	private int[] colours;
	private boolean[][] chars;
	
	public Slow(boolean[][] chars, int[] colours) {
		this.chars = chars;
		this.colours = colours;
	}

	@Override
	public Cell match(BufferedImage cell) {
		Cell result = new Cell(0, 0, 13);
		
		int diff[] = new int[2];
		int min = Integer.MAX_VALUE;
		int[] cellColor = cell.getRGB(0, 0, 8, 8, null, 0, 8);
		byte[] mcommon = getMCommon(cellColor);
		
		for (int cha = 3; cha < 255; cha++) {
			diff = new int[] {0, 0};
			
			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					int index = chars[cha][x * 8 + y] ? 0 : 1;
					int color = cellColor[x * 8 + y];
					
					for (int i = 0; i < 2; i++) {
						diff[i] += Math.abs(Color.getRed(color) - Color.getRed(colours[mcommon[index]])) + Math.abs(Color.getGreen(color) - Color.getGreen(colours[mcommon[index]])) + Math.abs(Color.getBlue(color) - Color.getBlue(colours[mcommon[index]])); //0 and 1
						index = 1 - index;
					}
				}
			}
			
			int index = 0;
			for (int i = 0; i < 2; i++) {
				if (diff[i] < min) {
					min = diff[i];
					result.setValues(cha, mcommon[index], mcommon[1 - index]);
				}
				index = 1;
			}
		}
		
		return result;
	}
	
	private byte[] getMCommon(int[] input) {
		byte[] results = new byte[16]; //stores occurences

		int min;
		int minval;
		int diff;

		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				minval = Integer.MAX_VALUE;
				min = 0;
				for (int colour = 0; colour < 16; colour++) {
					int cell = input[x * 8 + y];
					diff = Math.abs(Color.getRed(colours[colour]) - Color.getRed(cell)) + Math.abs(Color.getGreen(colours[colour]) - Color.getGreen(cell)) + Math.abs(Color.getBlue(colours[colour]) - Color.getBlue(cell));
					if (diff < minval) {
						minval = diff;
						min = colour;
					}
				}
				results[min] += 1;
			}
		}
		byte[] output = new byte[2];
		int max = 0;

		for (int c = 0; c < 16; c++) {
			if (results[c] > max) {
				max = results[c];
				output[0] = (byte) c;
			}
		}

		//tweak results if we get black, as grey will often follow obliterating detail
		if (output[0] == 0) {
			results[8] = (byte) (results[8] * 0.3);
			results[7] = (byte) (results[7] * 0.6);
		} else if (output[0] == 8) {
			results[0] = (byte) (results[8] * 0.6);
			results[7] = (byte) (results[7] * 0.6);
		}
		results[output[0]] = 0;
		max = 0;

		for (int c = 0; c < 16; c++) {
			if (results[c] > max) {
				max = results[c];
				output[1] = (byte) c;
			}
		}

		return output;
	}
	
}