package com.sobrenaturaldirector.execution;

import com.sobrenaturaldirector.domain.StableId;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;

/** Immutable last-mile authorization snapshot. It never replaces final validation. */
public final class ExecutionAuthorization {
    private final String executionId, planId, planFingerprint, scope, providerRevision, environmentFingerprint;
    private final long issuedTick;
    private final DirectorRuntimeMode mode;
    private final boolean testAuthorization;

    public ExecutionAuthorization(String executionId, String planId, String planFingerprint, String scope,
            int providerRevision, String environmentFingerprint, long issuedTick, DirectorRuntimeMode mode,
            boolean testAuthorization) {
        this.executionId = StableId.require(executionId, "executionId");
        this.planId = StableId.require(planId, "planId");
        this.planFingerprint = StableId.require(planFingerprint, "planFingerprint");
        this.scope = StableId.require(scope, "scope");
        if (providerRevision < 0 || issuedTick < 0) throw new IllegalArgumentException("invalid authorization revision");
        this.providerRevision = String.valueOf(providerRevision);
        this.environmentFingerprint = StableId.require(environmentFingerprint, "environmentFingerprint");
        if (mode == null) throw new IllegalArgumentException("mode is required");
        this.issuedTick = issuedTick; this.mode = mode; this.testAuthorization = testAuthorization;
    }
    public String getExecutionId() { return executionId; }
    public String getPlanId() { return planId; }
    public String getPlanFingerprint() { return planFingerprint; }
    public String getScope() { return scope; }
    public int getProviderRevision() { return Integer.parseInt(providerRevision); }
    public String getEnvironmentFingerprint() { return environmentFingerprint; }
    public long getIssuedTick() { return issuedTick; }
    public DirectorRuntimeMode getMode() { return mode; }
    public boolean isTestAuthorization() { return testAuthorization; }
}
