package com.sobrenaturaldirector.provider;

import java.util.Map;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import java.util.Collections;

/** Runtime provider metadata contract; it exposes no generic mutation API. */
public interface DirectorContentProvider {
    ProviderId getProviderId();
    String getModId();
    boolean isAvailable();
    String getDetectedVersion();
    ProviderStatus getStatus();
    Map<String, ProviderCapabilityState> getCapabilities();
    default java.util.Set<CapabilityPolicyBinding> getPolicyBindings() { return Collections.emptySet(); }
    default void onServerStarted() { }
}
