package com.sobrenaturaldirector.shadow;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Telemetry result only; it cannot be converted into a DecisionResult. */
public final class ShadowScoreResult {
    private final ShadowDecisionSnapshot snapshot;
    private final Map<String,Double> scores;
    private final String shadowSelectedId;
    private final long latencyNanos;
    public ShadowScoreResult(ShadowDecisionSnapshot snapshot,Map<String,Double> scores,String selected,long latencyNanos){
        if(snapshot==null||scores==null||selected==null||latencyNanos<0)throw new IllegalArgumentException("invalid shadow result");
        this.snapshot=snapshot;this.scores=Collections.unmodifiableMap(new LinkedHashMap<String,Double>(scores));this.shadowSelectedId=selected;this.latencyNanos=latencyNanos;
    }
    public ShadowDecisionSnapshot getSnapshot(){return snapshot;} public Map<String,Double> getScores(){return scores;} public String getShadowSelectedId(){return shadowSelectedId;} public long getLatencyNanos(){return latencyNanos;}
}
