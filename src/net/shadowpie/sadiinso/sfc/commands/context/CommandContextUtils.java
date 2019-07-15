package net.shadowpie.sadiinso.sfc.commands.context;

import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.sfc.SFC;
import net.shadowpie.sadiinso.sfc.utils.SStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandContextUtils {
	
	/**
	 * return message without caller (prefix or pre-mentions) and all mentions
	 * resolved
	 *
	 * @param msg The command message
	 * @param useMention true if the command mention the bot, false if it use the tag system
	 */
	public static char[] resolveMentions(String msg, boolean useMention) {
		return resolveMentions(msg.toCharArray(), useMention);
	}
	
	/**
	 * return message without caller (prefix or pre-mentions) and all mentions
	 * resolved
	 *
	 * @param msg The command message
	 * @param useMention true if the command mention the bot, false if it use the tag system
	 */
	public static char[] resolveMentions(char[] msg, boolean useMention) {
		int index = 0;
		
		// remove caller
		index += (useMention ? SFC.selfMention().length() : ConfigHandler.bot_tag().length());
		
		// remove post-caller whitespace
		while (Character.isWhitespace(msg[index]))
			++index;
		
		// resolve mentions (skip resolving if message size is less than 21 chars : no possible IDs left to resolve)
		if (msg.length - index > 20) {
			SStringBuilder builder = new SStringBuilder(msg.length - index); // ensure that the internal buffer will nether growth
			int lastPush = msg.length - 21;
			
			resolve: for (int t = index, len = msg.length - 20; t < len; t++) { // prevent "msg.length - 20" to be re-calculated each loop turn
				if ((msg[t] == '<') && !Character.isDigit(msg[t + 1])) {
					if (!Character.isDigit(msg[t + 2])) {
						if (msg[t + 21] != '>') {
							builder.append(msg[t]);
							continue;
						}
						
						for (int u = t + 3, len2 = t + 21; u < len2; u++) {
							if (!Character.isDigit(msg[u])) {
								builder.append(msg[t]);
								continue resolve;
							}
						}
						
						builder.append(msg, t + 3, 18);// push digits
						t += 21;
					} else {
						if (msg[t + 20] != '>') {
							builder.append(msg[t]);
							continue;
						}
						
						for (int u = t + 3, len2 = t + 20; u < len2; u++) {
							if (!Character.isDigit(msg[u])) {
								builder.append(msg[t]);
								continue resolve;
							}
						}
						
						builder.append(msg, t + 2, 18);// push digits
						t += 20;
					}
				} else {
					builder.append(msg[t]);
				}
				
				lastPush = t;
			}
			
			builder.append(msg, lastPush + 1);
			return builder.copy();
		} else {
			return Arrays.copyOfRange(msg, index, msg.length);
		}
	}
	
	public static String[] extractArguments(char[] cmd, List<String[]> pipe) {
		return extractArguments(cmd, 0, pipe);
	}
	
	public static String[] extractArguments(char[] cmd, int from, List<String[]> pipe) {
		ArrayList<String> tmpArgs = new ArrayList<>();
		SStringBuilder builder = new SStringBuilder(); // String buffer to build the arguments
		boolean inStr = false, escapeNext = false;
		int t, pindex = -1;
		
		iter:
		for (t = from; t < cmd.length; t++) {
			if(Character.isWhitespace(cmd[t])) {
				if(inStr) {
					builder.append(cmd[t]);
				} else if(!builder.isEmpty()) {
					tmpArgs.add(builder.toString());
					builder.reset();
				}
				continue;
			}
			
			if(escapeNext) {
				builder.append(cmd[t]);
				escapeNext = false;
			} else {
				switch(cmd[t]) {
					case '\\':
						escapeNext = true;
						break;
					
					case '\"':
						inStr = !inStr;
						break;
					
					case '>': // DOS/NT and Unix syntax for piping
					case '|':
						if(!inStr) { // execute and break only if not in string block, else run the default case
							pindex = t + 1;
							break iter;
						}
					
					default:
						builder.append(cmd[t]);
				}
			}
		}
		
		/* reconstruct arguments */
		String[] args;
		if(builder.isEmpty()) {
			args = tmpArgs.toArray(String[]::new);
		} else {
			args = new String[tmpArgs.size() + 1];
			tmpArgs.toArray(args);
			args[args.length - 1] = builder.toString();
		}
		
		/* add piped commands */
		if(pindex != -1) {
			pipe.add(extractArguments(cmd, pindex, pipe));
		}
		
		return args;
	}
	
}
