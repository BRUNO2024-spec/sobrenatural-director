package com.sobrenaturaldirector.execution;

import com.sobrenaturaldirector.content.model.ProviderId;

/** Provider-neutral prepared slice. Providers implement the callbacks, not the saga. */
public interface ControlledProviderSlice {
    String getSliceId();
    CoordinatedSlicePhase getPhase();
    ProviderId getProviderId();
    CoordinatedSliceResult preflight();
    CoordinatedSliceResult execute();
    CoordinatedSliceResult reconcile();
    CoordinatedSliceResult compensate();
}
