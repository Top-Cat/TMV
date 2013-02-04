package uk.co.thomasc.tmv;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import uk.co.thomasc.tmv.image.Cell;
import uk.co.thomasc.tmv.image.VideoFrame;
import uk.co.thomasc.tmv.match.Color;
import uk.co.thomasc.tmv.match.Fast;
import uk.co.thomasc.tmv.match.Matcher;
import uk.co.thomasc.tmv.match.Slow;

public class Worker extends Thread {
	
	private static int threshold = 60;
	private Cell[] result = new Cell[Main.getCellCount()];
	
	private Fast fastMatcher = new Fast();
	private Slow slowMatcher;
	
	private boolean[][] chars = new boolean[256][64];
	private int[] colours = new int[] {
		0xFF000000,
		0xFF0000AA,
		0xFF00AA00,
		0xFF00AAAA,
		0xFFAA0000,
		0xFFAA00AA,
		0xFFAA5500,
		0xFFAAAAAA,
		0xFF555555,
		0xFF5555FF,
		0xFF55FF55,
		0xFF55FFFF,
		0xFFFF5555,
		0xFFFF55FF,
		0xFFFFFF55,
		0xFFFFFFFF
	};
	
	public Worker() {
		InputStream stream = null;
		try {
			stream = Slow.class.getResourceAsStream("/font.bin");
			byte temp;
			byte var;
			for (int i = 0; i < 256; i++) {
				for (int row = 0; row < 8; row++) {
					temp = (byte) stream.read();
					for (int c = 7; c >= 0; c--) {
						var = (byte) (1 << c);
						var = (byte) (var & temp);
						
						chars[i][(7 - c) * 8 + row] = var > 0; //7-c reverses endian.
					}
				}
			}
			slowMatcher = new Slow(chars, colours);
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
	public void run() {
		VideoFrame frm;
		while (Main.isWorking() || Main.toProcess.size() > 0) {
			if ((frm = Main.toProcess.poll()) != null) {
				for (int row = 0; row < Main.getVCells(); row++) {
					for (int col = 0; col < Main.getHCells(); col++) {
						BufferedImage cell = frm.getOriginalFrame().getSubimage(8 * col, 8 * row, 8, 8);
						Matcher matcher = (getStdDev(cell) < threshold) ? fastMatcher : slowMatcher;
						result[row * Main.getHCells() + col] = matcher.match(cell);
					}
				}
				frm.setOutput(render());
				Main.completed.add(frm);
			}
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public BufferedImage render() { //input length must be correct size or else. Renders into a bitmap based on the cell
		BufferedImage output = new BufferedImage(Main.getWidth(), Main.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		
		Graphics2D graphics = output.createGraphics();
		for (int row = 0; row < Main.getVCells(); row++) { //scan top to bottom, left to right each time.
			for (int col = 0; col < Main.getHCells(); col++) {
				graphics.drawImage(getFBitmap(result[row * Main.getHCells() + col]), null, col * 8, row * 8);
			}
		}
		return output;
	}
	
	private BufferedImage getFBitmap(Cell input) {
		BufferedImage result = new BufferedImage(8, 8, BufferedImage.TYPE_3BYTE_BGR);
		for (int row = 0; row <= 7; row++) {
			for (int col = 0; col < 8; col++) {
				result.setRGB(col, row, colours[chars[input.getCharacter()][col * 8 + row] ? input.getColour() : input.getColourB()]);
			}
		}
		return result;
	}
	
	private double getStdDev(BufferedImage input) {
		long totalR = 0;
		long totalG = 0;
		long totalB = 0;
		long sigmaR2 = 0;
		long sigmaG2 = 0;
		long sigmaB2 = 0;

		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				int col = input.getRGB(x, y);
				
				totalR += Color.getRed(col);
				totalG += Color.getGreen(col);
				totalB += Color.getBlue(col);
				
				sigmaR2 += Math.pow(Color.getRed(col), 2);
				sigmaG2 += Math.pow(Color.getGreen(col), 2);
				sigmaB2 += Math.pow(Color.getBlue(col), 2);
			}
		}
		
		double mRed = Math.pow(totalR / 64, 2);
		double mGreen = Math.pow(totalG / 64, 2);
		double mBlue = Math.pow(totalB / 64, 2);

		double devRed = Math.sqrt((sigmaR2 - (64 * mRed)) / 63);
		double devGreen = Math.sqrt((sigmaG2 - (64 * mGreen)) / 63);
		double devBlue = Math.sqrt((sigmaB2 - (64 * mBlue)) / 63);

		return (devRed + devGreen + devBlue);
	}
	
}