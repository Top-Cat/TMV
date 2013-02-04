package uk.co.thomasc.tmv.match;

import java.awt.Color;
import java.awt.image.BufferedImage;
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
				ColC col = new ColC(stream.read(), stream.read(), new Color(stream.read(), stream.read(), stream.read()));
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
	public Cell match(BufferedImage cell) {
		Cell result = new Cell(177, 0, 0);
		
		int diff = 0;
		int min = Integer.MAX_VALUE;
		Color avg = getAvgColour(cell);
		
		for (int i = 0; i < 136; i++) {
			diff = Math.abs(avgs[i].getAvg().getRed() - avg.getRed()) + Math.abs(avgs[i].getAvg().getGreen() - avg.getGreen()) + Math.abs(avgs[i].getAvg().getBlue() - avg.getBlue());
			if (diff < min) {
				min = diff;
				result.setFromAverage(avgs[i]);
			}
		}
		
		return result;
	}
	
	private static Color getAvgColour(BufferedImage input) {
		int tRed = 0;
		int tBlue = 0;
		int tGreen = 0;
		for (int x = 0; x < input.getWidth(); x++) {
			for (int y = 0; y < input.getHeight(); y++) {
				Color color = new Color(input.getRGB(x, y));
				
				tRed += color.getRed();
				tGreen += color.getGreen();
				tBlue += color.getBlue();
			}
		}
		tRed = (int)(tRed / (input.getWidth() * input.getHeight()));
		tGreen = (int)(tGreen / (input.getWidth() * input.getHeight()));
		tBlue = (int)(tBlue / (input.getWidth() * input.getHeight()));
		
		return new Color(tRed, tGreen, tBlue);
	}
	
}