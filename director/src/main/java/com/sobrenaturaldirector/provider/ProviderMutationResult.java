package com.sobrenaturaldirector.provider;

import java.util.UUID;
import com.sobrenaturaldirector.mutation.runtime.MutationExecutionResult;

public final class ProviderMutationResult {
    private final MutationExecutionResult result;
    private final UUID entityUuid;

    public ProviderMutationResult(MutationExecutionResult result, UUID entityUuid) {
        if (result == null) throw new IllegalArgumentException("result is required");
        this.result = result;
        this.entityUuid = entityUuid;
    }
    public MutationExecutionResult getResult() { return result; }
    public UUID getEntityUuid() { return entityUuid; }
}
