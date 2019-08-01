package net.shadowpie.sadiinso.sfc.listeners.eventwaiter;

import net.dv8tion.jda.core.events.Event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.Predicate;

/**
 * EventWaiter class, used to subscribe to certains events under certains conditions
 * 
 * To subscribe to an event, you need to call {@link #attach(Class) attach(Class)} with the specified event class
 * This function will return you a {@link EventWaiter.EWNodeBuilder EWNodeBuilder} that will allow you to specify the actions to be taken
 *
 */
public class EventWaiter {
	
	@SuppressWarnings("rawtypes")
	private static final Map<Class<? extends Event>, List<EWNode>> nodeMap = new ConcurrentHashMap<>();
	
	public static <T extends Event> EWNodeBuilder<T> attach(Class<T> clazz) {
		return new EWNodeBuilder<>(clazz);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void onEvent(Event event) {
		long curTime = System.currentTimeMillis();
		Class<?> clazz = event.getClass();
		
		while(clazz != null) {
			List<EWNode> nodes = nodeMap.get(clazz);
			if(nodes != null)
				nodes.removeIf(e -> (e.expired(curTime) || e.attempt(event)));
			
			clazz = clazz.getSuperclass();
		}
	}
	
	public static class EWNodeBuilder<T extends Event> {
		private final Class<? extends Event> clazz;
		private final List<Predicate<T>> conditions;
		private Consumer<T> action;
		private LongConsumer expireAction;
		private long timeout;
		
		private EWNodeBuilder(Class<? extends Event> clazz) {
			this.clazz = clazz;
			conditions = new LinkedList<>();
			this.timeout = -1;
		}
		
		public EWNodeBuilder<T> filter(Predicate<T> condition) {
			this.conditions.add(condition);
			return this;
		}
		
		public EWNodeBuilder<T> onEvent(Consumer<T> action) {
			this.action = action;
			return this;
		}
		
		public EWNodeBuilder<T> timeout(long timeout) {
			return timeout(timeout, null);
		}
		
		public EWNodeBuilder<T> timeout(long timeout, LongConsumer expireAction) {
			this.timeout = timeout;
			this.expireAction = expireAction;
			return this;
		}
		
		public EWNode<T> subscribeOnce() {
			return subscribe(1);
		}
		
		public EWNode<T> subscribeEver() {
			return subscribe(-1);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public EWNode<T> subscribe(int runCount) {
			List<EWNode> nodes = nodeMap.computeIfAbsent(clazz, e -> createList());
			
			if(action == null)
				throw new RuntimeException("onEvent action cannot be null");
			
			Predicate<T>[] filters = (conditions.isEmpty() ? null : conditions.toArray(Predicate[]::new));
			EWNode cnode = new EWNode(filters, action, expireAction, runCount, timeout);
			nodes.add(cnode);
			
			return cnode;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static List<EWNode> createList() {
		return Collections.synchronizedList(new ArrayList<>());
	}
	
	public static class EWNode<T extends Event> {
		private final Predicate<T>[] conditions;
		private final Consumer<T> action;
		private final LongConsumer expireAction;
		private long expire;
		private int remainCall;
		
		private EWNode(Predicate<T>[] conditions, Consumer<T> action, LongConsumer expireAction, int runCount, long timeout) {
			this.conditions = conditions;
			this.action = action;
			this.expireAction = expireAction;
			this.remainCall = runCount;
			
			if(timeout == -1) {
				this.expire = timeout;
			} else {
				this.expire = System.currentTimeMillis() + timeout;
			}
		}
		
		private boolean expired(long currentTime) {
			if((expire <= currentTime) && (expire != -1)) {
				expireAction.accept(currentTime);
				return true;
			}
			
			return false;
		}
		
		private boolean attempt(T event) {
			if(conditions != null) {
				for (Predicate<T> condition : conditions) {
					if (!condition.test(event)) {
						return false;
					}
				}
			}
			
			action.accept(event);
			
			if(remainCall <= -1) {
				return false;
			} else {
				--remainCall;
				return (remainCall <= 0);
			}
		}
		
		public void unsubscribe() {
			expire = 0;
		}
	}
	
}
