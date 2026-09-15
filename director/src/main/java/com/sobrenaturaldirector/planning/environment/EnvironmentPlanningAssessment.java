package com.sobrenaturaldirector.planning.environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.environment.model.EnvironmentClassification;

public final class EnvironmentPlanningAssessment {
    private final EnvironmentActionImpact impact; private final EnvironmentClassification classification;
    private final int confidence, modifier; private final boolean allowed, hardRejected, perimeterPreferred;
    private final List<String> reasons;
    public EnvironmentPlanningAssessment(EnvironmentActionImpact impact,EnvironmentClassification classification,int confidence,int modifier,boolean allowed,boolean hardRejected,boolean perimeterPreferred,List<String> reasons){if(impact==null||classification==null||confidence<0||confidence>100||modifier<-60||modifier>10)throw new IllegalArgumentException("invalid environment assessment");this.impact=impact;this.classification=classification;this.confidence=confidence;this.modifier=modifier;this.allowed=allowed;this.hardRejected=hardRejected;this.perimeterPreferred=perimeterPreferred;this.reasons=Collections.unmodifiableList(new ArrayList<String>(reasons==null?Collections.<String>emptyList():reasons));}
    public EnvironmentActionImpact getImpact(){return impact;} public EnvironmentClassification getClassification(){return classification;} public int getConfidence(){return confidence;} public int getModifier(){return modifier;} public boolean isAllowed(){return allowed;} public boolean isHardRejected(){return hardRejected;} public boolean isPerimeterPreferred(){return perimeterPreferred;} public List<String> getReasons(){return reasons;}
}
