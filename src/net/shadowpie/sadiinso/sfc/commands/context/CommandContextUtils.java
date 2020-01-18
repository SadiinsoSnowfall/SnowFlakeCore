package net.shadowpie.sadiinso.sfc.commands.context;

import gnu.trove.list.array.TIntArrayList;
import net.shadowpie.sadiinso.sfc.config.SFConfig;
import net.shadowpie.sadiinso.sfc.sfc.SFC;
import net.shadowpie.sadiinso.sfc.utils.SStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class CommandContextUtils {
	
	/**
	 * return message without caller (prefix or pre-mentions) and all mentions
	 * resolved
	 *
	 * @param msg The command message
	 * @param useMention true if the command mention the bot, false if it use the tag system
	 */
	public static SStringBuilder resolveMentions(String msg, boolean useMention) {
		return resolveMentions(msg.toCharArray(), useMention);
	}
	
	/**
	 * return message without caller (prefix or pre-mentions) and all mentions
	 * resolved
	 *
	 * @param msg The command message
	 * @param useMention true if the command mention the bot, false if it use the tag system
	 */
	public static SStringBuilder resolveMentions(char[] msg, boolean useMention) {
		int index = 0;
		
		// remove caller
		index += (useMention ? SFC.selfMention().length() : SFConfig.bot_tag().length());
		
		// remove post-caller whitespaces (including ZWS)
		while ((index < msg.length) && (Character.isWhitespace(msg[index]) || (msg[index] == '\u200B'))) {
			++index;
		}
		
		if(index == msg.length) {
			return null;
		}
		
		// resolve mentions (skip resolving if message size is less than 21 chars : no possible IDs left to resolve)
		if (msg.length - index > 20) {
			SStringBuilder builder = new SStringBuilder(msg.length - index); // ensure that the internal buffer will nether growth
			int lastPush = msg.length - 21;
			
			resolve: for (int t = index, len = msg.length - 20; t < len; t++) { // prevent "msg.length - 20" to be re-calculated each loop turn
				if(msg[t] == '<') {
					int tmp = t + 1;
					
					if(msg[tmp] == '@') {
						if(msg[tmp + 1] == '!') {
							++tmp;
						}
					} else if (msg[tmp] != '#') {
						builder.append(msg[t]);
						lastPush = t;
						continue;
					}
					
					++tmp;
					if(msg[tmp + 18] == '>') {
						for (int u = tmp, len2 = tmp + 18; u < len2; u++) {
							if (!Character.isDigit(msg[u])) {
								builder.append(msg[t]);
								lastPush = t;
								continue resolve;
							}
						}
						
						builder.append(msg, tmp, 18);// push digits
						t = tmp + 18;
					} else {
						builder.append(msg[t]);
					}
				} else {
					builder.append(msg[t]);
				}
				
				lastPush = t;
			}
			
			builder.append(msg, lastPush + 1);
			return builder;
		} else {
			return new SStringBuilder(Arrays.copyOfRange(msg, index, msg.length));
		}
	}
	
	public static LinkedList<CommandContextFrame> extractFrames(char[] cmd) {
		return extractFrames(new SStringBuilder(cmd, true));
	}
	
	public static LinkedList<CommandContextFrame> extractFrames(SStringBuilder cmd) {
		LinkedList<CommandContextFrame> frames = new LinkedList<>();
		frames.addFirst(extractArguments(cmd, 0, frames));
		
		/* check if list or last frame is empty */
		if(frames.peekLast() == null) {
			return null;
		}
		
		return frames;
	}
	
	private static CommandContextFrame extractArguments(SStringBuilder cmd, int from, LinkedList<CommandContextFrame> frames) {
		ArrayList<String> tmpArgs = new ArrayList<>();
		TIntArrayList crs = new TIntArrayList();
		SStringBuilder builder = new SStringBuilder(); // buffer to build the arguments
		boolean inStr = false, escapeNext = false;
		int t, pindex = -1;
		
		iter:
		for (t = from; t < cmd.length(); t++) {
			if(Character.isWhitespace(cmd.fastCharAt(t))) {
				if(inStr) {
					builder.append(cmd.fastCharAt(t));
				} else {
					if(!builder.isEmpty()) {
						tmpArgs.add(builder.toString());
						builder.reset();
					}
					
					if((cmd.fastCharAt(t) == '\n') && (crs.isEmpty() || (crs.get(crs.size() - 1) != tmpArgs.size()))) {
						crs.add(tmpArgs.size());
					}
				}
				continue;
			}
			
			if(escapeNext) {
				builder.append(cmd.fastCharAt(t));
				escapeNext = false;
			} else {
				switch(cmd.fastCharAt(t)) {
					case '\\':
						escapeNext = true;
						break;
					
					case '\"':
						inStr = !inStr;
						break;
						
					case '|':
						if(!inStr) { // execute and break only if not in string block, else run the default case
							pindex = t + 1;
							break iter;
						}
					
					default:
						builder.append(cmd.fastCharAt(t));
				}
			}
		}
		
		/* reconstruct arguments */
		final String[] args;
		if(builder.isEmpty()) {
			args = tmpArgs.toArray(String[]::new);
		} else {
			args = new String[tmpArgs.size() + 1];
			tmpArgs.toArray(args);
			args[tmpArgs.size()] = builder.toString();
		}
		
		/* check for null frame */
		if(args.length == 0) {
			return null;
		}
		
		/* add piped commands */
		if(pindex != -1) {
			frames.addFirst(extractArguments(cmd, pindex, frames));
		}
		
		return new CommandContextFrame(args, (crs.isEmpty() ? null : crs.toArray()));
	}
	
}
