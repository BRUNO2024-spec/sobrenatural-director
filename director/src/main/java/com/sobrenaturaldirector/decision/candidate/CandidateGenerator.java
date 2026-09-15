package com.sobrenaturaldirector.decision.candidate;
import java.util.*; import com.sobrenaturaldirector.decision.model.*;
public final class CandidateGenerator {
    public static final int MAX_CANDIDATES=6;
    private static Map<String,String> req(String... values){Map<String,String> m=new LinkedHashMap<String,String>();for(int i=0;i+1<values.length;i+=2)m.put(values[i],values[i+1]);return m;}
    private static Map<String,Double> cost(Object... values){Map<String,Double> m=new LinkedHashMap<String,Double>();for(int i=0;i+1<values.length;i+=2)m.put((String)values[i],(Double)values[i+1]);return m;}
    private static CandidateAction a(String id,Intent i,double score,Map<String,String> req,Map<String,Double> cost,ActionSafety safety){return new CandidateAction(id,i,"GENERIC",score,req,Collections.<String,String>emptyMap(),cost,safety);}
    public List<CandidateAction> generate(DecisionContext c){
        List<CandidateAction> r=new ArrayList<CandidateAction>(); r.add(a("candidate:no_action",Intent.NO_ACTION,.52+c.getRecoveryNeed()*.18,Collections.<String,String>emptyMap(),Collections.<String,Double>emptyMap(),ActionSafety.NON_DESTRUCTIVE));
        double tension=Math.max(c.getTension(),c.getPressure());
        if(tension>.25)r.add(a("candidate:hint",Intent.AMBIENT_HINT,.42+tension*.35,Collections.<String,String>emptyMap(),Collections.<String,Double>emptyMap(),ActionSafety.NON_DESTRUCTIVE));
        if(c.getHealthRatio()>.2 && c.getProviders().get("HORROR")==ProviderStatus.AVAILABLE && c.getContentTags().contains("COMMON_THREAT"))r.add(a("candidate:minor",Intent.MINOR_ENCOUNTER,.35+c.getCombatPower()*.25+tension*.2,req("provider","HORROR","tags","COMMON_THREAT"),cost("threat",1.0,"entity",1.0),ActionSafety.SPAWN));
        if(tension>.58 && c.isUnderground() && c.getContentTags().contains("AMBUSHER") && c.getContentTags().contains("T3_HIGH") && c.getHealthRatio()>.35)r.add(a("candidate:ambush",Intent.AMBUSH,.38+tension*.45+c.getCombatPower()*.1,req("tags","AMBUSHER,T3_HIGH"),cost("threat",2.0,"entity",2.0),ActionSafety.SPAWN));
        if(tension>.8 && c.getCombatPower()>.7 && c.getLocationTags().contains("BOSS_ROOM") && c.getContentTags().contains("UNIQUE_BOSS") && c.getContentTags().contains("BOSS") && c.getEventConcurrency()>=1)r.add(a("candidate:boss",Intent.BOSS_ENCOUNTER,.45+tension*.3,req("uniqueClaim","UNIQUE_BOSS","tags","UNIQUE_BOSS,BOSS"),cost("threat",5.0,"entity",1.0,"event_concurrency",1.0),ActionSafety.MAJOR));
        if(c.getRecoveryNeed()>.45)r.add(a("candidate:recovery",Intent.RECOVERY,.45+c.getRecoveryNeed()*.4,Collections.<String,String>emptyMap(),Collections.<String,Double>emptyMap(),ActionSafety.NON_DESTRUCTIVE));
        return Collections.unmodifiableList(r);
    }
}
