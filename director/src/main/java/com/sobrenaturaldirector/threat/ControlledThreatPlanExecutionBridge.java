package com.sobrenaturaldirector.threat;

import java.util.UUID;
import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.capability.CapabilityPlanner;
import com.sobrenaturaldirector.capability.CapabilityQuery;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;
import com.sobrenaturaldirector.situation.ConfirmedThreatOutcome;
import com.sobrenaturaldirector.situation.SituationContext;
import com.sobrenaturaldirector.situation.SituationMemory;
import com.sobrenaturaldirector.situation.SituationIntensity;
import com.sobrenaturaldirector.situation.ThreatNarrativeAssessment;
import com.sobrenaturaldirector.situation.ThreatNarrativePolicy;
import com.sobrenaturaldirector.situation.ThreatOutcomeType;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.threat.PersistentNarrativeThreat;
import com.sobrenaturaldirector.threat.ThreatPhysicalBinding;

/** Explicit, role-scoped handoff from a semantic plan to the generic threat boundary. */
public final class ControlledThreatPlanExecutionBridge {
    private static final String SOURCE = "CONTROLLED_PLAN_BRIDGE";
    private final DirectorProviderRegistry registry;
    private final ThreatNarrativePolicy pacing = new ThreatNarrativePolicy();

    public ControlledThreatPlanExecutionBridge(DirectorProviderRegistry registry) { if (registry == null) throw new IllegalArgumentException("registry is required"); this.registry = registry; }

