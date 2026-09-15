package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.decision.model.Intent;

public final class NarrativeIntentDecision {
    public enum Status { INTENT_SELECTED, NO_ACTION, NO_ELIGIBLE_OPPORTUNITY }
    private final Status status; private final Intent intent; private final NarrativeOpportunityEvaluation selected; private final double score,confidence,threshold; private final List<NarrativeOpportunityEvaluation> evaluations; private final List<String> reasons; private final String tieBreakKey;
    public NarrativeIntentDecision(Status status,Intent intent,NarrativeOpportunityEvaluation selected,double score,double confidence,double threshold,List<NarrativeOpportunityEvaluation> evaluations,List<String> reasons,String tieBreakKey){if(status==null||intent==null||!finite(score)||!finite(confidence)||!finite(threshold)||evaluations==null||reasons==null)throw new IllegalArgumentException("invalid intent decision");this.status=status;this.intent=intent;this.selected=selected;this.score=score;this.confidence=confidence;this.threshold=threshold;this.evaluations=Collections.unmodifiableList(new ArrayList<NarrativeOpportunityEvaluation>(evaluations));this.reasons=Collections.unmodifiableList(new ArrayList<String>(reasons));this.tieBreakKey=tieBreakKey==null?"":tieBreakKey;}
    private static boolean finite(double x){return !Double.isNaN(x)&&!Double.isInfinite(x);}
    public Status getStatus(){return status;} public Intent getIntent(){return intent;} public NarrativeOpportunityEvaluation getSelected(){return selected;} public double getScore(){return score;} public double getConfidence(){return confidence;} public double getThreshold(){return threshold;} public List<NarrativeOpportunityEvaluation> getEvaluations(){return evaluations;} public List<String> getReasons(){return reasons;} public String getTieBreakKey(){return tieBreakKey;} public boolean isNoAction(){return intent==Intent.NO_ACTION;}
}
