package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import com.sobrenaturaldirector.action.*;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import com.sobrenaturaldirector.content.model.*;
import com.sobrenaturaldirector.control.*;
import com.sobrenaturaldirector.decision.model.*;
import com.sobrenaturaldirector.decision.DecisionResult;
import com.sobrenaturaldirector.provider.*;
import com.sobrenaturaldirector.situation.*;

public class SemanticActionCatalogTest {
    private static final ProviderId PROVIDER=new ProviderId("fixture");
    private static final DimensionRef DIMENSION=new DimensionRef(7,"fixture",Collections.singleton("test"),true);
    private static DirectorProviderRegistry registry(final String... capabilities){DirectorProviderRegistry r=new DirectorProviderRegistry();final Map<String,ProviderCapabilityState> values=new TreeMap<String,ProviderCapabilityState>();for(String c:capabilities)values.put(c,ProviderCapabilityState.MUTATION_VALIDATED);r.register(new DirectorContentProvider(){public ProviderId getProviderId(){return PROVIDER;}public String getModId(){return "fixture";}public boolean isAvailable(){return true;}public String getDetectedVersion(){return "1";}public ProviderStatus getStatus(){return ProviderStatus.AVAILABLE_SUPPORTED;}public Map<String,ProviderCapabilityState> getCapabilities(){return values;}public Set<CapabilityPolicyBinding> getPolicyBindings(){return Collections.emptySet();}});return r;}
    @Test public void standardCatalogIsDeterministicAndBounded(){SemanticActionCatalog a=SemanticActionCatalog.standard(),b=SemanticActionCatalog.standard();assertEquals(a.fingerprint(),b.fingerprint());assertTrue(a.size()>1);assertEquals(ActionKind.COMPOSITE,a.get("CREATE_INVESTIGATION_SITE").getKind());assertFalse(a.get("AMBUSH").isExecutable());}
    @Test public void missingCapabilityHasDiagnostic(){DecisionContext context=DecisionContext.calm(1).withDimensionContext(DIMENSION,Collections.<String,Integer>emptyMap());ActionAvailability result=SemanticActionCatalog.standard().availability("SPAWN_ACTOR",context, null,registry(CapabilityVocabularyCompat.ACTOR),null);assertEquals(ActionAvailabilityStatus.UNAVAILABLE_MISSING_CAPABILITY,result.getStatus());}
    @Test public void availablePrimitiveResolvesWithoutProviderName(){DecisionContext context=DecisionContext.calm(1).withDimensionContext(DIMENSION,Collections.<String,Integer>emptyMap());SemanticActionCatalog catalog=SemanticActionCatalog.standard();ActionAvailability result=catalog.availability("SPAWN_ACTOR",context,null,registry(CapabilityVocabularyCompat.ACTOR,CapabilityVocabularyCompat.LIFECYCLE),null);assertEquals(ActionAvailabilityStatus.AVAILABLE,result.getStatus());}
    @Test public void composerProducesDeterministicDag(){SemanticActionCatalog catalog=SemanticActionCatalog.standard();ActionPlan p=new ActionComposer().compose("investigation-1",ActionType.CREATE_INVESTIGATION_SITE,DIMENSION,catalog);assertEquals(2,p.getNodes().size());assertEquals("structure",p.getNodes().get(0).getNodeId());}
    @Test public void bridgePreservesNarrativeMeaning(){NarrativeActionBridge bridge=new NarrativeActionBridge();assertEquals(ActionType.CREATE_INVESTIGATION_SITE,bridge.fromGoal(SituationGoal.INVESTIGATION,DIMENSION).getType());assertEquals(ActionType.CREATE_THREAT_PRESENCE,bridge.fromGoal(SituationGoal.THREAT_EVENT,DIMENSION).getType());}
    @Test public void cyclesAreRejected(){List<ActionPlanNode> nodes=new ArrayList<ActionPlanNode>();nodes.add(new ActionPlanNode("a","NO_ACTION",Collections.singleton("b"),false,"ABORT"));nodes.add(new ActionPlanNode("b","NO_ACTION",Collections.singleton("a"),false,"ABORT"));ActionPlan plan=new ActionPlan("cycle",DIMENSION,nodes);assertFalse(new ActionPlanValidator().validate(plan,SemanticActionCatalog.standard()).isValid());}
    @Test public void decisionEngineCanConsumeSharedCatalog(){DecisionContext context=DecisionContext.calm(1).withDimensionContext(DIMENSION,Collections.<String,Integer>emptyMap());DecisionResult result=new com.sobrenaturaldirector.decision.DecisionEngine().decide(context,SemanticActionCatalog.standard(),registry(CapabilityVocabularyCompat.ACTOR,CapabilityVocabularyCompat.LIFECYCLE),null,null,41L);assertNotNull(result.getSelected());assertTrue(result.getSelected().getMetadata().containsKey("semanticActionId"));}
    @Test public void everyExecutableDefinitionHasBinding(){SemanticActionCatalog catalog=SemanticActionCatalog.standard();ActionExecutionBindingRegistry bindings=ActionExecutionBindingRegistry.standard();assertEquals(0,bindings.unboundExecutableCount(catalog));assertEquals(7,bindings.boundExecutableCount(catalog));}
    @Test public void entityBindingBuildsConcreteRequestWithoutModBranch(){ActionExecutionBinding binding=ActionExecutionBindingRegistry.standard().get(ActionType.SPAWN_ACTOR);EntityRef entity=new EntityRef("actor-ref",DIMENSION,EntityOwnership.DIRECTOR_SPAWNED);WorldPositionRef position=new WorldPositionRef(DIMENSION,8,65,8);ActionExecutionSpec spec=new ActionExecutionSpec(entity,position,null,new ControlLease("lease-actor","test",0,100,RestorationPolicy.RESTORABLE,true),ControlAuthority.FULL_DIRECTOR_CONTROL,"mutation-actor","Witness");assertTrue(binding.supports(spec));assertTrue(binding instanceof ActionExecutionBinding);}
    private static final class CapabilityVocabularyCompat {static final String ACTOR="director:actor_source";static final String LIFECYCLE="director:entity_lifecycle_control";}
}
