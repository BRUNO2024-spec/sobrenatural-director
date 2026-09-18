package com.sobrenaturaldirector.provider;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CapabilityQuery;
import com.sobrenaturaldirector.capability.ProviderAvailability;
import com.sobrenaturaldirector.content.model.ProviderDescriptor;
import com.sobrenaturaldirector.content.model.SemanticCapability;

/** Central registry for optional provider discovery; distinct from Minecraft registries. */
public final class DirectorProviderRegistry {
    private final Map<ProviderId, DirectorContentProvider> providers = new TreeMap<ProviderId, DirectorContentProvider>();
    private final Map<ProviderId, ProviderDescriptor> descriptors = new TreeMap<ProviderId, ProviderDescriptor>();
    private int revision;

    public void register(DirectorContentProvider provider) {
        if (provider == null || provider.getProviderId() == null) throw new IllegalArgumentException("provider is required");
        if (providers.put(provider.getProviderId(), provider) != null) throw new IllegalArgumentException("duplicate provider");
        java.util.List<CapabilityDescriptor> capabilities = new java.util.ArrayList<CapabilityDescriptor>();
        for (Map.Entry<String, ProviderCapabilityState> entry : provider.getCapabilities().entrySet()) {
            try { capabilities.add(new CapabilityDescriptor(new SemanticCapability(entry.getKey()), provider.getProviderId(), availability(provider, entry.getValue()), 0, 0)); }
            catch (IllegalArgumentException ignored) { }
        }
        descriptors.put(provider.getProviderId(), new ProviderDescriptor(provider.getProviderId(), provider.getModId(), com.sobrenaturaldirector.content.model.EvidenceStatus.CONFIRMED, true, provider.getStatus(), false, Collections.<String>emptySet(), provider.getDetectedVersion(), capabilities, provider.getPolicyBindings(), provider.isAvailable() ? ProviderAvailability.RUNTIME_CONTROL : ProviderAvailability.DETECTED));
        revision++;
    }
    /** Registers only a provider that passed its runtime availability gate. */
    public boolean registerIfAvailable(DirectorContentProvider provider) {
        if (provider == null || !provider.isAvailable()) return false;
        register(provider);
        return true;
    }
    public void registerDescriptor(ProviderDescriptor descriptor) { if (descriptor == null || descriptor.getId() == null) throw new IllegalArgumentException("descriptor is required"); if (descriptors.put(descriptor.getId(), descriptor) != null) throw new IllegalArgumentException("duplicate provider descriptor"); revision++; }
    public boolean unregister(ProviderId id) { if (id == null) return false; boolean removed = providers.remove(id) != null; boolean descriptor = descriptors.remove(id) != null; if (descriptor || removed) revision++; return descriptor || removed; }
    public int getRevision() { return revision; }
    public DirectorContentProvider get(ProviderId id) { return providers.get(id); }
    public DirectorContentProvider get(String id) { return get(id == null ? null : new ProviderId(id)); }
    public Map<ProviderId, DirectorContentProvider> getProviders() { return Collections.unmodifiableMap(providers); }
    public ProviderStatus status(ProviderId id) { DirectorContentProvider provider = get(id); return provider == null ? ProviderStatus.UNKNOWN : provider.getStatus(); }
    public Map<ProviderId, ProviderDescriptor> getDescriptors() { return Collections.unmodifiableMap(descriptors); }
    public java.util.List<CapabilityDescriptor> query(CapabilityQuery query) {
        if (query == null) return Collections.emptyList();
        java.util.List<CapabilityDescriptor> result = new java.util.ArrayList<CapabilityDescriptor>();
        for (ProviderDescriptor descriptor : descriptors.values()) if (descriptor.isAvailable()) for (CapabilityDescriptor capability : descriptor.getCapabilities()) if (query.getRequired().equals(capability.getId()) && capability.getAvailability().atLeast(query.getMinimumAvailability())) result.add(capability);
        Collections.sort(result, new java.util.Comparator<CapabilityDescriptor>() { public int compare(CapabilityDescriptor a, CapabilityDescriptor b) { int score=Integer.compare(b.score(),a.score()); return score!=0?score:a.getProvider().compareTo(b.getProvider()); } });
        return Collections.unmodifiableList(result);
    }
    private static ProviderAvailability availability(DirectorContentProvider provider, ProviderCapabilityState state) { if (!provider.isAvailable()) return ProviderAvailability.DETECTED; if (state == ProviderCapabilityState.MUTATION_VALIDATED) return ProviderAvailability.RUNTIME_CONTROL; if (state == ProviderCapabilityState.STATIC_KNOWN || state == ProviderCapabilityState.RUNTIME_SURFACE_KNOWN) return ProviderAvailability.DISCOVERED; return ProviderAvailability.DETECTED; }
}
