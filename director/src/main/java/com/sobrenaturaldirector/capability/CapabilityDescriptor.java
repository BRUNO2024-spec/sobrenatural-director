package com.sobrenaturaldirector.capability;

import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.content.model.SemanticCapability;

public final class CapabilityDescriptor {
    private final SemanticCapability id;
    private final ProviderId provider;
    private final ProviderAvailability availability;
    private final int priority;
    private final int cost;
    public CapabilityDescriptor(SemanticCapability id, ProviderId provider, ProviderAvailability availability, int priority, int cost) {
        if (id == null || provider == null || availability == null) throw new IllegalArgumentException("invalid capability descriptor");
        if (priority < 0 || cost < 0) throw new IllegalArgumentException("priority/cost must be non-negative");
        this.id = id; this.provider = provider; this.availability = availability; this.priority = priority; this.cost = cost;
    }
    public SemanticCapability getId() { return id; }
    public ProviderId getProvider() { return provider; }
    public ProviderAvailability getAvailability() { return availability; }
    public int getPriority() { return priority; }
    public int getCost() { return cost; }
    public int score() { return priority - cost; }
}
