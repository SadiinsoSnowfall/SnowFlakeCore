/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.shadowpie.sadiinso.sfc.listeners.eventwaiter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.utils.Checks;
import net.shadowpie.sadiinso.sfc.sfc.SFC;

/**
 * A {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu
 * AbstractMenu} implementation that creates a organized display of
 * emotes/emojis as buttons paired with options, and below the menu reactions
 * corresponding to each button.
 *
 * @author John Grosh
 */
public class ButtonMenu extends AbstractMenu {
	private final Map<String, OptionNode> options;
	private final OptionNode action;
	private final BiConsumer<Long, Message> finalAction;

	/**
	 * Create a new {@link ButtonMenu.Builder}
	 * 
	 * @return The {@link ButtonMenu.Builder}
	 */
	public static ButtonMenu.Builder create() {
		return new ButtonMenu.Builder();
	}

	ButtonMenu(Set<User> users, Set<Role> roles, long timeout, Map<String, OptionNode> options, OptionNode action, BiConsumer<Long, Message> finalAction) {
		super(users, roles, timeout);
		this.options = options;
		this.action = action;
		this.finalAction = finalAction;
	}

	@Override
	public void display(MessageChannel channel, MessageEmbed message, int subCount) {
		channel.sendMessage(message).queue(m -> {
			initialize(m, subCount);
		});
	}

	@Override
	public void attach(Message message, int subCount) {
		initialize(message, subCount);
	}

	// Initializes the ButtonMenu using a Message RestAction
	// This is either through editing a previously existing Message
	// OR through sending a new one to a TextChannel.
	private void initialize(Message m, int subCount) {
		String[] optionsCache = options.keySet().toArray(String[]::new);

		// display the emotes
		for (int i = 0; i < optionsCache.length; i++) {
			Emote emote;
			try {
				emote = SFC.getJDA().getEmoteById(optionsCache[i]);
				m.addReaction(emote).queue();
			} catch (Exception e) {
				m.addReaction(optionsCache[i]).queue();
				emote = null;
			}
		}
		
		EventWaiter.attach(GenericMessageReactionEvent.class).filter(event -> {
			// If the message is not the same as the ButtonMenu
			// currently being displayed.
			if (!event.getMessageId().equals(m.getId()))
				return false;

			// If the reaction is an Emote we get the Snowflake,
			// otherwise we get the unicode value.
			String re = event.getReaction().getReactionEmote().isEmote() ? event.getReaction().getReactionEmote().getId()
					: event.getReaction().getReactionEmote().getName();

			// If the value we got is not registered as a button to
			// the ButtonMenu being displayed we return false.
			if (!options.containsKey(re)) {
				event.getReaction().removeReaction().queue();
				return false;
			}

			// ignore self reactions add
			if(event.getUser().equals(SFC.getJDA().getSelfUser())) {
				return false;
			}
			
			// check if the user is valid
			if (!isValidUser(event.getUser(), event.getGuild())) {
				event.getReaction().removeReaction().queue();
				return false;
			}

			return true;
		}).onEvent((GenericMessageReactionEvent event) -> {
			// What happens next is after a valid event
			// is fired and processed above.
			String re = event.getReaction().getReactionEmote().isEmote() ? event.getReaction().getReactionEmote().getId()
					: event.getReaction().getReactionEmote().getName();

			// perform actions
			OptionNode node = options.get(re);
			if (node != null)
				node.accept(event);

			if (action != null)
				action.accept(event);
		}).timeout(timeout, l -> {
			if (finalAction != null)
				finalAction.accept(l, m);
		}).subscribe(subCount);
	}

	/**
	 * A wrapper for three reaction event consumers (generic, onAdd, onRemove)
	 * 
	 * @author SadiinsoSnowfall
	 *
	 */
	private static class OptionNode {
		private final Consumer<MessageReactionAddEvent> onAdd;
		private final Consumer<MessageReactionRemoveEvent> onRemove;
		private final Consumer<MessageReaction> genericAction;

		public OptionNode(Consumer<MessageReactionAddEvent> onAdd, Consumer<MessageReactionRemoveEvent> onRemove, Consumer<MessageReaction> genericAction) {
			this.onAdd = onAdd;
			this.onRemove = onRemove;
			this.genericAction = genericAction;
		}

