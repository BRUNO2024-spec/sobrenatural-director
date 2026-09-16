package com.sobrenaturaldirector.shadow;

/** Offline-staging contract; real observations are never training data by default. */
public final class ShadowExperienceRecord {
    private final ShadowDecisionSnapshot snapshot; private final ShadowScoreResult score; private final ShadowOutcome outcome;
    public ShadowExperienceRecord(ShadowDecisionSnapshot snapshot,ShadowScoreResult score,ShadowOutcome outcome){if(snapshot==null||score==null||outcome==null)throw new IllegalArgumentException("incomplete experience");this.snapshot=snapshot;this.score=score;this.outcome=outcome;}
    public ShadowDecisionSnapshot getSnapshot(){return snapshot;} public ShadowScoreResult getScore(){return score;} public ShadowOutcome getOutcome(){return outcome;} public boolean isTrainingAllowed(){return false;}
}
