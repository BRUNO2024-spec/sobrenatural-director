package com.sobrenaturaldirector.action;
public final class ActionAvailability {
    private final ActionAvailabilityStatus status; private final String reason;
    public ActionAvailability(ActionAvailabilityStatus status,String reason){if(status==null)throw new IllegalArgumentException("status");this.status=status;this.reason=reason==null?"":reason;}
    public ActionAvailabilityStatus getStatus(){return status;} public String getReason(){return reason;} public boolean isAvailable(){return status==ActionAvailabilityStatus.AVAILABLE;}
}
