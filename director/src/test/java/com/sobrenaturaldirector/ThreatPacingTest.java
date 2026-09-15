package com.sobrenaturaldirector;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import com.sobrenaturaldirector.capability.DecisionTrace;
import com.sobrenaturaldirector.capability.ProviderAvailability;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.EvidenceStatus;
import com.sobrenaturaldirector.content.model.ProviderDescriptor;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.situation.ConfirmedThreatOutcome;
import com.sobrenaturaldirector.situation.SemanticRole;
import com.sobrenaturaldirector.situation.SituationBlueprint;
import com.sobrenaturaldirector.situation.SituationBlueprintComposer;
import com.sobrenaturaldirector.situation.SituationContext;
import com.sobrenaturaldirector.situation.SituationGoal;
import com.sobrenaturaldirector.situation.SituationIntensity;
import com.sobrenaturaldirector.situation.SituationMemory;
import com.sobrenaturaldirector.situation.ThreatNarrativeAssessment;
import com.sobrenaturaldirector.situation.ThreatNarrativePolicy;
import com.sobrenaturaldirector.situation.ThreatOutcomeType;

public final class ThreatPacingTest {
    private static final SituationContext CONTEXT = new SituationContext(0, 10000, 4, 8, 77L, false, true, false, false, Collections.<String>emptySet());

    @Test public void noRecentThreatKeepsThreatEligible() {
        ThreatNarrativeAssessment assessment = policy().assess(optional(), CONTEXT, new SituationMemory(), null);
        assertTrue(assessment.isEligible()); assertEquals(0, assessment.getScoreModifier());
    }

    @Test public void recentConfirmedThreatCreatesHardCooldownForRequiredThreat() {
        SituationMemory memory = manifested(9000, 4, 8, SituationIntensity.HIGH);
        ThreatNarrativeAssessment assessment = policy().assess(required(), CONTEXT, memory, new DecisionTrace());
        assertFalse(assessment.isEligible()); assertTrue(assessment.isHardCooldown()); assertTrue(assessment.getReasons().toString().contains("THREAT_REQUIRED_INFEASIBLE_FOR_PACING"));
    }

    @Test public void recentConfirmedThreatSuppressesOptionalThreatWithoutInvalidatingBlueprint() {
        SituationMemory memory = manifested(9000, 4, 8, SituationIntensity.MEDIUM);
        ThreatNarrativeAssessment assessment = policy().assess(optional(), CONTEXT, memory, null);
        assertTrue(assessment.isEligible()); assertTrue(assessment.isOptionalSuppressed()); assertTrue(assessment.getReasons().toString().contains("THREAT_OPTIONAL_OMITTED_BY_PACING"));
        assertTrue(new SituationBlueprintComposer().compose(SituationGoal.DISCOVERY, CONTEXT, memory, registry(true), new DecisionTrace()).get(0).isFeasible());
    }

    @Test public void recoveryExpiresAndLongCalmRestoresReadiness() {
        SituationMemory memory = manifested(0, 4, 8, SituationIntensity.LOW);
        SituationContext recovered = context(13000, 4, 8);
        assertTrue(policy().assess(required(), recovered, memory, null).isEligible());
        SituationContext calm = context(24100, 4, 8);
        assertEquals(ThreatNarrativePolicy.LONG_CALM_BONUS, policy().assess(required(), calm, memory, null).getScoreModifier());
    }

    @Test public void multipleAndSameRegionThreatsAddBoundedPenalties() {
        SituationMemory memory = new SituationMemory();
        memory.recordConfirmedThreatOutcome(new ConfirmedThreatOutcome(9000, 4, 8, SituationIntensity.LOW, ThreatOutcomeType.MANIFESTED));
        memory.recordConfirmedThreatOutcome(new ConfirmedThreatOutcome(7000, 4, 8, SituationIntensity.LOW, ThreatOutcomeType.RESOLVED));
        ThreatNarrativeAssessment assessment = policy().assess(optional(), CONTEXT, memory, null);
        assertEquals(2, assessment.getRecentCount()); assertEquals(2, assessment.getSameRegionCount());
        assertTrue(assessment.getReasons().toString().contains("THREAT_REPETITION_PENALTY")); assertTrue(assessment.getReasons().toString().contains("THREAT_REGION_REPETITION"));
    }

    @Test public void failedOutcomeDoesNotCountAsExperiencedThreat() {
        SituationMemory memory = new SituationMemory();
        memory.recordConfirmedThreatOutcome(new ConfirmedThreatOutcome(9999, 4, 8, SituationIntensity.HIGH, ThreatOutcomeType.FAILED));
        ThreatNarrativeAssessment assessment = policy().assess(required(), CONTEXT, memory, null);
        assertTrue(assessment.isEligible()); assertEquals(0, assessment.getRecentCount());
    }

