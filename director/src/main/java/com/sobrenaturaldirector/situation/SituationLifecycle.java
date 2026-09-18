package com.sobrenaturaldirector.situation;

/** Explicit fail-closed state machine for situations. */
public final class SituationLifecycle {
    private SituationLifecycle() { }
    public static SituationLifecycleTransition transition(SituationLifecycleState from,
            SituationLifecycleState to, String reason, long tick, String trigger) {
        if (!allowed(from, to)) throw new IllegalStateException("illegal situation transition " + from + "->" + to);
        return new SituationLifecycleTransition(from, to, reason, tick, trigger);
    }
    public static boolean allowed(SituationLifecycleState from, SituationLifecycleState to) {
        if (from == null || to == null || from.isTerminal()) return false;
        if (from == to) return from == SituationLifecycleState.EXECUTING || from == SituationLifecycleState.ACTIVE || from == SituationLifecycleState.WAITING;
        switch (from) {
        case PLANNED: return to == SituationLifecycleState.READY || to == SituationLifecycleState.STALE || to == SituationLifecycleState.ABORTED;
        case READY: return to == SituationLifecycleState.EXECUTING || to == SituationLifecycleState.STALE || to == SituationLifecycleState.ABORTED;
        case EXECUTING: return to == SituationLifecycleState.ACTIVE || to == SituationLifecycleState.WAITING || to == SituationLifecycleState.SUSPENDED || to == SituationLifecycleState.COMPLETED || to == SituationLifecycleState.ABORTED || to == SituationLifecycleState.FAILED || to == SituationLifecycleState.COMPENSATED || to == SituationLifecycleState.STALE;
        case ACTIVE: return to == SituationLifecycleState.WAITING || to == SituationLifecycleState.SUSPENDED || to == SituationLifecycleState.COMPLETED || to == SituationLifecycleState.ABORTED || to == SituationLifecycleState.FAILED;
        case WAITING: return to == SituationLifecycleState.ACTIVE || to == SituationLifecycleState.SUSPENDED || to == SituationLifecycleState.COMPLETED || to == SituationLifecycleState.ABORTED || to == SituationLifecycleState.STALE;
        case SUSPENDED: return to == SituationLifecycleState.ACTIVE || to == SituationLifecycleState.WAITING || to == SituationLifecycleState.ABORTED || to == SituationLifecycleState.STALE;
        default: return false;
        }
    }
}
