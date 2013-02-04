package uk.co.thomasc.tmv.match;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import uk.co.thomasc.tmv.image.Cell;

public class MatcherTest {
	
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
	
	int[] pixels = new int[] {
			183, 193, 225,
			158, 170, 205,
			146, 153, 206,
			146, 153, 206,
			146, 153, 206,
			146, 153, 206,
			143, 151, 207,
			133, 147, 209,
			196, 196, 218,
			159, 169, 202,
			146, 153, 206,
			146, 153, 206,
			146, 153, 206,
			146, 153, 206,
			134, 148, 208,
			141, 151, 207,
			196, 196, 216,
			159, 169, 199,
			144, 151, 195,
			145, 152, 201,
			146, 153, 206,
			146, 153, 206,
			134, 148, 208,
			140, 150, 206,
			197, 201, 216,
			161, 171, 198,
			151, 162, 195,
			145, 152, 201,
			146, 153, 206,
			146, 153, 206,
			146, 153, 206,
			145, 152, 205,
			206, 211, 223,
			182, 190, 212,
			161, 180, 219,
			156, 180, 221,
			156, 180, 221,
			161, 180, 219,
			162, 180, 219,
			160, 182, 222,
			206, 211, 223,
			184, 194, 221,
			176, 191, 226,
			171, 191, 228,
			171, 191, 228,
			176, 191, 226,
			167, 191, 229,
			163, 194, 234,
			206, 211, 223,
			183, 195, 225,
			177, 191, 226,
			177, 191, 226,
			175, 196, 241,
			176, 193, 233,
			164, 195, 235,
			162, 196, 236,
			200, 207, 224,
			179, 193, 226,
			177, 191, 226,
			177, 191, 226,
			174, 198, 247,
			174, 198, 247,
			168, 201, 242,
			165, 201, 239
	};
	
	public MatcherTest() {
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
	
	@Test
	public void slowTest() {
		Cell correct = new Cell(32, 15, 7);
		
		BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = (WritableRaster) image.getData();
		raster.setPixels(0, 0, 8, 8, pixels);
		image.setData(raster);
		
		Cell cell = slowMatcher.match(image);
		Assert.assertEquals(correct, cell);
	}
	
	@Test
	public void fastTest() {
		Cell correct = new Cell(177, 13, 11);
		
		BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = (WritableRaster) image.getData();
		raster.setPixels(0, 0, 8, 8, pixels);
		image.setData(raster);
		
		Cell cell = new Fast().match(image);
		Assert.assertEquals(correct, cell);
	}
	
}