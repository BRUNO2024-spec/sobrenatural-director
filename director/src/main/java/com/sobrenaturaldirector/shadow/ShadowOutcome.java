package com.sobrenaturaldirector.shadow;

/** Objective lifecycle facts only; deliberately contains no reward or quality label. */
public final class ShadowOutcome {
    public enum Status { COMPLETE, ABORTED, REPLANNED, SAFETY_REJECTED, TIMEOUT, INCOMPLETE }
    private final String decisionId; private final Status status; private final long closeTick; private final int replans; private final String reason;
    public ShadowOutcome(String decisionId,Status status,long closeTick,int replans,String reason){if(decisionId==null||status==null||closeTick<0||replans<0)throw new IllegalArgumentException("invalid outcome");this.decisionId=decisionId;this.status=status;this.closeTick=closeTick;this.replans=replans;this.reason=reason==null?"":reason;}
    public String getDecisionId(){return decisionId;} public Status getStatus(){return status;} public long getCloseTick(){return closeTick;} public int getReplans(){return replans;} public String getReason(){return reason;}
}
