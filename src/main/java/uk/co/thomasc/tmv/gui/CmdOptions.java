package uk.co.thomasc.tmv.gui;

import lombok.Getter;

import com.beust.jcommander.Parameter;

public class CmdOptions {
	
	@Parameter(names = "-console", description = "Don't show a GUI")
	@Getter private boolean console = false;
	
	@Parameter(names = {"-in", "-i"}, description = "File to read")
	@Getter private String inFile;
	
	@Parameter(names = {"-out", "-o"}, description = "File to write")
	@Getter private String outFile;
	
	@Parameter(names = {"-bitrate", "-b"}, description = "Bitrate to write at")
	@Getter private int bitrate = 10000;
	
	@Parameter(names = {"-size", "-s"}, description = "Size to scale output to (Format: WxH)")
	@Getter private String size = "";
	
	@Parameter(names = {"-threads", "-t"}, description = "Number of worker threads to spawn")
	@Getter private int threads = Runtime.getRuntime().availableProcessors();
	
}