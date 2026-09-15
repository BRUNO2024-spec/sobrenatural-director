package com.sobrenaturaldirector;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sobrenaturaldirector.decision.model.*;
import com.sobrenaturaldirector.narrative.*;
import com.sobrenaturaldirector.situation.SituationMemory;
import com.sobrenaturaldirector.persistence.*;
import com.sobrenaturaldirector.domain.DirectorWorldState;
import net.minecraft.nbt.NBTTagCompound;

public final class AdaptivePacingDirectorTest {
    private final AdaptivePacingDirector pacing=new AdaptivePacingDirector();
    private static DecisionContext context(long tick,double recovery){return new DecisionContext(tick,tick,0,0,0,recovery,1,.5,0,false,false,true,0,Collections.<String,Double>emptyMap(),Collections.<String,ProviderStatus>emptyMap(),Collections.<String>emptySet(),Collections.<String>emptySet(),Collections.<String>emptySet(),Collections.<String,Long>emptyMap());}
    private static PacingHistoryEntry high(long tick,String scope){return new PacingHistoryEntry(scope,tick,"t1",Intent.BOSS_ENCOUNTER,NarrativeIntensity.HIGH,"PLANNED","HIGH_EVENT","high");}
    private static NarrativeOpportunity opportunity(String id,Intent intent){return new NarrativeOpportunity(id,intent,.95,.95,Collections.<String>emptySet(),Collections.<String>emptyList(),false,null);}
    @Test public void PacingUnit1NeutralWeakCanChooseNoAction(){PacingAssessment a=pacing.assess("p1",context(10,0),Collections.<PacingHistoryEntry>emptyList(),Intent.NO_ACTION);assertEquals(PacingMode.QUIET,a.getMode());assertTrue(a.getNoActionModifier()>=0);}
    @Test public void PacingUnit2HighHistoryRaisesRecovery(){PacingAssessment a=pacing.assess("p1",context(100,0),Collections.singletonList(high(99,"p1")),Intent.BOSS_ENCOUNTER);assertTrue(a.getRecoveryNeed()>=.5);assertTrue(a.getReasons().contains("RECENT_HIGH_INTENSITY"));}
    @Test public void PacingUnit3NoActionBoostIsBounded(){PacingAssessment a=pacing.assess("p1",context(100,0),Collections.singletonList(high(99,"p1")),Intent.NO_ACTION);assertTrue(a.getNoActionModifier()<=AdaptivePacingDirector.MAX_NO_ACTION_MODIFIER);}
    @Test public void PacingUnit4HighAfterHighIsDeferred(){PacingAssessment a=pacing.assess("p1",context(100,0),Collections.singletonList(high(99,"p1")),Intent.BOSS_ENCOUNTER);assertTrue(a.getReasons().contains("HIGH_INTENSITY_DEFERRED"));assertFalse(a.allows(NarrativeIntensity.EXTREME));}
    @Test public void PacingUnit5LowDuringRecoveryRemainsAllowed(){PacingAssessment a=pacing.assess("p1",context(100,0),Collections.singletonList(high(99,"p1")),Intent.AMBIENT_HINT);assertTrue(a.allows(NarrativeIntensity.SUBTLE));}
    @Test public void PacingUnit6RecoveryReenablesStrongerContent(){PacingAssessment a=pacing.assess("p1",context(25000,0),Collections.singletonList(high(0,"p1")),Intent.BOSS_ENCOUNTER);assertTrue(a.allows(NarrativeIntensity.EXTREME));}
    @Test public void PacingUnit7AgeDoesNotForceSequence(){PacingAssessment a=pacing.assess("p1",context(25000,0),Collections.<PacingHistoryEntry>emptyList(),Intent.NO_ACTION);assertEquals(PacingMode.QUIET,a.getMode());}
    @Test public void PacingUnit8ScopeIsolation(){PacingAssessment a=pacing.assess("p2",context(100,0),Collections.singletonList(high(99,"p1")),Intent.BOSS_ENCOUNTER);assertEquals(0,a.getRecoveryNeed(),.001);}
    @Test public void PacingUnit9DeterministicFingerprint(){List<PacingHistoryEntry> h=Arrays.asList(high(1,"p1"));assertEquals(pacing.assess("p1",context(10,0),h,Intent.AMBUSH).getFingerprint(),pacing.assess("p1",context(10,0),h,Intent.AMBUSH).getFingerprint());}
    @Test public void PacingUnit10CandidateOrderingDoesNotMatter(){PacingAssessment p=pacing.assess("p1",context(100,0),Collections.singletonList(high(99,"p1")),Intent.BOSS_ENCOUNTER);NarrativeArbitrationContext c=new NarrativeArbitrationContext(context(100,0),new SituationMemory(),Collections.<String>emptySet(),null,p,false,"",1,null);NarrativeIntentDecision a=new NarrativeIntentArbiter().arbitrate(c,Arrays.asList(opportunity("z",Intent.BOSS_ENCOUNTER),opportunity("a",Intent.AMBIENT_HINT)));NarrativeIntentDecision b=new NarrativeIntentArbiter().arbitrate(c,Arrays.asList(opportunity("a",Intent.AMBIENT_HINT),opportunity("z",Intent.BOSS_ENCOUNTER)));assertEquals(a.getIntent(),b.getIntent());}
    @Test public void PacingUnit11StrongOpportunityCanLoseToNoAction(){PacingAssessment p=pacing.assess("p1",context(100,0),Collections.singletonList(high(99,"p1")),Intent.BOSS_ENCOUNTER);NarrativeArbitrationContext c=new NarrativeArbitrationContext(context(100,0),new SituationMemory(),Collections.<String>emptySet(),null,p,false,"",1,null);NarrativeIntentDecision d=new NarrativeIntentArbiter().arbitrate(c,Collections.singletonList(opportunity("strong",Intent.BOSS_ENCOUNTER)));assertTrue(d.isNoAction());}
    @Test public void PacingUnit12RecoveryDoesNotForceNoAction(){PacingAssessment p=pacing.assess("p1",context(25000,0),Collections.singletonList(high(0,"p1")),Intent.AMBIENT_HINT);NarrativeArbitrationContext c=new NarrativeArbitrationContext(context(25000,0),new SituationMemory(),Collections.<String>emptySet(),null,p,false,"",1,null);assertFalse(new NarrativeIntentArbiter().arbitrate(c,Collections.singletonList(opportunity("hint",Intent.AMBIENT_HINT))).isNoAction());}
    @Test public void PacingUnit13IntensityMappingIsProviderNeutral(){assertEquals(NarrativeIntensity.EXTREME,AdaptivePacingDirector.intensity(Intent.BOSS_ENCOUNTER));assertEquals(NarrativeIntensity.SUBTLE,AdaptivePacingDirector.intensity(Intent.AMBIENT_HINT));}
    @Test public void PacingUnit14HistoryIsBoundedByAssessment(){List<PacingHistoryEntry> h=new ArrayList<PacingHistoryEntry>();for(int i=0;i<40;i++)h.add(high(i,"p1"));assertTrue(pacing.assess("p1",context(50,0),h,Intent.AMBUSH).getReasons().size()<=4);}
    @Test public void PacingUnit15ThreadScopeIsExplainable(){assertTrue(pacing.assess("p1",context(1,0),null,Intent.NO_ACTION,"thread-1").getReasons().contains("THREAD_ATTENTION_SCOPED"));}
    @Test public void PacingUnit16NoWallClockInput(){PacingAssessment a=pacing.assess("p1",context(100,0),Collections.singletonList(high(99,"p1")),Intent.NO_ACTION);assertTrue(a.getRecoveryNeed()>.9);}
    @Test public void PacingUnit17ModeIsNotStateMachine(){assertEquals(PacingMode.QUIET,pacing.assess("p1",context(100,0),Collections.<PacingHistoryEntry>emptyList(),Intent.NO_ACTION).getMode());assertEquals(PacingMode.RECOVERY,pacing.assess("p1",context(100,0),Collections.singletonList(high(99,"p1")),Intent.NO_ACTION).getMode());}
    @Test public void PacingUnit18NoProviderAccessModel(){assertNotNull(pacing.assess("p1",context(1,0),null,Intent.NO_ACTION));}
    @Test public void PacingUnit19NoExecutionModel(){assertEquals(Intent.NO_ACTION,AdaptivePacingDirector.intensity(Intent.NO_ACTION)==NarrativeIntensity.SUBTLE?Intent.NO_ACTION:Intent.AMBUSH);}
    @Test public void PacingUnit20ReasonsAreImmutable(){try{pacing.assess("p1",context(1,0),null,Intent.NO_ACTION).getReasons().add("x");fail();}catch(UnsupportedOperationException expected){}}
    @Test public void PacingUnit21HistorySurvivesRestartAndLegacyIsNeutral(){DirectorWorldSavedData a=new DirectorWorldSavedData();a.replaceState(DirectorWorldState.empty(4));a.recordPacing(high(9,"p1"));NBTTagCompound n=new NBTTagCompound();a.writeToNBT(n);DirectorWorldSavedData b=new DirectorWorldSavedData();b.readFromNBT(n);assertEquals(1,b.getPacingHistory().size());DirectorWorldSavedData legacy=new DirectorWorldSavedData();NBTTagCompound old=DirectorStateNbtCodec.encode(DirectorWorldState.empty(4));legacy.readFromNBT(old);assertTrue(legacy.getPacingHistory().isEmpty());}
}
