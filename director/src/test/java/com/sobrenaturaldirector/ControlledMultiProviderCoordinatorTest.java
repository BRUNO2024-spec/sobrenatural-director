package com.sobrenaturaldirector;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.ProviderAvailability;
import com.sobrenaturaldirector.capability.SituationIntent;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.execution.*;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;
import com.sobrenaturaldirector.narrative.PersistentNarrativePlan;
import com.sobrenaturaldirector.narrative.NarrativePlanLifecycleState;
import com.sobrenaturaldirector.planning.environment.EnvironmentActionImpact;
import com.sobrenaturaldirector.environment.model.EnvironmentClassification;

public class ControlledMultiProviderCoordinatorTest {
    @Test public void preflightsAllSlicesBeforeExecutingAndOrdersSemantically() {
        SituationIntent intent = new SituationIntent("mpe", Arrays.asList(CapabilityVocabulary.STRUCTURE_SOURCE, CapabilityVocabulary.ACTOR_SOURCE, CapabilityVocabulary.THREAT_SOURCE));
        Map<com.sobrenaturaldirector.content.model.SemanticCapability, CapabilityDescriptor> selected = new LinkedHashMap<com.sobrenaturaldirector.content.model.SemanticCapability, CapabilityDescriptor>();
        selected.put(CapabilityVocabulary.STRUCTURE_SOURCE, descriptor(CapabilityVocabulary.STRUCTURE_SOURCE, "structure"));
        selected.put(CapabilityVocabulary.ACTOR_SOURCE, descriptor(CapabilityVocabulary.ACTOR_SOURCE, "actor"));
        selected.put(CapabilityVocabulary.THREAT_SOURCE, descriptor(CapabilityVocabulary.THREAT_SOURCE, "threat"));
        CandidatePlan candidate = new CandidatePlan(intent, selected, Arrays.asList("all"));
        final StringBuilder events = new StringBuilder();
        List<ControlledProviderSlice> slices = Arrays.asList(slice("threat", CoordinatedSlicePhase.THREAT, "threat", events, true), slice("structure", CoordinatedSlicePhase.STRUCTURE, "structure", events, true), slice("actor", CoordinatedSlicePhase.ACTOR, "actor", events, true));
        PreparedMultiProviderPlan prepared = new PreparedMultiProviderPlan(candidate, "mpe-fingerprint", slices);
        MultiProviderAuthorization authorization = new MultiProviderAuthorization("execution-1", candidate.signature(), "mpe-fingerprint", DirectorRuntimeMode.CONTROLLED_EXECUTION, true);
        DirectorWorldSavedData saved = new DirectorWorldSavedData();
        assertEquals(MultiProviderExecutionResult.Status.EXECUTED, new ControlledMultiProviderCoordinator().execute(prepared, authorization, saved, true).getStatus());
        assertEquals("preflight:structure;preflight:actor;preflight:threat;execute:structure;execute:actor;execute:threat;", events.toString());
        assertEquals(MultiProviderExecutionResult.Status.ALREADY_HANDLED, new ControlledMultiProviderCoordinator().execute(prepared, authorization, saved, true).getStatus());
    }
    @Test public void factoryRequiresAllSemanticSlicesAndStaysProviderNeutral() {
        SituationIntent intent = new SituationIntent("factory", Arrays.asList(CapabilityVocabulary.STRUCTURE_SOURCE, CapabilityVocabulary.ACTOR_SOURCE, CapabilityVocabulary.THREAT_SOURCE));
        Map<com.sobrenaturaldirector.content.model.SemanticCapability, CapabilityDescriptor> selected = new LinkedHashMap<com.sobrenaturaldirector.content.model.SemanticCapability, CapabilityDescriptor>();
        selected.put(CapabilityVocabulary.STRUCTURE_SOURCE, descriptor(CapabilityVocabulary.STRUCTURE_SOURCE, "fake-structure"));
        selected.put(CapabilityVocabulary.ACTOR_SOURCE, descriptor(CapabilityVocabulary.ACTOR_SOURCE, "fake-actor"));
        selected.put(CapabilityVocabulary.THREAT_SOURCE, descriptor(CapabilityVocabulary.THREAT_SOURCE, "fake-threat"));
        CandidatePlan candidate = new CandidatePlan(intent, selected, Arrays.asList("fake providers"));
        ExecutionSlicePreparer fake = new ExecutionSlicePreparer() {
            public boolean supports(CapabilityDescriptor capability) { return capability.getProvider().getValue().startsWith("fake-"); }
            public ControlledProviderSlice prepare(CandidatePlan plan, CapabilityDescriptor capability, ExecutionPreparationContext context) { final String id = capability.getProvider().getValue(); final CoordinatedSlicePhase phase = id.contains("structure") ? CoordinatedSlicePhase.STRUCTURE : id.contains("actor") ? CoordinatedSlicePhase.ACTOR : CoordinatedSlicePhase.THREAT; return slice(id, phase, id, new StringBuilder(), true); }
        };
        PreparedMultiProviderPlan prepared = new PreparedExecutionPlanFactory(Arrays.asList(fake)).prepare(candidate, new ExecutionPreparationContext(null, null, null, null, null, 0, 0, 0), "factory-fingerprint");
        assertEquals(3, prepared.getSlices().size());
        assertEquals(CoordinatedSlicePhase.STRUCTURE, prepared.getSlices().get(0).getPhase());
        assertEquals(CoordinatedSlicePhase.THREAT, prepared.getSlices().get(2).getPhase());
    }
    @Test public void persistedPlanRehydratesProviderNeutralCandidate() {
        Map<String, PersistentNarrativePlan.CapabilityRecord> capabilities = new LinkedHashMap<String, PersistentNarrativePlan.CapabilityRecord>();
        capabilities.put(CapabilityVocabulary.STRUCTURE_SOURCE.getValue(), new PersistentNarrativePlan.CapabilityRecord("fake-structure", ProviderAvailability.RUNTIME_CONTROL, 4, 1));
        capabilities.put(CapabilityVocabulary.ACTOR_SOURCE.getValue(), new PersistentNarrativePlan.CapabilityRecord("fake-actor", ProviderAvailability.RUNTIME_CONTROL, 3, 1));
        capabilities.put(CapabilityVocabulary.THREAT_SOURCE.getValue(), new PersistentNarrativePlan.CapabilityRecord("fake-threat", ProviderAvailability.RUNTIME_CONTROL, 2, 1));
        PersistentNarrativePlan persisted = new PersistentNarrativePlan("persisted-plan", "narrative", "LOOT_DISCOVERY", "blueprint", "blueprint-fp", "plan-fp", "candidate-fp", "INSIDE", EnvironmentActionImpact.INTRUSIVE, capabilities, false, NarrativePlanLifecycleState.ACTIVE, 1L, 1L, 100L, 1L, "VALID", Arrays.asList("validated"), "", 0, 1, 0, 1, 1, EnvironmentClassification.UNKNOWN, 0, 0);
        CandidatePlan candidate = new PersistentNarrativePlanExecutionBridge(new PreparedExecutionPlanFactory(Arrays.asList(new ExecutionSlicePreparer() {
            public boolean supports(CapabilityDescriptor capability) { return true; }
            public ControlledProviderSlice prepare(CandidatePlan plan, CapabilityDescriptor capability, ExecutionPreparationContext context) { return slice(capability.getProvider().getValue(), CoordinatedSlicePhase.STRUCTURE, capability.getProvider().getValue(), new StringBuilder(), true); }
        }))).candidate(persisted);
        assertTrue(candidate.isComplete());
        assertEquals("director:loot_discovery", candidate.getIntent().getId());
        assertEquals("fake-structure", candidate.getSelections().get(CapabilityVocabulary.STRUCTURE_SOURCE).getProvider().getValue());
        assertEquals(3, candidate.getSelections().size());
    }
    @Test public void failedSliceCompensatesCompletedSlicesInReverseAndReplayIsIdempotent() {
        final StringBuilder events = new StringBuilder();
        SituationIntent intent = new SituationIntent("failure", Arrays.asList(CapabilityVocabulary.STRUCTURE_SOURCE, CapabilityVocabulary.ACTOR_SOURCE, CapabilityVocabulary.THREAT_SOURCE));
        Map<com.sobrenaturaldirector.content.model.SemanticCapability, CapabilityDescriptor> selected = new LinkedHashMap<com.sobrenaturaldirector.content.model.SemanticCapability, CapabilityDescriptor>();
        selected.put(CapabilityVocabulary.STRUCTURE_SOURCE, descriptor(CapabilityVocabulary.STRUCTURE_SOURCE, "structure"));
        selected.put(CapabilityVocabulary.ACTOR_SOURCE, descriptor(CapabilityVocabulary.ACTOR_SOURCE, "actor"));
        selected.put(CapabilityVocabulary.THREAT_SOURCE, descriptor(CapabilityVocabulary.THREAT_SOURCE, "threat"));
        CandidatePlan candidate = new CandidatePlan(intent, selected, Arrays.asList("failure"));
        List<ControlledProviderSlice> slices = Arrays.asList(slice("structure", CoordinatedSlicePhase.STRUCTURE, "structure", events, true), slice("actor", CoordinatedSlicePhase.ACTOR, "actor", events, true), failureSlice("threat", CoordinatedSlicePhase.THREAT, "threat", events));
        PreparedMultiProviderPlan prepared = new PreparedMultiProviderPlan(candidate, "failure-fingerprint", slices);
        MultiProviderAuthorization authorization = new MultiProviderAuthorization("failure-execution", candidate.signature(), "failure-fingerprint", DirectorRuntimeMode.CONTROLLED_EXECUTION, true);
        DirectorWorldSavedData saved = new DirectorWorldSavedData();
        ControlledMultiProviderCoordinator coordinator = new ControlledMultiProviderCoordinator();
        assertEquals(MultiProviderExecutionResult.Status.COMPENSATED, coordinator.execute(prepared, authorization, saved, true).getStatus());
        assertEquals("preflight:structure;preflight:actor;preflight:threat;execute:structure;execute:actor;execute:threat;compensate:actor;compensate:structure;", events.toString());
        assertEquals(MultiProviderExecutionResult.Status.ALREADY_HANDLED, coordinator.execute(prepared, authorization, saved, true).getStatus());
        assertEquals("preflight:structure;preflight:actor;preflight:threat;execute:structure;execute:actor;execute:threat;compensate:actor;compensate:structure;", events.toString());
    }
    private static ControlledProviderSlice failureSlice(final String id, final CoordinatedSlicePhase phase, String provider, final StringBuilder events) {
        final ProviderId providerId = new ProviderId(provider);
        return new ControlledProviderSlice() {
            public String getSliceId() { return id; }
            public CoordinatedSlicePhase getPhase() { return phase; }
            public ProviderId getProviderId() { return providerId; }
            public CoordinatedSliceResult preflight() { events.append("preflight:").append(id).append(';'); return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.EXECUTED, id); }
            public CoordinatedSliceResult execute() { events.append("execute:").append(id).append(';'); return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.FAILED, id); }
            public CoordinatedSliceResult reconcile() { return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.RECONCILED, id); }
            public CoordinatedSliceResult compensate() { events.append("compensate:").append(id).append(';'); return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.COMPENSATED, id); }
        };
    }
    private static CapabilityDescriptor descriptor(com.sobrenaturaldirector.content.model.SemanticCapability id, String provider) { return new CapabilityDescriptor(id, new ProviderId(provider), ProviderAvailability.RUNTIME_CONTROL, 0, 0); }
    private static ControlledProviderSlice slice(final String id, final CoordinatedSlicePhase phase, String provider, final StringBuilder events, final boolean success) {
        final ProviderId providerId = new ProviderId(provider);
        return new ControlledProviderSlice() {
            public String getSliceId() { return id; }
            public CoordinatedSlicePhase getPhase() { return phase; }
            public ProviderId getProviderId() { return providerId; }
            public CoordinatedSliceResult preflight() { events.append("preflight:").append(id).append(';'); return CoordinatedSliceResult.of(success ? CoordinatedSliceResult.Status.EXECUTED : CoordinatedSliceResult.Status.REJECTED, id); }
            public CoordinatedSliceResult execute() { events.append("execute:").append(id).append(';'); return CoordinatedSliceResult.of(success ? CoordinatedSliceResult.Status.EXECUTED : CoordinatedSliceResult.Status.FAILED, id); }
            public CoordinatedSliceResult reconcile() { return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.RECONCILED, id); }
            public CoordinatedSliceResult compensate() { events.append("compensate:").append(id).append(';'); return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.COMPENSATED, id); }
        };
    }
}
