package net.shadowpie.sadiinso.sfc.listeners;

import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.ConsoleCommandContext;

import java.util.Scanner;

public final class ConsoleListener {

	private ConsoleListener() {
	}

	private static Thread consoleTh;

	public static void setup() {
		if (consoleTh != null)
			return;

		consoleTh = new Thread(() -> {
			Scanner sc = new Scanner(System.in);

			while (!Thread.interrupted()) {
				if (sc.hasNextLine()) {
					try {
						switch (Commands.execute(ConsoleCommandContext.getContext(sc.nextLine()))) {
							case Commands.COMMAND_NOT_FOUND:
								System.err.println(Commands.err_cmd_not_found);
								break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException ignored) { }
			}

			sc.close();
		}, "Console");
		consoleTh.setDaemon(true);
		consoleTh.start();
	}

	public static void shutdown() {
		if (consoleTh != null)
			consoleTh.interrupt();
	}

}
