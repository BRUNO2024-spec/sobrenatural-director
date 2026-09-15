package com.sobrenaturaldirector.composition.model;

import com.sobrenaturaldirector.domain.StableId;

/** Registry identity only; it never carries a provider class or NBT. */
public final class ExternalBlockReference {
    private final String providerModId, registryName;
    private final int metadata;

    public ExternalBlockReference(String providerModId, String registryName, int metadata) {
        this.providerModId = StableId.require(providerModId, "providerModId");
        this.registryName = StableId.require(registryName, "registryName");
        if (metadata < 0 || metadata > 15) throw new IllegalArgumentException("metadata out of range");
        this.metadata = metadata;
    }
    public String getProviderModId() { return providerModId; }
    public String getRegistryName() { return registryName; }
    public int getMetadata() { return metadata; }
}
