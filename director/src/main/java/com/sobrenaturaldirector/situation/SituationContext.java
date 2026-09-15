package com.sobrenaturaldirector.situation;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import com.sobrenaturaldirector.capability.DirectorContext;
import com.sobrenaturaldirector.environment.model.SemanticRegionProfile;

/** Immutable world/player snapshot consumed by situation intelligence. */
public final class SituationContext {
    private final int dimension, regionX, regionZ;
    private final long tick, seed;
    private final boolean night, suitableRegion, playerPresent, lowRecentActivity;
    private final Set<String> environment;
    private final SemanticRegionProfile regionProfile;
    public SituationContext(int dimension, long tick, int regionX, int regionZ, long seed, boolean night,
            boolean suitableRegion, boolean playerPresent, boolean lowRecentActivity, Set<String> environment) {
        this(dimension, tick, regionX, regionZ, seed, night, suitableRegion, playerPresent, lowRecentActivity, environment, null);
    }
    public SituationContext(int dimension, long tick, int regionX, int regionZ, long seed, boolean night,
            boolean suitableRegion, boolean playerPresent, boolean lowRecentActivity, Set<String> environment,
            SemanticRegionProfile regionProfile) {
        if (tick < 0) throw new IllegalArgumentException("tick must be non-negative");
        this.dimension = dimension; this.tick = tick; this.regionX = regionX; this.regionZ = regionZ; this.seed = seed;
        this.night = night; this.suitableRegion = suitableRegion; this.playerPresent = playerPresent; this.lowRecentActivity = lowRecentActivity;
        this.regionProfile = regionProfile;
        TreeSet<String> values = new TreeSet<String>(); if (environment != null) values.addAll(environment);
        this.environment = Collections.unmodifiableSet(values);
    }
    public int getDimension() { return dimension; } public long getTick() { return tick; }
    public int getRegionX() { return regionX; } public int getRegionZ() { return regionZ; }
    public long getSeed() { return seed; } public boolean isNight() { return night; }
    public boolean isSuitableRegion() { return suitableRegion; } public boolean isPlayerPresent() { return playerPresent; }
    public boolean isLowRecentActivity() { return lowRecentActivity; } public Set<String> getEnvironment() { return environment; }
    public SemanticRegionProfile getRegionProfile() { return regionProfile; }
    public DirectorContext toPlannerContext() {
        TreeSet<String> values = new TreeSet<String>(environment);
        if (regionProfile != null) {
            values.add("region.classification=" + regionProfile.getClassification().name());
            values.add("region.confidence=" + regionProfile.getConfidence());
            values.add("region.protected=" + regionProfile.isProtectedPlayerArea());
        }
        return new DirectorContext(dimension, tick, regionX, regionZ, values);
    }
}
