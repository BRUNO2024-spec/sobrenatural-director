package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import com.sobrenaturaldirector.decision.*;
import com.sobrenaturaldirector.decision.model.*;
import com.sobrenaturaldirector.planner.*;
import com.sobrenaturaldirector.planner.lifecycle.*;
import com.sobrenaturaldirector.planner.model.*;
import com.sobrenaturaldirector.planner.replanning.*;
import com.sobrenaturaldirector.planner.requirement.*;
import com.sobrenaturaldirector.planner.validation.*;

public class PlannerTest {
    private static DecisionResult decision(Intent intent) { DecisionContext c=DecisionContext.calm(10); CandidateAction a=new CandidateAction("selected:"+intent.name(),intent,"GENERIC",1,null,null,null,ActionSafety.NON_DESTRUCTIVE); return new DecisionEngine().decide(c,Collections.singletonList(a),88); }
    private static PlanningContext planning(Intent intent,boolean safe) { Map<String,Boolean> caps=new HashMap<String,Boolean>(); for(String x:Arrays.asList("LOCATION:UNDERGROUND","STRUCTURE_TYPE:CATACOMB","CONTENT_ROLE:COMMON_HOSTILE","CONTENT_ROLE:BOSS","CONTENT_ROLE:HIGH_VALUE_REWARD"))caps.put(x,true); Map<String,Double> budgets=new HashMap<String,Double>();budgets.put("threat",10.0);budgets.put("structure",10.0);return new PlanningContext(decision(intent),DecisionContext.calm(10),caps,safe,budgets,10); }
    @Test public void noActionCreatesNoPlan(){PlanningResult r=new DirectorPlanner().plan(planning(Intent.NO_ACTION,true));assertEquals(PlanningResultStatus.NO_PLAN_REQUIRED,r.getStatus());assertNull(r.getPlan());}
    @Test public void ambientAndMinorHaveSmallPlans(){assertTrue(new DirectorPlanner().plan(planning(Intent.AMBIENT_HINT,true)).getPlan().getSteps().size()<=2);assertTrue(new DirectorPlanner().plan(planning(Intent.MINOR_ENCOUNTER,true)).getPlan().getSteps().size()<=3);}
    @Test public void majorPlanIsSemanticBoundedAndValid(){PlanningResult r=new DirectorPlanner().plan(planning(Intent.BOSS_ENCOUNTER,true));assertEquals(PlanningResultStatus.SUCCESS,r.getStatus());assertTrue(r.getPlan().getSteps().size()<=DirectorPlan.MAX_STEPS);assertTrue(new PlanValidator().validate(r.getPlan()).isValid());assertTrue(r.getPlan().getRequirements().get(0).getType().contains("LOCATION"));}
    @Test public void missingCapabilityOrSafetyDefers(){PlanningContext c=planning(Intent.BOSS_ENCOUNTER,false);PlanningResult r=new DirectorPlanner().plan(c);assertEquals(PlanningResultStatus.DEFERRED,r.getStatus());assertEquals(PlanStatus.SUSPENDED,r.getPlan().getStatus());}
    @Test public void selectedIntentIsNeverReplaced(){for(Intent i:Intent.values())if(i!=Intent.NO_ACTION){PlanningResult r=new DirectorPlanner().plan(planning(i,true));assertEquals(i,r.getPlan().getIntent());}}
    @Test public void planAndStepCollectionsAreImmutable(){DirectorPlan p=new DirectorPlanner().plan(planning(Intent.BOSS_ENCOUNTER,true)).getPlan();try{p.getSteps().clear();fail();}catch(UnsupportedOperationException expected){}try{p.getRequirements().clear();fail();}catch(UnsupportedOperationException expected){}}
    @Test public void validatorDetectsMissingDependencyAndCycle(){PlanStep a=new PlanStep("a",PlanStepType.HIDE,Collections.singletonList("missing"),null,null,null,null,false);DirectorPlan p=new DirectorPlan("p","d",Intent.AMBIENT_HINT,"g",PlanHorizon.SHORT,PlanStatus.TENTATIVE,Collections.singletonList(a),Collections.<PlanRequirement>emptyList(),null,"f",null,null);assertFalse(new PlanValidator().validate(p).isValid());PlanStep x=new PlanStep("x",PlanStepType.HIDE,Collections.singletonList("y"),null,null,null,null,false);PlanStep y=new PlanStep("y",PlanStepType.HIDE,Collections.singletonList("x"),null,null,null,null,false);DirectorPlan cycle=new DirectorPlan("p2","d",Intent.AMBIENT_HINT,"g",PlanHorizon.SHORT,PlanStatus.TENTATIVE,Arrays.asList(x,y),Collections.<PlanRequirement>emptyList(),null,"f",null,null);assertFalse(new PlanValidator().validate(cycle).isValid());}
    @Test public void lifecycleIsFiniteAndTerminal(){PlanLifecycle l=new PlanLifecycle();assertEquals(PlanStatus.TENTATIVE,l.transition(PlanStatus.IDEA,PlanStatus.TENTATIVE));assertEquals(PlanStatus.RESERVED,l.transition(PlanStatus.TENTATIVE,PlanStatus.RESERVED));assertEquals(PlanStatus.COMPLETE,l.transition(PlanStatus.ACTIVE,PlanStatus.COMPLETE));try{l.transition(PlanStatus.COMPLETE,PlanStatus.ACTIVE);fail();}catch(IllegalStateException expected){}}
    @Test public void replanIsBounded(){DirectorPlan p=new DirectorPlanner().plan(planning(Intent.AMBIENT_HINT,true)).getPlan();Replanner r=new Replanner();PlanFailure f=new PlanFailure("f","step:hint","CONTENT_UNAVAILABLE",true,10);assertEquals(ReplanOutcome.SUSPEND,r.replan(p,f,0).getOutcome());assertEquals(ReplanOutcome.ABANDON,r.replan(p,f,2).getOutcome());}
    @Test public void catacombScenarioHasOrderedAbstractSteps(){DirectorPlan p=new DirectorPlanner().plan(planning(Intent.BOSS_ENCOUNTER,true)).getPlan();Map<String,Integer>at=new HashMap<String,Integer>();for(int i=0;i<p.getSteps().size();i++)at.put(p.getSteps().get(i).getStepId(),i);assertTrue(at.get("step:location")<at.get("step:structure"));assertTrue(at.get("step:structure")<at.get("step:population"));assertTrue(at.get("step:boss")<at.get("step:activate"));for(PlanRequirement r:p.getRequirements())assertFalse(r.getType().contains("woodpecker"));}
    @Test public void deterministicPlanAndTenThousandInputs(){DirectorPlanner p=new DirectorPlanner();for(int i=0;i<10000;i++){PlanningResult a=p.plan(planning(Intent.BOSS_ENCOUNTER,true)),b=p.plan(planning(Intent.BOSS_ENCOUNTER,true));assertEquals(a.getPlan().getPlanId(),b.getPlan().getPlanId());assertEquals(a.getPlan().getSteps().size(),b.getPlan().getSteps().size());}}
}
