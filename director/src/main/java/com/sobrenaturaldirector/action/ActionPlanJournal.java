package com.sobrenaturaldirector.action;

import java.util.*;
import com.sobrenaturaldirector.control.ControlResultStatus;

/** Plan-level ledger; provider journals remain the mutation authority. */
public final class ActionPlanJournal {
    public static final class Entry {
        private final String stepId, actionId, binding, providerJournalReference, reason; private final ControlResultStatus result; private final ActionExecutionStatus compensation;
        public Entry(String stepId,String actionId,String binding,String providerJournalReference,ControlResultStatus result,ActionExecutionStatus compensation){this(stepId,actionId,binding,providerJournalReference,result,compensation,"");}
        public Entry(String stepId,String actionId,String binding,String providerJournalReference,ControlResultStatus result,ActionExecutionStatus compensation,String reason){this.stepId=stepId;this.actionId=actionId;this.binding=binding==null?"":binding;this.providerJournalReference=providerJournalReference==null?"":providerJournalReference;this.result=result;this.compensation=compensation;this.reason=reason==null?"":reason;}
        public String getStepId(){return stepId;}public String getActionId(){return actionId;}public String getBinding(){return binding;}public String getProviderJournalReference(){return providerJournalReference;}public ControlResultStatus getResult(){return result;}public ActionExecutionStatus getCompensation(){return compensation;}public String getReason(){return reason;}
    }
    private final String planId, planFingerprint; private final Map<String,Entry> entries; private ActionExecutionStatus status;
    public ActionPlanJournal(String planId,String planFingerprint){if(planId==null||planFingerprint==null)throw new IllegalArgumentException("journal identity");this.planId=planId;this.planFingerprint=planFingerprint;this.entries=new LinkedHashMap<String,Entry>();this.status=ActionExecutionStatus.NEW;}
    public synchronized boolean begin(){if(status!=ActionExecutionStatus.NEW)return false;status=ActionExecutionStatus.RUNNING;return true;}
    public synchronized void record(Entry entry){if(entry==null)throw new IllegalArgumentException("entry");entries.put(entry.getStepId(),entry);}
    public synchronized void status(ActionExecutionStatus value){if(value==null)throw new IllegalArgumentException("status");status=value;}
    public String getPlanId(){return planId;}public String getPlanFingerprint(){return planFingerprint;}public synchronized ActionExecutionStatus getStatus(){return status;}public synchronized Map<String,Entry> getEntries(){return Collections.unmodifiableMap(new LinkedHashMap<String,Entry>(entries));}
}
