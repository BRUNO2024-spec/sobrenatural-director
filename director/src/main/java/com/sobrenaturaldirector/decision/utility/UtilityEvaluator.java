package com.sobrenaturaldirector.decision.utility;
import java.util.*; import com.sobrenaturaldirector.decision.model.*;
import com.sobrenaturaldirector.planning.environment.*;
/** Faithful port of the provisional additive score formulas in 0F engine.py. */
public final class UtilityEvaluator {
    public UtilityScoreBreakdown evaluate(DecisionContext c,CandidateAction a){double t=Math.max(c.getTension(),c.getPressure()), score=a.getBaseUtility(); List<FactorResult> f=new ArrayList<FactorResult>(); Map<String,Double> p=new LinkedHashMap<String,Double>();
        switch(a.getIntent()){
        case NO_ACTION:f.add(new FactorResult("relevance",.5,.5,1));f.add(new FactorResult("pacing_fit",.8,.8,1));p.put("fatigue",c.getFatigue()*.1);break;
        case AMBIENT_HINT:f.add(new FactorResult("relevance",t,t,1));f.add(new FactorResult("novelty",.8,.8,1));break;
        case MINOR_ENCOUNTER:f.add(new FactorResult("threat_fit",c.getCombatPower(),c.getCombatPower(),1));f.add(new FactorResult("novelty",.7,.7,1));break;
        case AMBUSH:f.add(new FactorResult("threat_fit",c.getCombatPower(),c.getCombatPower(),1));f.add(new FactorResult("pacing_fit",1-c.getRecoveryNeed(),1-c.getRecoveryNeed(),1));p.put("fatigue",c.getFatigue()*.3);break;
        case BOSS_ENCOUNTER:f.add(new FactorResult("threat_fit",c.getCombatPower(),c.getCombatPower(),1));f.add(new FactorResult("arc_fit",.6,.6,1));p.put("fatigue",c.getFatigue()*.5);break;
        case RECOVERY:f.add(new FactorResult("recovery_fit",c.getRecoveryNeed(),c.getRecoveryNeed(),1));break;
        default: break; }
        EnvironmentPlanningAssessment environment=new PlayerAreaSafetyPolicy().assess(c.getEnvironmentProfile(),a);score+=environment.getModifier();p.put("environment",(double)environment.getModifier());f.add(new FactorResult("environment",environment.getModifier(),environment.getModifier(),1));
        return new UtilityScoreBreakdown(score,f,p);
    }
}
