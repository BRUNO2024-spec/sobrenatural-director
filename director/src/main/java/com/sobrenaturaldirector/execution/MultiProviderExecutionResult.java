package com.sobrenaturaldirector.execution;

public final class MultiProviderExecutionResult {
    public enum Status { EXECUTED, RECONCILED, PAUSED, PREFLIGHT_REJECTED, REJECTED_AUTHORIZATION, REJECTED_MODE, ALREADY_HANDLED, PARTIAL_FAILURE, COMPENSATED, COMPENSATION_FAILED }
    private final Status status;
    private final String executionId;
    public MultiProviderExecutionResult(Status status, String executionId) { this.status = status; this.executionId = executionId == null ? "" : executionId; }
    public Status getStatus() { return status; }
    public String getExecutionId() { return executionId; }
}
