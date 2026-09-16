package com.sobrenaturaldirector.research;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Session-only consent state. Raw player identity is never persisted. */
public final class SessionConsentRegistry {
    public enum State { PENDING, ACCEPTED, DECLINED }
    private final Map<String, State> states = new HashMap<String, State>();
    private final Map<String, String> participantIds = new HashMap<String, String>();
    public synchronized void join(String inMemoryPlayerKey) { if (inMemoryPlayerKey != null) states.put(inMemoryPlayerKey, State.PENDING); }
    public synchronized State state(String inMemoryPlayerKey) { State s=states.get(inMemoryPlayerKey); return s==null?State.PENDING:s; }
    public synchronized String accept(String inMemoryPlayerKey) { states.put(inMemoryPlayerKey, State.ACCEPTED); String id=participantIds.get(inMemoryPlayerKey); if(id==null){id="participant-"+UUID.randomUUID().toString();participantIds.put(inMemoryPlayerKey,id);} return id; }
    public synchronized void decline(String inMemoryPlayerKey) { states.put(inMemoryPlayerKey, State.DECLINED); participantIds.remove(inMemoryPlayerKey); }
    public synchronized void revoke(String inMemoryPlayerKey) { decline(inMemoryPlayerKey); }
    public synchronized String participantId(String inMemoryPlayerKey) { return state(inMemoryPlayerKey)==State.ACCEPTED?participantIds.get(inMemoryPlayerKey):null; }
    public synchronized void disconnect(String inMemoryPlayerKey) { states.remove(inMemoryPlayerKey); participantIds.remove(inMemoryPlayerKey); }
    public synchronized void clear() { states.clear(); participantIds.clear(); }
    /** Conservative policy for global Director decisions: every active player must consent. */
    public synchronized boolean allActiveAccepted() { if(states.isEmpty()) return false; for(State s:states.values()) if(s!=State.ACCEPTED)return false; return true; }
}
