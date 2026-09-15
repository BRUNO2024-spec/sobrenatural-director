package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.sobrenaturaldirector.decision.model.CandidateAction;
import com.sobrenaturaldirector.decision.model.Intent;
import com.sobrenaturaldirector.environment.model.EnvironmentClassification;
import com.sobrenaturaldirector.planning.environment.EnvironmentPlanningAssessment;
import com.sobrenaturaldirector.planning.environment.PlayerAreaSafetyPolicy;
import com.sobrenaturaldirector.situation.SituationGoal;
import com.sobrenaturaldirector.situation.SituationMemory;
import com.sobrenaturaldirector.situation.ThreatNarrativeAssessment;

/** Pure, bounded intent arbitration. It never touches Minecraft or provider state. */
public final class NarrativeIntentArbiter {
    public static final int MAX_OPPORTUNITIES=16;
    public static final double NO_ACTION_BASELINE=.35;
    public NarrativeIntentDecision arbitrate(NarrativeArbitrationContext context,List<NarrativeOpportunity> input){
        if(context==null||input==null)throw new IllegalArgumentException("invalid arbitration input");if(input.size()>MAX_OPPORTUNITIES)throw new IllegalArgumentException("opportunity limit exceeded");
        List<NarrativeOpportunity> opportunities=new ArrayList<NarrativeOpportunity>(input);Collections.sort(opportunities,new Comparator<NarrativeOpportunity>(){public int compare(NarrativeOpportunity a,NarrativeOpportunity b){return a.getId().compareTo(b.getId());}});Set<String> ids=new HashSet<String>();for(NarrativeOpportunity o:opportunities)if(!ids.add(o.getId()))throw new IllegalArgumentException("duplicate opportunity id");
        List<NarrativeOpportunityEvaluation> evaluations=new ArrayList<NarrativeOpportunityEvaluation>();for(NarrativeOpportunity o:opportunities)evaluations.add(evaluate(context,o));
        double adaptiveBoost=context.getAdaptivePacing()==null?0:context.getAdaptivePacing().getNoActionModifier();double threshold=Math.min(1,NO_ACTION_BASELINE+context.getDecision().getRecoveryNeed()*.25+(isHardRecovery(context.getPacing())?.15:0)+adaptiveBoost);NarrativeOpportunity no=new NarrativeOpportunity("opportunity:no_action",Intent.NO_ACTION,threshold,1,Collections.<String>emptySet(),Collections.singletonList("NO_ACTION_BASELINE"),false,null);NarrativeOpportunityEvaluation noEval=new NarrativeOpportunityEvaluation(no,true,threshold,1,Collections.singletonMap("noActionBaseline",threshold),Collections.singletonList("NO_ACTION_ALWAYS_ELIGIBLE"));evaluations.add(noEval);
        NarrativeOpportunityEvaluation winner=noEval;int eligibleOpportunities=0;for(NarrativeOpportunityEvaluation e:evaluations)if(e!=noEval&&e.isEligible())eligibleOpportunities++;for(NarrativeOpportunityEvaluation e:evaluations)if(e.isEligible()&&(e.getScore()>winner.getScore()||(Double.compare(e.getScore(),winner.getScore())==0&&e.getOpportunity().getId().compareTo(winner.getOpportunity().getId())<0)))winner=e;
        if(winner==noEval){List<String> reasons=eligibleOpportunities==0&&!opportunities.isEmpty()?Collections.singletonList("NO_ELIGIBLE_OPPORTUNITY"):Collections.singletonList("NO_ACTION_THRESHOLD");return new NarrativeIntentDecision(eligibleOpportunities==0&&!opportunities.isEmpty()?NarrativeIntentDecision.Status.NO_ELIGIBLE_OPPORTUNITY:NarrativeIntentDecision.Status.NO_ACTION,Intent.NO_ACTION,noEval,winner.getScore(),1,threshold,evaluations,reasons,winner.getOpportunity().getId());}
        return new NarrativeIntentDecision(NarrativeIntentDecision.Status.INTENT_SELECTED,winner.getOpportunity().getIntent(),winner,winner.getScore(),winner.getConfidence(),threshold,evaluations,Collections.singletonList("OPPORTUNITY_ABOVE_NO_ACTION"),winner.getOpportunity().getId());
    }
    private NarrativeOpportunityEvaluation evaluate(NarrativeArbitrationContext c,NarrativeOpportunity o){List<String> reasons=new ArrayList<String>(o.getReasons());Map<String,Double> parts=new LinkedHashMap<String,Double>();boolean eligible=true;for(String capability:o.getRequiredCapabilities())if(!c.getCapabilities().contains(capability)){eligible=false;reasons.add("CAPABILITY_UNAVAILABLE:"+capability);}
        if(c.hasActiveNarrative()&&isThreat(o.getIntent())&&!o.isContinuation()){eligible=false;reasons.add("EXISTING_NARRATIVE_ACTIVE");}
        ThreatNarrativeAssessment pacing=c.getPacing();if(isThreat(o.getIntent())&&pacing!=null&&!pacing.isEligible()&&pacing.isHardCooldown()){eligible=false;reasons.add("THREAT_REJECTED_HARD_COOLDOWN");}
        EnvironmentPlanningAssessment env=o.getAction()==null?null:new PlayerAreaSafetyPolicy().assess(c.getDecision().getEnvironmentProfile(),o.getAction());if(env!=null){parts.put("environment",env.getModifier()/100.0);reasons.addAll(env.getReasons());if(!env.isAllowed())eligible=false;}
        int repeated=repetition(c.getMemory(),o.getIntent(),c.getDecision().getTick());double repetitionPenalty=Math.min(.2,repeated*.05);double pacingValue=pacing==null?0:pacing.getScoreModifier()/100.0;double adaptiveValue=adaptiveModifier(c.getAdaptivePacing(),o.getIntent());double continuity=o.isContinuation()?.15:0;double score=clamp(o.getUtility()*.55+o.getConfidence()*.15+continuity+partsValue(parts)+pacingValue*.1+adaptiveValue-repetitionPenalty);parts.put("baseUtility",o.getUtility()*.55);parts.put("confidence",o.getConfidence()*.15);parts.put("continuity",continuity);parts.put("pacing",pacingValue*.1);parts.put("adaptivePacing",adaptiveValue);parts.put("repetition",-repetitionPenalty);if(repeated>0)reasons.add("REPETITION_PENALTY");if(adaptiveValue<0)reasons.add("ADAPTIVE_PACING_DEFER");if(score<.35)reasons.add("NO_ACTION_THRESHOLD");return new NarrativeOpportunityEvaluation(o,eligible,score,o.getConfidence(),parts,reasons);}
     private static double adaptiveModifier(PacingAssessment assessment,Intent intent){if(assessment==null)return 0;NarrativeIntensity intensity=AdaptivePacingDirector.intensity(intent);if(intensity.getRank()>Math.round(assessment.getEscalationAllowance()*4.0))return -Math.min(.35,intensity.getRank()*.08);return assessment.getRecoveryNeed()>0&&intensity.getRank()>=NarrativeIntensity.HIGH.getRank()?-Math.min(.2,assessment.getRecoveryNeed()*.2):0;}
    private static double partsValue(Map<String,Double> parts){Double value=parts.get("environment");return value==null?0:value*.2;}
    private static int repetition(SituationMemory m,Intent i,long tick){SituationGoal goal=i==Intent.MYSTERY_SETUP?SituationGoal.INVESTIGATION:i==Intent.LOOT_DISCOVERY?SituationGoal.DISCOVERY:i==Intent.AMBIENT_HINT?SituationGoal.AMBIENT_EVENT:null;return goal==null?0:m.recentGoalCount(goal,tick,24000L);}
    private static boolean isThreat(Intent i){return i==Intent.MINOR_ENCOUNTER||i==Intent.AMBUSH||i==Intent.BOSS_ENCOUNTER||i==Intent.STALKING_PRESSURE||i==Intent.HORROR_REVEAL;}
    private static boolean isHardRecovery(ThreatNarrativeAssessment a){return a!=null&&a.isRecoveryActive();}
    private static double clamp(double x){return Math.max(0,Math.min(1,x));}
}
