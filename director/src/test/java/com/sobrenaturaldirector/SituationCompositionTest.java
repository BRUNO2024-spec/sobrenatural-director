package com.sobrenaturaldirector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import com.sobrenaturaldirector.situation.BlueprintCandidate;
import com.sobrenaturaldirector.situation.RoleCapabilityResolver;
import com.sobrenaturaldirector.situation.SemanticRole;
import com.sobrenaturaldirector.situation.SituationBlueprint;
import com.sobrenaturaldirector.situation.SituationBlueprintComposer;
import com.sobrenaturaldirector.situation.SituationContext;
import com.sobrenaturaldirector.situation.SituationDecision;
import com.sobrenaturaldirector.situation.SituationGoal;
import com.sobrenaturaldirector.situation.SituationIntensity;
import com.sobrenaturaldirector.situation.SituationIntelligencePipeline;
import com.sobrenaturaldirector.situation.SituationMemory;
import com.sobrenaturaldirector.situation.SituationMemoryEntry;

public final class SituationCompositionTest {
    private static final SituationContext CONTEXT = new SituationContext(0, 100, 4, 8, 77L, true, true, true, true, Collections.singleton("forest"));

    @Test public void oneGoalProducesMultipleBlueprints() {
        List<BlueprintCandidate> candidates = new SituationBlueprintComposer().compose(SituationGoal.INVESTIGATION, CONTEXT, new SituationMemory(), registry(true, true), new DecisionTrace());
        assertEquals(2, candidates.size());
        assertNotEquals(candidates.get(0).getBlueprint().getId(), candidates.get(1).getBlueprint().getId());
        assertTrue(candidates.get(0).getScore() > candidates.get(1).getScore());
    }

    @Test public void blueprintDecisionIsDeterministic() {
        DirectorProviderRegistry registry = registry(true, true);
        SituationIntelligencePipeline pipeline = new SituationIntelligencePipeline();
        SituationDecision a = pipeline.evaluate(CONTEXT, new SituationMemory(), registry, new DecisionTrace());
        SituationDecision b = pipeline.evaluate(CONTEXT, new SituationMemory(), registry, new DecisionTrace());
        assertEquals(a.getSelectedBlueprint().getBlueprint().getId(), b.getSelectedBlueprint().getBlueprint().getId());
        assertEquals(a.getPlan().signature(), b.getPlan().signature());
    }

    @Test public void repeatedBlueprintLosesToAlternativeWithoutChangingGoal() {
        DirectorProviderRegistry registry = registry(true, true);
        SituationBlueprint first = new SituationBlueprintComposer().compose(SituationGoal.INVESTIGATION, CONTEXT, new SituationMemory(), registry, null).get(0).getBlueprint();
        SituationMemory memory = new SituationMemory();
        memory.record(new SituationMemoryEntry("blueprint-a", SituationGoal.INVESTIGATION, 90, 4, 8, first.fingerprint(), Arrays.asList(new ProviderId("structure"), new ProviderId("actor")), 103));
        SituationDecision decision = new SituationIntelligencePipeline().evaluate(CONTEXT, memory, registry, new DecisionTrace());
        assertEquals(SituationGoal.INVESTIGATION, decision.getSelected().getGoal());
        assertNotEquals(first.getId(), decision.getSelectedBlueprint().getBlueprint().getId());
        assertTrue(decision.getBlueprints().get(1).getReasons().toString().contains("similarityPenalty=-20"));
    }

    @Test public void missingRequiredCapabilityRejectsBlueprint() {
        DirectorProviderRegistry registry = registry(true, false);
        List<BlueprintCandidate> candidates = new SituationBlueprintComposer().compose(SituationGoal.INVESTIGATION, CONTEXT, new SituationMemory(), registry, new DecisionTrace());
        assertFalse(candidates.get(0).isFeasible());
        assertTrue(candidates.get(0).getReasons().toString().contains("missing required capability director:actor_source"));
        assertEquals(SituationGoal.DISCOVERY, new SituationIntelligencePipeline().evaluate(CONTEXT, new SituationMemory(), registry).getSelected().getGoal());
    }

