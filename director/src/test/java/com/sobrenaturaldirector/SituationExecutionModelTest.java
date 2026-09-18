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
import com.sobrenaturaldirector.action.ActionPlan;
import com.sobrenaturaldirector.action.ActionComposer;
import com.sobrenaturaldirector.action.ActionType;
import com.sobrenaturaldirector.narrative.*;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import net.minecraft.nbt.NBTTagCompound;

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
    @Test public void persistedSituationRoundTripKeepsIdentityAndPlanFingerprint() {
        ActionPlan plan = new ActionComposer().compose("persist-plan", ActionType.NO_ACTION,
                new DimensionRef(3, "fixture", Collections.singleton("nether"), true), SemanticActionCatalog.standard());
        SituationBlueprint b = new SituationBlueprint("ambient", SituationGoal.AMBIENT_EVENT,
                Collections.<SemanticRole>emptyList(), Collections.<SemanticRole>emptyList(), SituationIntensity.LOW, 1, 0);
        SituationInstance original = new SituationInstance("situation-1", SituationGoal.AMBIENT_EVENT, b, "thread-1",
                new DimensionRef(3, "fixture", Collections.singleton("nether"), true), plan.getDimension(), "ctx", "providers", "content", plan, 10L)
                .transition(SituationLifecycleState.READY, "ready", 11L, "TEST")
                .transition(SituationLifecycleState.EXECUTING, "execute", 12L, "TEST")
                .transition(SituationLifecycleState.ACTIVE, "setup", 13L, "ACTION_OUTCOME");
        SituationInstance restored = PersistentSituation.fromNbt(new PersistentSituation(original).toNbt()).getInstance();
        assertEquals(original.getId(), restored.getId()); assertEquals(original.getThreadId(), restored.getThreadId());
        assertEquals(original.getState(), restored.getState()); assertEquals(original.getActionPlan().getFingerprint(), restored.getActionPlan().getFingerprint());
        assertEquals(original.getRelevantDimension().getTags(), restored.getRelevantDimension().getTags());
    }
    @Test public void threadSituationMembershipIsIdempotent() {
        PersistentNarrativeThread thread = new PersistentNarrativeThread("thread-x", "subject", NarrativeThreadState.ACTIVE, 1L, 1L, 1L, 3, 1, 1,
                Collections.<String>emptySet(), "", Collections.<NarrativeThreadPlanHistory>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList());
        PersistentNarrativeThread linked = thread.linkSituation("situation-1", 2L, "LINK").linkSituation("situation-1", 3L, "LINK");
        assertEquals(1, linked.getSituationIds().size()); assertEquals("situation-1", linked.getSituationIds().get(0));
    }
    @Test public void worldSavedDataRoundTripKeepsSituationRecord() {
        ActionPlan plan = new ActionComposer().compose("world-plan", ActionType.NO_ACTION, new DimensionRef(4), SemanticActionCatalog.standard());
        SituationBlueprint b = new SituationBlueprint("world", SituationGoal.AMBIENT_EVENT, Collections.<SemanticRole>emptyList(), Collections.<SemanticRole>emptyList(), SituationIntensity.LOW, 1, 0);
        SituationInstance value = new SituationInstance("world-situation", SituationGoal.AMBIENT_EVENT, b, "", new DimensionRef(4), new DimensionRef(4), "c", "p", "f", plan, 1L);
        DirectorWorldSavedData before = new DirectorWorldSavedData(); before.recordSituation(new PersistentSituation(value));
        NBTTagCompound nbt = new NBTTagCompound(); before.writeToNBT(nbt);
        DirectorWorldSavedData after = new DirectorWorldSavedData(); after.readFromNBT(nbt);
        assertEquals(1, after.getSituations().size()); assertEquals("world-situation", after.getSituation("world-situation").getId());
    }
    @Test public void interruptedExecutionIsSuspendedWithoutReplay() {
        ActionPlan plan = new ActionComposer().compose("restart-plan", ActionType.NO_ACTION, new DimensionRef(5), SemanticActionCatalog.standard());
        SituationBlueprint b = new SituationBlueprint("restart", SituationGoal.AMBIENT_EVENT, Collections.<SemanticRole>emptyList(), Collections.<SemanticRole>emptyList(), SituationIntensity.LOW, 1, 0);
        SituationInstance executing = new SituationInstance("restart-situation", SituationGoal.AMBIENT_EVENT, b, "", new DimensionRef(5), new DimensionRef(5), "c", "p", "f", plan, 1L)
                .transition(SituationLifecycleState.READY, "ready", 1L, "TEST").transition(SituationLifecycleState.EXECUTING, "started", 2L, "TEST");
        DirectorWorldSavedData data = new DirectorWorldSavedData(); new SituationPersistenceCoordinator().save(data, executing);
        SituationInstance recovered = new SituationPersistenceCoordinator().reconcileAfterRestart(data, executing.getId(), 10L);
        assertEquals(SituationLifecycleState.SUSPENDED, recovered.getState()); assertEquals(1, data.getSituations().size());
    }
    private static ProviderDescriptor descriptor(String id, SemanticCapability capability) {
        ProviderId provider = new ProviderId(id);
        CapabilityDescriptor value = new CapabilityDescriptor(capability, provider, ProviderAvailability.RUNTIME_CONTROL, 80, 0);
        return new ProviderDescriptor(provider, id, EvidenceStatus.CONFIRMED, true, ProviderStatus.AVAILABLE, false,
                Collections.<String>emptySet(), "test", Collections.singleton(value), Collections.<CapabilityPolicyBinding>emptySet(), ProviderAvailability.RUNTIME_CONTROL);
    }
}
