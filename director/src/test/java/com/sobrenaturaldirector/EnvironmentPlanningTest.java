package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;
import com.sobrenaturaldirector.decision.DecisionEngine;
import com.sobrenaturaldirector.decision.model.*;
import com.sobrenaturaldirector.environment.EnvironmentClassifier;
import com.sobrenaturaldirector.environment.model.*;
import com.sobrenaturaldirector.planning.environment.*;
import org.junit.Test;

public class EnvironmentPlanningTest {
    private static SemanticRegionProfile profile(EnvironmentClassification type){
        Map<EnvironmentSignal,Integer> c=new EnumMap<EnvironmentSignal,Integer>(EnvironmentSignal.class);
        if(type==EnvironmentClassification.TEMPORARY_CAMP){c.put(EnvironmentSignal.LIGHT_SOURCE,2);c.put(EnvironmentSignal.STORAGE,1);c.put(EnvironmentSignal.CRAFTING,1);c.put(EnvironmentSignal.SLEEPING,1);}
        if(type==EnvironmentClassification.ESTABLISHED_PLAYER_AREA){c.put(EnvironmentSignal.LIGHT_SOURCE,2);c.put(EnvironmentSignal.STORAGE,3);c.put(EnvironmentSignal.CRAFTING,3);c.put(EnvironmentSignal.SMELTING,3);c.put(EnvironmentSignal.FORTIFICATION,8);c.put(EnvironmentSignal.PLAYER_MODIFICATION,35);c.put(EnvironmentSignal.SLEEPING,3);}
        if(type==EnvironmentClassification.PLAYER_MODIFIED_AREA)c.put(EnvironmentSignal.FORTIFICATION,11);
        EnvironmentEvidence e=new EnvironmentEvidence(new RegionKey(0,1,1),100,c,512,16,1,type==EnvironmentClassification.ESTABLISHED_PLAYER_AREA?100:0,false,false);
        return new EnvironmentClassifier().classify(e);
    }
    private static CandidateAction action(String id,Intent intent,ActionSafety safety){return new CandidateAction(id,intent,"TEST",1,null,null,null,safety);}
    private static CandidateAction action(String id,Intent intent,ActionSafety safety,String relation){Map<String,String> m=new HashMap<String,String>();m.put("environmentRelation",relation);return new CandidateAction(id,intent,"TEST",1,null,m,null,safety);}
    @Test public void missingProfileIsSafeForAmbientButCautiousForDestructive(){PlayerAreaSafetyPolicy p=new PlayerAreaSafetyPolicy();assertTrue(p.assess(null,action("a",Intent.AMBIENT_HINT,ActionSafety.NON_DESTRUCTIVE)).isAllowed());assertTrue(p.assess(null,action("b",Intent.BOSS_ENCOUNTER,ActionSafety.MAJOR)).isHardRejected());}
    @Test public void wildernessDoesNotRejectAndHasBoundedPreference(){EnvironmentPlanningAssessment a=new PlayerAreaSafetyPolicy().assess(profile(EnvironmentClassification.WILDERNESS),action("a",Intent.AMBIENT_HINT,ActionSafety.NON_DESTRUCTIVE));assertTrue(a.isAllowed());assertEquals(5,a.getModifier());}
    @Test public void campIsIntermediate(){PlayerAreaSafetyPolicy p=new PlayerAreaSafetyPolicy();assertTrue(p.assess(profile(EnvironmentClassification.TEMPORARY_CAMP),action("a",Intent.AMBIENT_HINT,ActionSafety.NON_DESTRUCTIVE)).isAllowed());assertEquals(-12,p.assess(profile(EnvironmentClassification.TEMPORARY_CAMP),action("b",Intent.AMBUSH,ActionSafety.SPAWN)).getModifier());assertTrue(p.assess(profile(EnvironmentClassification.TEMPORARY_CAMP),action("c",Intent.BOSS_ENCOUNTER,ActionSafety.MAJOR)).getModifier()>-60);}
    @Test public void establishedBaseRejectsHighImpactButAllowsClue(){PlayerAreaSafetyPolicy p=new PlayerAreaSafetyPolicy();assertTrue(p.assess(profile(EnvironmentClassification.ESTABLISHED_PLAYER_AREA),action("a",Intent.BOSS_ENCOUNTER,ActionSafety.MAJOR)).isHardRejected());EnvironmentPlanningAssessment clue=p.assess(profile(EnvironmentClassification.ESTABLISHED_PLAYER_AREA),action("b",Intent.AMBIENT_HINT,ActionSafety.NON_DESTRUCTIVE));assertTrue(clue.isAllowed());assertTrue(clue.getReasons().contains("ESTABLISHED_AREA_LOW_IMPACT_ALLOWED"));}
    @Test public void fortifiedOnlyIsNotEstablishedPolicy(){EnvironmentPlanningAssessment a=new PlayerAreaSafetyPolicy().assess(profile(EnvironmentClassification.PLAYER_MODIFIED_AREA),action("a",Intent.BOSS_ENCOUNTER,ActionSafety.MAJOR));assertFalse(a.isHardRejected());assertEquals(-25,a.getModifier());}
    @Test public void perimeterIsPreferredWithoutInsideRejection(){PlayerAreaSafetyPolicy p=new PlayerAreaSafetyPolicy();EnvironmentPlanningAssessment inside=p.assess(profile(EnvironmentClassification.ESTABLISHED_PLAYER_AREA),action("a",Intent.AMBUSH,ActionSafety.SPAWN));EnvironmentPlanningAssessment edge=p.assess(profile(EnvironmentClassification.ESTABLISHED_PLAYER_AREA),action("b",Intent.AMBUSH,ActionSafety.SPAWN,"PERIMETER"));assertTrue(inside.getModifier()<edge.getModifier());assertTrue(edge.isPerimeterPreferred());assertTrue(edge.isAllowed());}
    @Test public void villageIsConservative(){EnvironmentEvidence e=new EnvironmentEvidence(new RegionKey(0,1,1),100,Collections.<EnvironmentSignal,Integer>emptyMap(),10,1,0,0,true,false);EnvironmentPlanningAssessment a=new PlayerAreaSafetyPolicy().assess(new EnvironmentClassifier().classify(e),action("a",Intent.BOSS_ENCOUNTER,ActionSafety.MAJOR));assertTrue(a.isAllowed());assertEquals(-15,a.getModifier());}
    @Test public void modifiersAreBounded(){PlayerAreaSafetyPolicy p=new PlayerAreaSafetyPolicy();for(EnvironmentClassification c:EnvironmentClassification.values())for(ActionSafety s:ActionSafety.values()){int n=p.assess(profile(c),action(c.name()+s.name(),Intent.AMBUSH,s)).getModifier();assertTrue(n>=-60&&n<=10);}}
    @Test public void assessmentIsDeterministic(){CandidateAction a=action("a",Intent.AMBUSH,ActionSafety.SPAWN);PlayerAreaSafetyPolicy p=new PlayerAreaSafetyPolicy();assertEquals(p.assess(profile(EnvironmentClassification.ESTABLISHED_PLAYER_AREA),a).getReasons(),p.assess(profile(EnvironmentClassification.ESTABLISHED_PLAYER_AREA),a).getReasons());}
    @Test public void decisionEngineHardSafetyWinsOverUtility(){DecisionContext base=DecisionContext.calm(10).withEnvironmentProfile(profile(EnvironmentClassification.ESTABLISHED_PLAYER_AREA));CandidateAction boss=action("boss",Intent.BOSS_ENCOUNTER,ActionSafety.MAJOR);CandidateAction none=action("candidate:no_action",Intent.NO_ACTION,ActionSafety.NON_DESTRUCTIVE);assertEquals(Intent.NO_ACTION,new DecisionEngine().decide(base,Arrays.asList(boss,none),7).getSelected().getIntent());}
    @Test public void profileDoesNotMutateThroughDecision(){SemanticRegionProfile p=profile(EnvironmentClassification.ESTABLISHED_PLAYER_AREA);DecisionContext c=DecisionContext.calm(10).withEnvironmentProfile(p);new DecisionEngine().decide(c,Collections.singletonList(action("a",Intent.AMBIENT_HINT,ActionSafety.NON_DESTRUCTIVE)),1);assertEquals(p,c.getEnvironmentProfile());}
    @Test public void pacingStillRejectsCooldown(){Map<String,Long> cooldown=new HashMap<String,Long>();cooldown.put("BOSS_ENCOUNTER",100L);DecisionContext c=new DecisionContext(10,10,0,0,0,0,1,.5,0,false,false,true,1,Collections.singletonMap("threat",10.0),Collections.<String,ProviderStatus>emptyMap(),Collections.<String>emptySet(),Collections.<String>emptySet(),Collections.<String>emptySet(),cooldown,profile(EnvironmentClassification.WILDERNESS));CandidateAction boss=action("boss",Intent.BOSS_ENCOUNTER,ActionSafety.MAJOR);CandidateAction none=action("candidate:no_action",Intent.NO_ACTION,ActionSafety.NON_DESTRUCTIVE);assertFalse(new DecisionEngine().decide(c,Arrays.asList(boss,none),1).getTrace().getCandidates().get(0).isEligible());}
    @Test public void directorOwnedDiscountDoesNotBecomeProtected(){EnvironmentEvidence e=new EnvironmentEvidence(new RegionKey(0,1,1),100,Collections.singletonMap(EnvironmentSignal.PLAYER_MODIFICATION,50),512,16,0,0,false,true);SemanticRegionProfile p=new EnvironmentClassifier().classify(e);assertFalse(p.isProtectedPlayerArea());assertFalse(new PlayerAreaSafetyPolicy().assess(p,action("a",Intent.BOSS_ENCOUNTER,ActionSafety.MAJOR)).isHardRejected());}
    @Test public void unknownDoesNotRescan(){EnvironmentPlanningAssessment a=new PlayerAreaSafetyPolicy().assess(null,action("a",Intent.AMBIENT_HINT,ActionSafety.NON_DESTRUCTIVE));assertEquals(0,a.getConfidence());assertTrue(a.getReasons().contains("ENVIRONMENT_UNKNOWN_NEUTRAL"));}
}
