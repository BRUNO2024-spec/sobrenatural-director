package com.sobrenaturaldirector.narrative;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NarrativeOpportunityEvaluation {
    private final NarrativeOpportunity opportunity; private final boolean eligible; private final double score,confidence; private final Map<String,Double> contributions; private final java.util.List<String> reasons;
    public NarrativeOpportunityEvaluation(NarrativeOpportunity opportunity,boolean eligible,double score,double confidence,Map<String,Double> contributions,java.util.List<String> reasons){this.opportunity=opportunity;this.eligible=eligible;this.score=score;this.confidence=confidence;this.contributions=Collections.unmodifiableMap(new LinkedHashMap<String,Double>(contributions==null?Collections.<String,Double>emptyMap():contributions));this.reasons=Collections.unmodifiableList(new java.util.ArrayList<String>(reasons==null?java.util.Collections.<String>emptyList():reasons));}
    public NarrativeOpportunity getOpportunity(){return opportunity;} public boolean isEligible(){return eligible;} public double getScore(){return score;} public double getConfidence(){return confidence;} public Map<String,Double> getContributions(){return contributions;} public java.util.List<String> getReasons(){return reasons;}
}
