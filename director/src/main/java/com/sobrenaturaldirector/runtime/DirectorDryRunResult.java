package com.sobrenaturaldirector.runtime;

import com.sobrenaturaldirector.decision.model.Intent;

public final class DirectorDryRunResult {
    private final long tick;
    private final int dimension;
    private final String observationId;
    private final int stateFingerprint;
    private final Intent decision;
    private final String planId;
    private final int resolvedCapabilities;
    private final String compositionSummary;
    private final String spatialSummary;
    private final int mutationCount;
    private final String blockedReason;
    private final String deterministicFingerprint;

    public DirectorDryRunResult(long tick, int dimension, String observationId, int stateFingerprint,
            Intent decision, String planId, int resolvedCapabilities, String compositionSummary,
            String spatialSummary, int mutationCount, String blockedReason, String deterministicFingerprint) {
        this.tick = tick;
        this.dimension = dimension;
        this.observationId = observationId;
        this.stateFingerprint = stateFingerprint;
        this.decision = decision;
        this.planId = planId;
        this.resolvedCapabilities = resolvedCapabilities;
        this.compositionSummary = compositionSummary;
        this.spatialSummary = spatialSummary;
        this.mutationCount = mutationCount;
        this.blockedReason = blockedReason;
        this.deterministicFingerprint = deterministicFingerprint;
    }
    public long getTick() { return tick; }
    public int getDimension() { return dimension; }
    public String getObservationId() { return observationId; }
    public int getStateFingerprint() { return stateFingerprint; }
    public Intent getDecision() { return decision; }
    public String getPlanId() { return planId; }
    public int getResolvedCapabilities() { return resolvedCapabilities; }
    public String getCompositionSummary() { return compositionSummary; }
    public String getSpatialSummary() { return spatialSummary; }
    public int getMutationCount() { return mutationCount; }
    public String getBlockedReason() { return blockedReason; }
    public String getDeterministicFingerprint() { return deterministicFingerprint; }
}
