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

import net.dv8tion.jda.core.entities.*;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.utils.JdaUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A frame for wrapping a menu that waits on forms of user input such as reactions,
 * or key-phrases.
 *
 * <p>Classes extending this are able to take a provided {@link net.dv8tion.jda.core.entities.Message Message}
 * or {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} and display a visualized "Menu"
 * as or in it.
 *
 * <p>The JDA-Utilities default implementations of this superclass typically handle input through
 * the assistance of things such as {@link net.dv8tion.jda.core.entities.MessageReaction reactions},
 * but the actual implementation is only limited to the events provided by Discord and handled through JDA.
 *
 * <p>For custom implementations, readability of creating and integrating may be improved
 * by the implementation of a companion builder may be helpful (see the documentation on
 * {@link AbstractMenu.Builder AbstractMenu.Builder} for more info).
 *
 * @see    AbstractMenu.Builder
 *
 * @author John Grosh
 *
 * @implNote
 *         While the standard JDA-Utilities implementations of this and Menu are
 *         all handled as {@link net.dv8tion.jda.core.entities.MessageEmbed embeds},
 *         there is no bias or advantage of implementing a custom Menu as a message
 *         without an embed.
 */
public abstract class AbstractMenu {
    protected final Set<User> allowedUsers;
    protected final Set<Role> allowedRoles;
    protected final long timeout;
    
    protected AbstractMenu(Set<User> allowedUsers, Set<Role> allowedRoles, long timeout) {
    	if(allowedUsers.isEmpty()) {
            this.allowedUsers = null;
        } else {
            this.allowedUsers = allowedUsers;
        }
    	
    	if(allowedRoles.isEmpty()) {
    	    this.allowedRoles = null;
        } else {
            this.allowedRoles = allowedRoles;
        }
    	
        this.timeout = timeout;
    }
    
    /**
     * Send a message in the given {@link MessageChannel} and attach the menu to it.
     * 
     * @param channel The MessageChannel to display this Menu in
     * @param title The Title of the message to send
     */
    public void display(MessageChannel channel, String title) {
    	display(channel, JdaUtils.getEmbedBuilder(title).build());
    }

    /**
     * Send a message in the given {@link MessageChannel} and attach the menu to it.
     * 
     * @param channel The MessageChannel to display this Menu in
     * @param title The Title of the message to send
     * @param description The description of the message to send
     */
    public void display(MessageChannel channel, String title, String description) {
    	display(channel, title, description, ConfigHandler.color_theme());
    }
    
    /**
     * Send a message in the given {@link MessageChannel} and attach the menu to it.
     * 
     * @param channel The MessageChannel to display this Menu in
     * @param title The Title of the message to send
     * @param description The description of the message to send
     * @param color The color of the message to send
     */
    public void display(MessageChannel channel, String title, String description, Color color) {
    	display(channel, JdaUtils.sendAsEmbed(null, title, description, color));
    }
    
    
    /**
     * Displays this Menu in a {@link MessageChannel}.
     * 
     * @param channel The MessageChannel to display this Menu in
     * @param message The Message to send
     */
    public void display(MessageChannel channel, MessageEmbed message) {
    	display(channel, message, 1);
    }
    
    /**
     * Displays this Menu in a {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * 
     * @param channel The MessageChannel to display this Menu in
     * @param message The Message to send
     * @param subCount The maximum number of event to receive before closing the menu
     */
    public abstract void display(MessageChannel channel, MessageEmbed message, int subCount);
    
    /**
     * Attach the menu to the given {@link net.dv8tion.jda.core.entities.Message Message}.
     * 
     * @param message The Message to display this Menu as
     */
    public void attach(Message message) {
    	attach(message, 1);
    }
    
    /**
     * Attach the menu to the given {@link net.dv8tion.jda.core.entities.Message Message}.
     * 
     * @param message The Message to display this Menu as
     * @param subCount The maximum number of event to receive before closing the menu
     */
    public abstract void attach(Message message, int subCount);

    /**
     * Checks to see if the provided {@link net.dv8tion.jda.core.entities.User User}
     * is valid to interact with this Menu.<p>
     *
     * This is a shortcut for {@link #isValidUser(User, Guild)} where the Guild
     * is {@code null}.
     *
     * @param user  The User to validate.
     *
     * @return {@code true} if the User is valid, {@code false} otherwise.
     *
     * @see #isValidUser(User, Guild)
     */
    protected boolean isValidUser(User user) {
        return isValidUser(user, null);
    }

    /**
     *
     * Checks to see if the provided {@link net.dv8tion.jda.core.entities.User User}
     * is valid to interact with this Menu.<p>
     *
     * For a User to be considered "valid" to use a Menu, the following logic (in order) is applied:
     * <ul>
     *     <li>The User must not be a bot. If it is, this returns {@code false} immediately.</li>
     *
     *     <li>If no users and no roles were specified in the builder for this Menu, then this
     *         will return {@code true}.</li>
     *
     *     <li>If the User is among the users specified in the builder for this Menu, this will
     *         return {@code true}.</li>
     *
     *     <li>If the Guild is {@code null}, or if the User is not a member on the Guild, this
     *         will return {@code false}.</li>
     *
     *     <li>Finally, the determination will be if the User on the provided Guild has any
     *         of the builder-specified Roles.</li>
     * </ul>
     *
     * Custom-implementation-wise, it's highly recommended developers who might override this
     * attempt to follow a similar logic for their Menus, as this provides a full-proof guard
     * against exceptions when validating a User of a Menu.
     *
     * @param  user
     *         The User to validate.
     * @param  guild
     *         The Guild to validate the User on.<br>
     *         Can be provided {@code} null safely.
     *
     * @return {@code true} if the User is valid, {@code false} otherwise.
     */
    protected boolean isValidUser(User user, @Nullable Guild guild) {
        if(user.isBot()) {
            return false;
        }
        
        if((allowedUsers == null) && (allowedRoles == null)) {
            return true;
        }
        
        if(allowedUsers.contains(user)) {
            return true;
        }
        
        if((guild == null) || !guild.isMember(user)) {
            return false;
        }

        return guild.getMember(user).getRoles().stream().anyMatch(allowedRoles::contains);
    }

