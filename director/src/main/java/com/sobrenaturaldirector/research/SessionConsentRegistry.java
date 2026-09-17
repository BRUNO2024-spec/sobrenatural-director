package com.sobrenaturaldirector.research;

import java.util.HashMap;
import java.util.Map;
import java.security.SecureRandom;

/** Session-only consent state. Raw player identity is never persisted. */
public final class SessionConsentRegistry {
    public enum State { PENDING, ACCEPTED, DECLINED }
    private final Map<String, State> states = new HashMap<String, State>();
    private final Map<String, String> participantIds = new HashMap<String, String>();
    private final Map<String, String> participantSessions = new HashMap<String, String>();
    private ConsentLifecycleListener listener;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    public synchronized void setListener(ConsentLifecycleListener value) { listener=value; }
    public synchronized void join(String inMemoryPlayerKey) { if (inMemoryPlayerKey != null) states.put(inMemoryPlayerKey, State.PENDING); }
    public synchronized State state(String inMemoryPlayerKey) { State s=states.get(inMemoryPlayerKey); return s==null?State.PENDING:s; }
    public synchronized String accept(String inMemoryPlayerKey) { return accept(inMemoryPlayerKey,0); }
    public synchronized String accept(String inMemoryPlayerKey,long tick) { states.put(inMemoryPlayerKey, State.ACCEPTED); String id=participantIds.get(inMemoryPlayerKey); boolean fresh=id==null; if(fresh){id="p_"+token();participantIds.put(inMemoryPlayerKey,id);participantSessions.put(inMemoryPlayerKey,"ps_"+token());} if(fresh&&listener!=null)listener.accepted(participantSessions.get(inMemoryPlayerKey),id,tick); return id; }
    public synchronized void decline(String inMemoryPlayerKey) { states.put(inMemoryPlayerKey, State.DECLINED); participantIds.remove(inMemoryPlayerKey); participantSessions.remove(inMemoryPlayerKey); }
    public synchronized void revoke(String inMemoryPlayerKey) { revoke(inMemoryPlayerKey,0); }
    public synchronized void revoke(String inMemoryPlayerKey,long tick) { String id=participantIds.get(inMemoryPlayerKey);String session=participantSessions.get(inMemoryPlayerKey);decline(inMemoryPlayerKey);if(listener!=null&&id!=null&&session!=null)listener.revoked(session,id,tick); }
    public synchronized String participantId(String inMemoryPlayerKey) { return state(inMemoryPlayerKey)==State.ACCEPTED?participantIds.get(inMemoryPlayerKey):null; }
    public synchronized String participantSessionId(String inMemoryPlayerKey) { return state(inMemoryPlayerKey)==State.ACCEPTED?participantSessions.get(inMemoryPlayerKey):null; }
    public synchronized void disconnect(String inMemoryPlayerKey) { disconnect(inMemoryPlayerKey,0); }
    public synchronized void disconnect(String inMemoryPlayerKey,long tick) { String id=participantIds.remove(inMemoryPlayerKey);String session=participantSessions.remove(inMemoryPlayerKey);states.remove(inMemoryPlayerKey);if(listener!=null&&id!=null&&session!=null)listener.disconnected(session,id,tick); }
    public synchronized void closeAll(String reason) { if(listener!=null) for(String key:new java.util.ArrayList<String>(participantIds.keySet())) listener.ended(participantSessions.get(key),participantIds.get(key),reason,0); states.clear(); participantIds.clear(); participantSessions.clear(); }
    public synchronized void clear() { states.clear(); participantIds.clear(); participantSessions.clear(); }
    /** Conservative policy for global Director decisions: every active player must consent. */
    public synchronized boolean allActiveAccepted() { if(states.isEmpty()) return false; for(State s:states.values()) if(s!=State.ACCEPTED)return false; return true; }
    private static String token(){StringBuilder b=new StringBuilder(12);for(int i=0;i<12;i++)b.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);return b.toString();}
}
