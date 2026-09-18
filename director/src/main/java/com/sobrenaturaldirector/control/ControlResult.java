package com.sobrenaturaldirector.control;
public final class ControlResult {
    private final ControlResultStatus status; private final String reason;
    public ControlResult(ControlResultStatus status,String reason){if(status==null)throw new IllegalArgumentException("status");this.status=status;this.reason=reason==null?"":reason;}
    public ControlResultStatus getStatus(){return status;} public String getReason(){return reason;}
    public boolean isTerminalSuccess(){return status==ControlResultStatus.APPLIED;}
}