    public ThreatExecutionResult execute(PlanExecutionSlice slice, PlanExecutionAuthorization authorization, SituationContext context, SituationMemory memory, WorldServer world, DirectorWorldSavedData saved, boolean globalControlledExecution) {
        ThreatExecutionResult rejected = validate(slice, authorization, context, memory, world, saved, globalControlledExecution);
        if (rejected != null) return rejected;
        PlanExecutionRecord existing = saved.getPlanExecution(authorization.getExecutionId());
        if (existing != null) {
            if (existing.getStatus() == PlanExecutionStatus.RESOLVED) return result(ThreatExecutionResult.Status.REJECTED_ALREADY_HANDLED, existing.getRequestId(), existing.getEntityUuid());
            if (existing.getStatus() == PlanExecutionStatus.EXECUTED) {
                ThreatJournalEntry journal = saved.getThreat(existing.getRequestId());
                return journal != null && journal.getState() == ThreatLifecycleState.ACTIVE ? result(ThreatExecutionResult.Status.RECONCILED_EXISTING, existing.getRequestId(), existing.getEntityUuid()) : result(ThreatExecutionResult.Status.FAILED, existing.getRequestId(), null);
            }
            if (existing.getStatus() == PlanExecutionStatus.EXECUTING) {
                ThreatJournalEntry journal = saved.getThreat(existing.getRequestId());
                if (journal != null && journal.getState() == ThreatLifecycleState.ACTIVE) { saved.updatePlanExecution(existing.withStatus(PlanExecutionStatus.EXECUTED, journal.getEntityUuid(), journal.getThreatInstanceId(), existing.isOutcomeRecorded(), existing.getResolvedAt())); return result(ThreatExecutionResult.Status.RECONCILED_EXISTING, existing.getRequestId(), journal.getEntityUuid()); }
                return result(ThreatExecutionResult.Status.FAILED, existing.getRequestId(), null);
            }
        }
        if (!pacingEligible(slice, context, memory)) return result(ThreatExecutionResult.Status.REJECTED_PACING_CHANGED, authorization.getExecutionId(), null);
        if (world == null || saved == null || context == null || !"Server thread".equals(Thread.currentThread().getName())) return result(ThreatExecutionResult.Status.REJECTED_POLICY, authorization.getExecutionId(), null);
        ThreatProvider provider = provider(slice.getProviderId());
        ThreatDefinition definition = provider.getThreatDefinition();
        String requestId = authorization.getExecutionId() + ":threat";
        PlanExecutionRecord record = new PlanExecutionRecord(authorization.getExecutionId(), slice.getParentPlanId(), slice.getPlanFingerprint(), CapabilityVocabulary.THREAT_SOURCE.getValue(), slice.getProviderId(), requestId, "", PlanExecutionStatus.EXECUTING, null, false, context.getTick(), 0L);
        if (existing == null) saved.recordPlanExecution(record); else saved.updatePlanExecution(record);
        int spawnY = world.getSpawnPoint().posY;
        ThreatExecutionRequest request = new ThreatExecutionRequest(requestId, SOURCE, slice.getProviderId(), definition.getDefinitionId(), ThreatOwnership.ORIGIN_VALUE, provider.getExecutionVersion(), DirectorRuntimeMode.CONTROLLED_EXECUTION, world, context.getDimension(), context.getRegionX() * 16, spawnY, context.getRegionZ() * 16, true);
        String narrativeId = narrativeId(requestId);
        PersistentNarrativeThreat narrative = new PersistentNarrativeThreat(narrativeId, requestId, slice.getProviderId(), definition.getDefinitionId(), ThreatOwnership.ORIGIN_VALUE, ThreatLifecycleState.MATERIALIZING, request.getDimension(), request.getX(), request.getY(), request.getZ(), context.getTick(), context.getTick(), 0L);
        if (saved.getNarrativeThreat(narrativeId) == null) saved.recordNarrativeThreat(narrative);
        ThreatExecutionResult result = new ControlledThreatBoundary(registry).execute(request, saved);
        if (result.getStatus() == ThreatExecutionResult.Status.CREATED || result.getStatus() == ThreatExecutionResult.Status.RECONCILED_EXISTING) {
            ThreatJournalEntry journal = saved.getThreat(requestId);
            if (journal == null || journal.getState() != ThreatLifecycleState.ACTIVE || result.getEntityUuid() == null) { saved.updatePlanExecution(record.withStatus(PlanExecutionStatus.FAILED, null, null, false, 0L)); return result(ThreatExecutionResult.Status.FAILED, requestId, null); }
            PersistentNarrativeThreat materialized = narrative.withState(ThreatLifecycleState.MATERIALIZED, context.getTick());
            saved.updateNarrativeThreat(materialized);
            if (saved.getThreatBinding(narrativeId) == null) saved.recordThreatBinding(new ThreatPhysicalBinding(narrativeId, slice.getProviderId(), result.getEntityUuid(), "MATERIALIZED", request.getDimension(), request.getX(), request.getY(), request.getZ(), 1, context.getTick()));
            PlanExecutionRecord completed = record.withStatus(PlanExecutionStatus.EXECUTED, result.getEntityUuid(), journal.getThreatInstanceId(), false, 0L); saved.updatePlanExecution(completed);
            if (result.getStatus() == ThreatExecutionResult.Status.CREATED && memory != null && !materialized.isOutcomeConfirmed()) {
                saved.updateNarrativeThreat(materialized.withOutcomeConfirmed(true, context.getTick()));
                memory.recordConfirmedThreatOutcome(new ConfirmedThreatOutcome(context.getTick(), context.getRegionX(), context.getRegionZ(), slice.getBlueprint().getIntensity(), ThreatOutcomeType.MANIFESTED));
                saved.updatePlanExecution(completed.withStatus(PlanExecutionStatus.EXECUTED, result.getEntityUuid(), journal.getThreatInstanceId(), true, 0L));
            }
        } else { saved.updatePlanExecution(record.withStatus(PlanExecutionStatus.FAILED, null, null, false, 0L)); saved.updateNarrativeThreat(narrative.withState(ThreatLifecycleState.FAILED, context.getTick())); }
        return result;
    }

