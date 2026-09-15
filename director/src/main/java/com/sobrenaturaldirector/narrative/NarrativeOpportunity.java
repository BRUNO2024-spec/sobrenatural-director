package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.sobrenaturaldirector.decision.model.CandidateAction;
import com.sobrenaturaldirector.decision.model.Intent;

/** Intent-level opportunity, distinct from a provider plan or execution request. */
public final class NarrativeOpportunity {
    public static final int MAX_REASONS=12;
    private final String id; private final Intent intent; private final double utility,confidence; private final Set<String> capabilities; private final List<String> reasons; private final boolean continuation; private final CandidateAction action;
    public NarrativeOpportunity(String id,Intent intent,double utility,double confidence,Set<String> capabilities,List<String> reasons,boolean continuation,CandidateAction action){if(id==null||id.length()==0||intent==null||!finite(utility)||utility<0||utility>1||!finite(confidence)||confidence<0||confidence>1||reasons!=null&&reasons.size()>MAX_REASONS)throw new IllegalArgumentException("invalid narrative opportunity");this.id=id;this.intent=intent;this.utility=utility;this.confidence=confidence;this.capabilities=Collections.unmodifiableSet(new LinkedHashSet<String>(capabilities==null?Collections.<String>emptySet():capabilities));this.reasons=Collections.unmodifiableList(new ArrayList<String>(reasons==null?Collections.<String>emptyList():reasons));this.continuation=continuation;this.action=action;}
    private static boolean finite(double x){return !Double.isNaN(x)&&!Double.isInfinite(x);}
    public String getId(){return id;} public Intent getIntent(){return intent;} public double getUtility(){return utility;} public double getConfidence(){return confidence;} public Set<String> getRequiredCapabilities(){return capabilities;} public List<String> getReasons(){return reasons;} public boolean isContinuation(){return continuation;} public CandidateAction getAction(){return action;}
}
