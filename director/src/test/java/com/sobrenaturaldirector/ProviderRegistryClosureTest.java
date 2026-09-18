package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.provider.*;
import com.sobrenaturaldirector.derivation.*;
import com.sobrenaturaldirector.model.history.HistorySummaryBuilder;
import com.sobrenaturaldirector.model.player.*;
import com.sobrenaturaldirector.model.world.*;

public class ProviderRegistryClosureTest {
    @Test public void unavailableProviderIsNotRegistered() {
        DirectorProviderRegistry registry=new DirectorProviderRegistry();
        assertFalse(registry.registerIfAvailable(new Fixture(false,"fixture-a")));
        assertNull(registry.get("fixture-a"));
        assertTrue(registry.getProviders().isEmpty());
    }
    @Test public void availableProviderRegistersCapabilitiesAndUnregisters() {
        DirectorProviderRegistry registry=new DirectorProviderRegistry();
        Fixture provider=new Fixture(true,"fixture-a");
        assertTrue(registry.registerIfAvailable(provider));
        assertEquals(ProviderStatus.AVAILABLE_SUPPORTED,registry.status(new ProviderId("fixture-a")));
        assertEquals(1,registry.query(new com.sobrenaturaldirector.capability.CapabilityQuery(CapabilityVocabulary.ACTOR_SOURCE)).size());
        assertTrue(registry.unregister(new ProviderId("fixture-a")));
        assertNull(registry.get("fixture-a"));
        assertTrue(registry.query(new com.sobrenaturaldirector.capability.CapabilityQuery(CapabilityVocabulary.ACTOR_SOURCE)).isEmpty());
    }
    @Test public void fingerprintIncludesSortedCapabilitiesAndArtifactHash() {
        DirectorProviderRegistry a=new DirectorProviderRegistry(); DirectorProviderRegistry b=new DirectorProviderRegistry();
        a.register(new Fixture(true,"fixture-a")); b.register(new Fixture(true,"fixture-a"));
        Map<String,String> hash=Collections.singletonMap("fixture-mod","abc");
        assertEquals(ProviderStackFingerprint.of(a,hash),ProviderStackFingerprint.of(b,hash));
        assertNotEquals(ProviderStackFingerprint.of(a,hash),ProviderStackFingerprint.of(a,Collections.singletonMap("fixture-mod","def")));
    }
    @Test public void registryFlowsIntoDecisionContextProviderMap() {
        DirectorProviderRegistry registry=new DirectorProviderRegistry(); registry.register(new Fixture(true,"fixture-a"));
        PlayerModel player=new PlayerModel(UUID.randomUUID(),0,null,null,null,null,null,null,null,null,null,null,null,null,0,128,0,0,0,true,Confidence.KNOWN,false);
        WorldModel world=new WorldModel(0,10L,0L,0,WeatherBand.CLEAR,2,1,Collections.<String>emptyList(),Confidence.KNOWN);
        DecisionContextLiveSources sources=new DecisionContextLiveSources(null,null,registry,0,Collections.<String>emptySet(),Collections.<String>emptySet());
        com.sobrenaturaldirector.decision.model.DecisionContext context=new DecisionContextAssembler().assemble(player,world,new HistorySummaryBuilder().empty(),sources);
        assertEquals(ProviderStatus.AVAILABLE_SUPPORTED,context.getProviders().get("fixture-a"));
    }
    private static final class Fixture implements DirectorContentProvider {
        private final boolean available; private final ProviderId id; private final Map<String,ProviderCapabilityState> caps;
        Fixture(boolean available,String id){this.available=available;this.id=new ProviderId(id);Map<String,ProviderCapabilityState> m=new TreeMap<String,ProviderCapabilityState>();m.put(CapabilityVocabulary.ACTOR_SOURCE.getValue(),ProviderCapabilityState.MUTATION_VALIDATED);caps=Collections.unmodifiableMap(m);}
        public ProviderId getProviderId(){return id;} public String getModId(){return "fixture-mod";} public boolean isAvailable(){return available;} public String getDetectedVersion(){return "1";} public String getAdapterVersion(){return "test-1";} public ProviderStatus getStatus(){return available?ProviderStatus.AVAILABLE_SUPPORTED:ProviderStatus.MISSING;} public Map<String,ProviderCapabilityState> getCapabilities(){return caps;} public Set<CapabilityPolicyBinding> getPolicyBindings(){return Collections.emptySet();}
    }
}
