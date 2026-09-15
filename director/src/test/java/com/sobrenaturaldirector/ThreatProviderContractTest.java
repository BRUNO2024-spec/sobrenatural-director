package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import org.junit.Test;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.provider.DirectorContentProvider;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.provider.ProviderCapabilityState;
import com.sobrenaturaldirector.provider.slenderman.SlenderManThreatProvider;
import com.sobrenaturaldirector.threat.ThreatDefinition;
import com.sobrenaturaldirector.threat.ThreatExecutionRequest;
import com.sobrenaturaldirector.threat.ThreatExecutionResult;
import com.sobrenaturaldirector.threat.ThreatJournalEntry;
import com.sobrenaturaldirector.threat.ThreatLifecycleState;
import com.sobrenaturaldirector.threat.ThreatProvider;

public final class ThreatProviderContractTest {
    @Test public void capabilityIsProviderNeutralAndReplaceable() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        FakeThreatProvider fake = new FakeThreatProvider();
        registry.register(fake);
        assertEquals(1, registry.query(new com.sobrenaturaldirector.capability.CapabilityQuery(CapabilityVocabulary.THREAT_SOURCE, com.sobrenaturaldirector.capability.ProviderAvailability.DISCOVERED)).size());
        assertEquals("fake-threat", registry.query(new com.sobrenaturaldirector.capability.CapabilityQuery(CapabilityVocabulary.THREAT_SOURCE, com.sobrenaturaldirector.capability.ProviderAvailability.DISCOVERED)).get(0).getProvider().getValue());
        assertTrue(registry.unregister(fake.getProviderId()));
        assertTrue(registry.query(new com.sobrenaturaldirector.capability.CapabilityQuery(CapabilityVocabulary.THREAT_SOURCE, com.sobrenaturaldirector.capability.ProviderAvailability.DISCOVERED)).isEmpty());
    }

    @Test public void journalRoundTripIsGenericAndStable() {
        ThreatJournalEntry source = new ThreatJournalEntry("request", "instance", new ProviderId("fake-threat"), "definition", UUID.randomUUID(), "DIRECTOR_THREAT", ThreatLifecycleState.ACTIVE, 0, 1, 2, 3, 4L, 0L);
        assertEquals(source.getThreatInstanceId(), ThreatJournalEntry.fromNbt(source.toNbt()).getThreatInstanceId());
        assertEquals(source.getEntityUuid(), ThreatJournalEntry.fromNbt(source.toNbt()).getEntityUuid());
    }

    @Test public void slendermanVersionGateIsExact() {
        assertTrue(SlenderManThreatProvider.isSupportedVersion("3.3_1.7.10"));
        assertTrue(SlenderManThreatProvider.isSupportedVersion("v3.3 1.7.10"));
        assertFalse(SlenderManThreatProvider.isSupportedVersion("3.3"));
        assertFalse(SlenderManThreatProvider.isSupportedVersion("3.4_1.7.10"));
    }

    @Test public void threeCapabilityPlanRemainsPureAndProviderNeutral() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.registerDescriptor(descriptor("gravestone", CapabilityVocabulary.STRUCTURE_SOURCE));
        registry.registerDescriptor(descriptor("customnpcs", CapabilityVocabulary.ACTOR_SOURCE));
        registry.registerDescriptor(descriptor("slenderman", CapabilityVocabulary.THREAT_SOURCE));
        com.sobrenaturaldirector.capability.SituationIntent intent = new com.sobrenaturaldirector.capability.SituationIntent("manual-three-source", Arrays.asList(CapabilityVocabulary.STRUCTURE_SOURCE, CapabilityVocabulary.ACTOR_SOURCE, CapabilityVocabulary.THREAT_SOURCE));
        java.util.List<com.sobrenaturaldirector.capability.CandidatePlan> plans = new com.sobrenaturaldirector.capability.CapabilityPlanner(registry).plan(intent, new com.sobrenaturaldirector.capability.DirectorContext(0, 1L, 0, 0, Collections.<String>emptySet()), null);
        assertEquals(1, plans.size());
        assertEquals("slenderman", plans.get(0).getSelections().get(CapabilityVocabulary.THREAT_SOURCE).getProvider().getValue());
    }

    private static com.sobrenaturaldirector.content.model.ProviderDescriptor descriptor(String id, com.sobrenaturaldirector.content.model.SemanticCapability capability) {
        return new com.sobrenaturaldirector.content.model.ProviderDescriptor(new ProviderId(id), id, com.sobrenaturaldirector.content.model.EvidenceStatus.CONFIRMED, true, ProviderStatus.AVAILABLE_SUPPORTED, false, Collections.<String>emptySet(), "test", Collections.singleton(new com.sobrenaturaldirector.capability.CapabilityDescriptor(capability, new ProviderId(id), com.sobrenaturaldirector.capability.ProviderAvailability.COMPOSABLE, 1, 0)), Collections.<com.sobrenaturaldirector.capability.CapabilityPolicyBinding>emptySet(), com.sobrenaturaldirector.capability.ProviderAvailability.COMPOSABLE);
    }

    private static final class FakeThreatProvider implements ThreatProvider {
        private final ProviderId id = new ProviderId("fake-threat");
        public ProviderId getProviderId() { return id; }
        public String getModId() { return "fake"; }
        public boolean isAvailable() { return true; }
        public String getDetectedVersion() { return "test"; }
        public ProviderStatus getStatus() { return ProviderStatus.AVAILABLE_SUPPORTED; }
        public Map<String, ProviderCapabilityState> getCapabilities() { return Collections.singletonMap(CapabilityVocabulary.THREAT_SOURCE.getValue(), ProviderCapabilityState.STATIC_KNOWN); }
        public ThreatDefinition getThreatDefinition() { return new ThreatDefinition(id, "definition", "Fake Threat"); }
        public ThreatExecutionResult execute(ThreatExecutionRequest request, com.sobrenaturaldirector.persistence.DirectorWorldSavedData saved) { return ThreatExecutionResult.of(ThreatExecutionResult.Status.CREATED, request.getRequestId(), null); }
        public ThreatExecutionResult cleanup(ThreatExecutionRequest request, com.sobrenaturaldirector.persistence.DirectorWorldSavedData saved) { return ThreatExecutionResult.of(ThreatExecutionResult.Status.CLEANUP_EXECUTED, request.getRequestId(), null); }
    }
}
