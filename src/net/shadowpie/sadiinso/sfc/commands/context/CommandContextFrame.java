package net.shadowpie.sadiinso.sfc.commands.context;

public class CommandContextFrame {
	public String[] args;
	public int[] crs;
	
	CommandContextFrame(String[] args, int[] crs) {
		this.args = args;
		this.crs = crs;
	}
}
