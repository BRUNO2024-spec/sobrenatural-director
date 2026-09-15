package com.sobrenaturaldirector.execution;

import com.sobrenaturaldirector.domain.StableId;

/** Provider-neutral physical result; confirmation is separate from executor return. */
public final class ExecutionOutcome {
    public enum Status { ATTEMPTED, APPLIED, CONFIRMED, PARTIAL, FAILED, ABORTED, COMPENSATED, PENDING_CONFIRMATION }
    private final String outcomeId, executionId, planId, providerId, physicalIdentity, reason;
    private final Status status;
    private final long tick;
    private final boolean feedbackApplied;
    public ExecutionOutcome(String outcomeId, String executionId, String planId, String providerId, String physicalIdentity,
            Status status, long tick, String reason, boolean feedbackApplied) {
        this.outcomeId=StableId.require(outcomeId,"outcomeId"); this.executionId=StableId.require(executionId,"executionId");
        this.planId=StableId.require(planId,"planId"); this.providerId=StableId.require(providerId,"providerId");
        this.physicalIdentity=physicalIdentity==null?"":physicalIdentity; this.status=status; this.tick=tick;
        this.reason=reason==null?"":reason; this.feedbackApplied=feedbackApplied;
        if (status==null || tick<0) throw new IllegalArgumentException("invalid outcome");
    }
    public String getOutcomeId(){return outcomeId;} public String getExecutionId(){return executionId;} public String getPlanId(){return planId;}
    public String getProviderId(){return providerId;} public String getPhysicalIdentity(){return physicalIdentity;} public Status getStatus(){return status;}
    public long getTick(){return tick;} public String getReason(){return reason;} public boolean isFeedbackApplied(){return feedbackApplied;}
    public ExecutionOutcome withFeedbackApplied(){return new ExecutionOutcome(outcomeId,executionId,planId,providerId,physicalIdentity,status,tick,reason,true);}
}
