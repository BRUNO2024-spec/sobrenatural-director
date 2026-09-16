package com.sobrenaturaldirector.shadow;

import java.util.concurrent.atomic.AtomicLong;

public final class ShadowObservationMetrics {
    private final AtomicLong submitted=new AtomicLong(),scored=new AtomicLong(),dropped=new AtomicLong(),errors=new AtomicLong();
    public void submitted(){submitted.incrementAndGet();} public void scored(){scored.incrementAndGet();} public void dropped(){dropped.incrementAndGet();} public void error(){errors.incrementAndGet();}
    public long getSubmitted(){return submitted.get();} public long getScored(){return scored.get();} public long getDropped(){return dropped.get();} public long getErrors(){return errors.get();}
}
