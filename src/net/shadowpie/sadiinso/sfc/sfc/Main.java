package net.shadowpie.sadiinso.sfc.sfc;

public class Main {

	public static void main(String[] args) {
		if ((args.length > 0) && args[0].equals("-standalone")) {
			launchAsStandAlone();
		} else {
			System.out.println("Use -standalone option to run the library as a standalone bot");
		}
	}

	private static void launchAsStandAlone() {
		System.out.println("Launching the library as a standalone bot...");
		SFC.init();
	}

}
