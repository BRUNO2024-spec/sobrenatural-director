package com.sobrenaturaldirector.execution;

import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.decision.model.Intent;
import com.sobrenaturaldirector.narrative.AdaptivePacingDirector;
import com.sobrenaturaldirector.narrative.NarrativeIntensity;
import com.sobrenaturaldirector.narrative.NarrativePlanLifecycleState;
import com.sobrenaturaldirector.narrative.PersistentNarrativePlan;
import com.sobrenaturaldirector.narrative.PersistentNarrativeThread;
import com.sobrenaturaldirector.narrative.PacingHistoryEntry;
import com.sobrenaturaldirector.narrative.NarrativeThreadManager;

/** Applies confirmed feedback once, after the physical result is persisted. */
public final class ExecutionOutcomeFeedbackCoordinator {
    public enum Status { APPLIED, ALREADY_APPLIED, REJECTED_NOT_CONFIRMED, REJECTED_INVALID }
    public Status apply(DirectorWorldSavedData saved, String outcomeId) {
        if (saved == null || outcomeId == null || outcomeId.length() == 0) return Status.REJECTED_INVALID;
        ExecutionOutcome outcome=saved.getExecutionOutcome(outcomeId);
        if (outcome == null || outcome.getStatus() != ExecutionOutcome.Status.CONFIRMED) return Status.REJECTED_NOT_CONFIRMED;
        if (outcome.isFeedbackApplied()) return Status.ALREADY_APPLIED;
        saved.updateExecutionOutcome(outcome.withFeedbackApplied());
        return Status.APPLIED;
    }

    /** Applies confirmed physical feedback to every persisted narrative model once. */
    public Status apply(DirectorWorldSavedData saved, String outcomeId, String scope,
            NarrativeThreadManager threads) {
        if (saved == null || outcomeId == null || outcomeId.length() == 0 || scope == null || scope.length() == 0)
            return Status.REJECTED_INVALID;
        ExecutionOutcome outcome = saved.getExecutionOutcome(outcomeId);
        if (outcome == null || outcome.getStatus() != ExecutionOutcome.Status.CONFIRMED) return Status.REJECTED_NOT_CONFIRMED;
        if (outcome.isFeedbackApplied()) return Status.ALREADY_APPLIED;
        PersistentNarrativePlan plan = saved.getNarrativePlan(outcome.getPlanId());
        if (plan == null || plan.getState().isTerminal() && plan.getState() != NarrativePlanLifecycleState.COMPLETED)
            return Status.REJECTED_INVALID;
        Intent intent;
        try { intent = Intent.valueOf(plan.getIntent()); }
        catch (IllegalArgumentException invalidIntent) { return Status.REJECTED_INVALID; }
        if (plan.getState() != NarrativePlanLifecycleState.COMPLETED)
            saved.updateNarrativePlan(plan.withState(NarrativePlanLifecycleState.COMPLETED, outcome.getTick()));
        if (threads != null && plan.getThreadId().length() > 0) {
            PersistentNarrativeThread thread = saved.getNarrativeThread(plan.getThreadId());
            if (thread != null) saved.updateNarrativeThread(thread.meaningfulActivity(outcome.getTick(), "EXECUTION_CONFIRMED:" + outcomeId));
        }
        saved.recordPacing(new PacingHistoryEntry(scope, outcome.getTick(), plan.getThreadId(), intent,
                AdaptivePacingDirector.intensity(intent), "CONFIRMED", outcome.getReason(), outcomeId));
        saved.updateExecutionOutcome(outcome.withFeedbackApplied());
        return Status.APPLIED;
    }
}
