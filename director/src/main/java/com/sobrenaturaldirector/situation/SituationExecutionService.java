package com.sobrenaturaldirector.situation;

import java.util.Collections;
import java.util.Map;
import com.sobrenaturaldirector.action.ActionExecutionSpec;
import com.sobrenaturaldirector.action.ActionOutcome;
import com.sobrenaturaldirector.action.ActionExecutionStatus;
import com.sobrenaturaldirector.action.ActionPlanExecutor;
import com.sobrenaturaldirector.control.ControlExecutionContext;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;

/** Explicit caller-owned bridge from a validated situation to the shared executor. */
public final class SituationExecutionService {
    private final ActionPlanExecutor executor;
    private final DirectorProviderRegistry registry;
    public SituationExecutionService(ActionPlanExecutor executor, DirectorProviderRegistry registry) {
        if (executor == null || registry == null) throw new IllegalArgumentException("situation execution dependencies");
        this.executor=executor; this.registry=registry;
    }
    public Result execute(SituationInstance instance, Map<String,ActionExecutionSpec> specs,
            ControlExecutionContext context, SituationMemory memory, long tick) {
        if (instance == null || context == null || tick < 0) throw new IllegalArgumentException("situation execution input");
        if (instance.isTerminal() || instance.getState() != SituationLifecycleState.READY)
            return new Result(instance, null, "situation is not READY");
        if (!instance.getRelevantDimension().isAvailable() || instance.getRelevantDimension().getDimensionId() != context.getWorld().provider.dimensionId)
            return new Result(instance.withOutcome(null, SituationLifecycleState.STALE, "dimension unavailable or changed", tick, "DIMENSION_PREFLIGHT"), null, "dimension stale");
        SituationInstance running=instance.transition(SituationLifecycleState.EXECUTING, "validated execution", tick, "EXECUTION_REQUEST");
        ActionOutcome outcome=executor.execute(running.getActionPlan(), specs == null ? Collections.<String,ActionExecutionSpec>emptyMap() : specs, context, tick);
        SituationLifecycleState next=map(outcome);
        SituationInstance finished=running.withOutcome(outcome,next,outcome == null ? "missing action outcome" : outcome.getReason(),tick,"ACTION_OUTCOME");
        if (memory != null) memory.record(new SituationMemoryEntry(finished.getId(),finished.getGoal(),tick,
                context.getWorld().provider.dimensionId,0,finished.getBlueprint().fingerprint(),
                // Provider identities are intentionally omitted here: only the executor journal knows which
                // bindings were actually invoked, and planning availability is not a factual outcome.
                Collections.<com.sobrenaturaldirector.content.model.ProviderId>emptyList(),
                outcome == null ? 0 : outcome.getCompletedSteps()));
        return new Result(finished,outcome,"");
    }
    private SituationLifecycleState map(ActionOutcome outcome) {
        if (outcome == null) return SituationLifecycleState.FAILED;
        if (outcome.getStatus() == ActionExecutionStatus.COMPLETED) return SituationLifecycleState.ACTIVE;
        if (outcome.getStatus() == ActionExecutionStatus.COMPENSATED) return SituationLifecycleState.COMPENSATED;
        if (outcome.getStatus() == ActionExecutionStatus.STALE) return SituationLifecycleState.STALE;
        if (outcome.getStatus() == ActionExecutionStatus.SAFETY_REJECTED || outcome.getStatus() == ActionExecutionStatus.ABORTED ||
                outcome.getStatus() == ActionExecutionStatus.BUDGET_REJECTED) return SituationLifecycleState.ABORTED;
        return SituationLifecycleState.FAILED;
    }
    public static final class Result {
        private final SituationInstance instance; private final ActionOutcome outcome; private final String rejection;
        Result(SituationInstance instance, ActionOutcome outcome, String rejection){this.instance=instance;this.outcome=outcome;this.rejection=rejection;}
        public SituationInstance getInstance(){return instance;} public ActionOutcome getOutcome(){return outcome;} public String getRejection(){return rejection;}
        public boolean isExecuted(){return outcome != null;}
    }
}
