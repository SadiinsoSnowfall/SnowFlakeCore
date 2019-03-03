package net.shadowpie.sadiinso.sfc.listeners.eventwaiter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.Predicate;

import net.dv8tion.jda.core.events.Event;

/**
 * EventWaiter class, used to subscribe to certains events under certains conditions
 * 
 * To subscribe to an event, you need to call {@link EventWaiter#attach(Clazz)} with the specified event class
 * This function will return you a {@link EWNodeBuilder} that will allow you to specify the actions to be taken
 *
 */
public class EventWaiter {
	
	@SuppressWarnings("rawtypes")
	private static final Map<Class<? extends Event>, List<EWNode>> nodeMap = new ConcurrentHashMap<>();
	
	public static <T extends Event> EWNodeBuilder<T> attach(Class<T> clazz) {
		return new EWNodeBuilder<T>(clazz);
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
		private List<Predicate<T>> conditions;
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
		
		public EWNodeBuilder<T> onExpire(LongConsumer expireAction) {
			this.expireAction = expireAction;
			return this;
		}
		
		public EWNodeBuilder<T> onEvent(Consumer<T> action) {
			this.action = action;
			return this;
		}
		
		public EWNodeBuilder<T> timeout(long timeout) {
			this.timeout = timeout;
			return this;
		}
		
		public void subscribeOnce() {
			subscribe(1);
		}
		
		public void subscribeEver() {
			subscribe(-1);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void subscribe(int runCount) {
			List<EWNode> nodes = nodeMap.computeIfAbsent(clazz, e -> createList());
			
			if(action == null)
				throw new RuntimeException("onEvent action cannot be null");
			
			Predicate<T>[] filters = (conditions.isEmpty() ? null : conditions.toArray(new Predicate[conditions.size()]));
			nodes.add(new EWNode(filters, action, expireAction, runCount, timeout));
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static List<EWNode> createList() {
		return Collections.synchronizedList(new ArrayList<EWNode>());
	}
	
	private static class EWNode<T extends Event> {
		public final Predicate<T>[] conditions;
		public final Consumer<T> action;
		public final LongConsumer expireAction;
		public final long expire;
		public int remainCall;
		
		public EWNode(Predicate<T>[] conditions, Consumer<T> action, LongConsumer expireAction, int runCount, long timeout) {
			this.conditions = conditions;
			this.action = action;
			this.expireAction = expireAction;
			this.remainCall = runCount;
			
			if(timeout == -1)
				this.expire = timeout;
			else
				this.expire = System.currentTimeMillis() + timeout;
		}
		
		public boolean expired(long currentTime) {
			if((expire <= currentTime) && (expire != -1)) {
				expireAction.accept(currentTime);
				return true;
			}
			
			return false;
		}
		
		public boolean attempt(T event) {
			if(conditions != null)
				for(Predicate<T> condition : conditions)
					if(!condition.test(event))
						return false;
			
			action.accept(event);
			
			if(remainCall <= -1) {
				return false;
			} else {
				--remainCall;
				return (remainCall <= 0);
			}
		}
	}
	
}
