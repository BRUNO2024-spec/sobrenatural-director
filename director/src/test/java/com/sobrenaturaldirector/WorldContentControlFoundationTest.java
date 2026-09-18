package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import com.sobrenaturaldirector.control.*;
import com.sobrenaturaldirector.content.model.*;
import com.sobrenaturaldirector.provider.*;
import com.sobrenaturaldirector.decision.model.ProviderStatus;

public class WorldContentControlFoundationTest {
    private static final ProviderId PROVIDER = new ProviderId("fixture");
    private static final SemanticCapability CAPABILITY = new SemanticCapability("director:fixture_control");
    private static ControlLease lease() { return new ControlLease("lease-1", "test", 1, 10, RestorationPolicy.RESTORABLE, true); }

    @Test public void positionIncludesDimension() {
        DimensionRef a = new DimensionRef(0, "vanilla", Collections.singleton("overworld"), true);
        DimensionRef b = new DimensionRef(7, "twilight", Collections.singleton("forest"), true);
        assertNotEquals(new WorldPositionRef(a, 1, 2, 3), new WorldPositionRef(b, 1, 2, 3));
    }

    @Test public void entityRequestIsRequestNotExecution() {
        DimensionRef dimension = new DimensionRef(3);
        EntityRef target = new EntityRef("opaque-entity", dimension, EntityOwnership.DIRECTOR_SPAWNED);
        EntityControlRequest request = new EntityControlRequest(PROVIDER, CAPABILITY, target, lease(), ControlAuthority.FULL_DIRECTOR_CONTROL);
        assertEquals(ControlDomain.ENTITY, request.getDomain());
        assertEquals(ControlResultStatus.SAFETY_REJECTED, new ControlResult(ControlResultStatus.SAFETY_REJECTED, "fixture").getStatus());
    }

    @Test public void unknownAndPlayerOwnedProvenanceRemainExplicit() {
        assertEquals(EntityOwnership.UNKNOWN, new EntityRef("e", new DimensionRef(0), EntityOwnership.UNKNOWN).getOwnership());
        assertEquals(ItemProvenance.PLAYER_OWNED, new ItemRef("minecraft:stone", ItemProvenance.PLAYER_OWNED).getProvenance());
    }

    @Test public void leaseExpiresDeterministically() {
        assertTrue(lease().validAt(1));
        assertFalse(lease().validAt(11));
    }

    @Test public void catalogFingerprintIsStable() {
        ProviderDescriptor provider = new ProviderDescriptor(PROVIDER, "fixture", EvidenceStatus.CONFIRMED, true, com.sobrenaturaldirector.decision.model.ProviderStatus.AVAILABLE, false, Collections.singleton("test"));
        SemanticContentEntry entry = new SemanticContentEntry(new ContentKey("fixture:actor"), PROVIDER, ContentKind.ENTITY, Collections.singleton(CAPABILITY), EvidenceStatus.CONFIRMED, Provenance.RUNTIME_REGISTRY, true, false, ExecutionStatus.ELIGIBLE_FOR_FUTURE_EVALUATION, Collections.singleton("fixture"));
        com.sobrenaturaldirector.content.catalog.SemanticCatalog catalog = new com.sobrenaturaldirector.content.catalog.SemanticCatalog(Collections.singleton(provider), Collections.singleton(entry));
        assertEquals(catalog.fingerprint(), catalog.fingerprint());
        assertEquals(64, catalog.fingerprint().length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void positionCannotOmitDimension() { new WorldPositionRef(null, 0, 0, 0); }

    @Test public void routerFailsClosedAcrossProviderAndLeaseBoundaries() {
        final ProviderId id = new ProviderId("router-fixture");
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        registry.register(new DirectorContentProvider() {
            public ProviderId getProviderId(){return id;} public String getModId(){return "fixture";}
            public boolean isAvailable(){return true;} public String getDetectedVersion(){return "1";}
            public ProviderStatus getStatus(){return ProviderStatus.AVAILABLE_SUPPORTED;}
            public Map<String,ProviderCapabilityState> getCapabilities(){return Collections.singletonMap(CAPABILITY.getValue(), ProviderCapabilityState.MUTATION_VALIDATED);}
            public ControlResult executeControl(com.sobrenaturaldirector.control.ControlRequest request,long tick){return new ControlResult(ControlResultStatus.APPLIED,"fixture");}
        });
        ConcreteControlRouter router = new ConcreteControlRouter(registry);
        DimensionRef dimension = new DimensionRef(9);
        EntityRef entity = new EntityRef("e", dimension, EntityOwnership.DIRECTOR_SPAWNED);
        EntityControlRequest request = new EntityControlRequest(id, CAPABILITY, entity, lease(), ControlAuthority.LIMITED_CONTROL);
        assertEquals(ControlResultStatus.APPLIED, router.execute(request, 2).getStatus());
        assertEquals(ControlResultStatus.STALE_TARGET, router.execute(request, 11).getStatus());
        registry.unregister(id);
        assertEquals(ControlResultStatus.PROVIDER_UNAVAILABLE, router.execute(request, 2).getStatus());
    }
}
