package com.sobrenaturaldirector.situation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.SemanticCapability;

/** Decomposes goals into abstract requirements only. */
public final class GoalRequirementResolver {
    public List<SemanticCapability> resolve(SituationGoal goal) {
        if (goal == null) return Collections.emptyList();
        if (goal == SituationGoal.INVESTIGATION) return Arrays.asList(CapabilityVocabulary.STRUCTURE_SOURCE, CapabilityVocabulary.ACTOR_SOURCE);
        if (goal == SituationGoal.DISCOVERY) return Collections.singletonList(CapabilityVocabulary.STRUCTURE_SOURCE);
        return Collections.emptyList();
    }
}
