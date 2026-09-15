package com.sobrenaturaldirector.composition.model;

import com.sobrenaturaldirector.domain.StableId;

public final class CompositionBlockOperation {
    private final String childMutationId;
    private final int relativeX, relativeY, relativeZ;
    private final ExternalBlockReference block;

    public CompositionBlockOperation(String childMutationId, int relativeX, int relativeY, int relativeZ,
            ExternalBlockReference block) {
        this.childMutationId = StableId.require(childMutationId, "childMutationId");
        if (block == null) throw new IllegalArgumentException("block is required");
        this.relativeX = relativeX; this.relativeY = relativeY; this.relativeZ = relativeZ; this.block = block;
    }
    public String getChildMutationId() { return childMutationId; }
    public int getRelativeX() { return relativeX; }
    public int getRelativeY() { return relativeY; }
    public int getRelativeZ() { return relativeZ; }
    public ExternalBlockReference getBlock() { return block; }
}
