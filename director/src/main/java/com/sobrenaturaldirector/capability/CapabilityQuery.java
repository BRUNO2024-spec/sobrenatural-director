package com.sobrenaturaldirector.capability;

import com.sobrenaturaldirector.content.model.SemanticCapability;

public final class CapabilityQuery {
    private final SemanticCapability required;
    private final ProviderAvailability minimumAvailability;
    public CapabilityQuery(SemanticCapability required) { this(required, ProviderAvailability.SAFE_CAPABILITY); }
    public CapabilityQuery(SemanticCapability required, ProviderAvailability minimumAvailability) {
        if (required == null || minimumAvailability == null) throw new IllegalArgumentException("invalid capability query");
        this.required = required; this.minimumAvailability = minimumAvailability;
    }
    public SemanticCapability getRequired() { return required; }
    public ProviderAvailability getMinimumAvailability() { return minimumAvailability; }
}
