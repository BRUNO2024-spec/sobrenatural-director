package com.sobrenaturaldirector.narrative;

public enum NarrativePlanLifecycleState {
    PROPOSED, ACTIVE, DEFERRED, STALE, REPLANNING, EXECUTING, COMPLETED, ABORTED, SUPERSEDED;

    public boolean isTerminal() {
        return this == COMPLETED || this == ABORTED || this == SUPERSEDED;
    }
}