		public void accept(GenericMessageReactionEvent event) {
			if (event instanceof MessageReactionAddEvent) {
				if (onAdd != null)
					onAdd.accept((MessageReactionAddEvent) event);
			} else if (event instanceof MessageReactionRemoveEvent) {
				if (onRemove != null)
					onRemove.accept((MessageReactionRemoveEvent) event);
			}

			if (genericAction != null)
				genericAction.accept(event.getReaction());
		}
	}

	/**
	 * The
	 * {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu.Builder
	 * Menu.Builder} for a
	 * {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.ButtonMenu
	 * ButtonMenu}.
	 *
	 * @author John Grosh
	 */
	public static class Builder extends AbstractMenu.Builder<Builder, ButtonMenu> {
		private final Map<String, OptionNode> options = new LinkedHashMap<>();
		private OptionNode action;
		private BiConsumer<Long, Message> finalAction;

		/**
		 * Builds the {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.ButtonMenu
		 * ButtonMenu} with this Builder.
		 *
		 * @return The OrderedMenu built from this Builder.
		 *
		 * @throws java.lang.IllegalArgumentException if no choices where set
		 */
		@Override
		public ButtonMenu build() {
			Checks.check(!options.isEmpty(), "Must have at least one choice");
			return new ButtonMenu(allowedUsers, allowedRoles, timeout, options, action, finalAction);
		}

		/**
		 * Sets the {@link java.util.function.Consumer Consumer} action to perform upon
		 * selecting a button.
		 *
		 * @param action The Consumer action to perform upon selecting a button
		 * @return This builder
		 */
		public Builder setAction(Consumer<MessageReaction> action) {
			this.action = new OptionNode(null, null, action);
			return this;
		}

		/**
		 * Sets the {@link java.util.function.Consumer Consumer} actions to perform upon
		 * selecting a button.
		 *
		 * @param onAdd    The Consumer action to perform upon selecting a button
		 * @param onRemove The Consumer action to perform upon deselecting a button
		 * @return This builder
		 */
		public Builder setAction(Consumer<MessageReactionAddEvent> onAdd, Consumer<MessageReactionRemoveEvent> onRemove) {
			this.action = new OptionNode(onAdd, onRemove, null);
			return this;
		}

		/**
		 * Sets the {@link java.util.function.Consumer Consumer} to perform if the
		 * {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.ButtonMenu
		 * ButtonMenu} is done, either via cancellation, a timeout, or a selection being
		 * made.
		 * <p>
		 *
		 * This accepts the message used to display the menu when called.
		 *
		 * @param finalAction The Runnable action to perform if the ButtonMenu is done
		 * @return This builder
		 */
		public Builder setFinalAction(BiConsumer<Long, Message> finalAction) {
			this.finalAction = finalAction;
			return this;
		}

		/**
		 * Adds String unicode emojis as button choices.
		 *
		 * <p>
		 * Any non-unicode {@link net.dv8tion.jda.core.entities.Emote Emote}s should be
		 * added using {@link ButtonMenu.Builder#addChoice(Emote...)
		 * ButtonMenu.Builder#addChoice(Emote...)}.
		 *
		 * @param emojis The String unicode emojis to add
		 * @return This builder
		 */
		public Builder addChoice(String... emojis) {
			for (String emoji : emojis)
				this.options.put(emoji, null);
			return this;
		}

		/**
		 * Adds String unicode emojis as button choices.
		 *
		 * <p>
		 * Any non-unicode {@link net.dv8tion.jda.core.entities.Emote Emote}s should be
		 * added using {@link ButtonMenu.Builder#addChoice(Emote...)
		 * ButtonMenu.Builder#addChoice(Emote...)}.
		 *
		 * @param emoji  The String unicode emojis to add
		 * @param action The code to run when this button is clicked
		 * @return This builder
		 */
		public Builder addChoice(String emoji, Consumer<MessageReaction> action) {
			this.options.put(emoji, new OptionNode(null, null, action));
			return this;
		}

		/**
		 * Adds String unicode emojis as button choices.
		 *
		 * <p>
		 * Any non-unicode {@link net.dv8tion.jda.core.entities.Emote Emote}s should be
		 * added using {@link ButtonMenu.Builder#addChoice(Emote...)
		 * ButtonMenu.Builder#addChoice(Emote...)}.
		 *
		 * @param emoji    The String unicode emojis to add
		 * 
		 * @param onAdd    The code to run when this button is selected
		 *
		 * @param onRemove The code to run when this button is deselected
		 *
		 * @return This builder
		 */
		public Builder addChoice(String emoji, Consumer<MessageReactionAddEvent> onAdd, Consumer<MessageReactionRemoveEvent> onRemove) {
			this.options.put(emoji, new OptionNode(onAdd, onRemove, null));
			return this;
		}

