package com.sobrenaturaldirector.threat;

import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.domain.StableId;
import com.sobrenaturaldirector.situation.SemanticRole;

/** Explicit, non-cryptographic runtime permit scoped to one plan slice and one attempt identity. */
public final class PlanExecutionAuthorization {
    private final String executionId, parentPlanId, planFingerprint;
    private final SemanticRole role;
    private final String capability;
    private final ProviderId providerId;
    private final boolean authorized;

    public PlanExecutionAuthorization(String executionId, String parentPlanId, String planFingerprint, SemanticRole role, String capability, ProviderId providerId, boolean authorized) {
        this.executionId = StableId.require(executionId, "executionId"); this.parentPlanId = StableId.require(parentPlanId, "parentPlanId"); this.planFingerprint = StableId.require(planFingerprint, "planFingerprint");
        if (role == null || providerId == null) throw new IllegalArgumentException("role/providerId are required");
        this.role = role; this.capability = StableId.require(capability, "capability"); this.providerId = providerId; this.authorized = authorized;
    }
    public String getExecutionId() { return executionId; }
    public String getParentPlanId() { return parentPlanId; }
    public String getPlanFingerprint() { return planFingerprint; }
    public SemanticRole getRole() { return role; }
    public String getCapability() { return capability; }
    public ProviderId getProviderId() { return providerId; }
    public boolean isAuthorized() { return authorized; }
}
