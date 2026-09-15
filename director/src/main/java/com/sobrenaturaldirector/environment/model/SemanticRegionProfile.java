package com.sobrenaturaldirector.environment.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class SemanticRegionProfile {
    private final EnvironmentEvidence evidence; private final EnvironmentClassification classification; private final int confidence, recentActivity;
    private final Set<EnvironmentSignal> tags; private final List<String> reasons;
    public SemanticRegionProfile(EnvironmentEvidence evidence,EnvironmentClassification classification,int confidence,int recentActivity,Set<EnvironmentSignal> tags,List<String> reasons){
        if(evidence==null||classification==null||confidence<0||confidence>100||recentActivity<0||recentActivity>100)throw new IllegalArgumentException("invalid region profile");
        this.evidence=evidence;this.classification=classification;this.confidence=confidence;this.recentActivity=recentActivity;this.tags=Collections.unmodifiableSet(tags==null?EnumSet.noneOf(EnvironmentSignal.class):EnumSet.copyOf(tags));this.reasons=Collections.unmodifiableList(new ArrayList<String>(reasons==null?Collections.<String>emptyList():reasons));
    }
    public EnvironmentEvidence getEvidence(){return evidence;} public EnvironmentClassification getClassification(){return classification;} public int getConfidence(){return confidence;} public int getRecentActivity(){return recentActivity;} public Set<EnvironmentSignal> getTags(){return tags;} public List<String> getReasons(){return reasons;}
    public boolean isProtectedPlayerArea(){return classification==EnvironmentClassification.ESTABLISHED_PLAYER_AREA&&confidence>=70;}
}
