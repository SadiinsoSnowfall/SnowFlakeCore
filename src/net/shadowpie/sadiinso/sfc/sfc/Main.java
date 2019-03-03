package net.shadowpie.sadiinso.sfc.sfc;

import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.shadowpie.sadiinso.sfc.listeners.eventwaiter.EventWaiter;

public class Main {

	public static void main(String[] args) {
		if(args.length <= 0) {
			System.out.println("Use -standalone option to run the library as a standalone bot");
			return;
		}
		
		if(args[0].equals("-standalone"))
			launchAsStandAlone();
	}
	
	private static void launchAsStandAlone() {
		System.out.println("Launching the library as a standalone bot...");
		SFC.init();
		
		EventWaiter.attach(PrivateMessageReceivedEvent.class).onEvent(e -> {
			/* miaou miaou */
		}).subscribeEver();
	}

}