    /**
     * An extendable frame for a chain-method builder that constructs a specified type of
     * {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu AbstractMenu}.<p>
     *
     * Conventionally, implementations of Menu should have a static nested class called
     * {@code Builder}, which extends this superclass:
     * <pre><code>
     * public class MyMenu extends Menu
     * {
     *     // Menu Code
     *
     *    {@literal public static class Builder extends Menu.Builder<Builder, MyMenu>}
     *     {
     *         // Builder Code
     *     }
     * }
     * </code></pre>
     *
     * @author John Grosh
     *
     * @implNote
     *         Before 2.0 this were a separate class known as {@code MenuBuilder}.<br>
     *         Note that while the standard JDA-Utilities implementations of this and Menu are
     *         all handled as {@link net.dv8tion.jda.core.entities.MessageEmbed embeds}, there
     *         is no bias or advantage of implementing a custom Menu as a message without an embed.
     */
    @SuppressWarnings("unchecked")
    public abstract static class Builder<T extends Builder<T, V>, V extends AbstractMenu> {
        protected Set<User> allowedUsers = new HashSet<>();
        protected Set<Role> allowedRoles = new HashSet<>();
        protected long timeout = 300_000; // aka 5 minutes

        /**
         * Builds the {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu AbstractMenu} corresponding to
         * this {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu.Builder AbstractMenu.Builder}.
         * <br>After doing this, no modifications of the displayed Menu can be made.
         *
         * @return The built Menu of corresponding type to this {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu.Builder}.
         */
        public abstract V build();

        /**
         * Adds {@link net.dv8tion.jda.core.entities.User User}s that are allowed to use the
         * {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu AbstractMenu} that will be built.
         *
         * @param  users
         *         The Users allowed to use the Menu
         *
         * @return This builder
         */
        public final T addUsers(User... users) {
            allowedUsers.addAll(Arrays.asList(users));
            
            return (T)this;
        }

        /**
         * Sets {@link net.dv8tion.jda.core.entities.User User}s that are allowed to use the
         * {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu AbstractMenu} that will be built.
         * <br>This clears any Users already registered before adding the ones specified.
         *
         * @param  users
         *         The Users allowed to use the Menu
         *
         * @return This builder
         */
        public final T setUsers(User... users) {
            this.allowedUsers.clear();
            return addUsers(users);
        }

        /**
         * Adds {@link net.dv8tion.jda.core.entities.Role Role}s that are allowed to use the
         * {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu AbstractMenu} that will be built.
         *
         * @param  roles
         *         The Roles allowed to use the Menu
         *
         * @return This builder
         */
        public final T addRoles(Role... roles) {
            allowedRoles.addAll(Arrays.asList(roles));
        	
            return (T)this;
        }

        /**
         * Sets {@link net.dv8tion.jda.core.entities.Role Role}s that are allowed to use the
         * {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu AbstractMenu} that will be built.
         * <br>This clears any Roles already registered before adding the ones specified.
         *
         * @param  roles
         *         The Roles allowed to use the Menu
         *
         * @return This builder
         */
        public final T setRoles(Role... roles) {
            this.allowedRoles.clear();
            return addRoles(roles);
        }

        /**
         * Sets the timeout that the {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu AbstractMenu} should
         * stay available. The default timeout is set to 300_000 ms (5 minutes).
         *
         * <p>After this has expired, the a final action in the form of a
         * {@link java.lang.Runnable Runnable} may execute.
         *
         * @see #setTimeout(long, TimeUnit) to specify the time unit
         *
         * @param  timeout
         *         The amount of time for the Menu to stay available in milliseconds
         *
         * @return This builder
         */
        public final T setTimeout(long timeout) {
        	this.timeout = timeout;
        	return (T) this;
        }
        
        /**
         * Sets the timeout that the {@link net.shadowpie.sadiinso.sfc.listeners.eventwaiter.AbstractMenu AbstractMenu} should
         * stay available. The default timeout is set to 5 minutes.
         *
         * <p>After this has expired, the a final action in the form of a
         * {@link java.lang.Runnable Runnable} may execute.
         *
         * @see #setTimeout(long) to set the timeout directly in ms
         *
         * @param  timeout
         *         The amount of time for the Menu to stay available
         * @param  unit
         *         The {@link java.util.concurrent.TimeUnit TimeUnit} for the timeout
         *
         * @return This builder
         */
        public final T setTimeout(long timeout, TimeUnit unit) {
            return setTimeout(unit.toMillis(timeout));
        }
        
    }
}
