package com.sobrenaturaldirector.execution;

import com.sobrenaturaldirector.domain.StableId;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;

public final class MultiProviderAuthorization {
    private final String executionId, parentPlanId, planFingerprint;
    private final DirectorRuntimeMode mode;
    private final boolean authorized;
    public MultiProviderAuthorization(String executionId, String parentPlanId, String planFingerprint,
            DirectorRuntimeMode mode, boolean authorized) {
        this.executionId = StableId.require(executionId, "executionId");
        this.parentPlanId = StableId.require(parentPlanId, "parentPlanId");
        this.planFingerprint = StableId.require(planFingerprint, "planFingerprint");
        if (mode == null) throw new IllegalArgumentException("mode is required");
        this.mode = mode; this.authorized = authorized;
    }
    public String getExecutionId() { return executionId; }
    public String getParentPlanId() { return parentPlanId; }
    public String getPlanFingerprint() { return planFingerprint; }
    public DirectorRuntimeMode getMode() { return mode; }
    public boolean isAuthorized() { return authorized; }
}
