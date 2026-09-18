package com.sobrenaturaldirector.provider;

import java.util.Map;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import java.util.Collections;
import com.sobrenaturaldirector.control.ControlRequest;
import com.sobrenaturaldirector.control.ControlResult;

/** Runtime provider metadata contract; it exposes no generic mutation API. */
public interface DirectorContentProvider {
    ProviderId getProviderId();
    String getModId();
    boolean isAvailable();
    String getDetectedVersion();
    /** Version of the Director-side adapter contract, not the external mod. */
    default String getAdapterVersion() { return "1"; }
    ProviderStatus getStatus();
    Map<String, ProviderCapabilityState> getCapabilities();
    default java.util.Set<CapabilityPolicyBinding> getPolicyBindings() { return Collections.emptySet(); }
    /** Semantic control hook. Providers must override only for a proven adapter operation. */
    default ControlResult executeControl(ControlRequest request, long tick) {
        return new ControlResult(com.sobrenaturaldirector.control.ControlResultStatus.UNSUPPORTED, "provider control adapter not implemented");
    }
    default ControlResult executeControl(ControlRequest request, com.sobrenaturaldirector.control.ControlExecutionContext context, long tick) {
        return executeControl(request, tick);
    }
    default void onServerStarted() { }
}
