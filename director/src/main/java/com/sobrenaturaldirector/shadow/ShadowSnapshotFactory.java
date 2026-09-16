package com.sobrenaturaldirector.shadow;

import java.util.ArrayList;
import java.util.List;
import com.sobrenaturaldirector.decision.model.CandidateAction;
import com.sobrenaturaldirector.decision.model.DecisionContext;

/** Encodes only immutable decision inputs; unavailable temporal fields remain explicit zeroes. */
public final class ShadowSnapshotFactory {
    private ShadowSnapshotFactory() { }
    public static ShadowDecisionSnapshot create(DecisionContext context,List<CandidateAction> candidates,String selected,long tick,String fingerprint) {
        List<ShadowCandidate> values=new ArrayList<ShadowCandidate>();
        for(CandidateAction c:candidates) values.add(new ShadowCandidate(c.getCandidateId(),c.getIntent().name(),numeric(context,c),new int[8]));
        return new ShadowDecisionSnapshot("decision:"+tick+":"+fingerprint,"DIRECTOR_EXPERIENCE_FEATURES_V4",tick,fingerprint,selected,values);
    }
    private static double[] numeric(DecisionContext c,CandidateAction a){return new double[]{c.getTension(),c.getPressure(),c.getFatigue(),c.getRecoveryNeed(),c.getHealthRatio(),c.getCombatPower(),c.getIsolation(),c.isUnderground()?1:0,c.isObservingSite()?1:0,c.isSafetyKnown()?1:0,c.getEventConcurrency(),0,c.getCooldownUntil().isEmpty()?0:1,0,0,0,0,0,a.getBaseUtility()/100.0};}
}
