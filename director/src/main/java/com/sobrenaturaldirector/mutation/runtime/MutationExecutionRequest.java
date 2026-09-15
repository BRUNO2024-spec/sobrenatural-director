package com.sobrenaturaldirector.mutation.runtime;

import com.sobrenaturaldirector.domain.StableId;

/** Explicit validation request; automatic Director plans cannot construct this boundary. */
public final class MutationExecutionRequest {
    private final String mutationId, planId, sourceId, blockName;
    private final MutationOperation operation;
    private final int dimension, x, y, z, metadata;
    private final String expectedBlock;

    public MutationExecutionRequest(String mutationId, String planId, String sourceId, MutationOperation operation,
            int dimension, int x, int y, int z, String blockName, int metadata, String expectedBlock) {
        this.mutationId = StableId.require(mutationId, "mutationId");
        this.planId = StableId.require(planId, "planId");
        this.sourceId = StableId.require(sourceId, "sourceId");
        if (operation == null) throw new IllegalArgumentException("operation is required");
        this.operation = operation;
        this.dimension = dimension; this.x = x; this.y = y; this.z = z; this.metadata = metadata;
        this.blockName = StableId.require(blockName, "blockName");
        this.expectedBlock = StableId.require(expectedBlock, "expectedBlock");
    }
    public String getMutationId() { return mutationId; }
    public String getPlanId() { return planId; }
    public String getSourceId() { return sourceId; }
    public MutationOperation getOperation() { return operation; }
    public int getDimension() { return dimension; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getMetadata() { return metadata; }
    public String getBlockName() { return blockName; }
    public String getExpectedBlock() { return expectedBlock; }
}
