package net.shadowpie.sadiinso.sfc.listeners;

import java.util.Scanner;

import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.context.ConsoleCommandContext;

public final class ConsoleListener {

	private ConsoleListener() {}
	
	private static Thread consoleTh;
	
	public static void setup() {
		if(consoleTh != null)
			return;
		
		consoleTh = new Thread(new Runnable() {
			@Override
			public void run() {
				Scanner sc = new Scanner(System.in);
				
				while(!Thread.interrupted()) {
					if(sc.hasNext()) {
						CommandContext ctx = ConsoleCommandContext.getContext(sc.nextLine());
						
						if(ctx != null) {
							switch(Commands.execute(ctx)) {
								case Commands.COMMAND_NOT_FOUND:
									System.out.println("Command not found");
									break;
							}
						}
					}
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException ignored) {}
				}
				
				sc.close();
			}
		}, "Console");
		consoleTh.setDaemon(true);
		consoleTh.start();
	}
	
	public static void shutdown() {
		if(consoleTh != null)
			consoleTh.interrupt();
	}
	
}
