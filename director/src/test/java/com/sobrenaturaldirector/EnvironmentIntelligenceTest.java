package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.EnumMap;
import java.util.Map;
import com.sobrenaturaldirector.environment.EnvironmentClassifier;
import com.sobrenaturaldirector.environment.model.*;
import org.junit.Test;

public class EnvironmentIntelligenceTest {
    private static EnvironmentEvidence evidence(long tick,long activity, int players, EnvironmentSignal... signals) {
        Map<EnvironmentSignal,Integer> c=new EnumMap<EnvironmentSignal,Integer>(EnvironmentSignal.class);
        for(EnvironmentSignal s:signals)c.put(s,c.containsKey(s)?c.get(s)+1:1);
        return new EnvironmentEvidence(new RegionKey(0,2,-3),tick,c,256,4,players,activity,false,false);
    }
    @Test public void dimensionSafeRegionIdentity(){assertNotEquals(new RegionKey(0,2,3),new RegionKey(-1,2,3));assertEquals(new RegionKey(0,-1,-1),RegionKey.fromChunk(0,-1,-1));}
    @Test public void wildernessAndTrivialSignalsDoNotBecomeBase(){EnvironmentClassifier c=new EnvironmentClassifier();assertEquals(EnvironmentClassification.WILDERNESS,c.classify(evidence(100,0,0)).getClassification());assertEquals(EnvironmentClassification.PLAYER_MODIFIED_AREA,c.classify(evidence(100,90,0,EnvironmentSignal.LIGHT_SOURCE)).getClassification());assertFalse(c.classify(evidence(100,90,0,EnvironmentSignal.STORAGE)).isProtectedPlayerArea());}
    @Test public void campIsStrongerThanWilderness(){EnvironmentClassifier c=new EnvironmentClassifier();SemanticRegionProfile camp=c.classify(evidence(100,90,1,EnvironmentSignal.SLEEPING,EnvironmentSignal.CRAFTING,EnvironmentSignal.LIGHT_SOURCE));assertEquals(EnvironmentClassification.TEMPORARY_CAMP,camp.getClassification());assertTrue(camp.getConfidence()>c.classify(evidence(100,0,0)).getConfidence());}
    @Test public void establishedAreaRequiresMultipleSignals(){EnvironmentClassifier c=new EnvironmentClassifier();SemanticRegionProfile base=c.classify(evidence(100,100,1,EnvironmentSignal.SLEEPING,EnvironmentSignal.STORAGE,EnvironmentSignal.STORAGE,EnvironmentSignal.CRAFTING,EnvironmentSignal.SMELTING,EnvironmentSignal.LIGHT_SOURCE,EnvironmentSignal.FORTIFICATION,EnvironmentSignal.ACCESS_CONTROL,EnvironmentSignal.PLAYER_MODIFICATION));assertEquals(EnvironmentClassification.ESTABLISHED_PLAYER_AREA,base.getClassification());assertTrue(base.isProtectedPlayerArea());assertTrue(base.getReasons().contains("FORTIFICATION"));}
    @Test public void fortifiedUnoccupiedAreaIsNotAutomaticallyBase(){EnvironmentEvidence e=evidence(100,0,0,EnvironmentSignal.FORTIFICATION,EnvironmentSignal.ACCESS_CONTROL,EnvironmentSignal.PLAYER_MODIFICATION);assertFalse(new EnvironmentClassifier().classify(e).getClassification()==EnvironmentClassification.ESTABLISHED_PLAYER_AREA);}
    @Test public void villageCanCoexistAsSemanticClassification(){EnvironmentEvidence e=new EnvironmentEvidence(new RegionKey(0,0,0),10,new EnumMap<EnvironmentSignal,Integer>(EnvironmentSignal.class),10,1,1,9,true,false);SemanticRegionProfile p=new EnvironmentClassifier().classify(e);assertEquals(EnvironmentClassification.VANILLA_VILLAGE,p.getClassification());assertTrue(p.getTags().contains(EnvironmentSignal.VANILLA_VILLAGE));}
    @Test public void directorOwnedEvidenceIsDiscounted(){EnvironmentEvidence e=new EnvironmentEvidence(new RegionKey(0,0,0),100,new EnumMap<EnvironmentSignal,Integer>(EnvironmentSignal.class),10,1,0,0,false,true);assertNotEquals(EnvironmentClassification.ESTABLISHED_PLAYER_AREA,new EnvironmentClassifier().classify(e).getClassification());}
    @Test public void activityDecaysDeterministically(){assertEquals(100,EnvironmentClassifier.activity(100,100));assertEquals(50,EnvironmentClassifier.activity(6100,100));assertEquals(0,EnvironmentClassifier.activity(12100,100));}
    @Test public void repeatedClassificationIsStable(){EnvironmentEvidence e=evidence(100,80,1,EnvironmentSignal.SLEEPING,EnvironmentSignal.CRAFTING,EnvironmentSignal.STORAGE);EnvironmentClassifier c=new EnvironmentClassifier();assertEquals(c.classify(e).getClassification(),c.classify(e).getClassification());assertEquals(c.classify(e).getReasons(),c.classify(e).getReasons());}
    @Test public void unknownBlocksCanProduceSafeEmptyEvidence(){EnvironmentEvidence e=evidence(100,0,0);assertNotNull(new EnvironmentClassifier().classify(e).getTags());}
    @Test public void profileReachesPlanOnlyContextAsReadOnlyTags(){EnvironmentEvidence e=evidence(100,100,1,EnvironmentSignal.SLEEPING,EnvironmentSignal.STORAGE,EnvironmentSignal.CRAFTING);com.sobrenaturaldirector.situation.SituationContext context=new com.sobrenaturaldirector.situation.SituationContext(0,100,0,0,1,false,true,true,false,null,new EnvironmentClassifier().classify(e));assertTrue(context.toPlannerContext().getEnvironment().contains("region.classification=TEMPORARY_CAMP"));}
}
