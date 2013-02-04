package uk.co.thomasc.tmv.image;

import lombok.Getter;

public abstract class Frame {
	
	@Getter private long timestamp;
	
	public Frame(long timestamp) {
		this.timestamp = timestamp;
	}
	
}