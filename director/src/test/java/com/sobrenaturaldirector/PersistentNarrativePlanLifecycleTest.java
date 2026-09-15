package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.Set;
import org.junit.Test;
import net.minecraft.nbt.NBTTagCompound;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.capability.ProviderAvailability;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import com.sobrenaturaldirector.content.model.*;
import com.sobrenaturaldirector.decision.model.Intent;
import com.sobrenaturaldirector.environment.model.*;
import com.sobrenaturaldirector.narrative.*;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.situation.SituationContext;

public class PersistentNarrativePlanLifecycleTest {
    private static final SituationContext CONTEXT=new SituationContext(0,100,1,1,7L,false,true,true,true,Collections.<String>emptySet());
    @Test public void synthesisPersistsAndRoundTripsAcrossWorldData() {
        DirectorProviderRegistry registry=registry();
        NarrativePlanSynthesisResult result=synthesis(registry);
        PersistentNarrativePlan plan=PersistentNarrativePlan.fromSynthesis("plan-1",result,10L,100L,registry.getRevision());
        DirectorWorldSavedData data=new DirectorWorldSavedData(); data.recordNarrativePlan(plan);
        NBTTagCompound nbt=new NBTTagCompound(); data.writeToNBT(nbt);
        DirectorWorldSavedData reloaded=new DirectorWorldSavedData(); reloaded.readFromNBT(nbt);
        assertEquals(plan.getId(),reloaded.getNarrativePlan("plan-1").getId());
        assertEquals(plan.getPlanFingerprint(),reloaded.getNarrativePlan("plan-1").getPlanFingerprint());
        assertEquals(plan.getCapabilities().keySet(),reloaded.getNarrativePlan("plan-1").getCapabilities().keySet());
    }
    @Test public void threadIdPersistsOnSynthesizedPlan() {
        DirectorProviderRegistry registry=registry(); SituationContext context=CONTEXT;
        NarrativePlanSynthesisResult result=new NarrativePlanSynthesizer().synthesize(new NarrativePlanSynthesisRequest(Intent.LOOT_DISCOVERY,context,null,registry,null,true,"story","INSIDE","thread-key",Collections.singleton("continuity"),8,3,8,"thread-1"));
        PersistentNarrativePlan plan=PersistentNarrativePlan.fromSynthesis("plan-thread",result,10L,100L,registry.getRevision());
        assertEquals("thread-1",plan.getThreadId()); assertEquals("thread-1",PersistentNarrativePlan.fromNbt(plan.toNbt()).getThreadId());
    }
    @Test public void directReplanPreservesThreadLineageAndCurrentPlan() {
        DirectorProviderRegistry registry=registry(); DirectorWorldSavedData data=new DirectorWorldSavedData(); NarrativeThreadManager manager=new NarrativeThreadManager();
        PersistentNarrativeThread thread=manager.create(data,"direct-thread","direct-subject",0,1,1,Collections.singleton("continuity"),10L,"THREAD_WORTHY");
        NarrativePlanSynthesisRequest request=new NarrativePlanSynthesisRequest(Intent.LOOT_DISCOVERY,CONTEXT,null,registry,null,true,"direct-subject","INSIDE","direct-p1",Collections.singleton("continuity"),8,3,8,thread.getId());
        PersistentNarrativePlan p1=PersistentNarrativePlan.fromSynthesis("direct-p1",new NarrativePlanSynthesizer().synthesize(request),10L,100L,registry.getRevision()); data.recordNarrativePlan(p1); manager.attach(data,thread.getId(),p1,10L,"INITIAL");
        registry.unregister(new ProviderId("structure")); new NarrativePlanLifecycleCoordinator().revalidate(p1,registry,null,11L,data); registry.registerDescriptor(descriptor("structure",CapabilityVocabulary.STRUCTURE_SOURCE));
        PersistentNarrativePlan p2=manager.replan(data,thread.getId(),p1,request,new NarrativePlanSynthesizer(),registry,12L,100L);
        assertNotNull(p2); assertEquals(thread.getId(),p2.getThreadId()); assertEquals(p1.getId(),p2.getParentPlanId()); assertEquals(NarrativePlanLifecycleState.SUPERSEDED,data.getNarrativePlan(p1.getId()).getState()); assertEquals(p2.getId(),data.getNarrativeThread(thread.getId()).getCurrentPlanId()); assertEquals(2,data.getNarrativeThread(thread.getId()).getHistory().size()); assertNull(manager.replan(data,thread.getId(),p1,request,new NarrativePlanSynthesizer(),registry,13L,100L));
    }
    @Test public void providerChangeMakesPlanStaleWithoutExecuting() {
        DirectorProviderRegistry registry=registry(); PersistentNarrativePlan plan=PersistentNarrativePlan.fromSynthesis("plan-2",synthesis(registry),10L,100L,registry.getRevision());
        registry.unregister(new ProviderId("structure"));
        NarrativePlanValidationResult result=new PersistentNarrativePlanValidator().validate(plan,registry,null,11L);
        assertEquals(NarrativePlanValidationResult.Status.STALE,result.getStatus()); assertTrue(result.getReasons().toString().contains("CAPABILITY_UNAVAILABLE"));
    }
    @Test public void terminalPlanCannotBeResurrected() {
        DirectorProviderRegistry registry=registry(); PersistentNarrativePlan plan=PersistentNarrativePlan.fromSynthesis("plan-3",synthesis(registry),10L,100L,registry.getRevision()).withState(NarrativePlanLifecycleState.COMPLETED,20L);
        try { plan.withState(NarrativePlanLifecycleState.ACTIVE,21L); fail(); } catch (IllegalStateException expected) { }
    }
    @Test public void replanCreatesNewIdentityAndSupersedesParent() {
        DirectorProviderRegistry registry=registry(); DirectorWorldSavedData data=new DirectorWorldSavedData();
        NarrativePlanSynthesisResult result=synthesis(registry); PersistentNarrativePlan oldPlan=PersistentNarrativePlan.fromSynthesis("plan-4",result,10L,100L,registry.getRevision()); data.recordNarrativePlan(oldPlan);
        PersistentNarrativePlan replacement=new NarrativePlanLifecycleCoordinator().replan(oldPlan,new NarrativePlanSynthesisRequest(Intent.LOOT_DISCOVERY,CONTEXT,null,registry,null,false,"story",null,"life-key-2",null,8,3,8),new NarrativePlanSynthesizer(),registry,data,20L,100L);
        assertNotNull(replacement); assertNotEquals(oldPlan.getId(),replacement.getId()); assertEquals(oldPlan.getId(),replacement.getParentPlanId()); assertEquals(NarrativePlanLifecycleState.SUPERSEDED,data.getNarrativePlan(oldPlan.getId()).getState());
    }
    @Test public void irrelevantRegistryRevisionDoesNotInvalidatePlan() {
        DirectorProviderRegistry registry=registry(); PersistentNarrativePlan plan=PersistentNarrativePlan.fromSynthesis("plan-5",synthesis(registry),10L,100L,registry.getRevision());
        registry.registerDescriptor(descriptor("irrelevant",new SemanticCapability("director:actor_source")));
        assertEquals(NarrativePlanValidationResult.Status.VALID,new PersistentNarrativePlanValidator().validate(plan,registry,null,11L).getStatus());
    }
    @Test public void expiryIsExplicitAndWorldTickBased() {
        DirectorProviderRegistry registry=registry(); PersistentNarrativePlan plan=PersistentNarrativePlan.fromSynthesis("plan-6",synthesis(registry),10L,10L,registry.getRevision());
        NarrativePlanValidationResult result=new PersistentNarrativePlanValidator().validate(plan,registry,null,11L);
        assertEquals(NarrativePlanValidationResult.Status.EXPIRED,result.getStatus()); assertTrue(result.getReasons().contains("PLAN_EXPIRED"));
    }
    @Test public void capabilityCanReturnWithoutCreatingDuplicatePlan() {
        DirectorProviderRegistry registry=registry(); PersistentNarrativePlan plan=PersistentNarrativePlan.fromSynthesis("plan-7",synthesis(registry),10L,100L,registry.getRevision());
        registry.unregister(new ProviderId("structure")); assertFalse(new PersistentNarrativePlanValidator().validate(plan,registry,null,11L).isValid());
        registry.registerDescriptor(descriptor("structure",CapabilityVocabulary.STRUCTURE_SOURCE));
        assertTrue(new PersistentNarrativePlanValidator().validate(plan,registry,null,12L).isValid());
    }
    @Test public void coordinatorProcessesOnlyBoundedNonTerminalPlans() {
        DirectorProviderRegistry registry=registry(); DirectorWorldSavedData data=new DirectorWorldSavedData(); NarrativePlanSynthesisResult result=synthesis(registry); NarrativePlanLifecycleCoordinator coordinator=new NarrativePlanLifecycleCoordinator();
        for(int i=0;i<10;i++) data.recordNarrativePlan(PersistentNarrativePlan.fromSynthesis("bounded-"+i,result,10L,100L,registry.getRevision()));
        assertEquals(NarrativePlanLifecycleCoordinator.MAX_PLANS_PER_INVOCATION,coordinator.evaluateBounded(data,registry,new NarrativePlanLifecycleCoordinator.ProfileLookup(){public SemanticRegionProfile get(int dimension,int x,int z){return null;}},11L));
    }
    @Test public void replanReplayUsesPersistedSupersededState() {
        DirectorProviderRegistry registry=registry(); DirectorWorldSavedData data=new DirectorWorldSavedData(); PersistentNarrativePlan oldPlan=PersistentNarrativePlan.fromSynthesis("plan-8",synthesis(registry),10L,100L,registry.getRevision()); data.recordNarrativePlan(oldPlan);
        NarrativePlanLifecycleCoordinator coordinator=new NarrativePlanLifecycleCoordinator(); registry.unregister(new ProviderId("structure")); coordinator.revalidate(oldPlan,registry,null,11L,data); registry.registerDescriptor(descriptor("structure",CapabilityVocabulary.STRUCTURE_SOURCE)); PersistentNarrativePlan replacement=coordinator.replan(oldPlan,new NarrativePlanSynthesisRequest(Intent.LOOT_DISCOVERY,CONTEXT,null,registry,null,false,"story",null,"replay-key",null,8,3,8),new NarrativePlanSynthesizer(),registry,data,12L,100L);
        assertNotNull(replacement); assertNull(coordinator.replan(oldPlan,new NarrativePlanSynthesisRequest(Intent.LOOT_DISCOVERY,CONTEXT,null,registry,null,false,"story",null,"replay-key",null,8,3,8),new NarrativePlanSynthesizer(),registry,data,13L,100L)); assertEquals(2,data.getNarrativePlans().size());
    }
    private static NarrativePlanSynthesisResult synthesis(DirectorProviderRegistry registry) { return new NarrativePlanSynthesizer().synthesize(new NarrativePlanSynthesisRequest(Intent.LOOT_DISCOVERY,CONTEXT,null,registry,null,false,"story",null,"life-key",null,8,3,8)); }
    private static DirectorProviderRegistry registry(){ DirectorProviderRegistry r=new DirectorProviderRegistry(); r.registerDescriptor(descriptor("structure",CapabilityVocabulary.STRUCTURE_SOURCE)); return r; }
    private static ProviderDescriptor descriptor(String id,SemanticCapability c){ProviderId p=new ProviderId(id);return new ProviderDescriptor(p,id,EvidenceStatus.CONFIRMED,true,com.sobrenaturaldirector.decision.model.ProviderStatus.AVAILABLE,false,Collections.<String>emptySet(),"1",Collections.singleton(new CapabilityDescriptor(c,p,ProviderAvailability.RUNTIME_CONTROL,80,0)),Collections.<CapabilityPolicyBinding>emptySet(),ProviderAvailability.RUNTIME_CONTROL);}
}
