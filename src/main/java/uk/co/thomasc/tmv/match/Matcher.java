package uk.co.thomasc.tmv.match;

import java.awt.image.BufferedImage;

import uk.co.thomasc.tmv.image.Cell;

public interface Matcher {
	
	public Cell match(BufferedImage cell);
	
}