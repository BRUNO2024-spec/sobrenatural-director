package com.sobrenaturaldirector.capability;

import com.sobrenaturaldirector.content.model.SemanticCapability;
import com.sobrenaturaldirector.domain.StableId;

public final class CapabilityPolicyBinding {
    private final SemanticCapability capability;
    private final String policyId;
    public CapabilityPolicyBinding(SemanticCapability capability, String policyId) {
        if (capability == null) throw new IllegalArgumentException("capability is required");
        this.capability = capability; this.policyId = StableId.require(policyId, "policyId");
    }
    public SemanticCapability getCapability() { return capability; }
    public String getPolicyId() { return policyId; }
}
