package uk.co.thomasc.tmv;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import uk.co.thomasc.tmv.image.Cell;
import uk.co.thomasc.tmv.image.VideoFrame;
import uk.co.thomasc.tmv.match.Color;
import uk.co.thomasc.tmv.match.Fast;
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
						int[] cell = frm.getOriginalFrame().getSubimage(8 * col, 8 * row, 8, 8).getRGB(0, 0, 8, 8, null, 0, 8);
						result[row * Main.getHCells() + col] = ((getStdDev(cell) < threshold) ? fastMatcher : slowMatcher).match(cell);
					}
				}
				frm.setOutput(render());
				Main.completed.add(frm);
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public BufferedImage render() { //input length must be correct size or else. Renders into a bitmap based on the cell
		BufferedImage output = new BufferedImage(Main.getWidth(), Main.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		
		//Graphics2D graphics = output.createGraphics();
		for (int row = 0; row < Main.getVCells(); row++) { //scan top to bottom, left to right each time.
			for (int col = 0; col < Main.getHCells(); col++) {
				output.setRGB(col * 8, row * 8, 8, 8, getFBitmap(result[row * Main.getHCells() + col]), 0, 8);
				//graphics.drawImage(getFBitmap(result[row * Main.getHCells() + col]), null, col * 8, row * 8);
			}
		}
		return output;
	}
	
	private int[] getFBitmap(Cell input) {
		//BufferedImage result = new BufferedImage(8, 8, BufferedImage.TYPE_3BYTE_BGR);
		int[] result = new int[64];
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				result[row * 8 + col] = colours[chars[input.getCharacter()][col * 8 + row] ? input.getColour() : input.getColourB()];
				//result.setRGB(col, row, colours[chars[input.getCharacter()][col * 8 + row] ? input.getColour() : input.getColourB()]);
			}
		}
		return result;
	}
	
	public double getStdDev(int[] input) {
		long totalR = 0;
		long totalG = 0;
		long totalB = 0;
		long sigmaR2 = 0;
		long sigmaG2 = 0;
		long sigmaB2 = 0;

		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				int col = input[y * 8 + x];
				int r = Color.getRed(col);
				int g = Color.getRed(col);
				int b = Color.getRed(col);
				
				totalR += r;
				totalG += g;
				totalB += b;
				
				sigmaR2 += r * r;
				sigmaG2 += g * g;
				sigmaB2 += b * b;
			}
		}
		
		double mRed = (totalR * totalR) >> 6;
		double mGreen = (totalG * totalG) >> 6;
		double mBlue = (totalB * totalB) >> 6;
		
		// You could / 7.937253933 if you wanted to keep continuity
		return (int) (Math.sqrt(sigmaR2 - mRed) + Math.sqrt(sigmaG2 - mGreen) + Math.sqrt(sigmaB2 - mBlue)) >> 3;
	}
	
}