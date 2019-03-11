package net.shadowpie.sadiinso.sfc.sfc;

public class Main {

	public static void main(String[] args) {
		if (args.length <= 0) {
			System.out.println("Use -standalone option to run the library as a standalone bot");
			return;
		}

		if (args[0].equals("-standalone"))
			launchAsStandAlone();
	}

	private static void launchAsStandAlone() {
		System.out.println("Launching the library as a standalone bot...");
		SFC.init();
		
		
		/**
		var chan = JdaUtils.getPrivateChannel(233189013131886592L);
		ButtonMenu.create("miaou ?", "chamallow").addChoice(JdaUtils.EMOJI_ACCEPT, r -> {
			r.getChannel().sendMessage("accepted").queue();
		}).addChoice(JdaUtils.EMOJI_DENY, r -> {
			r.getChannel().sendMessage("denied").queue();
		}).build().display(chan);
		**/
	}

}
