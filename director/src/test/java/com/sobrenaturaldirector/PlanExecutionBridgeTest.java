package com.sobrenaturaldirector;

import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CapabilityPlanner;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.capability.ProviderAvailability;
import com.sobrenaturaldirector.capability.SituationIntent;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.DirectorContentProvider;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.provider.ProviderCapabilityState;
import com.sobrenaturaldirector.situation.ConfirmedThreatOutcome;
import com.sobrenaturaldirector.situation.SemanticRole;
import com.sobrenaturaldirector.situation.SituationBlueprint;
import com.sobrenaturaldirector.situation.SituationContext;
import com.sobrenaturaldirector.situation.SituationGoal;
import com.sobrenaturaldirector.situation.SituationIntensity;
import com.sobrenaturaldirector.situation.SituationMemory;
import com.sobrenaturaldirector.situation.ThreatOutcomeType;
import com.sobrenaturaldirector.threat.ControlledThreatPlanExecutionBridge;
import com.sobrenaturaldirector.threat.PlanExecutionAuthorization;
import com.sobrenaturaldirector.threat.PlanExecutionSlice;
import com.sobrenaturaldirector.threat.PlanExecutionRecord;
import com.sobrenaturaldirector.threat.PlanExecutionStatus;
import com.sobrenaturaldirector.threat.ThreatDefinition;
import com.sobrenaturaldirector.threat.ThreatExecutionRequest;
import com.sobrenaturaldirector.threat.ThreatExecutionResult;
import com.sobrenaturaldirector.threat.ThreatProvider;

public final class PlanExecutionBridgeTest {
    private static final SituationContext CONTEXT = new SituationContext(0, 10000, 4, 8, 77L, false, true, false, false, Collections.<String>emptySet());

    @Test public void authorizationIsRequired() {
        Fixture fixture = fixture();
        assertEquals(ThreatExecutionResult.Status.REJECTED_NOT_AUTHORIZED, fixture.bridge.execute(fixture.slice, fixture.authorization(false), CONTEXT, new SituationMemory(), null, new DirectorWorldSavedData(), true).getStatus());
    }

    @Test public void globalControlledExecutionIsRequired() {
        Fixture fixture = fixture();
        assertEquals(ThreatExecutionResult.Status.REJECTED_DISABLED, fixture.bridge.execute(fixture.slice, fixture.authorization(true), CONTEXT, new SituationMemory(), null, new DirectorWorldSavedData(), false).getStatus());
    }

    @Test public void planFingerprintAndProviderAreRevalidated() {
        Fixture fixture = fixture();
        PlanExecutionAuthorization stale = new PlanExecutionAuthorization("exec-stale", fixture.slice.getParentPlanId(), "wrong-fingerprint", SemanticRole.THREAT, CapabilityVocabulary.THREAT_SOURCE.getValue(), fixture.providerId, true);
        assertEquals(ThreatExecutionResult.Status.REJECTED_STALE_PLAN, fixture.bridge.execute(fixture.slice, stale, CONTEXT, new SituationMemory(), null, new DirectorWorldSavedData(), true).getStatus());
    }

    @Test public void pacingChangeBlocksPreviouslyValidPlan() {
        Fixture fixture = fixture(); SituationMemory memory = new SituationMemory();
        memory.recordConfirmedThreatOutcome(new ConfirmedThreatOutcome(9999, 4, 8, SituationIntensity.HIGH, ThreatOutcomeType.MANIFESTED));
        assertEquals(ThreatExecutionResult.Status.REJECTED_PACING_CHANGED, fixture.bridge.execute(fixture.slice, fixture.authorization(true), CONTEXT, memory, null, new DirectorWorldSavedData(), true).getStatus());
    }

    @Test public void planWithoutThreatSliceCannotBeConstructed() {
        Fixture fixture = fixture();
        try { new PlanExecutionSlice(fixture.plan, new SituationBlueprint("no-threat", SituationGoal.DISCOVERY, Collections.<SemanticRole>emptyList(), Collections.<SemanticRole>emptyList(), SituationIntensity.LOW, 1, 0), SemanticRole.THREAT); fail(); } catch (IllegalArgumentException expected) { }
    }

