package uk.co.thomasc.tmv.match;

import uk.co.thomasc.tmv.image.Cell;

public class Slow implements Matcher {
	
	private int[] colours;
	private boolean[][] chars;
	
	public Slow(boolean[][] chars, int[] colours) {
		this.chars = chars;
		this.colours = colours;
	}

	@Override
	public Cell match(int[] cellColor) {
		Cell result = new Cell(0, 0, 13);
		
		int diff[] = new int[2];
		int min = Integer.MAX_VALUE;
		byte[] mcommon = getMCommon(cellColor);
		
		for (int cha = 3; cha < 255; cha++) {
			for (int pix = 0; pix < 64; pix++) {
				boolean index = chars[cha][pix] ? false : true;
				int color = cellColor[pix];
				
				for (int i = 0; i < 2; i++) {
					diff[i] += Color.diff(color, colours[mcommon[index ? 1 : 0]]);
					index = !index;
				}
			}
			
			for (int i = 0; i < 2; i++) {
				if (diff[i] < min) {
					min = diff[i];
					result.setValues(cha, mcommon[i], mcommon[1 - i]);
				}
				diff[i] = 0;
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
				int cell = input[y * 8 + x];
				minval = Integer.MAX_VALUE;
				min = 0;
				for (int colour = 0; colour < 16; colour++) {
					if ((diff = Color.diff(colours[colour], cell)) < minval) {
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