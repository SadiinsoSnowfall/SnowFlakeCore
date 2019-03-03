package net.shadowpie.sadiinso.sfc.sfc;

import java.util.HashMap;
import java.util.Map;

public class OwnerSession {

	private static final Map<Long, OwnerSession> sessions = new HashMap<>();
	
	public static OwnerSession get(long userID) {
		return sessions.get(userID);
	}
	
	public static boolean isOwner(long userID) {
		return (sessions.get(userID) != null);
	}
	
	public static void createSession(long userID) {
		OwnerSession os = new OwnerSession();
		sessions.put(userID, os);
	}
	
}
