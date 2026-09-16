package com.sobrenaturaldirector.shadow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable pre-decision copy. No World, Entity, provider, or server reference is allowed. */
public final class ShadowDecisionSnapshot {
    private final String decisionId, schema, contextFingerprint, heuristicCandidateId;
    private final long tick;
    private final List<ShadowCandidate> candidates;
    public ShadowDecisionSnapshot(String decisionId,String schema,long tick,String contextFingerprint,String heuristicCandidateId,List<ShadowCandidate> candidates){
        if(decisionId==null||schema==null||contextFingerprint==null||heuristicCandidateId==null||candidates==null||candidates.isEmpty())throw new IllegalArgumentException("invalid shadow snapshot");
        this.decisionId=decisionId;this.schema=schema;this.tick=tick;this.contextFingerprint=contextFingerprint;this.heuristicCandidateId=heuristicCandidateId;this.candidates=Collections.unmodifiableList(new ArrayList<ShadowCandidate>(candidates));
    }
    public String getDecisionId(){return decisionId;} public String getSchema(){return schema;} public long getTick(){return tick;} public String getContextFingerprint(){return contextFingerprint;} public String getHeuristicCandidateId(){return heuristicCandidateId;} public List<ShadowCandidate> getCandidates(){return candidates;}
}
