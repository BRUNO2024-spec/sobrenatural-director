package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import com.sobrenaturaldirector.composition.CompositionEngine;
import com.sobrenaturaldirector.composition.model.*;
import com.sobrenaturaldirector.composition.validation.CompositionValidator;
import com.sobrenaturaldirector.decision.*;
import com.sobrenaturaldirector.decision.constraint.*;
import com.sobrenaturaldirector.decision.model.*;
import com.sobrenaturaldirector.planner.*;import com.sobrenaturaldirector.planner.requirement.*;
import com.sobrenaturaldirector.planner.model.*;

public class CompositionTest {
    private static DirectorPlan plan(Intent intent) {List<PlanRequirement> r=new ArrayList<PlanRequirement>();if(intent!=Intent.NO_ACTION){r.add(new PlanRequirement("location","LOCATION:UNDERGROUND",RequirementStatus.SATISFIED,false,null));r.add(new PlanRequirement("common","CONTENT_ROLE:COMMON_HOSTILE",RequirementStatus.DEFERRED,false,null));}if(intent==Intent.BOSS_ENCOUNTER)r.add(new PlanRequirement("boss","CONTENT_ROLE:BOSS",RequirementStatus.DEFERRED,false,null));return new DirectorPlan("plan:"+intent.name().toLowerCase(),"decision",intent,"goal",PlanHorizon.SHORT,PlanStatus.TENTATIVE,Collections.<PlanStep>emptyList(),r,Collections.<String,Double>emptyMap(),"fingerprint",null,null);}
    @Test public void noActionDoesNotCompose(){CompositionResult r=new CompositionEngine().compose(new CompositionRequest(plan(Intent.NO_ACTION),"generic",StructureScale.SMALL,1,null));assertEquals(CompositionStatus.NO_ACTION,r.getStatus());assertNull(r.getStructure());}
    @Test public void bossCompositionIsLogicalAndBounded(){CompositionResult r=new CompositionEngine().compose(new CompositionRequest(plan(Intent.BOSS_ENCOUNTER),"catacomb",StructureScale.MEDIUM,7,null));assertNotNull(r.getStructure());assertEquals(r.getTrace().getEvents().toString(),2,r.getEncounters().size());assertEquals(CompositionStatus.PARTIAL,r.getStatus());assertTrue(r.getStructure().getNodes().size()<=32);assertEquals(0,CompositionValidator.validate(r.getStructure()).size());}
    @Test public void compositionIsDeterministicAcrossTenThousandIterations(){CompositionEngine e=new CompositionEngine();CompositionRequest q=new CompositionRequest(plan(Intent.BOSS_ENCOUNTER),"catacomb",StructureScale.MEDIUM,7,null);CompositionResult expected=e.compose(q);for(int seed=0;seed<10000;seed++){CompositionResult actual=e.compose(new CompositionRequest(q.getPlan(),q.getTheme(),q.getScale(),seed,null));assertEquals(expected.getTrace().getEvents(),actual.getTrace().getEvents());assertEquals(expected.getStructure().getCriticalPath(),actual.getStructure().getCriticalPath());}}
}
