package com.sobrenaturaldirector.narrative;

public enum NarrativeThreadState {
    ACTIVE, DORMANT, SUSPENDED, RESOLVED, ABANDONED;
    public boolean isTerminal() { return this == RESOLVED || this == ABANDONED; }
}
