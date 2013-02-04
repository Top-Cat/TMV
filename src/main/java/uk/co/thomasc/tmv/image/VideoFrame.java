package uk.co.thomasc.tmv.image;

import java.awt.image.BufferedImage;

import lombok.Getter;

public class VideoFrame extends Frame {
	
	@Getter private BufferedImage originalFrame;
	@Getter private BufferedImage outputFrame;
	
	public VideoFrame(BufferedImage originalFrame, long timestamp) {
		super(timestamp);
		this.originalFrame = originalFrame;
	}

	public void setOutput(BufferedImage render) {
		this.outputFrame = render;
	}
	
}