package com.sobrenaturaldirector;

import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sobrenaturaldirector.decision.model.DecisionContext;
import com.sobrenaturaldirector.domain.DirectorWorldState;
import com.sobrenaturaldirector.narrative.ControlledAutonomousPlanningScheduler;
import com.sobrenaturaldirector.narrative.NarrativeArbitrationContext;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.situation.SituationContext;
import com.sobrenaturaldirector.situation.SituationMemory;
import com.sobrenaturaldirector.decision.model.Intent;
import com.sobrenaturaldirector.narrative.NarrativeOpportunity;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.ProviderDescriptor;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.content.model.EvidenceStatus;
import com.sobrenaturaldirector.capability.ProviderAvailability;

public final class ControlledAutonomousPlanningSchedulerTest {
    private static ControlledAutonomousPlanningScheduler.CycleInput input(long tick, String fingerprint, DirectorWorldSavedData data) {
        return input("p:0:1:2", tick, fingerprint, data);
    }
    private static ControlledAutonomousPlanningScheduler.CycleInput input(String scope, long tick, String fingerprint, DirectorWorldSavedData data) {
        DecisionContext decision = DecisionContext.calm(tick);
        SituationContext situation = new SituationContext(0, tick, 1, 2, 7, false, true, true, true, Collections.<String>emptySet());
        NarrativeArbitrationContext arbitration = new NarrativeArbitrationContext(decision, new SituationMemory(),
                Collections.<String>emptySet(), null, false, "", 7, null);
        return new ControlledAutonomousPlanningScheduler.CycleInput(scope, tick, fingerprint,
                Collections.emptyList(), arbitration, situation, new SituationMemory(), new DirectorProviderRegistry(),
                "narrative", "INSIDE", fingerprint, Collections.<String>emptySet(), "", null, data);
    }

    @Test public void meaningfulChangeInterruptsBoundedBackoff() {
        DirectorWorldSavedData data = new DirectorWorldSavedData(); data.replaceState(DirectorWorldState.empty(7));
        ControlledAutonomousPlanningScheduler scheduler = new ControlledAutonomousPlanningScheduler(true);
        scheduler.evaluate(input(0, "stable", data)); scheduler.evaluate(input(200, "stable", data)); scheduler.evaluate(input(400, "stable", data));
        assertEquals(ControlledAutonomousPlanningScheduler.Decision.NO_ACTION,
                scheduler.evaluate(input(500, "changed", data)).getDecision());
        assertTrue(data.getSchedulerMetadata("p:0:1:2").getBackoffTicks() >= 200);
    }

    @Test public void scopesAndDimensionsDoNotShareMetadata() {
        DirectorWorldSavedData data = new DirectorWorldSavedData(); data.replaceState(DirectorWorldState.empty(7));
        ControlledAutonomousPlanningScheduler scheduler = new ControlledAutonomousPlanningScheduler(true);
        scheduler.evaluate(input("player-a:0:1:2", 0, "a", data)); scheduler.evaluate(input("player-b:0:1:2", 0, "b", data)); scheduler.evaluate(input("player-a:1:1:2", 0, "c", data));
        assertNotNull(data.getSchedulerMetadata("player-a:0:1:2")); assertNotNull(data.getSchedulerMetadata("player-b:0:1:2")); assertNotNull(data.getSchedulerMetadata("player-a:1:1:2"));
        assertEquals("a", data.getSchedulerMetadata("player-a:0:1:2").getFingerprint()); assertEquals("b", data.getSchedulerMetadata("player-b:0:1:2").getFingerprint());
    }

    @Test public void disabledHasNoSideEffects() {
        DirectorWorldSavedData data = new DirectorWorldSavedData(); data.replaceState(DirectorWorldState.empty(7));
        ControlledAutonomousPlanningScheduler scheduler = new ControlledAutonomousPlanningScheduler(false);
        assertEquals(ControlledAutonomousPlanningScheduler.Decision.DISABLED, scheduler.evaluate(input(0, "a", data)).getDecision());
        assertTrue(data.getNarrativePlans().isEmpty()); assertTrue(data.getNarrativeThreads().isEmpty());
    }

