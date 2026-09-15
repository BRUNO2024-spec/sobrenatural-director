package com.sobrenaturaldirector.execution;

/** Semantic order for a coordinated provider execution. */
public enum CoordinatedSlicePhase {
    STRUCTURE(0), ACTOR(1), THREAT(2);
    private final int order;
    CoordinatedSlicePhase(int order) { this.order = order; }
    public int getOrder() { return order; }
}
