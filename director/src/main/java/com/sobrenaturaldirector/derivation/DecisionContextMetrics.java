package com.sobrenaturaldirector.derivation;

import java.util.concurrent.atomic.AtomicLong;

/** Observability-only counters for missing live context sources. */
public final class DecisionContextMetrics {
    private final AtomicLong tensionFallback=new AtomicLong();
    private final AtomicLong pressureFallback=new AtomicLong();
    private final AtomicLong recoveryFallback=new AtomicLong();
    private final AtomicLong providerFallback=new AtomicLong();
    private final AtomicLong contentFallback=new AtomicLong();
    private final AtomicLong locationFallback=new AtomicLong();
    public void tensionFallback(){tensionFallback.incrementAndGet();}
    public void pressureFallback(){pressureFallback.incrementAndGet();}
    public void recoveryFallback(){recoveryFallback.incrementAndGet();}
    public void providerFallback(){providerFallback.incrementAndGet();}
    public void contentFallback(){contentFallback.incrementAndGet();}
    public void locationFallback(){locationFallback.incrementAndGet();}
    public long getTensionFallback(){return tensionFallback.get();}
    public long getPressureFallback(){return pressureFallback.get();}
    public long getRecoveryFallback(){return recoveryFallback.get();}
    public long getProviderFallback(){return providerFallback.get();}
    public long getContentFallback(){return contentFallback.get();}
    public long getLocationFallback(){return locationFallback.get();}
}
