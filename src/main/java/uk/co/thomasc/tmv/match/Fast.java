package uk.co.thomasc.tmv.match;

import java.io.IOException;
import java.io.InputStream;

import uk.co.thomasc.tmv.image.Cell;
import uk.co.thomasc.tmv.image.ColC;

public class Fast implements Matcher {
	
	private ColC[] avgs = new ColC[136];
	
	public Fast() {
		InputStream stream = null;
		try {
			stream = Fast.class.getResourceAsStream("/Fcols.dat");
			for (int i = 0; i < 136; i++) {
				ColC col = new ColC(stream.read(), stream.read(), Color.combine(stream.read(), stream.read(), stream.read()));
				avgs[i] = col;
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
			}
		}
	}

	@Override
	public Cell match(int[] cellColor) {
		Cell result = new Cell(177, 0, 0);
		
		int diff = 0;
		int min = Integer.MAX_VALUE;
		int avg = getAvgColour(cellColor);
		
		for (int i = 0; i < 136; i++) {
			if ((diff = Color.diff(avgs[i].getAvg(), avg)) < min) {
				min = diff;
				result.setFromAverage(avgs[i]);
			}
		}
		
		return result;
	}
	
	private static int getAvgColour(int[] input) {
		int tRed = 0;
		int tBlue = 0;
		int tGreen = 0;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				int color = input[y * 8 + x];
				
				tRed += Color.getRed(color);
				tGreen += Color.getGreen(color);
				tBlue += Color.getBlue(color);
			}
		}
		tRed = tRed / 64;
		tGreen = tGreen / 64;
		tBlue = tBlue / 64;
		
		return Color.combine(tRed, tGreen, tBlue);
	}
	
}