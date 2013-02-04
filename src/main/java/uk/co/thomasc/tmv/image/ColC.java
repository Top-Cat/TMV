package uk.co.thomasc.tmv.image;

import lombok.Getter;

public class ColC {
	
	@Getter private int avg;
	@Getter private byte col1;
	@Getter private byte col2;
	
	public ColC(int col1, int col2, int color) {
		this.avg = color;
		this.col1 = (byte) col1;
		this.col2 = (byte) col2;
	}
	
}