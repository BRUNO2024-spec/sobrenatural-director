package com.sobrenaturaldirector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CapabilityPlanner;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import com.sobrenaturaldirector.capability.DecisionTrace;
import com.sobrenaturaldirector.capability.ProviderAvailability;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.EvidenceStatus;
import com.sobrenaturaldirector.content.model.ProviderDescriptor;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.situation.SituationContext;
import com.sobrenaturaldirector.situation.SituationGoal;
import com.sobrenaturaldirector.situation.SituationGoalCandidate;
import com.sobrenaturaldirector.situation.SituationIntelligencePipeline;
import com.sobrenaturaldirector.situation.SituationMemory;
import com.sobrenaturaldirector.situation.SituationMemoryEntry;
import com.sobrenaturaldirector.situation.SituationDecision;

public final class SituationIntelligenceTest {
    private static final SituationContext CONTEXT = new SituationContext(0, 100, 4, 8, 77L, true, true, true, true, Collections.singleton("forest"));

    @Test public void sameContextStateAndSeedAreDeterministic() {
        DirectorProviderRegistry registry = registry(true, true);
        SituationIntelligencePipeline pipeline = new SituationIntelligencePipeline();
        DecisionTrace firstTrace = new DecisionTrace();
        DecisionTrace secondTrace = new DecisionTrace();
        assertEquals(pipeline.evaluate(CONTEXT, new SituationMemory(), registry, firstTrace).getSelected().getGoal(), pipeline.evaluate(CONTEXT, new SituationMemory(), registry, secondTrace).getSelected().getGoal());
        assertEquals(firstTrace.getEvents(), secondTrace.getEvents());
    }

    @Test public void investigationDecomposesAndUsesRealProviderIdsOnlyInPlanner() {
        DirectorProviderRegistry registry = registry(true, true);
        DecisionTrace trace = new DecisionTrace();
        com.sobrenaturaldirector.situation.SituationDecision decision = new SituationIntelligencePipeline().evaluate(CONTEXT, new SituationMemory(), registry, trace);
        assertEquals(SituationGoal.INVESTIGATION, decision.getSelected().getGoal());
        assertEquals(Arrays.asList(CapabilityVocabulary.STRUCTURE_SOURCE, CapabilityVocabulary.ACTOR_SOURCE), decision.getRequirements());
        assertEquals(Arrays.asList(new ProviderId("customnpcs"), new ProviderId("gravestone")), decision.getPlan().getProviders());
        assertTrue(trace.getEvents().toString().contains("requirements INVESTIGATION"));
    }

    @Test public void memoryPenaltyChangesRankingPredictably() {
        DirectorProviderRegistry registry = registry(true, true);
        SituationIntelligencePipeline pipeline = new SituationIntelligencePipeline();
        SituationMemory memory = new SituationMemory();
        assertEquals(SituationGoal.INVESTIGATION, pipeline.evaluate(CONTEXT, memory, registry).getSelected().getGoal());
        memory.record(entry("one", 90)); memory.record(entry("two", 91));
        DecisionTrace trace = new DecisionTrace();
        List<SituationGoalCandidate> candidates = pipeline.evaluate(CONTEXT, memory, registry, trace).getCandidates();
        assertEquals(SituationGoal.DISCOVERY, candidates.get(0).getGoal());
        assertTrue(trace.getEvents().toString().contains("recentPenalty=-30"));
    }

    @Test public void missingActorMakesInvestigationUnsatisfied() {
        DirectorProviderRegistry registry = registry(true, false);
        SituationDecision decision = new SituationIntelligencePipeline().evaluate(CONTEXT, new SituationMemory(), registry);
        SituationGoalCandidate investigation = decision.getCandidates().get(0).getGoal() == SituationGoal.INVESTIGATION ? decision.getCandidates().get(0) : decision.getCandidates().get(1);
        assertFalse(investigation.isSatisfiable());
        assertNotEquals(SituationGoal.INVESTIGATION, decision.getSelected().getGoal());
        assertNull(decision.getPlan() == null ? null : decision.getPlan().getSelections().get(CapabilityVocabulary.ACTOR_SOURCE));
    }

    @Test public void fakeStructureReplacementNeedsNoSituationChanges() {
        DirectorProviderRegistry registry = registry(false, true);
        registry.registerDescriptor(descriptor("fake-structure", CapabilityVocabulary.STRUCTURE_SOURCE, 70));
        SituationDecision decision = new SituationIntelligencePipeline().evaluate(CONTEXT, new SituationMemory(), registry);
        assertEquals(SituationGoal.INVESTIGATION, decision.getSelected().getGoal());
        assertEquals("fake-structure", decision.getPlan().getSelections().get(CapabilityVocabulary.STRUCTURE_SOURCE).getProvider().getValue());
    }

    @Test public void memoryIsBounded() {
        SituationMemory memory = new SituationMemory();
        for (int i = 0; i < SituationMemory.MAX_ENTRIES + 5; i++) memory.record(entry("x" + i, i));
        assertEquals(SituationMemory.MAX_ENTRIES, memory.snapshot().size());
    }

    private static SituationMemoryEntry entry(String id, long tick) { return new SituationMemoryEntry(id, SituationGoal.INVESTIGATION, tick, 4, 8, "director:structure_source+director:actor_source", Arrays.asList(new ProviderId("customnpcs"), new ProviderId("gravestone")), 108); }
    private static DirectorProviderRegistry registry(boolean structure, boolean actor) { DirectorProviderRegistry registry = new DirectorProviderRegistry(); if (structure) registry.registerDescriptor(descriptor("gravestone", CapabilityVocabulary.STRUCTURE_SOURCE, 80)); if (actor) registry.registerDescriptor(descriptor("customnpcs", CapabilityVocabulary.ACTOR_SOURCE, 90)); return registry; }
    private static ProviderDescriptor descriptor(String id, com.sobrenaturaldirector.content.model.SemanticCapability capability, int priority) { ProviderId provider = new ProviderId(id); CapabilityDescriptor value = new CapabilityDescriptor(capability, provider, ProviderAvailability.RUNTIME_CONTROL, priority, 0); return new ProviderDescriptor(provider, id, EvidenceStatus.CONFIRMED, true, ProviderStatus.AVAILABLE, false, Collections.<String>emptySet(), "test", Collections.singleton(value), Collections.<CapabilityPolicyBinding>emptySet(), ProviderAvailability.RUNTIME_CONTROL); }
}
