package com.sobrenaturaldirector.observation.model;

public final class EnvironmentSnapshot {
    private final int biomeId;
    private final String biomeName;
    private final int blockLight;
    private final boolean canSeeSky;
    private final double y;

    public EnvironmentSnapshot(int biomeId, String biomeName, int blockLight, boolean canSeeSky, double y) {
        if (biomeName == null || biomeName.length() > 128 || blockLight < 0 || blockLight > 15 || !finite(y)) throw new IllegalArgumentException("invalid environment snapshot");
        this.biomeId = biomeId; this.biomeName = biomeName; this.blockLight = blockLight; this.canSeeSky = canSeeSky; this.y = y;
    }
    private static boolean finite(double value) { return !Double.isNaN(value) && !Double.isInfinite(value); }
    public int getBiomeId() { return biomeId; }
    public String getBiomeName() { return biomeName; }
    public int getBlockLight() { return blockLight; }
    public boolean canSeeSky() { return canSeeSky; }
    public double getY() { return y; }
    @Override public boolean equals(Object o) { if (!(o instanceof EnvironmentSnapshot)) return false; EnvironmentSnapshot x=(EnvironmentSnapshot)o; return biomeId==x.biomeId&&biomeName.equals(x.biomeName)&&blockLight==x.blockLight&&canSeeSky==x.canSeeSky&&Double.compare(y,x.y)==0; }
    @Override public int hashCode() { return java.util.Arrays.hashCode(new Object[]{biomeId,biomeName,blockLight,canSeeSky,y}); }
}
