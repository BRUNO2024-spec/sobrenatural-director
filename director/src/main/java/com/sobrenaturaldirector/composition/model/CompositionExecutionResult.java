package com.sobrenaturaldirector.composition.model;

public final class CompositionExecutionResult {
    public enum Status { EXECUTED, ALREADY_EXECUTED, REJECTED_MODE, REJECTED_DISABLED,
        REJECTED_PROVIDER_UNAVAILABLE, REJECTED_PROVIDER_UNSUPPORTED, REJECTED_CONTENT_NOT_FOUND,
        REJECTED_CONTENT_OWNER, REJECTED_METADATA, REJECTED_CHUNK_NOT_LOADED, REJECTED_BLOCK_NOT_AIR,
        REJECTED_TILE_ENTITY, REJECTED_ENTITY_OCCUPIED, REJECTED_INVALID_Y, REJECTED_SUPPORT,
        REJECTED_DUPLICATE_TARGET, REJECTED_TOO_MANY_BLOCKS, PREFLIGHT_REJECTED,
        PARTIAL_EXECUTION_FAILURE, ROLLBACK_EXECUTED, ROLLBACK_REJECTED_STALE, ROLLBACK_NOT_FOUND }
    private final Status status;
    private final String compositionId;
    private final int writes;
    public CompositionExecutionResult(Status status, String compositionId, int writes) { this.status = status; this.compositionId = compositionId; this.writes = writes; }
    public Status getStatus() { return status; }
    public String getCompositionId() { return compositionId; }
    public int getWrites() { return writes; }
}