		/**
		 * Adds custom {@link net.dv8tion.jda.core.entities.Emote Emote}s as button
		 * choices.
		 *
		 * <p>
		 * Any regular unicode emojis should be added using
		 * {@link ButtonMenu.Builder#addChoice(String...)
		 * ButtonMenu.Builder#addChoice(String...)}.
		 *
		 * @param emotes The Emote objects to add
		 * @return This builder
		 */
		public Builder addChoice(Emote... emotes) {
			for (Emote emote : emotes)
				this.options.put(emote.getId(), null);
			return this;
		}

		/**
		 * Adds custom {@link net.dv8tion.jda.core.entities.Emote Emote}s as button choices.
		 * <p>
		 * Any regular unicode emojis should be added using
		 * {@link ButtonMenu.Builder#addChoice(String...)
		 * ButtonMenu.Builder#addChoice(String...)}.
		 *
		 * @param emote  The Emote objects to add
		 * @param action The code to run when this button is clicked
		 * @return This builder
		 */
		public Builder addChoice(Emote emote, Consumer<MessageReaction> action) {
			return addChoice(emote.getId(), action);
		}

		/**
		 * Adds custom {@link net.dv8tion.jda.core.entities.Emote Emote}s as button choices.
		 * <p>
		 * Any regular unicode emojis should be added using
		 * {@link ButtonMenu.Builder#addChoice(String...)
		 * ButtonMenu.Builder#addChoice(String...)}.
		 *
		 * @param emote    The Emote objects to add
		 * @param onAdd    The code to run when this button is selected
		 * @param onRemove The code to run when this button is deselected
		 * @return This builder
		 */
		public Builder addChoice(Emote emote, Consumer<MessageReactionAddEvent> onAdd, Consumer<MessageReactionRemoveEvent> onRemove) {
			return addChoice(emote.getId(), onAdd, onRemove);
		}
		
		/**
		 * Adds Emojis or Emotes as button choices.
		 * <p>
		 * This function only accept Objects that are instances of String or {@link net.dv8tion.jda.core.entities.Emote Emote}
		 *
		 * @param objs The Emote / emojis to add
		 * @return This builder
		 */
		public Builder addChoice(Object... objs) {
			for (Object obj : objs) {
				if(obj instanceof String) {
					addChoice((String) obj);
				} else if (obj instanceof Emote) {
					addChoice((Emote) obj);
				} else {
					throw new RuntimeException("One of the given objects is not a String nor an Emote");
				}
			}
			
			return this;
		}
		
		/**
		 * Adds Emojis or Emotes as button choices.
		 * <p>
		 * This function only accept Objects that are instances of String or {@link net.dv8tion.jda.core.entities.Emote Emote}
		 *
		 * @param obj  The Emote / emoji to add
		 * @param action The code to run when this button is clicked
		 * @return This builder
		 */
		public Builder addChoice(Object obj, Consumer<MessageReaction> action) {
			if (obj instanceof String) {
				return addChoice((String) obj, action);
			} else if (obj instanceof Emote) {
				return addChoice((Emote) obj, action);
			} else {
				throw new RuntimeException("The given object is not a String nor an Emote");
			}
		}
		
		/**
		 * Adds Emojis or Emotes as button choices.
		 * <p>
		 * This function only accept Objects that are instances of String or {@link net.dv8tion.jda.core.entities.Emote Emote}
		 *
		 * @param obj      The Emote / emoji to add
		 * @param onAdd    The code to run when this button is selected
		 * @param onRemove The code to run when this button is deselected
		 * @return This builder
		 */
		public Builder addChoice(Object obj, Consumer<MessageReactionAddEvent> onAdd, Consumer<MessageReactionRemoveEvent> onRemove) {
			if (obj instanceof String) {
				return addChoice((String) obj, onAdd, onRemove);
			} else if (obj instanceof Emote) {
				return addChoice((Emote) obj, onAdd, onRemove);
			} else {
				throw new RuntimeException("The given object is not a String nor an Emote");
			}
		}
	}
}
