package com.sobrenaturaldirector.provider.gravestone;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.provider.DirectorContentProvider;
import com.sobrenaturaldirector.provider.ProviderCapabilityState;
import cpw.mods.fml.common.Loader;

/** GraveStone metadata adapter; it exposes only capabilities proven by 1L.2A. */
public final class GraveStoneCapabilityProvider implements DirectorContentProvider {
    public static final ProviderId PROVIDER_ID = new ProviderId("gravestone");
    public static final String MOD_ID = "GraveStone";
    public static final String SUPPORTED_VERSION = "2.13.0";
    private final boolean enabled;
    private final Map<String, ProviderCapabilityState> capabilities;
    private ProviderStatus status;
    private String version;
    public GraveStoneCapabilityProvider(boolean enabled) {
        this.enabled = enabled;
        Map<String, ProviderCapabilityState> values = new LinkedHashMap<String, ProviderCapabilityState>();
        values.put(CapabilityVocabulary.BLOCK_MUTATION.getValue(), ProviderCapabilityState.MUTATION_VALIDATED);
        values.put(CapabilityVocabulary.ROLLBACK_SAFE.getValue(), ProviderCapabilityState.MUTATION_VALIDATED);
        values.put(CapabilityVocabulary.STRUCTURE_SOURCE.getValue(), ProviderCapabilityState.MUTATION_VALIDATED);
        capabilities = Collections.unmodifiableMap(values);
        initialize();
    }
    public void initialize() {
        version = null;
        if (!enabled) { status = ProviderStatus.DISABLED; return; }
        try {
            if (!Loader.isModLoaded(MOD_ID)) { status = ProviderStatus.MISSING; return; }
            version = Loader.instance().getIndexedModList().get(MOD_ID).getVersion();
            status = SUPPORTED_VERSION.equals(version) ? ProviderStatus.AVAILABLE_SUPPORTED : ProviderStatus.AVAILABLE_UNSUPPORTED_VERSION;
        } catch (RuntimeException failure) { status = ProviderStatus.FAILED_INITIALIZATION; }
    }
    public ProviderId getProviderId() { return PROVIDER_ID; }
    public String getModId() { return MOD_ID; }
    public boolean isAvailable() { return status == ProviderStatus.AVAILABLE_SUPPORTED; }
    public String getDetectedVersion() { return version; }
    public ProviderStatus getStatus() { return status; }
    public Map<String, ProviderCapabilityState> getCapabilities() { return capabilities; }
    public java.util.Set<CapabilityPolicyBinding> getPolicyBindings() { java.util.Set<CapabilityPolicyBinding> values = new java.util.HashSet<CapabilityPolicyBinding>(); values.add(new CapabilityPolicyBinding(CapabilityVocabulary.BLOCK_MUTATION, "gravestone.allowlisted_blocks")); values.add(new CapabilityPolicyBinding(CapabilityVocabulary.ROLLBACK_SAFE, "director.stale_safe_rollback")); values.add(new CapabilityPolicyBinding(CapabilityVocabulary.STRUCTURE_SOURCE, "gravestone.allowlisted_structure")); return Collections.unmodifiableSet(values); }
}
