package com.sobrenaturaldirector.control;

import com.sobrenaturaldirector.composition.model.DirectorOwnedCompositionPlan;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.content.model.SemanticCapability;

/** Provider-neutral request for an allowlisted Director-owned composition. */
public final class StructureControlRequest extends ControlRequest {
    public enum Operation { APPLY, ROLLBACK }
    private final DirectorOwnedCompositionPlan plan;
    private final Operation operation;
    public StructureControlRequest(ProviderId provider, SemanticCapability capability,
            DirectorOwnedCompositionPlan plan, Operation operation, DimensionRef dimension,
            ControlLease lease, ControlAuthority authority) {
        super(ControlDomain.STRUCTURE, provider, capability, dimension, lease, authority);
        if (plan == null || operation == null || !dimension.isAvailable() || plan.getDimension() != dimension.getDimensionId())
            throw new IllegalArgumentException("invalid structure request");
        this.plan = plan; this.operation = operation;
    }
    public DirectorOwnedCompositionPlan getPlan() { return plan; }
    public Operation getOperation() { return operation; }
}
