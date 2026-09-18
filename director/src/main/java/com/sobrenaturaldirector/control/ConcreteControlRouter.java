package com.sobrenaturaldirector.control;

import com.sobrenaturaldirector.provider.DirectorContentProvider;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;

/**
 * Provider-neutral control routing boundary. It performs common preflight and
 * delegates only after registry/capability/lease/dimension checks. It never
 * contains a mod-specific branch and never turns unsupported into success.
 */
public final class ConcreteControlRouter {
    private final DirectorProviderRegistry registry;
    public ConcreteControlRouter(DirectorProviderRegistry registry) { if (registry == null) throw new IllegalArgumentException("registry"); this.registry = registry; }
    public ControlResult execute(ControlRequest request, long tick) {
        return execute(request, null, tick);
    }
    public ControlResult execute(ControlRequest request, ControlExecutionContext context, long tick) {
        if (request == null || tick < 0) return result(ControlResultStatus.INVALID_REQUEST, "invalid request");
        if (!request.getDimension().isAvailable()) return result(ControlResultStatus.PROVIDER_UNAVAILABLE, "target dimension unavailable");
        if (!request.getLease().validAt(tick)) return result(ControlResultStatus.STALE_TARGET, "lease expired or inactive");
        if (request.getAuthority() == ControlAuthority.OBSERVE_ONLY) return result(ControlResultStatus.SAFETY_REJECTED, "observe-only authority");
        DirectorContentProvider provider = registry.get(request.getProvider());
        if (provider == null || !provider.isAvailable()) return result(ControlResultStatus.PROVIDER_UNAVAILABLE, "provider unavailable");
        if (!provider.getCapabilities().containsKey(request.getCapability().getValue())) return result(ControlResultStatus.UNSUPPORTED, "capability not advertised");
        try { return provider.executeControl(request, context, tick); }
        catch (RuntimeException failure) { return result(ControlResultStatus.FAILED, "provider exception: " + failure.getClass().getName()); }
    }
    private static ControlResult result(ControlResultStatus status, String reason) { return new ControlResult(status, reason); }
}
