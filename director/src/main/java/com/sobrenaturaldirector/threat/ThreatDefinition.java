package com.sobrenaturaldirector.threat;

import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.domain.StableId;

public final class ThreatDefinition {
    private final ProviderId providerId;
    private final String definitionId;
    private final String displayName;

    public ThreatDefinition(ProviderId providerId, String definitionId, String displayName) {
        if (providerId == null) throw new IllegalArgumentException("providerId is required");
        this.providerId = providerId;
        this.definitionId = StableId.require(definitionId, "definitionId");
        this.displayName = StableId.require(displayName, "displayName");
    }
    public ProviderId getProviderId() { return providerId; }
    public String getDefinitionId() { return definitionId; }
    public String getDisplayName() { return displayName; }
}
