package uk.co.thomasc.tmv.image;

import java.awt.Color;

import lombok.Getter;

public class ColC {
	
	@Getter private Color avg;
	@Getter private byte col1;
	@Getter private byte col2;
	
	public ColC(int col1, int col2, Color color) {
		this.avg = color;
		this.col1 = (byte) col1;
		this.col2 = (byte) col2;
	}
	
}