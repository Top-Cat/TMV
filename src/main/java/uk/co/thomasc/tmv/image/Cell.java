package uk.co.thomasc.tmv.image;

import lombok.Getter;

public class Cell {
	
	@Getter private int character;
	@Getter private byte colour;
	@Getter private byte colourB;
	
	public Cell(int character, int colour, int colourB) {
		setValues(character, (byte) colour, (byte) colourB);
	}

	public void setFromAverage(ColC colC) {
		colour = colC.getCol1();
		colourB = colC.getCol2();
	}

	public void setValues(int character, byte colour, byte colourB) {
		this.character = character;
		this.colour = colour;
		this.colourB = colourB;
	}
	
	@Override
	public String toString() {
		return "<Cell: " + character + ", " + colour + ", " + colourB + ">";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Cell) {
			Cell other = (Cell) obj;
			if (other.getCharacter() == getCharacter() && other.getColour() == getColour() && other.getColourB() == getColourB()) {
				return true;
			}
		}
		return false;
	}
	
}