    @Test public void optionalActorDoesNotInvalidateDiscovery() {
        DirectorProviderRegistry registry = registry(true, false);
        DecisionTrace trace = new DecisionTrace();
        SituationDecision decision = new SituationIntelligencePipeline().evaluate(new SituationContext(0, 100, 4, 8, 77L, false, true, true, false, Collections.<String>emptySet()), new SituationMemory(), registry, trace);
        assertEquals(SituationGoal.DISCOVERY, decision.getSelected().getGoal());
        assertEquals(1, decision.getPlan().getSelections().size());
        assertTrue(trace.getEvents().toString().contains("optional element skipped"));
    }

    @Test public void sameBlueprintResolvesWithFakeProviders() {
        DirectorProviderRegistry registry = registry(false, false);
        registry.registerDescriptor(descriptor("fake-structure", CapabilityVocabulary.STRUCTURE_SOURCE, 80));
        registry.registerDescriptor(descriptor("fake-actor", CapabilityVocabulary.ACTOR_SOURCE, 90));
        SituationDecision decision = new SituationIntelligencePipeline().evaluate(CONTEXT, new SituationMemory(), registry);
        assertEquals("fake-structure", decision.getPlan().getSelections().get(CapabilityVocabulary.STRUCTURE_SOURCE).getProvider().getValue());
        assertEquals("fake-actor", decision.getPlan().getSelections().get(CapabilityVocabulary.ACTOR_SOURCE).getProvider().getValue());
        assertFalse(decision.getSelectedBlueprint().getBlueprint().getRequiredRoles().toString().contains("fake"));
    }

    @Test public void rolesDoNotEncodeProviders() {
        assertEquals(CapabilityVocabulary.STRUCTURE_SOURCE, new RoleCapabilityResolver().required(Collections.singletonList(SemanticRole.POINT_OF_INTEREST)).get(0));
        assertEquals(CapabilityVocabulary.ACTOR_SOURCE, new RoleCapabilityResolver().required(Collections.singletonList(SemanticRole.WITNESS)).get(0));
        assertFalse(new SituationBlueprint("check", SituationGoal.INVESTIGATION, Collections.singletonList(SemanticRole.WITNESS), Collections.<SemanticRole>emptyList(), SituationIntensity.LOW, 1, 0).fingerprint().contains("GraveStone"));
    }

    @Test public void threatRoleResolvesToGenericThreatSource() {
        assertEquals(CapabilityVocabulary.THREAT_SOURCE, new RoleCapabilityResolver().required(Collections.singletonList(SemanticRole.THREAT)).get(0));
    }

    @Test public void requiredThreatIsFeasibleOnlyWhenProviderExists() {
        List<BlueprintCandidate> absent = new SituationBlueprintComposer().compose(SituationGoal.DISCOVERY, CONTEXT, new SituationMemory(), registry(true, false), null);
        assertFalse(candidate(absent, "discovery:threatened-landmark").isFeasible());
        List<BlueprintCandidate> present = new SituationBlueprintComposer().compose(SituationGoal.DISCOVERY, CONTEXT, new SituationMemory(), registry(true, true, true), null);
        assertTrue(candidate(present, "discovery:threatened-landmark").isFeasible());
    }

    @Test public void optionalThreatIsIncludedWhenAvailableAndOmittedWhenAbsent() {
        SituationContext discovery = new SituationContext(0, 100, 4, 8, 77L, false, true, false, false, Collections.<String>emptySet());
        SituationDecision present = new SituationIntelligencePipeline().evaluate(discovery, discoveryMemory(), registry(true, true, true), new DecisionTrace());
        assertEquals(SituationGoal.DISCOVERY, present.getSelected().getGoal());
        assertTrue(present.getRequirements().contains(CapabilityVocabulary.THREAT_SOURCE));
        DecisionTrace absentTrace = new DecisionTrace();
        SituationDecision absent = new SituationIntelligencePipeline().evaluate(discovery, discoveryMemory(), registry(true, true), absentTrace);
        assertEquals(SituationGoal.DISCOVERY, absent.getSelected().getGoal());
        assertFalse(absent.getRequirements().contains(CapabilityVocabulary.THREAT_SOURCE));
        assertTrue(absentTrace.getEvents().toString().contains("THREAT"));
    }

