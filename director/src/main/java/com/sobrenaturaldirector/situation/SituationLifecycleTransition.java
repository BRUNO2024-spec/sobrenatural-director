package com.sobrenaturaldirector.situation;

/** Immutable factual lifecycle transition; no model or preference label is stored. */
public final class SituationLifecycleTransition {
    private final SituationLifecycleState from, to;
    private final String reason, trigger;
    private final long tick;
    public SituationLifecycleTransition(SituationLifecycleState from, SituationLifecycleState to,
            String reason, long tick, String trigger) {
        if (from == null || to == null || reason == null || trigger == null || tick < 0)
            throw new IllegalArgumentException("invalid situation transition");
        this.from = from; this.to = to; this.reason = reason; this.tick = tick; this.trigger = trigger;
    }
    public SituationLifecycleState getFrom() { return from; }
    public SituationLifecycleState getTo() { return to; }
    public String getReason() { return reason; }
    public long getTick() { return tick; }
    public String getTrigger() { return trigger; }
}
