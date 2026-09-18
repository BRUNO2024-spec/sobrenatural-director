package com.sobrenaturaldirector.situation;

/** Minimal factual lifecycle for an executable narrative situation. */
public enum SituationLifecycleState {
    PLANNED, READY, EXECUTING, ACTIVE, WAITING, SUSPENDED,
    COMPLETED, ABORTED, FAILED, COMPENSATED, STALE;
    public boolean isTerminal() {
        return this == COMPLETED || this == ABORTED || this == FAILED || this == COMPENSATED || this == STALE;
    }
}
