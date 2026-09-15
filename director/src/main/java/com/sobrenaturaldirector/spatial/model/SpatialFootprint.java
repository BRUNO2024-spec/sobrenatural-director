package com.sobrenaturaldirector.spatial.model;
import com.sobrenaturaldirector.spatial.geometry.LocalAabb;
public final class SpatialFootprint {private final LocalAabb bounds;private final long volume;public SpatialFootprint(LocalAabb bounds,long volume){if(bounds==null||volume<=0)throw new IllegalArgumentException("invalid footprint");this.bounds=bounds;this.volume=volume;}public LocalAabb getBounds(){return bounds;}public long getVolume(){return volume;}}
