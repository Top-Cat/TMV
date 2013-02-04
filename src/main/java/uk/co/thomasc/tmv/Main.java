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
		if (cmd.getSize().contains("x")) {
			String[] size = cmd.getSize().split("x");
			width = Integer.parseInt(size[0]);
			height = Integer.parseInt(size[1]);
		}
		new Main(cmd.getInFile(), cmd.getOutFile());
	}
	
	public static Queue<VideoFrame> toProcess = new ConcurrentLinkedQueue<VideoFrame>();
	public static Queue<Frame> completed = new ConcurrentLinkedQueue<Frame>();
	
	public Main(String inFile, String outFile) {
		Decoder dec = new Decoder(inFile, getWidth(), getHeight());
		int processors = Runtime.getRuntime().availableProcessors();
		for (int i = 0; i < processors; i++) {
			Thread thread = new Worker();
			thread.setName("Worker-" + i);
			thread.start();
		}
		new Writer(outFile, dec.getFrameRate(), dec.getTotalFrames()).start();
		
		Frame frame;
		while ((frame = dec.readFrame()) != null) {
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