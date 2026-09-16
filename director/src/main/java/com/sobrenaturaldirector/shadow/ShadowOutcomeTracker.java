package com.sobrenaturaldirector.shadow;

import java.util.LinkedHashMap;
import java.util.Map;

/** Bounded factual outcome tracker with deterministic TTL expiry. */
public final class ShadowOutcomeTracker {
    private static final class Open { final String id; final long tick; Open(String id,long tick){this.id=id;this.tick=tick;} }
    private final Map<String,Open> open=new LinkedHashMap<String,Open>(); private final int max; private final long ttl;
    public ShadowOutcomeTracker(int max,long ttl){if(max<1||ttl<1)throw new IllegalArgumentException("invalid outcome bounds");this.max=max;this.ttl=ttl;}
    public synchronized boolean open(String id,long tick){if(id==null||id.length()==0||tick<0||open.size()>=max||open.containsKey(id))return false;open.put(id,new Open(id,tick));return true;}
    public synchronized ShadowOutcome close(String id,ShadowOutcome.Status status,long tick,int replans,String reason){Open x=open.remove(id);return x==null?null:new ShadowOutcome(id,status,tick,replans,reason);}
    public synchronized int expire(long tick){int n=0;java.util.Iterator<Open> i=open.values().iterator();while(i.hasNext()){Open x=i.next();if(tick>=x.tick&&tick-x.tick>=ttl){i.remove();n++;}}return n;}
    public synchronized int size(){return open.size();}
}
