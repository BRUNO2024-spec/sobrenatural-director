package com.sobrenaturaldirector.naturalpressure;

public final class NaturalThreatPressureScope {
    private final int dimension, minChunkX, maxChunkX, minChunkZ, maxChunkZ;
    private NaturalThreatPressureScope(int dimension, int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) { if (minChunkX > maxChunkX || minChunkZ > maxChunkZ) throw new IllegalArgumentException("invalid pressure scope"); this.dimension=dimension; this.minChunkX=minChunkX; this.maxChunkX=maxChunkX; this.minChunkZ=minChunkZ; this.maxChunkZ=maxChunkZ; }
    public static NaturalThreatPressureScope world(int dimension) { return new NaturalThreatPressureScope(dimension, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static NaturalThreatPressureScope region(int dimension, int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) { return new NaturalThreatPressureScope(dimension, minChunkX, maxChunkX, minChunkZ, maxChunkZ); }
    public boolean contains(int dimension, int chunkX, int chunkZ) { return this.dimension == dimension && chunkX >= minChunkX && chunkX <= maxChunkX && chunkZ >= minChunkZ && chunkZ <= maxChunkZ; }
    public boolean isWorldWide() { return minChunkX == Integer.MIN_VALUE && maxChunkX == Integer.MAX_VALUE && minChunkZ == Integer.MIN_VALUE && maxChunkZ == Integer.MAX_VALUE; }
    public int getDimension() { return dimension; }
    public int getMinChunkX() { return minChunkX; }
    public int getMaxChunkX() { return maxChunkX; }
    public int getMinChunkZ() { return minChunkZ; }
    public int getMaxChunkZ() { return maxChunkZ; }
}
