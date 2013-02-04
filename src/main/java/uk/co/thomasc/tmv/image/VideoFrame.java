package uk.co.thomasc.tmv.image;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import uk.co.thomasc.tmv.Main;

import lombok.Getter;

public class VideoFrame extends Frame {
	
	private BufferedImage originalFrame;
	private BufferedImage originalResizedFrame;
	@Getter private BufferedImage outputFrame;
	
	public VideoFrame(BufferedImage originalFrame, long timestamp) {
		super(timestamp);
		this.originalFrame = originalFrame;
	}

	public BufferedImage getOriginalFrame() {
		if (originalResizedFrame == null) {
			originalResizedFrame = new BufferedImage(Main.getWidth(), Main.getHeight(), BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			at.scale((Main.getWidth() * 1d) / originalFrame.getWidth(), (Main.getHeight() * 1d) / originalFrame.getHeight());
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
			originalResizedFrame = scaleOp.filter(originalFrame, originalResizedFrame);
			originalFrame = null;
		}
		return originalResizedFrame;
	}
	
	public void setOutput(BufferedImage render) {
		this.outputFrame = render;
	}
	
}