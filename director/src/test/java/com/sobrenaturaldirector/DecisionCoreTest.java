package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import com.sobrenaturaldirector.decision.*;
import com.sobrenaturaldirector.decision.candidate.CandidateGenerator;
import com.sobrenaturaldirector.decision.model.*;
import com.sobrenaturaldirector.decision.random.DeterministicRng;
import com.sobrenaturaldirector.decision.trace.CandidateTrace;

public class DecisionCoreTest {
    private static Map<String,Double> m(String k,double v){Map<String,Double>x=new HashMap<String,Double>();x.put(k,v);return x;}
    private static CandidateAction candidate(String id,Intent i,double score,ActionSafety safety){return new CandidateAction(id,i,"GENERIC",score,null,null,null,safety);}
    private static DecisionContext context(double tension,double recovery,boolean safe){return new DecisionContext(10,100,tension,tension,.2,recovery,1,.5,.2,true,false,safe,1,m("threat",10),Collections.singletonMap("HORROR",ProviderStatus.AVAILABLE),new HashSet<String>(Arrays.asList("COMMON_THREAT","AMBUSHER","T3_HIGH","UNIQUE_BOSS","BOSS")),Collections.singleton("BOSS_ROOM"),Collections.<String>emptySet(),Collections.<String,Long>emptyMap());}

    @Test public void noActionIsFirstClassAndStable(){DecisionResult r=new DecisionEngine().decide(DecisionContext.calm(1),88);assertEquals("candidate:no_action",r.getSelected().getCandidateId());}
    @Test public void noActionCanRepeat(){DecisionEngine e=new DecisionEngine();for(int i=0;i<20;i++)assertEquals(Intent.NO_ACTION,e.decide(DecisionContext.calm(i),88).getSelected().getIntent());}
    @Test public void boundedAndDeterministicGeneration(){List<CandidateAction> c=new CandidateGenerator().generate(context(.9,.1,true));assertTrue(c.size()<=CandidateGenerator.MAX_CANDIDATES);for(int i=1;i<c.size();i++)assertNotEquals(c.get(i-1).getCandidateId(),c.get(i).getCandidateId());}
    @Test public void hardRejectedHighScoreCannotWin(){DecisionContext c=context(0,0,false);List<CandidateAction>x=Arrays.asList(candidate("a",Intent.BOSS_ENCOUNTER,999999,ActionSafety.MAJOR),candidate("candidate:no_action",Intent.NO_ACTION,.1,ActionSafety.NON_DESTRUCTIVE));assertEquals(Intent.NO_ACTION,new DecisionEngine().decide(c,x,1).getSelected().getIntent());}
    @Test public void negativeBudgetAndInvalidContextRejected(){try{new DecisionContext(0,0,0,0,0,0,1,.5,0,false,false,true,0,m("threat",-1),null,null,null,null,null);fail();}catch(IllegalArgumentException expected){} try{new DecisionContext(0,0,Double.NaN,0,0,0,1,.5,0,false,false,true,0,null,null,null,null,null,null);fail();}catch(IllegalArgumentException expected){}}
    @Test public void duplicateCandidatesRejected(){try{new DecisionEngine().decide(DecisionContext.calm(1),Arrays.asList(candidate("x",Intent.NO_ACTION,0,ActionSafety.NON_DESTRUCTIVE),candidate("x",Intent.RECOVERY,0,ActionSafety.NON_DESTRUCTIVE)),1);fail();}catch(IllegalArgumentException expected){}}
    @Test public void cooldownUniqueAndProviderConstraintsReject(){DecisionContext c=new DecisionContext(10,10,0,0,0,0,1,.5,0,false,false,false,1,m("threat",10),Collections.singletonMap("HORROR",ProviderStatus.MISSING),Collections.<String>emptySet(),Collections.<String>emptySet(),Collections.singleton("UNIQUE_BOSS"),Collections.singletonMap("RECOVERY",20L));List<CandidateAction>x=Arrays.asList(candidate("candidate:no_action",Intent.NO_ACTION,0,ActionSafety.NON_DESTRUCTIVE),candidate("boss",Intent.BOSS_ENCOUNTER,999,ActionSafety.MAJOR),candidate("recovery",Intent.RECOVERY,999,ActionSafety.NON_DESTRUCTIVE));assertEquals(Intent.NO_ACTION,new DecisionEngine().decide(c,x,1).getSelected().getIntent());}
    @Test public void immutableInputsAndOutputs(){DecisionContext c=DecisionContext.calm(1);try{c.getBudgets().put("x",1.0);fail();}catch(UnsupportedOperationException expected){}DecisionResult r=new DecisionEngine().decide(c,1);try{r.getTrace().getCandidates().clear();fail();}catch(UnsupportedOperationException expected){}}
    @Test public void exactTieIsSeededAndStreamsAreIsolated(){DeterministicRng a=DeterministicRng.stream(88,"decision","10"),b=DeterministicRng.stream(88,"decision","10"),other=DeterministicRng.stream(88,"tie_break","10");assertEquals(a.getSeed(),b.getSeed());assertEquals(a.nextInt(100),b.nextInt(100));assertNotEquals(a.getSeed(),other.getSeed());}
    @Test public void traceContainsEveryCandidateAndSelectedScore(){DecisionResult r=new DecisionEngine().decide(context(.2,.1,true),88);int selected=0;for(CandidateTrace t:r.getTrace().getCandidates()){assertNotNull(t.getScore());if(t.isSelected())selected++;}assertEquals(1,selected);assertEquals(r.getSelected().getCandidateId(),r.getTrace().getSelectedId());}
    @Test public void propertySweepTenThousandContextsAndThousandSeeds(){DecisionEngine e=new DecisionEngine();for(int i=0;i<10000;i++){double v=(i%1000)/1000.0;DecisionResult r=e.decide(context(v,(i%700)/1000.0,true),i);assertNotNull(r.getSelected());assertFalse(Double.isNaN(r.getTrace().getCandidates().get(0).getScore().getFinalScore()));}for(int seed=0;seed<1000;seed++){assertEquals(e.decide(context(.1,.1,true),seed).getTrace().getSelectedId(),e.decide(context(.1,.1,true),seed).getTrace().getSelectedId());assertEquals(e.decide(context(.1,.1,true),seed).getTrace().getSelectedId(),"candidate:no_action");}}
}
