package com.sobrenaturaldirector.composition;

import com.sobrenaturaldirector.composition.model.ExternalBlockReference;

/** Provider-supplied policy consumed by the generic composition executor. */
public interface ExternalBlockPolicy {
    boolean isProviderAvailable();
    boolean accepts(String providerModId, String providerVersion, ExternalBlockReference reference);
}
