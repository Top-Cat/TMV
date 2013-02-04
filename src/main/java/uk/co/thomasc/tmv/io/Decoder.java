package uk.co.thomasc.tmv.io;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import lombok.Getter;

import uk.co.thomasc.tmv.image.AudioFrame;
import uk.co.thomasc.tmv.image.Frame;
import uk.co.thomasc.tmv.image.VideoFrame;

import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

public class Decoder {
	
	private IContainer container;
	private IStreamCoder videoStreamCoder;
	private IStreamCoder audioStreamCoder;
	private int videoStreamId = -1;
	private int audioStreamId = -1;
	private IPacket pkt;
	private IConverter converter;
	
	private int offset;
	private IVideoPicture picture;
	private IAudioSamples samples;
	
	private int outWidth;
	private int outHeight;
	
	@Getter private IRational frameRate;
	@Getter private long totalFrames;
	
	public Decoder(String fileName, int outWidth, int outHeight) {
		this.outWidth = outWidth;
		this.outHeight = outHeight;
		
		container = IContainer.make();
		container.open(fileName, IContainer.Type.READ, null);
		
		int numStreams = container.getNumStreams();
		for (int i = 0; i < numStreams; i++) {
			IStream stream = container.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				videoStreamCoder = coder;
				videoStreamId = i;
			} else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
				audioStreamCoder = coder;
				audioStreamId = i;
			} else {
				continue;
			}
			coder.open(null, null);
		}
		
		if (videoStreamId == -1 || audioStreamId == -1) {
		      throw new RuntimeException("could not find audio and video stream in container");
		}
		
		frameRate = videoStreamCoder.getFrameRate();
		totalFrames = ((container.getDuration() * frameRate.getNumerator()) / frameRate.getDenominator()) / (1000 * 1000);
		
		pkt = IPacket.make();
		offset = pkt.getSize();
		converter = ConverterFactory.createConverter("XUGGLER-BGR-24", videoStreamCoder.getPixelType(), videoStreamCoder.getWidth(), videoStreamCoder.getHeight(), videoStreamCoder.getWidth(), videoStreamCoder.getHeight());
	}
	
	public Frame readFrame() {
		int retval = 0;
		do {
			if (offset < pkt.getSize()) {
				while (offset < pkt.getSize()) {
					if (pkt.getStreamIndex() == videoStreamId) {
						offset += videoStreamCoder.decodeVideo(picture, pkt, offset);
						if (picture.isComplete()) {
							BufferedImage img = converter.toImage(picture);
							
							BufferedImage after = new BufferedImage(outWidth, outHeight, BufferedImage.TYPE_INT_ARGB);
							AffineTransform at = new AffineTransform();
							at.scale((outWidth * 1d) / img.getWidth(), (outHeight * 1d) / img.getHeight());
							AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
							after = scaleOp.filter(img, after);
							
							return new VideoFrame(after, picture.getTimeStamp());
						}
					} else if (pkt.getStreamIndex() == audioStreamId) {
						offset += audioStreamCoder.decodeAudio(samples, pkt, offset);
						if (samples.isComplete()) {
							return new AudioFrame(samples, samples.getTimeStamp());
						}
					}
				}
			}
			retval = container.readNextPacket(pkt);
			if (pkt.getStreamIndex() == 0) {
				picture = IVideoPicture.make(videoStreamCoder.getPixelType(), videoStreamCoder.getWidth(), videoStreamCoder.getHeight());
			} else {
				samples = IAudioSamples.make(1024, audioStreamCoder.getChannels());
			}
			offset = 0;
		} while (retval >= 0);
		
		return null;
	}
	
	public void close() {
		int retval = videoStreamCoder.close();
		if (retval < 0) {
			throw new RuntimeException("could not close coder");
		}
		
		retval = audioStreamCoder.close();
		if (retval < 0) {
			throw new RuntimeException("could not close coder");
		}
		
		retval = container.close();
		if (retval < 0) {
			throw new RuntimeException("could not close container");
		}
	}
	
}