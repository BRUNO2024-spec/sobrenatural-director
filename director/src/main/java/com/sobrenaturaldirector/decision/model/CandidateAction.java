package com.sobrenaturaldirector.decision.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CandidateAction {
    private final String candidateId; private final Intent intent; private final String theme;
    private final double baseUtility; private final Map<String,String> requirements, metadata; private final Map<String,Double> estimatedCosts;
    private final ActionSafety safety;
    public CandidateAction(String candidateId, Intent intent, String theme, double baseUtility, Map<String,String> requirements, Map<String,String> metadata, Map<String,Double> estimatedCosts, ActionSafety safety) {
        if (candidateId == null || candidateId.length() == 0 || intent == null || Double.isNaN(baseUtility) || Double.isInfinite(baseUtility)) throw new IllegalArgumentException("invalid candidate");
        this.candidateId=candidateId; this.intent=intent; this.theme=theme == null ? "GENERIC" : theme; this.baseUtility=baseUtility; this.requirements=map(requirements); this.metadata=map(metadata); this.estimatedCosts=costs(estimatedCosts); this.safety=safety == null ? ActionSafety.NON_DESTRUCTIVE : safety;
    }
    private static Map<String,String> map(Map<String,String> m){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(m == null ? Collections.<String,String>emptyMap() : m));}
    private static Map<String,Double> costs(Map<String,Double> m){Map<String,Double> r=new LinkedHashMap<String,Double>(); if(m!=null)for(Map.Entry<String,Double> e:m.entrySet()){if(e.getValue()==null||Double.isNaN(e.getValue())||Double.isInfinite(e.getValue())||e.getValue()<0)throw new IllegalArgumentException("invalid cost");r.put(e.getKey(),e.getValue());}return Collections.unmodifiableMap(r);}
    public String getCandidateId(){return candidateId;} public Intent getIntent(){return intent;} public String getTheme(){return theme;} public double getBaseUtility(){return baseUtility;} public Map<String,String> getRequirements(){return requirements;} public Map<String,String> getMetadata(){return metadata;} public Map<String,Double> getEstimatedCosts(){return estimatedCosts;} public ActionSafety getSafety(){return safety;}
}
