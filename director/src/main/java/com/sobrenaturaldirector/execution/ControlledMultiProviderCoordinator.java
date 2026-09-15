package com.sobrenaturaldirector.execution;

import java.util.ArrayList;
import java.util.List;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;

/** Provider-neutral COORDINATED SAGA. No provider is selected or called by name here. */
public final class ControlledMultiProviderCoordinator {
    public MultiProviderExecutionResult execute(PreparedMultiProviderPlan plan, MultiProviderAuthorization authorization,
            DirectorWorldSavedData saved, boolean enabled) {
        return execute(plan, authorization, saved, enabled, null);
    }
    public MultiProviderExecutionResult execute(PreparedMultiProviderPlan plan, MultiProviderAuthorization authorization,
            DirectorWorldSavedData saved, boolean enabled, ExecutionStageHook hook) {
        if (plan == null || authorization == null || saved == null) return result(MultiProviderExecutionResult.Status.PREFLIGHT_REJECTED, authorization);
        if (!enabled) return result(MultiProviderExecutionResult.Status.REJECTED_AUTHORIZATION, authorization);
        if (!authorization.isAuthorized()) return result(MultiProviderExecutionResult.Status.REJECTED_AUTHORIZATION, authorization);
        if (authorization.getMode() != DirectorRuntimeMode.CONTROLLED_EXECUTION) return result(MultiProviderExecutionResult.Status.REJECTED_MODE, authorization);
        if (!plan.getPlan().isComplete() || !plan.getFingerprint().equals(authorization.getPlanFingerprint())) return result(MultiProviderExecutionResult.Status.PREFLIGHT_REJECTED, authorization);
        CoordinatedExecutionRecord existing = saved.getCoordinatedExecution(authorization.getExecutionId());
        if (existing != null && ("COMPLETED".equals(existing.getStatus()) || "EXECUTED".equals(existing.getStatus()) || "RESOLVED".equals(existing.getStatus()) || "COMPENSATED".equals(existing.getStatus()) || "COMPENSATION_FAILED".equals(existing.getStatus()))) return result(MultiProviderExecutionResult.Status.ALREADY_HANDLED, authorization);
        for (ControlledProviderSlice slice : plan.getSlices()) {
            CoordinatedSliceResult check = slice.preflight();
            if (check == null || (check.getStatus() != CoordinatedSliceResult.Status.EXECUTED && check.getStatus() != CoordinatedSliceResult.Status.RECONCILED)) {
                return result(MultiProviderExecutionResult.Status.PREFLIGHT_REJECTED, authorization);
            }
        }
        CoordinatedExecutionRecord record = existing == null ? new CoordinatedExecutionRecord(authorization.getExecutionId(), authorization.getParentPlanId(), plan.getFingerprint(), "PREPARED") : existing.withStatus("PREPARED");
        if (existing == null) saved.recordCoordinatedExecution(record); else saved.updateCoordinatedExecution(record);
        List<ControlledProviderSlice> executed = new ArrayList<ControlledProviderSlice>();
        for (ControlledProviderSlice slice : plan.getSlices()) {
            if (existing != null && existing.getCompletedSlices().contains(slice.getSliceId())) { executed.add(slice); continue; }
            CoordinatedSliceResult current = slice.execute();
            if (current == null || !current.succeeded()) {
                saved.updateCoordinatedExecution(record.withStatus("COMPENSATING"));
                for (int i = executed.size() - 1; i >= 0; i--) if (!executed.get(i).compensate().succeeded()) { saved.updateCoordinatedExecution(record.withStatus("COMPENSATION_FAILED")); return result(MultiProviderExecutionResult.Status.COMPENSATION_FAILED, authorization); }
                saved.updateCoordinatedExecution(record.withStatus("COMPENSATED"));
                return result(MultiProviderExecutionResult.Status.COMPENSATED, authorization);
            }
            executed.add(slice);
            record = record.withSliceCompleted(slice.getSliceId());
            saved.updateCoordinatedExecution(record);
            if (hook != null && hook.pauseAfter(slice)) { saved.updateCoordinatedExecution(record.withStatus("PAUSED")); return result(MultiProviderExecutionResult.Status.PAUSED, authorization); }
        }
        saved.updateCoordinatedExecution(record.withStatus("COMPLETED"));
        return result(MultiProviderExecutionResult.Status.EXECUTED, authorization);
    }
    /** Explicit normal resolution, distinct from failure compensation. */
    public MultiProviderExecutionResult resolve(PreparedMultiProviderPlan plan, MultiProviderAuthorization authorization, DirectorWorldSavedData saved) {
        if (plan == null || authorization == null || saved == null || !authorization.isAuthorized() || authorization.getMode() != DirectorRuntimeMode.CONTROLLED_EXECUTION) return result(MultiProviderExecutionResult.Status.REJECTED_AUTHORIZATION, authorization);
        CoordinatedExecutionRecord record = saved.getCoordinatedExecution(authorization.getExecutionId());
        if (record == null || !record.matches(authorization, plan.getFingerprint()) || !("COMPLETED".equals(record.getStatus()) || "EXECUTED".equals(record.getStatus()))) return result(MultiProviderExecutionResult.Status.ALREADY_HANDLED, authorization);
        for (int i = plan.getSlices().size() - 1; i >= 0; i--) if (!plan.getSlices().get(i).compensate().succeeded()) return result(MultiProviderExecutionResult.Status.COMPENSATION_FAILED, authorization);
        saved.updateCoordinatedExecution(record.withStatus("RESOLVED"));
        return result(MultiProviderExecutionResult.Status.COMPENSATED, authorization);
    }
    public MultiProviderExecutionResult reconcile(PreparedMultiProviderPlan plan, MultiProviderAuthorization authorization, DirectorWorldSavedData saved) {
        if (plan == null || authorization == null || saved == null) return result(MultiProviderExecutionResult.Status.PREFLIGHT_REJECTED, authorization);
        CoordinatedExecutionRecord record = saved.getCoordinatedExecution(authorization.getExecutionId());
        if (record == null || !record.matches(authorization, plan.getFingerprint())) return result(MultiProviderExecutionResult.Status.PREFLIGHT_REJECTED, authorization);
        boolean complete = true;
        for (ControlledProviderSlice slice : plan.getSlices()) if (record.getCompletedSlices().contains(slice.getSliceId())) { CoordinatedSliceResult current = slice.reconcile(); if (current == null || !current.succeeded()) return new MultiProviderExecutionResult(MultiProviderExecutionResult.Status.PARTIAL_FAILURE, authorization.getExecutionId()); } else complete = false;
        saved.updateCoordinatedExecution(record.withStatus(complete ? "COMPLETED" : "PAUSED"));
        return result(complete ? MultiProviderExecutionResult.Status.RECONCILED : MultiProviderExecutionResult.Status.PAUSED, authorization);
    }
    private MultiProviderExecutionResult result(MultiProviderExecutionResult.Status status, MultiProviderAuthorization authorization) { return new MultiProviderExecutionResult(status, authorization == null ? "" : authorization.getExecutionId()); }
}
