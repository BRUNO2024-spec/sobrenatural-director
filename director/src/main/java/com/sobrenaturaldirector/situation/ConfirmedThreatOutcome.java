package com.sobrenaturaldirector.situation;

/** Provider-neutral record of an outcome that was actually confirmed. */
public final class ConfirmedThreatOutcome {
    private final long tick;
    private final int regionX, regionZ;
    private final SituationIntensity intensity;
    private final ThreatOutcomeType type;

    public ConfirmedThreatOutcome(long tick, int regionX, int regionZ, SituationIntensity intensity, ThreatOutcomeType type) {
        if (tick < 0 || intensity == null || type == null) throw new IllegalArgumentException("invalid threat outcome");
        this.tick = tick; this.regionX = regionX; this.regionZ = regionZ; this.intensity = intensity; this.type = type;
    }
    public long getTick() { return tick; }
    public int getRegionX() { return regionX; }
    public int getRegionZ() { return regionZ; }
    public SituationIntensity getIntensity() { return intensity; }
    public ThreatOutcomeType getType() { return type; }
    public boolean isConfirmedThreat() { return type == ThreatOutcomeType.MANIFESTED || type == ThreatOutcomeType.RESOLVED; }
}
