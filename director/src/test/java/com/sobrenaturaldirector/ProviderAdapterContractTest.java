package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.mutation.runtime.MutationExecutionResult;
import com.sobrenaturaldirector.provider.ControlledNpcRequest;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.provider.ProviderMutationResult;
import com.sobrenaturaldirector.provider.customnpcs.CustomNpcsProviderAdapter;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;

public final class ProviderAdapterContractTest {
    @Test public void registryContainsOptionalCustomNpcsWithoutWorld() {
        DirectorProviderRegistry registry = new DirectorProviderRegistry();
        CustomNpcsProviderAdapter provider = new CustomNpcsProviderAdapter(true, true);
        registry.register(provider);
        assertEquals(provider, registry.get(new ProviderId("customnpcs")));
        assertFalse(provider.getCapabilities().containsKey("customnpcs:npc_create_controlled"));
        assertFalse(provider.getCapabilities().containsKey("customnpcs:npc_remove_controlled"));
        assertTrue(provider.getCapabilities().containsKey(CapabilityVocabulary.ACTOR_SOURCE.getValue()));
        assertFalse(provider.getCapabilities().containsKey("customnpcs:script_execute"));
    }

    @Test public void absentProviderIsNonFatalAndDoesNotAdvertiseAvailability() {
        CustomNpcsProviderAdapter provider = new CustomNpcsProviderAdapter(true);
        assertTrue(provider.getStatus() == ProviderStatus.MISSING || provider.getStatus() == ProviderStatus.FAILED_INITIALIZATION);
        assertFalse(provider.isAvailable());
    }

    @Test public void disabledProviderDoesNotInitialize() {
        CustomNpcsProviderAdapter provider = new CustomNpcsProviderAdapter(false);
        assertEquals(ProviderStatus.DISABLED, provider.getStatus());
        assertFalse(provider.isAvailable());
    }

    @Test public void unknownVersionFailsVersionGate() {
        assertFalse(CustomNpcsProviderAdapter.isSupportedVersion("1.7.10"));
        assertFalse(CustomNpcsProviderAdapter.isSupportedVersion("unknown"));
        assertTrue(CustomNpcsProviderAdapter.isSupportedVersion("1.7.10d"));
    }

    @Test public void nullMutationRequestFailsClosed() {
        CustomNpcsProviderAdapter provider = new CustomNpcsProviderAdapter(false);
        ProviderMutationResult result = provider.createControlledNpc((ControlledNpcRequest) null, null);
        assertEquals(MutationExecutionResult.Status.REJECTED_POLICY, result.getResult().getStatus());
    }
}
