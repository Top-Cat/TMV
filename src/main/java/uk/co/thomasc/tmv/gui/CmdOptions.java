package uk.co.thomasc.tmv.gui;

import lombok.Getter;

import com.beust.jcommander.Parameter;

public class CmdOptions {
	
	@Parameter(names = {"-in", "-i"}, description = "File to read", required = true)
	@Getter private String inFile;
	
	@Parameter(names = {"-out", "-o"}, description = "File to write", required = true)
	@Getter private String outFile;
	
	@Parameter(names = {"-bitrate", "-b"}, description = "Bitrate to write at")
	@Getter private int bitrate = 10000;
	
	@Parameter(names = {"-size", "-s"}, description = "Size to scale output to (Format: WxH)")
	@Getter private String size = "";
	
}