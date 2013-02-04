package uk.co.thomasc.tmv.image;

import lombok.Getter;

import com.xuggle.xuggler.IAudioSamples;

public class AudioFrame extends Frame {

	@Getter private IAudioSamples samples;
	
	public AudioFrame(IAudioSamples samples, long timestamp) {
		super(timestamp);
		this.samples = samples;
	}
	
}