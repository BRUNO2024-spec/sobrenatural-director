package com.sobrenaturaldirector.action;

import java.util.*;

/** Bounded plan-level journal metadata; provider journals remain authoritative for mutations. */
public final class ActionExecutionJournal {
    private final String planId,planFingerprint; private final Map<String,ActionExecutionStatus> nodes;
    public ActionExecutionJournal(String planId,String planFingerprint,Map<String,ActionExecutionStatus> nodes){if(planId==null||planFingerprint==null)throw new IllegalArgumentException("journal identity");this.planId=planId;this.planFingerprint=planFingerprint;TreeMap<String,ActionExecutionStatus> copy=new TreeMap<String,ActionExecutionStatus>();if(nodes!=null)copy.putAll(nodes);this.nodes=Collections.unmodifiableMap(copy);}
    public String getPlanId(){return planId;}public String getPlanFingerprint(){return planFingerprint;}public Map<String,ActionExecutionStatus> getNodes(){return nodes;}
}
