package com.sobrenaturaldirector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sobrenaturaldirector.capability.CapabilityConstraint;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CapabilityPlanner;
import com.sobrenaturaldirector.capability.CapabilityQuery;
import com.sobrenaturaldirector.capability.DirectorContext;
import com.sobrenaturaldirector.capability.ProviderAvailability;
import com.sobrenaturaldirector.capability.SituationIntent;
import com.sobrenaturaldirector.capability.DecisionTrace;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.EvidenceStatus;
import com.sobrenaturaldirector.content.model.ProviderDescriptor;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.content.model.SemanticCapability;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.provider.gravestone.GraveStoneCapabilityProvider;

public class CapabilityFoundationTest {
    private static final SemanticCapability STRUCTURE = new SemanticCapability("director:structure_source");
    private static final SemanticCapability THREAT = new SemanticCapability("director:threat_source");
    private static final SemanticCapability REWARD = new SemanticCapability("director:reward_source");
    private static final DirectorContext CONTEXT = new DirectorContext(0, 42L, 0, 0, Collections.singleton("clear"));

    @Test public void emptyRegistryIsFailClosed() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        assertTrue(registry.query(new CapabilityQuery(STRUCTURE)).isEmpty());
        assertTrue(new CapabilityPlanner(registry).plan(new SituationIntent("empty", Collections.singletonList(STRUCTURE)), CONTEXT, null).isEmpty());
    }

    @Test public void availableAndUnavailableProvidersAreDistinguished() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.registerDescriptor(descriptor("available", ProviderStatus.AVAILABLE, STRUCTURE, ProviderAvailability.SAFE_CAPABILITY, 1));
        registry.registerDescriptor(descriptor("missing", ProviderStatus.MISSING, STRUCTURE, ProviderAvailability.COMPOSABLE, 9));
        assertEquals("available", registry.query(new CapabilityQuery(STRUCTURE)).get(0).getProvider().getValue());
    }

    @Test public void multipleProvidersAreRankedDeterministically() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.registerDescriptor(descriptor("low", ProviderStatus.AVAILABLE, STRUCTURE, ProviderAvailability.COMPOSABLE, 1));
        registry.registerDescriptor(descriptor("high", ProviderStatus.AVAILABLE, STRUCTURE, ProviderAvailability.COMPOSABLE, 5));
        List<CapabilityDescriptor> found = registry.query(new CapabilityQuery(STRUCTURE));
        assertEquals("high", found.get(0).getProvider().getValue());
        assertEquals("low", found.get(1).getProvider().getValue());
    }

    @Test public void removingAProviderRemovesItsCapabilities() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.registerDescriptor(descriptor("temporary", ProviderStatus.AVAILABLE, STRUCTURE, ProviderAvailability.COMPOSABLE, 1));
        assertEquals(1, registry.query(new CapabilityQuery(STRUCTURE)).size());
        assertTrue(registry.unregister(new ProviderId("temporary")));
        assertTrue(registry.query(new CapabilityQuery(STRUCTURE)).isEmpty());
    }

    @Test public void threeProvidersComposeWithoutProviderBranches() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.registerDescriptor(descriptor("provider-a", ProviderStatus.AVAILABLE, STRUCTURE, ProviderAvailability.COMPOSABLE, 1));
        registry.registerDescriptor(descriptor("provider-b", ProviderStatus.AVAILABLE, THREAT, ProviderAvailability.COMPOSABLE, 1));
        registry.registerDescriptor(descriptor("provider-c", ProviderStatus.AVAILABLE, REWARD, ProviderAvailability.COMPOSABLE, 1));
        SituationIntent intent = new SituationIntent("situation", Arrays.asList(STRUCTURE, THREAT, REWARD));
        List<com.sobrenaturaldirector.capability.CandidatePlan> plans = new CapabilityPlanner(registry).plan(intent, CONTEXT, null);
        assertEquals(1, plans.size());
        assertEquals(Arrays.asList("provider-a", "provider-b", "provider-c"), names(plans.get(0).getProviders()));
        assertTrue(plans.get(0).isComplete());
    }

    @Test public void missingCapabilityAndRejectedConstraintProduceNoPlan() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.registerDescriptor(descriptor("provider-a", ProviderStatus.AVAILABLE, STRUCTURE, ProviderAvailability.COMPOSABLE, 1));
        SituationIntent impossible = new SituationIntent("impossible", Arrays.asList(STRUCTURE, THREAT));
        assertTrue(new CapabilityPlanner(registry).plan(impossible, CONTEXT, null).isEmpty());
        SituationIntent constrained = new SituationIntent("constrained", Collections.singletonList(STRUCTURE));
        CapabilityConstraint reject = new CapabilityConstraint() { public boolean accepts(SituationIntent i, DirectorContext c, List<CapabilityDescriptor> selected) { return false; } };
        assertTrue(new CapabilityPlanner(registry).plan(constrained, CONTEXT, Collections.singletonList(reject)).isEmpty());
    }

    @Test public void gravestoneRegistersOnlyValidatedCapabilitiesAndFailsClosedWhenAbsent() {
        GraveStoneCapabilityProvider provider = new GraveStoneCapabilityProvider(false);
        assertEquals(ProviderStatus.DISABLED, provider.getStatus());
        assertEquals(3, provider.getCapabilities().size());
        assertEquals(3, provider.getPolicyBindings().size());
        DirectorProviderRegistry registry = new DirectorProviderRegistry(); registry.register(provider);
        assertTrue(registry.query(new CapabilityQuery(new SemanticCapability("director:block_mutation"))).isEmpty());
    }

    @Test public void realProviderCapabilitiesComposeAndStalePlanIsRejected() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.registerDescriptor(descriptor("gravestone", ProviderStatus.AVAILABLE, CapabilityVocabulary.STRUCTURE_SOURCE, ProviderAvailability.RUNTIME_CONTROL, 80));
        registry.registerDescriptor(descriptor("customnpcs", ProviderStatus.AVAILABLE, CapabilityVocabulary.ACTOR_SOURCE, ProviderAvailability.RUNTIME_CONTROL, 90));
        DecisionTrace trace = new DecisionTrace();
        SituationIntent intent = new SituationIntent("structure-and-actor", Arrays.asList(CapabilityVocabulary.STRUCTURE_SOURCE, CapabilityVocabulary.ACTOR_SOURCE));
        com.sobrenaturaldirector.capability.CandidatePlan plan = new CapabilityPlanner(registry).plan(intent, CONTEXT, null, trace).get(0);
        assertEquals(Arrays.asList("customnpcs", "gravestone"), names(plan.getProviders()));
        assertTrue(trace.getEvents().toString().contains("selected director:structure_source gravestone"));
        registry.unregister(new ProviderId("gravestone"));
        assertTrue(new CapabilityPlanner(registry).plan(intent, CONTEXT, null).isEmpty());
        assertFalse(new CapabilityPlanner(registry).isCurrent(plan));
        registry.registerDescriptor(descriptor("gravestone", ProviderStatus.AVAILABLE, CapabilityVocabulary.STRUCTURE_SOURCE, ProviderAvailability.RUNTIME_CONTROL, 80));
        assertFalse(new CapabilityPlanner(registry).plan(intent, CONTEXT, null).isEmpty());
    }

    @Test public void abstractStructureCanBeReplacedByFakeProvider() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.registerDescriptor(descriptor("fake-structure", ProviderStatus.AVAILABLE, CapabilityVocabulary.STRUCTURE_SOURCE, ProviderAvailability.COMPOSABLE, 70));
        registry.registerDescriptor(descriptor("customnpcs", ProviderStatus.AVAILABLE, CapabilityVocabulary.ACTOR_SOURCE, ProviderAvailability.COMPOSABLE, 90));
        SituationIntent intent = new SituationIntent("replacement", Arrays.asList(CapabilityVocabulary.STRUCTURE_SOURCE, CapabilityVocabulary.ACTOR_SOURCE));
        assertEquals("fake-structure", new CapabilityPlanner(registry).plan(intent, CONTEXT, null).get(0).getSelections().get(CapabilityVocabulary.STRUCTURE_SOURCE).getProvider().getValue());
    }

    @Test public void abstractActorCanBeReplacedByFakeProvider() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.registerDescriptor(descriptor("gravestone", ProviderStatus.AVAILABLE, CapabilityVocabulary.STRUCTURE_SOURCE, ProviderAvailability.COMPOSABLE, 80));
        registry.registerDescriptor(descriptor("fake-actor", ProviderStatus.AVAILABLE, CapabilityVocabulary.ACTOR_SOURCE, ProviderAvailability.COMPOSABLE, 70));
        SituationIntent intent = new SituationIntent("actor-replacement", Arrays.asList(CapabilityVocabulary.STRUCTURE_SOURCE, CapabilityVocabulary.ACTOR_SOURCE));
        assertEquals("fake-actor", new CapabilityPlanner(registry).plan(intent, CONTEXT, null).get(0).getSelections().get(CapabilityVocabulary.ACTOR_SOURCE).getProvider().getValue());
    }

    @Test public void plannerProducesDataOnlyAndCannotBypassExecutionBoundary() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.registerDescriptor(descriptor("provider-a", ProviderStatus.AVAILABLE, STRUCTURE, ProviderAvailability.RUNTIME_CONTROL, 1));
        com.sobrenaturaldirector.capability.CandidatePlan plan = new CapabilityPlanner(registry).plan(new SituationIntent("plan-only", Collections.singletonList(STRUCTURE)), CONTEXT, null).get(0);
        assertTrue(plan.isComplete());
        assertEquals(0, plan.getSatisfiedConstraints().size());
    }

    private static ProviderDescriptor descriptor(String id, ProviderStatus status, SemanticCapability capability, ProviderAvailability availability, int priority) {
        ProviderId provider = new ProviderId(id);
        CapabilityDescriptor value = new CapabilityDescriptor(capability, provider, availability, priority, 0);
        return new ProviderDescriptor(provider, id, EvidenceStatus.CONFIRMED, true, status, false, Collections.<String>emptySet(), "1.0", Collections.singleton(value), Collections.<com.sobrenaturaldirector.capability.CapabilityPolicyBinding>emptySet(), availability);
    }
    private static List<String> names(List<ProviderId> ids) { java.util.ArrayList<String> result = new java.util.ArrayList<String>(); for (ProviderId id : ids) result.add(id.getValue()); return result; }
}
