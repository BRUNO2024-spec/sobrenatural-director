package com.sobrenaturaldirector.situation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Bounded short memory; it is not learning or persistence. */
public final class SituationMemory {
    public static final int MAX_ENTRIES = 32;
    public static final int MAX_THREAT_OUTCOMES = 16;
    private final Deque<SituationMemoryEntry> entries = new ArrayDeque<SituationMemoryEntry>();
    private final Deque<ConfirmedThreatOutcome> threatOutcomes = new ArrayDeque<ConfirmedThreatOutcome>();
    public void record(SituationMemoryEntry entry) { if (entry == null) return; for (SituationMemoryEntry existing : entries) if (existing.getId().equals(entry.getId())) return; if (entries.size() == MAX_ENTRIES) entries.removeFirst(); entries.addLast(entry); }
    public List<SituationMemoryEntry> snapshot() { return Collections.unmodifiableList(new ArrayList<SituationMemoryEntry>(entries)); }
    public int recentGoalCount(SituationGoal goal, long tick, long window) { int count = 0; for (SituationMemoryEntry e : entries) if (e.getGoal() == goal && tick >= e.getTick() && tick - e.getTick() <= window) count++; return count; }
    public int recentRegionCount(SituationGoal goal, int x, int z, long tick, long window) { int count = 0; for (SituationMemoryEntry e : entries) if (e.getGoal() == goal && e.getRegionX() == x && e.getRegionZ() == z && tick >= e.getTick() && tick - e.getTick() <= window) count++; return count; }
    public int recentBlueprintCount(String fingerprint, long tick, long window) { int count = 0; for (SituationMemoryEntry e : entries) if (fingerprint != null && fingerprint.equals(e.getCombination()) && tick >= e.getTick() && tick - e.getTick() <= window) count++; return count; }
    /** Records an outcome, not a plan; failed/aborted outcomes remain bounded but do not count as confirmed threats. */
    public void recordConfirmedThreatOutcome(ConfirmedThreatOutcome outcome) { if (outcome == null) return; if (threatOutcomes.size() == MAX_THREAT_OUTCOMES) threatOutcomes.removeFirst(); threatOutcomes.addLast(outcome); }
    public List<ConfirmedThreatOutcome> confirmedThreats() { return Collections.unmodifiableList(new ArrayList<ConfirmedThreatOutcome>(threatOutcomes)); }
}
