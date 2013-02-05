package uk.co.thomasc.tmv;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.beust.jcommander.JCommander;

import lombok.Getter;

import uk.co.thomasc.tmv.gui.CmdOptions;
import uk.co.thomasc.tmv.image.Frame;
import uk.co.thomasc.tmv.image.VideoFrame;
import uk.co.thomasc.tmv.io.Decoder;

public class Main {
	
	@Getter private static boolean working = true;
	@Getter private static int width = 480;
	@Getter private static int height = 360;
	
	public static void main(String[] args) {
		CmdOptions cmd = new CmdOptions();
		new JCommander(cmd, args);
		if (!cmd.isConsole()) {
			
		} else if (cmd.getInFile() != null && cmd.getOutFile() != null) {
			if (cmd.getSize().contains("x")) {
				String[] size = cmd.getSize().split("x");
				width = Integer.parseInt(size[0]);
				height = Integer.parseInt(size[1]);
			}
			new Main(cmd.getInFile(), cmd.getOutFile(), cmd.getBitrate(), cmd.getThreads());
		} else {
			throw new RuntimeException("Missing input or output filename");
		}
	}
	
	public static Queue<VideoFrame> toProcess = new ConcurrentLinkedQueue<VideoFrame>();
	public static Queue<Frame> completed = new ConcurrentLinkedQueue<Frame>();
	
	public Main(String inFile, String outFile, int bitrate, int threads) {
		Decoder dec = new Decoder(inFile, getWidth(), getHeight());
		for (int i = 0; i < threads; i++) {
			Thread thread = new Worker();
			thread.setName("Worker-" + i);
			thread.start();
		}
		new Writer(outFile, bitrate, dec.getFrameRate(), dec.getTotalFrames(), dec.getSamplerate()).start();
		
		Frame frame;
		while ((frame = dec.readFrame()) != null) {
			while (toProcess.size() > threads * 2);
			if (frame instanceof VideoFrame) {
				toProcess.add((VideoFrame) frame);
			} else {
				completed.add(frame);
			}
		}
		
		dec.close();
		working = false;
	}
	
	public static int getHCells() {
		return getWidth() / 8;
	}
	
	public static int getVCells() {
		return getHeight() / 8;
	}
	
	public static int getCellCount() {
		return (getWidth() * getHeight()) / 64;
	}
	
}