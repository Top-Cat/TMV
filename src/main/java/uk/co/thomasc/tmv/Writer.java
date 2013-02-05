package uk.co.thomasc.tmv;

import com.xuggle.xuggler.IRational;

import uk.co.thomasc.tmv.image.AudioFrame;
import uk.co.thomasc.tmv.image.Frame;
import uk.co.thomasc.tmv.image.VideoFrame;
import uk.co.thomasc.tmv.io.Encoder;

public class Writer extends Thread {
	
	private int written = 0;
	private long startTime = System.currentTimeMillis();
	
	private String outFile;
	private IRational frameRate;
	private long totalFrames;
	private int bitrate;
	private int samplerate;
	
	public Writer(String outFile, int bitrate, IRational frameRate, long totalFrames, int samplerate) {
		setName("Writer");
		this.frameRate = frameRate;
		this.totalFrames = totalFrames;
		this.outFile = outFile;
		this.bitrate = bitrate;
		this.samplerate = samplerate;
	}
	
	@Override
	public void run() {
		Encoder enc = new Encoder(outFile, Main.getWidth(), Main.getHeight(), bitrate, samplerate, frameRate);
		Frame frm;
		
		while (Main.isWorking() || Main.completed.size() > 0 || Main.toProcess.size() > 0) {
			if ((frm = Main.completed.poll()) != null) {
				if (frm instanceof VideoFrame) {
					enc.writeFrame(((VideoFrame) frm).getOutputFrame(), frm.getTimestamp());
					written++;
				} else {
					enc.writeAudio(((AudioFrame) frm).getSamples(), frm.getTimestamp());
				}
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long duration = (System.currentTimeMillis() - startTime) / 1000;
			String nowString = String.format("%d:%02d:%02d", duration/3600, (duration%3600)/60, (duration%60));
			String percent = String.format("%,.2f", (written * 100d) / totalFrames);
			System.out.print(nowString + " (" + percent + "%) " + written + " frames rendered, " + Main.isWorking() + ", " + Main.toProcess.size() + " -> " + Main.completed.size() + "      \r");
		}
		System.out.println("");
		
		enc.close();
	}
	
}