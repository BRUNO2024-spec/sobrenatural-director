package com.sobrenaturaldirector.content.model;

import java.util.Locale;
import com.sobrenaturaldirector.domain.StableId;

public final class ProviderId implements Comparable<ProviderId> {
    private final String value;
    public ProviderId(String value) {
        String v = StableId.require(value, "providerId").toLowerCase(Locale.ENGLISH);
        if (!v.matches("[a-z0-9][a-z0-9._-]{0,63}")) throw new IllegalArgumentException("invalid providerId");
        value = v;
        this.value = value;
    }
    public String getValue() { return value; }
    public int compareTo(ProviderId other) { return value.compareTo(other.value); }
    @Override public boolean equals(Object o) { return o instanceof ProviderId && value.equals(((ProviderId)o).value); }
    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}
