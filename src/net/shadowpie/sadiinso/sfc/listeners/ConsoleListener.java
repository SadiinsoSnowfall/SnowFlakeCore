package net.shadowpie.sadiinso.sfc.listeners;

import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.context.ConsoleCommandContext;
import net.shadowpie.sadiinso.sfc.config.SFConfig;

import java.util.Scanner;

public final class ConsoleListener {

	private ConsoleListener() {}

	private static Thread consoleTh;
	private static int SLEEP_TIME;

	public static void setup() {
		if (consoleTh != null) {
			return;
		}

		SLEEP_TIME = SFConfig.sfConfig.getInt("console_listener_sleep_ms", 250);
		
		consoleTh = new Thread(() -> {
			Scanner sc = new Scanner(System.in);

			while (!Thread.interrupted()) {
				if (sc.hasNextLine()) {
					try {
						CommandContext ctx = ConsoleCommandContext.getContext(sc.nextLine());
						if(ctx != null) {
							switch (Commands.execute(ctx)) {
								case Commands.COMMAND_NOT_FOUND:
									System.err.println(Commands.err_cmd_not_found);
									break;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException ignored) { }
			}

			sc.close();
		}, "Console");
		consoleTh.setDaemon(true);
		consoleTh.start();
	}

	public static void shutdown() {
		if (consoleTh != null) {
			consoleTh.interrupt();
		}
	}

}