    @Test public void unchangedLongRunIsBoundedAndPersisted() {
        DirectorWorldSavedData data = new DirectorWorldSavedData(); data.replaceState(DirectorWorldState.empty(7));
        ControlledAutonomousPlanningScheduler scheduler = new ControlledAutonomousPlanningScheduler(true);
        int skipped = 0;
        for (int i = 0; i < 1000; i++) if (scheduler.evaluate(input(i * 200, "stable", data)).getDecision() == ControlledAutonomousPlanningScheduler.Decision.SKIPPED_NO_CHANGE) skipped++;
        assertEquals(0, data.getNarrativePlans().size()); assertEquals(0, data.getNarrativeThreads().size());
        assertTrue(skipped > 0); assertNotNull(data.getSchedulerMetadata("p:0:1:2"));
    }

    @Test public void schedulerOwnsPlanCreationAndThenIsIdempotent() {
        DirectorWorldSavedData data = new DirectorWorldSavedData(); data.replaceState(DirectorWorldState.empty(7));
        DecisionContext decision = DecisionContext.calm(0);
        SituationContext situation = new SituationContext(0, 0, 1, 2, 7, false, true, true, true, Collections.<String>emptySet());
        NarrativeArbitrationContext arbitration = new NarrativeArbitrationContext(decision, new SituationMemory(),
                Collections.<String>emptySet(), null, false, "", 7, null);
        ControlledAutonomousPlanningScheduler.CycleInput planInput = new ControlledAutonomousPlanningScheduler.CycleInput(
                "p:0:1:2", 0, "meaningful", Collections.singletonList(new NarrativeOpportunity("ambient", Intent.AMBIENT_HINT,
                .95, .95, Collections.<String>emptySet(), Collections.singletonList("FIXTURE"), false, null)), arbitration, situation,
                new SituationMemory(), registry(), "narrative", "INSIDE", "meaningful",
                Collections.<String>emptySet(), "", null, data);
        ControlledAutonomousPlanningScheduler scheduler = new ControlledAutonomousPlanningScheduler(true);
        ControlledAutonomousPlanningScheduler.PlanningCycleResult created = scheduler.evaluate(planInput);
        assertEquals(created.getReason(), ControlledAutonomousPlanningScheduler.Decision.PLAN_CREATED, created.getDecision());
        assertEquals(1, data.getNarrativePlans().size()); assertEquals(1, data.getNarrativeThreads().size());
        assertEquals(ControlledAutonomousPlanningScheduler.Decision.SKIPPED_NO_CHANGE,
                scheduler.evaluate(new ControlledAutonomousPlanningScheduler.CycleInput("p:0:1:2", 200, "meaningful",
                        planInputOpportunity(), arbitration, situation, new SituationMemory(), registry(),
                        "narrative", "INSIDE", "meaningful", Collections.<String>emptySet(), "",
                        data.getNarrativePlans().get(0), data)).getDecision());
    }

    private static java.util.List<NarrativeOpportunity> planInputOpportunity() {
        return Collections.singletonList(new NarrativeOpportunity("ambient", Intent.AMBIENT_HINT, .95, .95,
                Collections.<String>emptySet(), Collections.singletonList("FIXTURE"), false, null));
    }
    private static DirectorProviderRegistry registry() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        ProviderId id = new ProviderId("structure");
        registry.registerDescriptor(new ProviderDescriptor(id, "structure", EvidenceStatus.CONFIRMED, true,
                com.sobrenaturaldirector.decision.model.ProviderStatus.AVAILABLE, false, Collections.<String>emptySet(), "1",
                Collections.singleton(new CapabilityDescriptor(CapabilityVocabulary.STRUCTURE_SOURCE, id,
                        ProviderAvailability.RUNTIME_CONTROL, 80, 0)), Collections.emptySet(), ProviderAvailability.RUNTIME_CONTROL));
        return registry;
    }
}
