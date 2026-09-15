package com.sobrenaturaldirector.mutation.runtime;

public final class MutationExecutionResult {
    public enum Status {
        EXECUTED, ALREADY_EXECUTED, REJECTED_POLICY, REJECTED_MODE, REJECTED_DISABLED,
        REJECTED_CHUNK_NOT_LOADED, REJECTED_DIMENSION, REJECTED_BLOCK_NOT_AIR,
        REJECTED_TILE_ENTITY, REJECTED_ENTITY_OCCUPIED, REJECTED_INVALID_Y,
        REJECTED_UNSUPPORTED_OPERATION, REJECTED_UNSUPPORTED_BLOCK,
        ABORT_STALE_PRECONDITION, FAILED_WORLD_WRITE, ROLLBACK_STALE_STATE,
        ROLLBACK_EXECUTED, ROLLBACK_NOT_FOUND
    }
    private final Status status;
    private final String mutationId;
    private MutationExecutionResult(Status status, String mutationId) { this.status = status; this.mutationId = mutationId; }
    public static MutationExecutionResult of(Status status, String mutationId) { return new MutationExecutionResult(status, mutationId); }
    public Status getStatus() { return status; }
    public String getMutationId() { return mutationId; }
    public boolean isExecuted() { return status == Status.EXECUTED || status == Status.ROLLBACK_EXECUTED; }
}
