package com.sobrenaturaldirector.shadow;

import java.util.concurrent.atomic.AtomicLong;

public final class ShadowObservationMetrics {
    private final AtomicLong submitted=new AtomicLong(),requests=new AtomicLong(),scored=new AtomicLong(),dropped=new AtomicLong(),errors=new AtomicLong(),highWatermark=new AtomicLong();
    public void submitted(){submitted.incrementAndGet();} public void request(){requests.incrementAndGet();} public void scored(){scored.incrementAndGet();} public void dropped(){dropped.incrementAndGet();} public void error(){errors.incrementAndGet();} public void observeQueue(long depth){for(long old=highWatermark.get();depth>old&&!highWatermark.compareAndSet(old,depth);old=highWatermark.get()){}
    }
    public long getSubmitted(){return submitted.get();} public long getRequests(){return requests.get();} public long getScored(){return scored.get();} public long getDropped(){return dropped.get();} public long getErrors(){return errors.get();} public long getHighWatermark(){return highWatermark.get();}
}
