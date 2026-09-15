package com.sobrenaturaldirector.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.domain.StableId;
import com.sobrenaturaldirector.content.model.SemanticCapability;

public final class SituationIntent {
    private final String id;
    private final List<SemanticCapability> requirements;
    public SituationIntent(String id, List<SemanticCapability> requirements) {
        this.id = StableId.require(id, "intentId");
        if (requirements == null || requirements.isEmpty()) throw new IllegalArgumentException("requirements are required");
        ArrayList<SemanticCapability> values = new ArrayList<SemanticCapability>(requirements);
        for (SemanticCapability value : values) if (value == null) throw new IllegalArgumentException("null capability");
        this.requirements = Collections.unmodifiableList(values);
    }
    public String getId() { return id; }
    public List<SemanticCapability> getRequirements() { return requirements; }
}
