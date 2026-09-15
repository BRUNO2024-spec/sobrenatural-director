package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable, explainable result of one bounded pacing assessment. */
public final class PacingAssessment {
    private final String scope, fingerprint;
    private final PacingMode mode;
    private final double recoveryNeed, escalationAllowance, noActionModifier, attention;
    private final List<String> reasons;
    public PacingAssessment(String scope, PacingMode mode, double recoveryNeed, double escalationAllowance,
            double noActionModifier, double attention, List<String> reasons, String fingerprint) {
        if(scope==null||scope.length()==0||mode==null||!valid(recoveryNeed)||!valid(escalationAllowance)||!valid(noActionModifier)||!valid(attention))throw new IllegalArgumentException("invalid pacing assessment");
        this.scope=scope;this.mode=mode;this.recoveryNeed=recoveryNeed;this.escalationAllowance=escalationAllowance;this.noActionModifier=noActionModifier;this.attention=attention;this.reasons=Collections.unmodifiableList(new ArrayList<String>(reasons==null?Collections.<String>emptyList():reasons));this.fingerprint=fingerprint==null?"":fingerprint;
    }
    private static boolean valid(double v){return !Double.isNaN(v)&&!Double.isInfinite(v)&&v>=0&&v<=1;}
    public String getScope(){return scope;}public PacingMode getMode(){return mode;}public double getRecoveryNeed(){return recoveryNeed;}public double getEscalationAllowance(){return escalationAllowance;}public double getNoActionModifier(){return noActionModifier;}public double getAttention(){return attention;}public List<String> getReasons(){return reasons;}public String getFingerprint(){return fingerprint;}
    public boolean allows(NarrativeIntensity intensity){return intensity==null||intensity.getRank()<=Math.round(escalationAllowance*4.0);}
}
