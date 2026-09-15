package com.sobrenaturaldirector.threat;

import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;

/** Applies bounded, fail-safe binding observations without materializing entities. */
public final class ThreatLifecycleReconciler {
    public ThreatLifecycleState reconcile(DirectorWorldSavedData saved, String narrativeId,
            ThreatPhysicalObservation observation, long tick) {
        if (saved == null || narrativeId == null || observation == null) throw new IllegalArgumentException("reconciliation inputs are required");
        PersistentNarrativeThreat threat = saved.getNarrativeThreat(narrativeId);
        if (threat == null) throw new IllegalArgumentException("narrative threat not found");
        if (threat.getState() == ThreatLifecycleState.RESOLVED) return ThreatLifecycleState.RESOLVED;
        ThreatLifecycleState next = stateFor(observation);
        saved.updateNarrativeThreat(threat.withState(next, tick));
        ThreatPhysicalBinding binding = saved.getThreatBinding(narrativeId);
        if (binding != null) {
            String status = observation.name();
            saved.updateThreatBinding(binding.withStatus(status, binding.getGeneration(), tick));
        }
        return next;
    }

    private static ThreatLifecycleState stateFor(ThreatPhysicalObservation observation) {
        switch (observation) {
        case ENTITY_PRESENT: return ThreatLifecycleState.MATERIALIZED;
        case CHUNK_UNAVAILABLE: return ThreatLifecycleState.TEMPORARILY_UNBOUND;
        case ENTITY_ABSENT: return ThreatLifecycleState.TEMPORARILY_UNBOUND;
        case ENTITY_DEAD: return ThreatLifecycleState.FAILED;
        case OWNERSHIP_MISMATCH: return ThreatLifecycleState.OWNERSHIP_MISMATCH;
        case WORLD_UNAVAILABLE: return ThreatLifecycleState.SUSPENDED;
        case PROVIDER_UNAVAILABLE: return ThreatLifecycleState.SUSPENDED;
        case RESOLVED: return ThreatLifecycleState.RESOLVED;
        default: throw new IllegalArgumentException("unsupported observation");
        }
    }
}
