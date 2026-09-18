package com.sobrenaturaldirector.action;
public final class ActionPlanValidation { private final boolean valid;private final String reason;public ActionPlanValidation(boolean valid,String reason){this.valid=valid;this.reason=reason==null?"":reason;}public boolean isValid(){return valid;}public String getReason(){return reason;} }
