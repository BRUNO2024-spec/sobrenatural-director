package com.sobrenaturaldirector;

import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sobrenaturaldirector.action.SemanticActionCatalog;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.capability.*;
import com.sobrenaturaldirector.content.model.*;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.situation.*;
import com.sobrenaturaldirector.control.DimensionRef;

/** Pure model closure tests; no world or provider mutation is performed. */
public final class SituationExecutionModelTest {
    private SituationContext context() { return new SituationContext(7, 40L, 2, 3, 99L, false, true, true, true, Collections.singleton("forest")); }
    @Test public void executableCompositionIsReadyAndDimensionBound() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.registerDescriptor(descriptor("structure", CapabilityVocabulary.STRUCTURE_SOURCE));
        registry.registerDescriptor(descriptor("actor", CapabilityVocabulary.ACTOR_SOURCE));
        SituationInstance instance = new SituationIntelligencePipeline().composeExecutable(context(), new SituationMemory(),
                registry, SemanticActionCatalog.standard(), "thread-opaque");
        assertEquals(SituationLifecycleState.READY, instance.getState());
        assertEquals(7, instance.getOriginDimension().getDimensionId());
        assertNotNull(instance.getActionPlan());
        assertEquals("thread-opaque", instance.getThreadId());
    }
    @Test public void terminalStatesCannotResume() {
        SituationBlueprint b = new SituationBlueprint("b", SituationGoal.AMBIENT_EVENT,
                Collections.singletonList(SemanticRole.POINT_OF_INTEREST), Collections.<SemanticRole>emptyList(), SituationIntensity.LOW, 1, 0);
        SituationInstance i = new SituationInstance("s", SituationGoal.AMBIENT_EVENT, b, "", new DimensionRef(1), new DimensionRef(1), "c", "p", "f",
                new com.sobrenaturaldirector.action.ActionComposer().compose("p", com.sobrenaturaldirector.action.ActionType.NO_ACTION, new DimensionRef(1), SemanticActionCatalog.standard()), 1L)
                .transition(SituationLifecycleState.READY, "ready", 1L, "TEST")
                .transition(SituationLifecycleState.EXECUTING, "run", 2L, "TEST")
                .transition(SituationLifecycleState.COMPLETED, "done", 3L, "TEST");
        try { i.transition(SituationLifecycleState.ACTIVE, "invalid", 4L, "TEST"); fail(); } catch (IllegalStateException expected) { }
    }
    @Test public void memoryRecordIsIdempotent() {
        SituationMemory m = new SituationMemory();
        SituationMemoryEntry e = new SituationMemoryEntry("same", SituationGoal.DISCOVERY, 1L, 2, 3, "fp", Collections.emptyList(), 1);
        m.record(e); m.record(e); assertEquals(1, m.snapshot().size());
    }
    private static ProviderDescriptor descriptor(String id, SemanticCapability capability) {
        ProviderId provider = new ProviderId(id);
        CapabilityDescriptor value = new CapabilityDescriptor(capability, provider, ProviderAvailability.RUNTIME_CONTROL, 80, 0);
        return new ProviderDescriptor(provider, id, EvidenceStatus.CONFIRMED, true, ProviderStatus.AVAILABLE, false,
                Collections.<String>emptySet(), "test", Collections.singleton(value), Collections.<CapabilityPolicyBinding>emptySet(), ProviderAvailability.RUNTIME_CONTROL);
    }
}
