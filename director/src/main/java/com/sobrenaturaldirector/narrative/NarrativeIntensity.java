package com.sobrenaturaldirector.narrative;

/** Provider-neutral semantic pressure, deliberately separate from world impact. */
public enum NarrativeIntensity {
    SUBTLE(0), LOW(1), MODERATE(2), HIGH(3), EXTREME(4);
    private final int rank;
    NarrativeIntensity(int rank) { this.rank = rank; }
    public int getRank() { return rank; }
}
