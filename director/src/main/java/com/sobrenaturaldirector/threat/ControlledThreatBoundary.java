package com.sobrenaturaldirector.threat;

import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;

/** Generic dispatch boundary. It contains no provider implementation knowledge. */
public final class ControlledThreatBoundary {
    private final DirectorProviderRegistry registry;
    public ControlledThreatBoundary(DirectorProviderRegistry registry) { if (registry == null) throw new IllegalArgumentException("registry is required"); this.registry = registry; }
    public ThreatExecutionResult execute(ThreatExecutionRequest request, DirectorWorldSavedData saved) { return dispatch(request, saved, false); }
    public ThreatExecutionResult cleanup(ThreatExecutionRequest request, DirectorWorldSavedData saved) { return dispatch(request, saved, true); }
    private ThreatExecutionResult dispatch(ThreatExecutionRequest request, DirectorWorldSavedData saved, boolean cleanup) {
        if (request == null) return ThreatExecutionResult.of(ThreatExecutionResult.Status.REJECTED_POLICY, "", null);
        ThreatProvider provider = provider(request.getProviderId());
        if (provider == null || !provider.isAvailable()) return ThreatExecutionResult.of(ThreatExecutionResult.Status.REJECTED_PROVIDER_UNAVAILABLE, request.getRequestId(), null);
        if (!provider.getThreatDefinition().getDefinitionId().equals(request.getDefinitionId())) return ThreatExecutionResult.of(ThreatExecutionResult.Status.REJECTED_POLICY, request.getRequestId(), null);
        return cleanup ? provider.cleanup(request, saved) : provider.execute(request, saved);
    }
    private ThreatProvider provider(ProviderId id) { Object value = registry.get(id); return value instanceof ThreatProvider ? (ThreatProvider) value : null; }
}
