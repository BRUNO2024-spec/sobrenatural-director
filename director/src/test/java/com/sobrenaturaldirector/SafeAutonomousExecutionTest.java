package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import org.junit.Test;
import com.sobrenaturaldirector.config.FoundationConfig;
import com.sobrenaturaldirector.execution.ExecutionOutcome;
import com.sobrenaturaldirector.execution.ExecutionOutcomeFeedbackCoordinator;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.narrative.NarrativeThreadManager;
import com.sobrenaturaldirector.narrative.PersistentNarrativePlan;
import com.sobrenaturaldirector.narrative.PersistentNarrativeThread;
import com.sobrenaturaldirector.narrative.NarrativePlanLifecycleState;
import com.sobrenaturaldirector.planning.environment.EnvironmentActionImpact;
import com.sobrenaturaldirector.environment.model.EnvironmentClassification;
import java.util.Collections;

public final class SafeAutonomousExecutionTest {
    @Test public void autonomousExecutionIsConservativeByDefault() {
        assertFalse(FoundationConfig.defaults().isAutonomousExecutionEnabled());
        assertTrue(FoundationConfig.defaults().isAutonomousPlanningEnabled());
    }
    @Test public void feedbackRequiresConfirmationAndIsIdempotent() {
        DirectorWorldSavedData saved = new DirectorWorldSavedData();
        ExecutionOutcome pending = new ExecutionOutcome("outcome-pending", "exec-pending", "plan-pending", "gravestone", "", ExecutionOutcome.Status.APPLIED, 1L, "applied", false);
        saved.recordExecutionOutcome(pending);
        ExecutionOutcomeFeedbackCoordinator feedback = new ExecutionOutcomeFeedbackCoordinator();
        assertEquals(ExecutionOutcomeFeedbackCoordinator.Status.REJECTED_NOT_CONFIRMED, feedback.apply(saved, pending.getOutcomeId()));
        ExecutionOutcome confirmed = new ExecutionOutcome("outcome-confirmed", "exec-confirmed", "plan-confirmed", "gravestone", "block:1", ExecutionOutcome.Status.CONFIRMED, 2L, "verified", false);
        saved.recordExecutionOutcome(confirmed);
        assertEquals(ExecutionOutcomeFeedbackCoordinator.Status.APPLIED, feedback.apply(saved, confirmed.getOutcomeId()));
        assertEquals(ExecutionOutcomeFeedbackCoordinator.Status.ALREADY_APPLIED, feedback.apply(saved, confirmed.getOutcomeId()));
        assertTrue(saved.getExecutionOutcome(confirmed.getOutcomeId()).isFeedbackApplied());
    }
    @Test public void outcomePersistenceIsBoundedAndReadable() {
        DirectorWorldSavedData saved = new DirectorWorldSavedData();
        saved.recordExecutionOutcome(new ExecutionOutcome("outcome-1", "exec-1", "plan-1", "provider", "physical-1", ExecutionOutcome.Status.CONFIRMED, 1L, "verified", true));
        assertEquals(1, saved.getExecutionOutcomes().size());
        assertEquals("exec-1", saved.getExecutionOutcome("outcome-1").getExecutionId());
    }
    @Test public void confirmedFeedbackAdvancesNarrativeModelsOnce() {
        DirectorWorldSavedData saved = new DirectorWorldSavedData();
        NarrativeThreadManager threads = new NarrativeThreadManager();
        PersistentNarrativeThread thread = threads.create(saved, "feedback-thread", "story", 0, 1, 1,
                Collections.singleton("continuity"), 1L, "CREATED");
        PersistentNarrativePlan plan = new PersistentNarrativePlan("feedback-plan", "story", "LOOT_DISCOVERY", "blueprint", "blueprint-fp", "plan-fp", "candidate-fp", "INSIDE", EnvironmentActionImpact.NON_INTRUSIVE, Collections.<String, PersistentNarrativePlan.CapabilityRecord>emptyMap(), false, NarrativePlanLifecycleState.ACTIVE, 1L, 1L, 100L, 1L, "VALID", Collections.<String>emptyList(), "", 0, 1, 0, 1, 1, EnvironmentClassification.UNKNOWN, 0, 0).withThreadId(thread.getId());
        saved.recordNarrativePlan(plan);
        ExecutionOutcome outcome = new ExecutionOutcome("feedback-outcome", "feedback-execution", plan.getId(), "provider", "physical", ExecutionOutcome.Status.CONFIRMED, 5L, "verified", false);
        saved.recordExecutionOutcome(outcome);
        ExecutionOutcomeFeedbackCoordinator feedback = new ExecutionOutcomeFeedbackCoordinator();
        assertEquals(ExecutionOutcomeFeedbackCoordinator.Status.APPLIED, feedback.apply(saved, outcome.getOutcomeId(), "feedback-scope", threads));
        assertEquals(NarrativePlanLifecycleState.COMPLETED, saved.getNarrativePlan(plan.getId()).getState());
        assertEquals(5L, saved.getNarrativeThread(thread.getId()).getLastMeaningfulActivityTick());
        assertEquals(1, saved.getPacingHistory().size());
        assertEquals(ExecutionOutcomeFeedbackCoordinator.Status.ALREADY_APPLIED, feedback.apply(saved, outcome.getOutcomeId(), "feedback-scope", threads));
        assertEquals(1, saved.getPacingHistory().size());
    }
}
