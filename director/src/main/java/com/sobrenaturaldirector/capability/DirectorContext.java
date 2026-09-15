package com.sobrenaturaldirector.capability;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/** Small immutable planning context; it has no Minecraft or provider implementation types. */
public final class DirectorContext {
    private final int dimension;
    private final long tick;
    private final int regionX, regionZ;
    private final Set<String> environment;
    public DirectorContext(int dimension, long tick, int regionX, int regionZ, Set<String> environment) {
        this.dimension = dimension; this.tick = tick; this.regionX = regionX; this.regionZ = regionZ;
        TreeSet<String> values = new TreeSet<String>(); if (environment != null) values.addAll(environment);
        this.environment = Collections.unmodifiableSet(values);
    }
    public int getDimension() { return dimension; }
    public long getTick() { return tick; }
    public int getRegionX() { return regionX; }
    public int getRegionZ() { return regionZ; }
    public Set<String> getEnvironment() { return environment; }
}