    public ThreatExecutionResult cleanup(PlanExecutionSlice slice, PlanExecutionAuthorization authorization, WorldServer world, DirectorWorldSavedData saved, boolean globalControlledExecution) {
        if (slice == null || authorization == null || !authorization.isAuthorized() || !globalControlledExecution) return result(ThreatExecutionResult.Status.REJECTED_NOT_AUTHORIZED, authorization == null ? "" : authorization.getExecutionId(), null);
        PlanExecutionRecord record = saved == null ? null : saved.getPlanExecution(authorization.getExecutionId());
        if (record != null && record.getStatus() == PlanExecutionStatus.RESOLVED) return result(ThreatExecutionResult.Status.REJECTED_ALREADY_HANDLED, record.getRequestId(), record.getEntityUuid());
        if (record == null || record.getStatus() != PlanExecutionStatus.EXECUTED || !matches(slice, authorization, record)) return result(ThreatExecutionResult.Status.REJECTED_STALE_PLAN, record == null ? authorization.getExecutionId() : record.getRequestId(), null);
        ThreatProvider provider = provider(record.getProviderId());
        ThreatExecutionRequest request = new ThreatExecutionRequest(record.getRequestId(), SOURCE, record.getProviderId(), provider.getThreatDefinition().getDefinitionId(), ThreatOwnership.ORIGIN_VALUE, provider.getExecutionVersion(), DirectorRuntimeMode.CONTROLLED_EXECUTION, world, 0, world.getSpawnPoint().posY, 0, 0, true);
        ThreatExecutionResult result = new ControlledThreatBoundary(registry).cleanup(request, saved);
        if (result.getStatus() == ThreatExecutionResult.Status.CLEANUP_EXECUTED) {
            saved.updatePlanExecution(record.withStatus(PlanExecutionStatus.RESOLVED, result.getEntityUuid(), record.getThreatInstanceId(), record.isOutcomeRecorded(), world.getTotalWorldTime()));
            PersistentNarrativeThreat narrative = saved.getNarrativeThreat(narrativeId(record.getRequestId()));
            if (narrative != null) saved.updateNarrativeThreat(narrative.withState(ThreatLifecycleState.RESOLVED, world.getTotalWorldTime()));
            ThreatPhysicalBinding binding = saved.getThreatBinding(narrativeId(record.getRequestId()));
            if (binding != null) saved.updateThreatBinding(binding.withStatus("RESOLVED", binding.getGeneration(), world.getTotalWorldTime()));
        }
        return result;
    }

    private ThreatExecutionResult validate(PlanExecutionSlice slice, PlanExecutionAuthorization authorization, SituationContext context, SituationMemory memory, WorldServer world, DirectorWorldSavedData saved, boolean global) {
        if (slice == null || authorization == null || !authorization.isAuthorized()) return result(ThreatExecutionResult.Status.REJECTED_NOT_AUTHORIZED, authorization == null ? "" : authorization.getExecutionId(), null);
        if (!global) return result(ThreatExecutionResult.Status.REJECTED_DISABLED, authorization.getExecutionId(), null);
        if (!matches(slice, authorization, null) || !slice.getPlan().isComplete() || !new CapabilityPlanner(registry).isCurrent(slice.getPlan())) return result(ThreatExecutionResult.Status.REJECTED_STALE_PLAN, authorization.getExecutionId(), null);
        if (authorization.getRole() != slice.getRole() || !CapabilityVocabulary.THREAT_SOURCE.getValue().equals(authorization.getCapability())) return result(ThreatExecutionResult.Status.REJECTED_INVALID_SCOPE, authorization.getExecutionId(), null);
        ThreatProvider selectedProvider = provider(slice.getProviderId());
        boolean selectedCapability = false; for (com.sobrenaturaldirector.capability.CapabilityDescriptor descriptor : registry.query(new CapabilityQuery(CapabilityVocabulary.THREAT_SOURCE))) if (slice.getProviderId().equals(descriptor.getProvider())) { selectedCapability = true; break; }
        if (selectedProvider == null || !selectedProvider.isAvailable() || !selectedCapability) return result(ThreatExecutionResult.Status.REJECTED_PROVIDER_UNAVAILABLE, authorization.getExecutionId(), null);
        return null;
    }
    private boolean pacingEligible(PlanExecutionSlice slice, SituationContext context, SituationMemory memory) {
        ThreatNarrativeAssessment assessment = pacing.assess(slice.getBlueprint(), context, memory, null);
        return assessment.isEligible() && !assessment.isOptionalSuppressed();
    }
    private boolean matches(PlanExecutionSlice slice, PlanExecutionAuthorization authorization, PlanExecutionRecord record) { return slice != null && authorization != null && slice.getParentPlanId().equals(authorization.getParentPlanId()) && slice.getPlanFingerprint().equals(authorization.getPlanFingerprint()) && slice.getRole() == authorization.getRole() && slice.getProviderId().equals(authorization.getProviderId()) && (record == null || record.getParentPlanId().equals(authorization.getParentPlanId())); }
    private ThreatProvider provider(ProviderId id) { Object value = registry.get(id); return value instanceof ThreatProvider ? (ThreatProvider) value : null; }
    private static String narrativeId(String requestId) { return "narrative-threat:" + requestId; }
    private static ThreatExecutionResult result(ThreatExecutionResult.Status status, String id, UUID uuid) { return ThreatExecutionResult.of(status, id, uuid); }
}
