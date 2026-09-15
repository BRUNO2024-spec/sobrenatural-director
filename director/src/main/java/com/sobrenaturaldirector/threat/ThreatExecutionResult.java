package com.sobrenaturaldirector.threat;

import java.util.UUID;

public final class ThreatExecutionResult {
    public enum Status { CREATED, ALREADY_EXECUTED, RECONCILED_EXISTING, CLEANUP_EXECUTED,
        REJECTED_DISABLED, REJECTED_POLICY, REJECTED_WRONG_THREAD, REJECTED_PROVIDER_UNAVAILABLE,
        REJECTED_UNSUPPORTED_VERSION, REJECTED_NO_SAFE_SITE, ENTITY_NOT_LOADED,
        OWNERSHIP_MISMATCH, FAILED, REJECTED_NOT_AUTHORIZED, REJECTED_STALE_PLAN,
        REJECTED_PACING_CHANGED, REJECTED_INVALID_SCOPE, REJECTED_ALREADY_HANDLED,
        REJECTED_ACTIVE_CAP }
    private final Status status;
    private final String requestId;
    private final UUID entityUuid;
    private ThreatExecutionResult(Status status, String requestId, UUID entityUuid) { this.status = status; this.requestId = requestId; this.entityUuid = entityUuid; }
    public static ThreatExecutionResult of(Status status, String requestId, UUID entityUuid) { return new ThreatExecutionResult(status, requestId == null ? "" : requestId, entityUuid); }
    public Status getStatus() { return status; }
    public String getRequestId() { return requestId; }
    public UUID getEntityUuid() { return entityUuid; }
}
