package com.sobrenaturaldirector.content.model;

import java.util.Locale;
import com.sobrenaturaldirector.domain.StableId;

public final class ContentKey implements Comparable<ContentKey> {
    private final String value;
    public ContentKey(String value) {
        String v = StableId.require(value, "contentKey").toLowerCase(Locale.ENGLISH);
        if (!v.matches("[a-z0-9][a-z0-9._-]{0,63}:[a-z0-9][a-z0-9._-]{0,127}")) throw new IllegalArgumentException("invalid contentKey");
        this.value = v;
    }
    public String getValue() { return value; }
    public int compareTo(ContentKey other) { return value.compareTo(other.value); }
    @Override public boolean equals(Object o) { return o instanceof ContentKey && value.equals(((ContentKey)o).value); }
    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}
