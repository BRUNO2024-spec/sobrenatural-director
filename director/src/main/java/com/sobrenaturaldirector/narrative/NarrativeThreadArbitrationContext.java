package com.sobrenaturaldirector.narrative;
import java.util.*;
/** Immutable semantic context passed to an explicit arbitration caller. */
public final class NarrativeThreadArbitrationContext {
 private final String threadId,subject,currentPlanIntent;private final NarrativeThreadState state;private final Set<String> tags;private final List<String> recentIntents;private final List<NarrativeThreadPlanHistory> history;
 public NarrativeThreadArbitrationContext(PersistentNarrativeThread t,String currentPlanIntent){if(t==null)throw new IllegalArgumentException("thread required");threadId=t.getId();subject=t.getSubject();state=t.getState();tags=Collections.unmodifiableSet(new TreeSet<String>(t.getTags()));this.currentPlanIntent=currentPlanIntent==null?"":currentPlanIntent;recentIntents=Collections.unmodifiableList(new ArrayList<String>(t.getRecentIntents()));history=Collections.unmodifiableList(new ArrayList<NarrativeThreadPlanHistory>(t.getHistory()));}
 public String getThreadId(){return threadId;}public String getSubject(){return subject;}public NarrativeThreadState getState(){return state;}public Set<String> getTags(){return tags;}public String getCurrentPlanIntent(){return currentPlanIntent;}public List<String> getRecentIntents(){return recentIntents;}public List<NarrativeThreadPlanHistory> getHistory(){return history;}
}
