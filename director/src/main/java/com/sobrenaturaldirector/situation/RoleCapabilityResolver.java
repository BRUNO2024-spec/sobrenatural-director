package com.sobrenaturaldirector.situation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.SemanticCapability;

/** Provider-neutral role mapping. */
public final class RoleCapabilityResolver {
    public List<SemanticCapability> required(List<SemanticRole> roles) { return resolve(roles); }
    public List<SemanticCapability> optional(List<SemanticRole> roles) { return resolve(roles); }
    private List<SemanticCapability> resolve(List<SemanticRole> roles) {
        ArrayList<SemanticCapability> result = new ArrayList<SemanticCapability>();
        for (SemanticRole role : roles) {
            SemanticCapability capability = capability(role);
            if (capability != null && !result.contains(capability)) result.add(capability);
        }
        return Collections.unmodifiableList(result);
    }
    private SemanticCapability capability(SemanticRole role) {
        if (role == SemanticRole.POINT_OF_INTEREST || role == SemanticRole.RUIN) return CapabilityVocabulary.STRUCTURE_SOURCE;
        if (role == SemanticRole.WITNESS || role == SemanticRole.VICTIM) return CapabilityVocabulary.ACTOR_SOURCE;
        if (role == SemanticRole.THREAT) return CapabilityVocabulary.THREAT_SOURCE;
        return null;
    }
}
