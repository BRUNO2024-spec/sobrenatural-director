package com.sobrenaturaldirector.execution;

import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.mutation.runtime.ControlledMutationExecutor;
import com.sobrenaturaldirector.mutation.runtime.MutationExecutionRequest;
import com.sobrenaturaldirector.mutation.runtime.MutationExecutionResult;
import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.narrative.PersistentNarrativePlan;

/** Narrow autonomous handoff into the existing bounded MPE saga. */
public final class SafeAutonomousExecutionCoordinator {
    public enum Status { EXECUTED, ALREADY_HANDLED, REJECTED_DISABLED, REJECTED_AUTHORIZATION, REJECTED_REVALIDATION, FAILED }
    public static final int MAX_EXECUTIONS_PER_CYCLE = 1;
    private final ControlledMultiProviderCoordinator coordinator = new ControlledMultiProviderCoordinator();
    private final ControlledMutationExecutor mutationExecutor = new ControlledMutationExecutor(com.sobrenaturaldirector.runtime.DirectorRuntimeMode.CONTROLLED_EXECUTION);
    private int attempts, executed, denied;

    public Result execute(PreparedMultiProviderPlan plan, ExecutionPreparationContext context,
            ExecutionAuthorization authorization, boolean autonomousExecutionEnabled) {
        attempts++;
        if (!autonomousExecutionEnabled) return deny(Status.REJECTED_DISABLED, "AUTONOMOUS_EXECUTION_DISABLED");
        if (plan == null || context == null || authorization == null || !authorization.isTestAuthorization()) return deny(Status.REJECTED_AUTHORIZATION, "AUTHORIZATION_REQUIRED");
        if (authorization.getMode() != com.sobrenaturaldirector.runtime.DirectorRuntimeMode.CONTROLLED_EXECUTION
                || !plan.getFingerprint().equals(authorization.getPlanFingerprint())
                || !plan.getPlan().isComplete()) return deny(Status.REJECTED_REVALIDATION, "FINAL_REVALIDATION_FAILED");
        if (context.getWorld() == null || context.getSaved() == null) return deny(Status.REJECTED_REVALIDATION, "RUNTIME_CONTEXT_REQUIRED");
        MultiProviderAuthorization mpe = new MultiProviderAuthorization(authorization.getExecutionId(), authorization.getPlanId(), authorization.getPlanFingerprint(), authorization.getMode(), true);
        MultiProviderExecutionResult result = coordinator.execute(plan, mpe, context.getSaved(), true);
        if (result.getStatus() == MultiProviderExecutionResult.Status.EXECUTED) { executed++; return new Result(Status.EXECUTED, result.getExecutionId(), "EXECUTION_APPLIED"); }
        if (result.getStatus() == MultiProviderExecutionResult.Status.ALREADY_HANDLED) return new Result(Status.ALREADY_HANDLED, result.getExecutionId(), "ALREADY_EXECUTED");
        return new Result(Status.FAILED, result.getExecutionId(), result.getStatus().name());
    }
    public Result execute(PersistentNarrativePlan plan, ExecutionPreparationContext context,
            ExecutionAuthorization authorization, boolean autonomousExecutionEnabled,
            PreparedExecutionPlanFactory factory) {
        if (plan == null || plan.getState().isTerminal() || plan.getState() == com.sobrenaturaldirector.narrative.NarrativePlanLifecycleState.STALE)
            return deny(Status.REJECTED_REVALIDATION, "FINAL_REVALIDATION_FAILED");
        try {
            return execute(new PersistentNarrativePlanExecutionBridge(factory).prepare(plan, context), context,
                    authorization, autonomousExecutionEnabled);
        } catch (RuntimeException failure) {
            return deny(Status.REJECTED_REVALIDATION, "PLAN_PREPARATION_FAILED");
        }
    }
    private Result deny(Status status, String reason) { denied++; return new Result(status, "", reason); }
    public int getAttempts() { return attempts; }
    public int getExecuted() { return executed; }
    public int getDenied() { return denied; }
    public MutationExecutionResult rollbackMutation(WorldServer world, DirectorWorldSavedData saved,
            String mutationId, ExecutionAuthorization authorization) {
        if (world == null || saved == null || authorization == null || !authorization.isTestAuthorization()
                || authorization.getMode() != com.sobrenaturaldirector.runtime.DirectorRuntimeMode.CONTROLLED_EXECUTION)
            return MutationExecutionResult.of(MutationExecutionResult.Status.REJECTED_POLICY, mutationId == null ? "" : mutationId);
        return mutationExecutor.rollback(world, saved, mutationId);
    }
    /** Narrow single-action path used when a complete provider-neutral MPE plan is unavailable. */
    public MutationExecutionResult executeMutation(WorldServer world, DirectorWorldSavedData saved,
            MutationExecutionRequest request, ExecutionAuthorization authorization, boolean autonomousExecutionEnabled) {
        attempts++;
        if (!autonomousExecutionEnabled) return MutationExecutionResult.of(MutationExecutionResult.Status.REJECTED_DISABLED, request == null ? "" : request.getMutationId());
        if (world == null || saved == null || request == null || authorization == null || !authorization.isTestAuthorization()) return MutationExecutionResult.of(MutationExecutionResult.Status.REJECTED_POLICY, request == null ? "" : request.getMutationId());
        if (authorization.getMode() != com.sobrenaturaldirector.runtime.DirectorRuntimeMode.CONTROLLED_EXECUTION
                || !authorization.getPlanId().equals(request.getPlanId()) || !authorization.getPlanFingerprint().equals(request.getSourceId())
                || !"Server thread".equals(Thread.currentThread().getName())) return MutationExecutionResult.of(MutationExecutionResult.Status.REJECTED_POLICY, request.getMutationId());
        MutationExecutionResult result=mutationExecutor.execute(world, saved, request, true);
        if (result.getStatus()==MutationExecutionResult.Status.EXECUTED) executed++;
        return result;
    }
    public static final class Result {
        private final Status status; private final String executionId, reason;
        private Result(Status status, String executionId, String reason) { this.status=status; this.executionId=executionId; this.reason=reason; }
        public Status getStatus() { return status; } public String getExecutionId() { return executionId; } public String getReason() { return reason; }
    }
}
