package com.sobrenaturaldirector.content.model;

import java.util.Locale;
import com.sobrenaturaldirector.domain.StableId;

public final class SemanticCapability implements Comparable<SemanticCapability> {
    private final String value;
    public SemanticCapability(String value) {
        String v = StableId.require(value, "capability").toLowerCase(Locale.ENGLISH);
        if (!v.matches("[a-z][a-z0-9._-]{0,31}:[a-z][a-z0-9._-]{0,63}")) throw new IllegalArgumentException("invalid capability");
        this.value = v;
    }
    public String getValue() { return value; }
    public int compareTo(SemanticCapability other) { return value.compareTo(other.value); }
    @Override public boolean equals(Object o) { return o instanceof SemanticCapability && value.equals(((SemanticCapability)o).value); }
    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}
