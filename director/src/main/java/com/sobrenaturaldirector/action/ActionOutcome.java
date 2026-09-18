package com.sobrenaturaldirector.action;

/** Factual semantic outcome, separate from low-level ControlResult. */
public final class ActionOutcome {
    private final String planId; private final ActionExecutionStatus status; private final int completedSteps, failedSteps, compensatedSteps; private final String reason;
    public ActionOutcome(String planId,ActionExecutionStatus status,int completedSteps,int failedSteps,String reason){this(planId,status,completedSteps,failedSteps,0,reason);}
    public ActionOutcome(String planId,ActionExecutionStatus status,int completedSteps,int failedSteps,int compensatedSteps,String reason){if(planId==null||status==null||completedSteps<0||failedSteps<0||compensatedSteps<0)throw new IllegalArgumentException("invalid action outcome");this.planId=planId;this.status=status;this.completedSteps=completedSteps;this.failedSteps=failedSteps;this.compensatedSteps=compensatedSteps;this.reason=reason==null?"":reason;}
    public String getPlanId(){return planId;}public ActionExecutionStatus getStatus(){return status;}public int getCompletedSteps(){return completedSteps;}public int getFailedSteps(){return failedSteps;}public int getCompensatedSteps(){return compensatedSteps;}public String getReason(){return reason;}
}
