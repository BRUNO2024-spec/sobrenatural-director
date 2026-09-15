package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Bounded, explainable attention result for one pacing scope. */
public final class ThreadAttentionDecision {
    public static final class Candidate { private final String threadId; private final double score; private final boolean relevant; private final List<String> reasons;
        Candidate(String id,double score,boolean relevant,List<String> reasons){threadId=id;this.score=score;this.relevant=relevant;this.reasons=Collections.unmodifiableList(new ArrayList<String>(reasons));}
        public String getThreadId(){return threadId;} public double getScore(){return score;} public boolean isRelevant(){return relevant;} public List<String> getReasons(){return reasons;}}
    private final List<Candidate> candidates; private final String selected;
    ThreadAttentionDecision(List<Candidate> values){candidates=Collections.unmodifiableList(new ArrayList<Candidate>(values));selected=candidates.isEmpty()?"":candidates.get(0).getThreadId();}
    public List<Candidate> getCandidates(){return candidates;} public String getSelectedThreadId(){return selected;}
}