    @Test public void fakeProviderBindingIsResolvedWithoutProviderSpecificBridgeLogic() {
        Fixture fixture = fixture();
        assertEquals(fixture.providerId, fixture.slice.getProviderId());
        assertEquals(CapabilityVocabulary.THREAT_SOURCE, fixture.slice.getCapability().getId());
    }

    @Test public void resolutionReplayIsIdempotent() {
        Fixture fixture = fixture();
        DirectorWorldSavedData saved = new DirectorWorldSavedData();
        saved.recordPlanExecution(new PlanExecutionRecord("exec-1", fixture.slice.getParentPlanId(), fixture.slice.getPlanFingerprint(),
                CapabilityVocabulary.THREAT_SOURCE.getValue(), fixture.providerId, "exec-1:threat", "instance",
                PlanExecutionStatus.RESOLVED, null, true, 1L, 2L));
        assertEquals(ThreatExecutionResult.Status.REJECTED_ALREADY_HANDLED,
                fixture.bridge.cleanup(fixture.slice, fixture.authorization(true), null, saved, true).getStatus());
    }

    private static Fixture fixture() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry(); FakeProvider provider = new FakeProvider(); registry.register(provider);
        CandidatePlan plan = new CapabilityPlanner(registry).plan(new SituationIntent("plan", Collections.singletonList(CapabilityVocabulary.THREAT_SOURCE)), CONTEXT.toPlannerContext(), null).get(0);
        SituationBlueprint blueprint = new SituationBlueprint("threat", SituationGoal.DISCOVERY, Collections.singletonList(SemanticRole.THREAT), Collections.<SemanticRole>emptyList(), SituationIntensity.MEDIUM, 1, 0);
        return new Fixture(registry, provider.getProviderId(), plan, blueprint);
    }
    private static final class Fixture {
        final DirectorProviderRegistry registry; final ProviderId providerId; final CandidatePlan plan; final SituationBlueprint blueprint; final PlanExecutionSlice slice; final ControlledThreatPlanExecutionBridge bridge;
        Fixture(DirectorProviderRegistry registry, ProviderId providerId, CandidatePlan plan, SituationBlueprint blueprint) { this.registry=registry; this.providerId=providerId; this.plan=plan; this.blueprint=blueprint; this.slice=new PlanExecutionSlice(plan, blueprint, SemanticRole.THREAT); this.bridge=new ControlledThreatPlanExecutionBridge(registry); }
        PlanExecutionAuthorization authorization(boolean allowed) { return new PlanExecutionAuthorization("exec-1", slice.getParentPlanId(), slice.getPlanFingerprint(), SemanticRole.THREAT, CapabilityVocabulary.THREAT_SOURCE.getValue(), providerId, allowed); }
    }
    private static final class FakeProvider implements ThreatProvider {
        final ProviderId id = new ProviderId("fake-threat");
        public ProviderId getProviderId() { return id; } public String getModId() { return "fake"; } public boolean isAvailable() { return true; } public String getDetectedVersion() { return "test"; } public ProviderStatus getStatus() { return ProviderStatus.AVAILABLE_SUPPORTED; }
        public Map<String, ProviderCapabilityState> getCapabilities() { return Collections.singletonMap(CapabilityVocabulary.THREAT_SOURCE.getValue(), ProviderCapabilityState.MUTATION_VALIDATED); }
        public ThreatDefinition getThreatDefinition() { return new ThreatDefinition(id, "fake", "Fake Threat"); }
        public ThreatExecutionResult execute(ThreatExecutionRequest request, DirectorWorldSavedData saved) { return ThreatExecutionResult.of(ThreatExecutionResult.Status.CREATED, request.getRequestId(), null); }
        public ThreatExecutionResult cleanup(ThreatExecutionRequest request, DirectorWorldSavedData saved) { return ThreatExecutionResult.of(ThreatExecutionResult.Status.CLEANUP_EXECUTED, request.getRequestId(), null); }
    }
}
