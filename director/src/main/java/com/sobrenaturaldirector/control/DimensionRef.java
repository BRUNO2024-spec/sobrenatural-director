package com.sobrenaturaldirector.control;

import java.util.*;

/** Immutable dimension identity safe for planning and persistence. */
public final class DimensionRef implements Comparable<DimensionRef> {
    private final int dimensionId;
    private final String providerIdentity;
    private final Set<String> tags;
    private final boolean available;
    public DimensionRef(int id, String provider, Collection<String> tags, boolean available) {
        if (provider == null || provider.length() > 128) throw new IllegalArgumentException("invalid dimension provider");
        this.dimensionId = id; this.providerIdentity = provider;
        TreeSet<String> copy = new TreeSet<String>();
        if (tags != null) for (String tag : tags) { if (tag == null || tag.length() == 0 || tag.length() > 64) throw new IllegalArgumentException("invalid dimension tag"); copy.add(tag); }
        this.tags = Collections.unmodifiableSet(copy); this.available = available;
    }
    public DimensionRef(int id) { this(id, "unknown", Collections.<String>emptySet(), true); }
    public int getDimensionId() { return dimensionId; }
    public String getProviderIdentity() { return providerIdentity; }
    public Set<String> getTags() { return tags; }
    public boolean isAvailable() { return available; }
    public int compareTo(DimensionRef other) { int c = Integer.compare(dimensionId, other.dimensionId); return c != 0 ? c : providerIdentity.compareTo(other.providerIdentity); }
    @Override public boolean equals(Object o) { return o instanceof DimensionRef && compareTo((DimensionRef)o) == 0 && tags.equals(((DimensionRef)o).tags) && available == ((DimensionRef)o).available; }
    @Override public int hashCode() { return Arrays.hashCode(new Object[] { dimensionId, providerIdentity, tags, available }); }
}
