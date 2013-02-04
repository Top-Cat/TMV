package uk.co.thomasc.tmv.io;

import java.awt.image.BufferedImage;

import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

public class Encoder {
	
	private int width;// = 1920;
	private int height;// = 1080;
	
	private ICodec codec = ICodec.findEncodingCodecByName("libx264");
	private ICodec audioCodec = ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_MP3);
	private IContainer outContainer;
	private IStream outStream;
	private IStreamCoder outVideoStreamCoder;
	private IStreamCoder outAudioStreamCoder;
	private IPacket packet;
	
	public Encoder(String fileName, int outWidth, int outHeight, int bitrate, IRational frameRate) {
		this.width = outWidth;
		this.height = outHeight;
		
		outContainer = IContainer.make();
		int retval = outContainer.open(fileName, IContainer.Type.WRITE, null);
		if (retval < 0) {
			throw new RuntimeException("Could not open output file D:");
		}
		
		//ICodec codec = ICodec.guessEncodingCodec(null, null, "/home/top_cat/test.mpg", null, ICodec.Type.CODEC_TYPE_VIDEO);
		
		outStream = outContainer.addNewStream(codec);
		outVideoStreamCoder = outStream.getStreamCoder();
		
		outVideoStreamCoder.setNumPicturesInGroupOfPictures(1);
		outVideoStreamCoder.setBitRate(bitrate * 1024);
		outVideoStreamCoder.setBitRateTolerance(200 * 1024);
		outVideoStreamCoder.setPixelType(IPixelFormat.Type.YUV420P);
		outVideoStreamCoder.setHeight(height);
		outVideoStreamCoder.setWidth(width);
		outVideoStreamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
		outVideoStreamCoder.setGlobalQuality(0);
		
		outVideoStreamCoder.setFrameRate(frameRate);
		outVideoStreamCoder.setTimeBase(IRational.make(frameRate.getDenominator(), frameRate.getNumerator()));
		
		//**//
		IStream outAudioStream = outContainer.addNewStream(audioCodec);
		outAudioStreamCoder = outAudioStream.getStreamCoder();
		
		outAudioStreamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, false);
		outAudioStreamCoder.setBitRate(32000);
		outAudioStreamCoder.setSampleRate(44100);
		outAudioStreamCoder.setChannels(2);
		//outAudioStreamCoder.setProperty("frame_size", 1024);
		//outAudioStreamCoder.setProperty("strict", -2);
		
		//**//
		retval = outVideoStreamCoder.open(null, null);
		if (retval < 0) {
			throw new RuntimeException("could not open video encoder");
		}
		retval = outAudioStreamCoder.open(null, null);
		if (retval < 0) {
			throw new RuntimeException("could not open audio encoder");
		}
		retval = outContainer.writeHeader();
		if (retval < 0) {
			throw new RuntimeException("could not write file header");
		}
		
		packet = IPacket.make();
	}
	
	public void writeFrame(BufferedImage originalImage, long timestamp) {
		BufferedImage worksWithXugglerBufferedImage = convertToType(originalImage, BufferedImage.TYPE_3BYTE_BGR);
		IConverter converter = ConverterFactory.createConverter(worksWithXugglerBufferedImage, IPixelFormat.Type.YUV420P);
		
		IVideoPicture outFrame = converter.toPicture(worksWithXugglerBufferedImage, timestamp);
		outFrame.setQuality(0);
		int retval = outVideoStreamCoder.encodeVideo(packet, outFrame, 0);
		if (retval < 0) {
			throw new RuntimeException("could not encode video");
		}
		if (packet.isComplete()) {
			retval = outContainer.writePacket(packet);
			if (retval < 0) {
				throw new RuntimeException("could not save packet to container");
			}
		}
	}
	
	public void writeAudio(IAudioSamples samples, long timestamp) {
		int samplesConsumed = 0;
		while (samplesConsumed < samples.getNumSamples()) {
			int retval = outAudioStreamCoder.encodeAudio(packet, samples, samplesConsumed);
			if (retval < 0) {
				throw new RuntimeException("could not encode audio");
			} else {
				samplesConsumed += retval;
			}
			if (packet.isComplete()) {
				retval = outContainer.writePacket(packet);
				if (retval < 0) {
					throw new RuntimeException("could not save packet to container");
				}
			}
		}
	}
	
	public void close() {
		int retval = outContainer.writeTrailer();
		if (retval < 0) {
			throw new RuntimeException("could not write trailer");
		}
		
		retval = outVideoStreamCoder.close();
		if (retval < 0) {
			throw new RuntimeException("could not close coder");
		}
		
		retval = outAudioStreamCoder.close();
		if (retval < 0) {
			throw new RuntimeException("could not close coder");
		}
		
		retval = outContainer.close();
		if (retval < 0) {
			throw new RuntimeException("could not close container");
		}
	}
	
	private BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
		BufferedImage image;

		// if the source image is already the target type, return the source image

		if (sourceImage.getType() == targetType) {
			image = sourceImage;

		// otherwise create a new image of the target type and draw the new image 
		} else {
			image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), targetType);
			image.getGraphics().drawImage(sourceImage, 0, 0, null);
		}

		return image;
	}
	
}