    @Test public void planOnlySelectionDoesNotCreateOutcomeHistory() {
        SituationMemory memory = new SituationMemory();
        assertTrue(new com.sobrenaturaldirector.situation.SituationIntelligencePipeline().evaluate(CONTEXT, memory, registry(true), new DecisionTrace()).isPlanOnly());
        assertTrue(memory.confirmedThreats().isEmpty());
        memory.recordConfirmedThreatOutcome(new ConfirmedThreatOutcome(9999, 4, 8, SituationIntensity.MEDIUM, ThreatOutcomeType.MANIFESTED));
        assertFalse(policy().assess(required(), CONTEXT, memory, null).isEligible());
    }

    @Test public void requiredSuppressionIsInfeasibleAndOptionalSuppressionIsTraceable() {
        SituationMemory memory = manifested(9999, 4, 8, SituationIntensity.HIGH);
        DecisionTrace trace = new DecisionTrace();
        java.util.List<com.sobrenaturaldirector.situation.BlueprintCandidate> values = new SituationBlueprintComposer().compose(SituationGoal.DISCOVERY, CONTEXT, memory, registry(true), trace);
        assertFalse(candidate(values, "discovery:threatened-landmark").isFeasible());
        assertTrue(trace.getEvents().toString().contains("pacing suppressed"));
    }

    @Test public void providerIdentityDoesNotChangePacingResult() {
        SituationMemory memory = manifested(9000, 1, 1, SituationIntensity.LOW);
        ThreatNarrativeAssessment a = policy().assess(optional(), CONTEXT, memory, null);
        ThreatNarrativeAssessment b = policy().assess(optional(), CONTEXT, memory, null);
        assertEquals(a.getScoreModifier(), b.getScoreModifier()); assertEquals(a.getReasons(), b.getReasons());
    }

    @Test public void historyIsBoundedAndEmptyHistoryIsSafe() {
        SituationMemory memory = new SituationMemory();
        for (int i = 0; i < SituationMemory.MAX_THREAT_OUTCOMES + 4; i++) memory.recordConfirmedThreatOutcome(new ConfirmedThreatOutcome(i, i, i, SituationIntensity.LOW, ThreatOutcomeType.MANIFESTED));
        assertEquals(SituationMemory.MAX_THREAT_OUTCOMES, memory.confirmedThreats().size());
        assertTrue(policy().assess(optional(), CONTEXT, new SituationMemory(), null).isEligible());
    }

    private static ThreatNarrativePolicy policy() { return new ThreatNarrativePolicy(); }
    private static SituationBlueprint optional() { return new SituationBlueprint("optional", SituationGoal.DISCOVERY, Collections.singletonList(SemanticRole.POINT_OF_INTEREST), Arrays.asList(SemanticRole.THREAT), SituationIntensity.MEDIUM, 1, 0); }
    private static SituationBlueprint required() { return new SituationBlueprint("required", SituationGoal.DISCOVERY, Arrays.asList(SemanticRole.POINT_OF_INTEREST, SemanticRole.THREAT), Collections.<SemanticRole>emptyList(), SituationIntensity.HIGH, 1, 0); }
    private static SituationMemory manifested(long tick, int x, int z, SituationIntensity intensity) { SituationMemory memory = new SituationMemory(); memory.recordConfirmedThreatOutcome(new ConfirmedThreatOutcome(tick, x, z, intensity, ThreatOutcomeType.MANIFESTED)); return memory; }
    private static SituationContext context(long tick, int x, int z) { return new SituationContext(0, tick, x, z, 77L, false, true, false, false, Collections.<String>emptySet()); }
    private static com.sobrenaturaldirector.situation.BlueprintCandidate candidate(java.util.List<com.sobrenaturaldirector.situation.BlueprintCandidate> values, String id) { for (com.sobrenaturaldirector.situation.BlueprintCandidate value : values) if (id.equals(value.getBlueprint().getId())) return value; throw new AssertionError(id); }
    private static DirectorProviderRegistry registry(boolean threat) { DirectorProviderRegistry registry = new DirectorProviderRegistry(); registry.registerDescriptor(descriptor("structure", CapabilityVocabulary.STRUCTURE_SOURCE)); if (threat) registry.registerDescriptor(descriptor("fake-threat", CapabilityVocabulary.THREAT_SOURCE)); return registry; }
    private static ProviderDescriptor descriptor(String id, com.sobrenaturaldirector.content.model.SemanticCapability capability) { ProviderId provider = new ProviderId(id); CapabilityDescriptor value = new CapabilityDescriptor(capability, provider, ProviderAvailability.RUNTIME_CONTROL, 1, 0); return new ProviderDescriptor(provider, id, EvidenceStatus.CONFIRMED, true, ProviderStatus.AVAILABLE, false, Collections.<String>emptySet(), "test", Collections.singleton(value), Collections.<CapabilityPolicyBinding>emptySet(), ProviderAvailability.RUNTIME_CONTROL); }
}