    @Test public void threatFingerprintIsPenalizedWithoutChangingProviderIdentity() {
        DirectorProviderRegistry registry = registry(true, true, true);
        List<BlueprintCandidate> initial = new SituationBlueprintComposer().compose(SituationGoal.DISCOVERY, CONTEXT, new SituationMemory(), registry, null);
        SituationBlueprint threat = candidate(initial, "discovery:landmark").getBlueprint();
        SituationMemory memory = new SituationMemory();
        memory.record(new SituationMemoryEntry("threat", SituationGoal.DISCOVERY, 90, 4, 8, threat.fingerprint(), Collections.singletonList(new ProviderId("slenderman")), 90));
        BlueprintCandidate repeated = candidate(new SituationBlueprintComposer().compose(SituationGoal.DISCOVERY, CONTEXT, memory, registry, null), threat.getId());
        assertTrue(repeated.getReasons().toString().contains("similarityPenalty=-20"));
    }

    @Test public void planOnlyThreatResolutionDoesNotExecute() {
        SituationContext discovery = new SituationContext(0, 100, 4, 8, 77L, false, true, false, false, Collections.<String>emptySet());
        SituationDecision decision = new SituationIntelligencePipeline().evaluate(discovery, discoveryMemory(), registry(true, true, true));
        assertTrue(decision.isPlanOnly());
        assertEquals("slenderman", decision.getPlan().getSelections().get(CapabilityVocabulary.THREAT_SOURCE).getProvider().getValue());
    }

    private static BlueprintCandidate candidate(List<BlueprintCandidate> values, String id) { for (BlueprintCandidate value : values) if (id.equals(value.getBlueprint().getId())) return value; throw new AssertionError("missing blueprint " + id); }
    private static SituationMemory discoveryMemory() { SituationMemory memory = new SituationMemory(); memory.record(new SituationMemoryEntry("investigation-a", SituationGoal.INVESTIGATION, 90, 4, 8, "old-a", Collections.<ProviderId>emptyList(), 0)); memory.record(new SituationMemoryEntry("investigation-b", SituationGoal.INVESTIGATION, 91, 4, 8, "old-b", Collections.<ProviderId>emptyList(), 0)); return memory; }
    private static DirectorProviderRegistry registry(boolean structure, boolean actor) { return registry(structure, actor, false); }
    private static DirectorProviderRegistry registry(boolean structure, boolean actor, boolean threat) { DirectorProviderRegistry registry = new DirectorProviderRegistry(); if (structure) registry.registerDescriptor(descriptor("structure", CapabilityVocabulary.STRUCTURE_SOURCE, 80)); if (actor) registry.registerDescriptor(descriptor("actor", CapabilityVocabulary.ACTOR_SOURCE, 90)); if (threat) registry.registerDescriptor(descriptor("slenderman", CapabilityVocabulary.THREAT_SOURCE, 70)); return registry; }
    private static ProviderDescriptor descriptor(String id, com.sobrenaturaldirector.content.model.SemanticCapability capability, int priority) { ProviderId provider = new ProviderId(id); CapabilityDescriptor value = new CapabilityDescriptor(capability, provider, ProviderAvailability.RUNTIME_CONTROL, priority, 0); return new ProviderDescriptor(provider, id, EvidenceStatus.CONFIRMED, true, ProviderStatus.AVAILABLE, false, Collections.<String>emptySet(), "test", Collections.singleton(value), Collections.<CapabilityPolicyBinding>emptySet(), ProviderAvailability.RUNTIME_CONTROL); }
}
